package teamproject.wipeout.game.farm.entity;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.PickableComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.input.Clickable;
import teamproject.wipeout.engine.component.input.Hoverable;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.Rectangle;
import teamproject.wipeout.engine.component.render.FarmRenderer;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputHoverableAction;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.FarmData;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.item.components.PlantComponent;

import java.io.FileNotFoundException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Defines a farm in the game.
 */
public class FarmEntity extends GameEntity {

    public static int SQUARE_SIZE = 32;
    public static double SKEW_FACTOR = 0.89;

    public FarmData data;

    public Point2D size;
    public Transform transform;

    protected SpriteManager spriteManager;
    protected ItemStore itemStore;

    private Item placingItem;
    private SeedEntity seedEntity;
    private Consumer<Item> abortPlacing;

    private DestroyerEntity destroyerEntity;
    private Consumer<FarmItem> destroyerDelegate;

    /**
     * Creates a new instance of {@code FarmEntity}
     *
     * @param scene The GameScene this entity is part of
     * @param location Location of the farm
     * @param playerID Player's ID this entity belongs to
     * @param spriteManager {@link SpriteManager} for the {@link ItemsRowEntity}
     * @param itemStore {@link ItemStore} for the {@link ItemsRowEntity}
     */
    public FarmEntity(GameScene scene, Point2D location, Integer playerID, SpriteManager spriteManager, ItemStore itemStore) {
        super(scene);
        scene.addEntity(this);
        this.data = new FarmData(playerID);

        this.transform = new Transform(location, 0.0);
        Point2D squareGrid = new Point2D(FarmData.FARM_COLUMNS, FarmData.FARM_ROWS);
        this.size = squareGrid.multiply(SQUARE_SIZE).add(SQUARE_SIZE * 2, SQUARE_SIZE * 2);

        this.spriteManager = spriteManager;
        this.itemStore = itemStore;

        this.placingItem = null;
        this.seedEntity = null;
        this.destroyerEntity = null;
        this.destroyerDelegate = null;

        this.addComponent(this.transform);
        this.addComponent(new RenderComponent(new FarmRenderer(this.size, spriteManager)));
        this.addComponent(this.makeClickable());

        //Create row entities for the rows of the farm
        for (int i = 0; i < squareGrid.getY(); i++) {
            ItemsRowEntity rowEntity = new ItemsRowEntity(scene, this.data.getItemsInRow(i), this.data.getGrowthDelegate(), spriteManager, itemStore);
            Point2D rowPoint = new Point2D(0, (SQUARE_SIZE / 1.5) + (SQUARE_SIZE * i));
            rowEntity.addComponent(new Transform(rowPoint, 0.0, 1));

            rowEntity.setParent(this);
            this.addChild(rowEntity);
        }
    }

    /**
     * Starts placing a given item on the farm through creating a {@link Hoverable} component.
     *
     * @param item {@link Item} to be placed
     * @param mousePosition Current {@link Point2D} position of the mouse cursor
     * @throws FileNotFoundException Thrown if the sprites for the {@code Item} cannot be found.
     */
    public void startPlacingItem(Item item, Point2D mousePosition, Consumer<Item> abortPlacing) throws FileNotFoundException {
        this.placingItem = item;
        this.abortPlacing = abortPlacing;

        // Create seed entity for the item being placed and display it at the mouse coordinates
        this.seedEntity = new SeedEntity(this.scene, this.placingItem, this.spriteManager);
        Transform seedTransform = new Transform(mousePosition.getX(), mousePosition.getY(), 0.0, 1);
        this.seedEntity.addComponent(seedTransform);

        // Activates a Hoverable component which updates the position of the item being placed
        // based on the position of the mouse cursor
        InputHoverableAction hoverableAction = (x, y) -> {
            Point2D point = this.isWithinFarm(x, y);
            Transform sTransform = this.seedEntity.getComponent(Transform.class);

            PlantComponent plant = placingItem.getComponent(PlantComponent.class);
            if (point == null || !this.canBePlaced(x, y, plant.width, plant.height)) {
                // Is NOT within the farm or it CANNOT be placed at the current position
                sTransform.setPosition(new Point2D(x, y));
                this.seedEntity.hideAreaOverlay();

            } else {
                // Is within the farm
                sTransform.setPosition(point);
                this.seedEntity.showAreaOverlay();
            }
        };
        hoverableAction.performMouseHoverAction(mousePosition.getX(), mousePosition.getY());
        this.makeHoverable(hoverableAction);
    }

    /**
     * Stops placing the item and removes the {@link Hoverable} component.
     */
    public void stopPlacingItem(boolean successful) {
        this.removeComponent(Hoverable.class);

        if (!successful) {
            this.abortPlacing.accept(this.placingItem);
        }
        this.abortPlacing = null;
        this.placingItem = null;

        if (this.seedEntity == null) {
            return;
        }
        this.seedEntity.destroy();
        this.seedEntity = null;
    }

    /**
     * @return {@code true} if the player is placing an item, otherwise {@code false}.
     */
    public boolean isPlacingItem() {
        return this.placingItem != null;
    }

    /**
     * Starts picking an item from the farm through creating a {@link Hoverable} component.
     *
     * @param mousePosition Current {@link Point2D} position of the mouse cursor
     */
    public void startPickingItem(Point2D mousePosition) {
        // Create destroyer entity for the "tool" used to pick items and display it at the mouse coordinates
        this.destroyerEntity = new DestroyerEntity(this.scene);
        Transform destroyerTransform = new Transform(mousePosition.getX(), mousePosition.getY(), 0.0, 0);
        this.destroyerEntity.addComponent(destroyerTransform);

        // Activates a Hoverable component which updates the position of the destroyer "tool"
        // based on the position of the mouse cursor
        InputHoverableAction hoverableAction = (x, y) -> {
            Point2D point = this.isWithinFarm(x, y);
            Transform dTransform = this.destroyerEntity.getComponent(Transform.class);

            if (point == null) {
                // Is NOT within the farm
                dTransform.setPosition(new Point2D(x, y));
                this.destroyerEntity.adaptToFarmItem(null);
            } else {
                // Is within the farm. But is there any item at that coordinates?...
                Pair<FarmItem, Boolean> pickingItem = this.canBePicked(x, y);
                if (pickingItem == null) {
                    //...NO :( there is no item
                    dTransform.setPosition(point);
                    this.destroyerEntity.adaptToFarmItem(null);

                } else {
                    //...YES :) there is an item
                    int[] itemFarmPosition = this.data.positionForItem(pickingItem.getKey());
                    dTransform.setPosition(this.coordinatesForItemAt(itemFarmPosition[0], itemFarmPosition[1]));
                    this.destroyerEntity.adaptToFarmItem(pickingItem);

                    // Set up a delegate so that the destroyer tool gets updates about the item's growth progress
                    this.destroyerDelegate = (updatedFarmItem) -> {
                        if (this.destroyerEntity == null) {
                            return;
                        }
                        if (pickingItem.getKey() == updatedFarmItem) {
                            this.destroyerEntity.setColorForPickable(updatedFarmItem.isFullyGrown());
                        }
                    };
                    this.data.addGrowthDelegate(this.destroyerDelegate);
                }
            }
        };
        hoverableAction.performMouseHoverAction(mousePosition.getX(), mousePosition.getY());
        this.makeHoverable(hoverableAction);
    }

    /**
     * Stops picking an item form the farm and removes the {@link Hoverable} component.
     */
    public void stopPickingItem() {
        this.removeComponent(Hoverable.class);
        this.data.removeGrowthDelegate(this.destroyerDelegate);
        this.destroyerDelegate = null;
        if (this.destroyerEntity == null) {
            return;
        }
        this.destroyerEntity.destroy();
        this.destroyerEntity = null;
    }

    /**
     * @return {@code true} if the player is picking an item, otherwise {@code false}.
     */
    public boolean isPickingItem() {
        return this.destroyerEntity != null;
    }

    /**
     * Checks if a given position at the farm is empty.
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
     * Checks if a given position at the farm contains fully grown plant.
     *
     * @param x X scene coordinate
     * @param y Y scene coordinate
     * @return {@link Pair} of {@link FarmItem} and {@code Boolean} -
     * ({@code true} if fully grown, otherwise {@code false}).
     * {@code null} if the given position is empty.
     */
    protected Pair<FarmItem, Boolean> canBePicked(double x, double y) {
        Point2D coors = this.rescaleCoordinates(x, y);
        int row = (int) coors.getY();
        int column = (int) coors.getX();
        return this.data.canBePicked(row, column);
    }

    /**
     * Puts a given Item on a given position defined by the X and Y coordinates.
     *
     * @param item {@link Item} to be stored
     * @param x X scene coordinate
     * @param y Y scene coordinate
     */
    protected boolean placeItem(Item item, double x, double y) {
        Point2D coors = this.rescaleCoordinates(x, y);
        int row = (int) coors.getY();
        int column = (int) coors.getX();
        return this.data.placeItem(item, row, column);
    }

    /**
     * Picks(= removes) an Item from a given position defined by the X and Y coordinates.
     *
     * @param x X scene coordinate
     * @param y Y scene coordinate
     */
    public void pickItemAt(double x, double y) {
        Point2D coors = this.rescaleCoordinates(x, y);
        int row = (int) coors.getY();
        int column = (int) coors.getX();

        Item pickedItem = this.data.pickItemAt(row, column);
        if (pickedItem == null) {
            return;
        }

        int inventoryID = pickedItem.getComponent(PlantComponent.class).grownItemID;
        Item inventoryItem = this.itemStore.getItem(inventoryID);

        try {
            this.createPickablesFor(inventoryItem, x, y);
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Checks whether a given X, Y coordinates are within the {@code FarmEntity}.
     *
     * @param x X coordinate to be checked
     * @param y Y coordinate to be checked
     * @return {@link Point2D} of the correct "square" at the farm,
     * or {@code null} if the coordinates are not within the farm's bounds.
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
            return new Point2D(startX + (column * SQUARE_SIZE), startY + (row * SQUARE_SIZE));
        }
        return null;
    }

    /**
     * Gives scene coordinates for a given row and column.
     *
     * @param row Item row
     * @param column Item column
     * @return {@link Point2D} scene coordinates of the row and column
     */
    public Point2D coordinatesForItemAt(int row, int column) {
        double startX = this.transform.getWorldPosition().getX() + SQUARE_SIZE;
        double startY = this.transform.getWorldPosition().getY() + SQUARE_SIZE;
        return new Point2D(startX + (column * SQUARE_SIZE), startY + (row * SQUARE_SIZE));
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

    /**
     * Creates a {@code Clickable} component for the farm entity.
     *
     * @return {@link Clickable} component
     */
    private Clickable makeClickable() {
        Clickable clickable = new Clickable((x, y, button, entity) -> {
            if (this.isPickingItem()) {
                this.pickItemAt(x, y);
                this.stopPickingItem();
                return;
            }

            if (this.placingItem != null && this.placeItem(placingItem, x, y)) {
                this.stopPlacingItem(true);
            }
        });
        clickable.setEntity(this);
        return clickable;
    }

    /**
     * Activates a {@link Hoverable} component for the farm entity
     * (only if no other {@code Hoverable} component is present).
     *
     * @param hoverableAction Action to be executed by the {@code Hoverable} component.
     */
    private void makeHoverable(InputHoverableAction hoverableAction) {
        if (this.hasComponent(Hoverable.class)) {
            return;
        }
        Hoverable hoverable = new Hoverable(hoverableAction);
        hoverable.setEntity(this);
        this.addComponent(hoverable);
    }

    /**
     * Creates pickable entity(/entities) for a given item after it was harvested.
     * The entity(/entities) are rendered around a given X, Y scene coordinates.
     *
     * @param item Harvested {@link Item}
     * @param x X scene coordinate
     * @param y Y scene coordinate
     * @throws FileNotFoundException Thrown if the inventory sprites cannot be found for the given {@code Item}.
     */
    private void createPickablesFor(Item item, double x, double y) throws FileNotFoundException {
        GameEntity entity = this.scene.createEntity();
        InventoryComponent invComponent = item.getComponent(InventoryComponent.class);
        Image sprite = this.spriteManager.getSpriteSet(invComponent.spriteSheetName, invComponent.spriteSetName)[0];
        entity.addComponent(new RenderComponent(new SpriteRenderable(sprite)));

        entity.addComponent(new Transform(this.giveRandomPositionAround(x, y), 0.0, 2));
        entity.addComponent(new HitboxComponent(new Rectangle(0, 0, sprite.getWidth(), sprite.getHeight())));
        entity.addComponent(new PickableComponent(item));
    }

    /**
     * Gives random X, Y coordinates centered around a given X, Y scene coordinates
     *
     * @param x X scene coordinate
     * @param y Y scene coordinate
     * @return Randomised X, Y coordinates in form of a {@link Point2D}
     */
    private Point2D giveRandomPositionAround(double x, double y) {
        ThreadLocalRandom randomiser = ThreadLocalRandom.current();

        double randX;
        double randY;
        if (randomiser.nextBoolean()) {
            randX = randomiser.nextDouble(x - (2 * SQUARE_SIZE), x);
            randY = randomiser.nextDouble(y - (2 * SQUARE_SIZE), y);
        } else {
            randX = randomiser.nextDouble(x, x + (SQUARE_SIZE / 4.0));
            randY = randomiser.nextDouble(y, y + (SQUARE_SIZE / 4.0));
        }
        return new Point2D(randX, randY);
    }

}
