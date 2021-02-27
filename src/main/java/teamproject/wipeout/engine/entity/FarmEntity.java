package teamproject.wipeout.engine.entity;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import teamproject.wipeout.engine.component.Clickable;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.FarmRenderer;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.FarmData;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;

import java.io.FileNotFoundException;

/**
 * Defines a farm in the game.
 */
public class FarmEntity extends GameEntity {

    public static int SQUARE_SIZE = 32;

    public FarmData data;

    public Point2D size;
    public Transform transform;

    /**
     * Creates a new instance of {@code FarmEntity}
     *
     * @param scene The GameScene this entity is part of
     * @param location Location of the farm
     * @param playerID Player's ID this entity belongs to
     * @param spriteManager {@link SpriteManager} for the {@link CropsRowEntity}
     * @param itemStore {@link ItemStore} for the {@link CropsRowEntity}
     */
    public FarmEntity(GameScene scene, Point2D location, String playerID, SpriteManager spriteManager, ItemStore itemStore) {
        super(scene);
        this.data = new FarmData(playerID);

        Point2D squareGrid = new Point2D(FarmData.FARM_COLUMNS, FarmData.FARM_ROWS);
        this.size = squareGrid.multiply(SQUARE_SIZE).add(SQUARE_SIZE * 2, SQUARE_SIZE * 2);

        this.transform = new Transform(location, 0.0);
        this.addComponent(this.transform);

        this.addComponent(new RenderComponent(new FarmRenderer(this.size, spriteManager)));

        for (int i = 0; i < squareGrid.getY(); i++) {
            CropsRowEntity rowEntity = new CropsRowEntity(scene, this.data.getItemsInRow(i), spriteManager, itemStore);
            rowEntity.addComponent(new Transform(0, (SQUARE_SIZE / 2) + (SQUARE_SIZE * i), 0.0, 1));
            rowEntity.setParent(this);
            this.addChild(rowEntity);
        }
    }

    /**
     * Checks if a given "square" at the farm is empty.
     *
     * @param x X coordinate of the "square"
     * @param y Y coordinate of the "square"
     * @return {@code true} if the "square" is empty, <br> otherwise {@code false}
     */
    public boolean isEmpty(double x, double y) {
        Point2D coors = this.rescaleCoordinates(x, y);
        int row = (int) coors.getY();
        int column = (int) coors.getX();
        return this.data.isEmpty(row, column);
    }

    /**
     * Puts a given Item on a given "square" defined by the X and Y coordinates.
     *
     * @param item {@link Item} to be stored
     * @param x X coordinate of the "square"
     * @param y Y coordinate of the "square"
     */
    public void putItem(Item item, double x, double y) throws FileNotFoundException {
        Point2D coors = this.rescaleCoordinates(x, y);
        int row = (int) coors.getY();
        int column = (int) coors.getX();
        if (!this.data.isEmpty(row, column)) {
            return;
        }
        this.data.putItem(item, row, column);
    }

    /**
     * Picks(= removes) an Item from a given "square" defined by the X and Y coordinates.
     *
     * @param x X coordinate of the "square"
     * @param y Y coordinate of the "square"
     * @return Picked(= removed) item
     */
    public Item pickItemAt(double x, double y) {
        Point2D coors = this.rescaleCoordinates(x, y);
        int row = (int) coors.getY();
        int column = (int) coors.getX();

        if (!this.data.canBePicked(row, column)) {
            return null;
        }
        return this.data.pickItemAt(row, column);
    }

    /**
     * Checks whether a given X, Y coordinates are within the {@code FarmEntity}.
     *
     * @param x X coordinate to be checked
     * @param y Y coordinate to be checked
     * @return {@link Point2D} of the correct "square" at the farm,
     *         or {@code null} if the coordinates are not within the farm's bounds.
     */
    public Point2D isWithinFarm(double x, double y) {
        double startX = this.transform.getWorldPosition().getX() + SQUARE_SIZE;
        double startY = this.transform.getWorldPosition().getY() + SQUARE_SIZE;
        double endX = startX + this.size.getX() - (SQUARE_SIZE * 2);
        double endY = startY + this.size.getY() - (SQUARE_SIZE * 2);
        if ((startX < x && x < endX) && (startY < y && y < endY)) {
            Point2D coors = this.rescaleCoordinates(x, y);
            int row = (int) coors.getY();
            int column = (int) coors.getX();
            return new Point2D(startX + (column * SQUARE_SIZE),
                    startY + (row * SQUARE_SIZE));
        }
        return null;
    }

    /**
     * Rescales a given X, Y scene coordinates to "square" coordinates at the farm.
     *
     * @param x X coordinate to be rescaled
     * @param y Y coordinate to be rescaled
     * @return Rescaled point
     */
    protected Point2D rescaleCoordinates(double x, double y) {
        double startX = this.transform.getWorldPosition().getX() + SQUARE_SIZE;
        double startY = this.transform.getWorldPosition().getY() + SQUARE_SIZE;
        double itemX = x - startX;
        double itemY = y - startY;

        int itemRow = (int) itemY / SQUARE_SIZE;
        int itemColumn = (int) itemX / SQUARE_SIZE;

        return new Point2D(itemColumn, itemRow);
    }

}
