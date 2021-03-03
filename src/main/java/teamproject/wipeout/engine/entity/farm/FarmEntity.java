package teamproject.wipeout.engine.entity.farm;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.input.Clickable;
import teamproject.wipeout.engine.component.input.Hoverable;
import teamproject.wipeout.engine.component.render.FarmRenderer;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.ui.UIComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.FarmData;
import teamproject.wipeout.game.farm.FarmUI;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.PlantComponent;

import java.io.FileNotFoundException;

/**
 * Defines a farm in the game.
 */
public class FarmEntity extends GameEntity {

    public static int SQUARE_SIZE = 32;

    public FarmData data;

    public Point2D size;
    public Transform transform;

    protected SpriteManager spriteManager;
    protected Item placingItem;
    protected SeedEntity seedEntity;

    /**
     * Creates a new instance of {@code FarmEntity}
     *
     * @param scene The GameScene this entity is part of
     * @param location Location of the farm
     * @param playerID Player's ID this entity belongs to
     * @param spriteManager {@link SpriteManager} for the {@link ItemsRowEntity}
     * @param itemStore {@link ItemStore} for the {@link ItemsRowEntity}
     */
    public FarmEntity(GameScene scene, Point2D location, String playerID, SpriteManager spriteManager, ItemStore itemStore) {
        super(scene);
        scene.addEntity(this);
        this.data = new FarmData(playerID);

        this.spriteManager = spriteManager;

        Point2D squareGrid = new Point2D(FarmData.FARM_COLUMNS, FarmData.FARM_ROWS);
        this.size = squareGrid.multiply(SQUARE_SIZE).add(SQUARE_SIZE * 2, SQUARE_SIZE * 2);

        this.transform = new Transform(location, 0.0);
        this.addComponent(this.transform);

        this.addComponent(new RenderComponent(true, new FarmRenderer(this.size, spriteManager)));

        for (int i = 0; i < squareGrid.getY(); i++) {
            ItemsRowEntity rowEntity = new ItemsRowEntity(scene, this.data.getItemsInRow(i), this.data.growthCallback, spriteManager, itemStore);
            Point2D rowPoint = new Point2D(0, (SQUARE_SIZE / 1.65) + (SQUARE_SIZE * i));
            rowEntity.addComponent(new Transform(rowPoint, 0.0, 1));
            rowEntity.setParent(this);
            this.addChild(rowEntity);
        }

        Clickable clickable = new Clickable((x, y, button, entity) -> {
            if (placingItem == null) {
                FarmUI farmUI = new FarmUI(this.data, this.spriteManager);
                this.addComponent(new UIComponent(farmUI));
            } else {
                if (this.placeItem(placingItem, x, y)) {
                    this.stopPlacingItem();
                }
            }
        });
        clickable.setEntity(this);
        this.addComponent(clickable);
    }

    public void startPlacingItem(Item item) throws FileNotFoundException {
        if (item == null) {
            return;
        }
        this.placingItem = item;

        this.seedEntity = new SeedEntity(this.scene, this.placingItem, this.spriteManager);

        Hoverable hoverable = new Hoverable((x, y) -> {
            Point2D point = this.isWithinFarm(x, y);
            Transform transform = this.seedEntity.getComponent(Transform.class);

            if (transform == null) {
                transform = new Transform(x, y, 0.0, 3);
                this.seedEntity.addComponent(transform);
            }

            PlantComponent plant = placingItem.getComponent(PlantComponent.class);
            if (point == null || !this.canBePlaced(x, y, plant.width, plant.height)) {
                transform.setPosition(new Point2D(x, y));
                this.seedEntity.hideAreaOverlay();
            } else {
                transform.setPosition(point);
                this.seedEntity.showAreaOverlay();
            }
        });
        hoverable.setEntity(this);
        this.addComponent(hoverable);
    }

    public void stopPlacingItem() {
        this.removeComponent(Hoverable.class);
        this.seedEntity.destroy();
        this.seedEntity = null;
        this.placingItem = null;
    }

    public Item getPlacingItem() {
        return this.placingItem;
    }

    /**
     * Checks if a given "square" at the farm is empty.
     *
     * @param x X coordinate of the "square"
     * @param y Y coordinate of the "square"
     * @return {@code true} if the "square" is empty, <br> otherwise {@code false}
     */
    protected boolean canBePlaced(double x, double y, int w, int h) {
        Point2D coors = this.rescaleCoordinates(x, y);
        int row = (int) coors.getY();
        int column = (int) coors.getX();
        return this.data.canBePlaced(row, column, w ,h);
    }

    /**
     * Puts a given Item on a given "square" defined by the X and Y coordinates.
     *
     * @param item {@link Item} to be stored
     * @param x X coordinate of the "square"
     * @param y Y coordinate of the "square"
     */
    protected boolean placeItem(Item item, double x, double y) {
        Point2D coors = this.rescaleCoordinates(x, y);
        int row = (int) coors.getY();
        int column = (int) coors.getX();
        return this.data.placeItem(item, row, column);
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
