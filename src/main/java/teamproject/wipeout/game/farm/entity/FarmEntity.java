package teamproject.wipeout.game.farm.entity;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.input.Clickable;
import teamproject.wipeout.engine.component.input.Hoverable;
import teamproject.wipeout.engine.component.render.FarmRenderer;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputHoverableAction;
import teamproject.wipeout.engine.input.InputKeyAction;
import teamproject.wipeout.engine.system.input.MouseHoverSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.FarmData;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.farm.Pickables;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.state.FarmState;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Defines a farm in the game.
 */
public class FarmEntity extends GameEntity {

    public static int SQUARE_SIZE = 32;
    public static double SKEW_FACTOR = 0.89;

    public final Integer farmID;

    private FarmData data;

    protected Transform transform;
    protected Point2D size;

    private final SpriteManager spriteManager;
    private final ItemStore itemStore;

    private final List<ItemsRowEntity> rowEntities;

    //private FarmUI farmUI;

    private Item placingItem;
    private SeedEntity seedEntity;
    private Consumer<Item> abortPlacing;

    private DestroyerEntity destroyerEntity;
    private Consumer<FarmItem> destroyerDelegate;

    private Supplier<GameClient> clientSupplier;

    private AudioComponent audio;

    private final Pickables pickables;

    /**
     * Creates a new instance of {@code FarmEntity}
     *
     * @param scene The GameScene this entity is part of
     * @param location Location of the farm
     * @param spriteManager {@link SpriteManager} for the {@link ItemsRowEntity}
     * @param itemStore {@link ItemStore} for the {@link ItemsRowEntity}
     */
    public FarmEntity(GameScene scene, Integer farmID, Point2D location, Pickables pickables, SpriteManager spriteManager, ItemStore itemStore) {
        super(scene);
        this.farmID = farmID;

        this.transform = new Transform(location, 0.0, -1);

        Point2D squareGrid = new Point2D(FarmData.FARM_COLUMNS, FarmData.FARM_ROWS);
        this.size = squareGrid.multiply(SQUARE_SIZE).add(SQUARE_SIZE * 2, SQUARE_SIZE * 2);

        this.spriteManager = spriteManager;
        this.itemStore = itemStore;

        this.rowEntities = new ArrayList<ItemsRowEntity>();

        this.placingItem = null;
        this.seedEntity = null;
        this.destroyerEntity = null;
        this.destroyerDelegate = null;

        this.addComponent(this.transform);
        this.addComponent(new RenderComponent(false, new FarmRenderer(this.size, this.spriteManager)));

        audio = new AudioComponent();
        this.addComponent(audio);

        this.data = new FarmData(-13, null, this.itemStore);

        //Create row entities for the rows of the farm
        for (int r = 0; r < this.data.getNumberOfRows(); r++) {
            ItemsRowEntity rowEntity = new ItemsRowEntity(this.scene, this.data.getItemsInRow(r), this.data.getGrowthDelegate(), this.spriteManager);
            Point2D rowPoint = new Point2D(0, (SQUARE_SIZE / 1.5) + (SQUARE_SIZE * r));
            rowEntity.addComponent(new Transform(rowPoint, 0.0, 1));

            rowEntity.setParent(this);
            this.addChild(rowEntity);
            this.rowEntities.add(rowEntity);
        }

        this.pickables = pickables;
    }

    public void assignPlayer(Integer playerID, boolean activePlayer, Supplier<GameClient> clientFunction) {
        this.removePlayer();

        this.data = new FarmData(this.farmID, playerID, this.itemStore);

        int row = 0;
        for (ItemsRowEntity rowEntity : this.rowEntities) {
            rowEntity.setFarmRow(this.data.getItemsInRow(row));
            row += 1;
        }

        if (activePlayer) {
            //this.farmUI = new FarmUI(this.data, spriteManager);
            //this.farmUI.setParent(uiContainer);
            this.addComponent(this.makeClickable(clientFunction));
        }
    }

    private void removePlayer() {
        this.data = new FarmData(-13, null, this.itemStore);

        int row = 0;
        for (ItemsRowEntity rowEntity : this.rowEntities) {
            rowEntity.setFarmRow(this.data.getItemsInRow(row));
            row += 1;
        }

        this.removeComponent(Clickable.class);
        if (this.isPlacingItem()) {
            this.stopPlacingItem(false);
        }
        if (this.isPickingItem()) {
            this.stopPickingItem();
        }
        if (this.isDestroyingItem()) {
            this.stopPickingItem();
        }
    }

    public void updateFromState(FarmState farmState) {
        this.data.updateFromState(farmState);
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
     * Starts destroying an item from the farm through creating a {@link Hoverable} component.
     *
     * @param mousePosition Current {@link Point2D} position of the mouse cursor
     */
    public void startDestroyingItem(Point2D mousePosition) {
        // Create destroyer entity for the "tool" used to destroy items and display it at the mouse coordinates
        this.destroyerEntity = new DestroyerEntity(this.scene, true);
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
                this.destroyerEntity.adaptToDestroyFarmItem(null);
            } else {
                // Is within the farm. But is there any item at that coordinates?...
                Pair<FarmItem, Boolean> pickingItem = this.canBePicked(x, y);
                if (pickingItem == null) {
                    //...NO :( there is no item
                    dTransform.setPosition(point);
                    this.destroyerEntity.adaptToDestroyFarmItem(null);
                } else {
                    //...YES :) there is an item
                    
                    int[] itemFarmPosition = this.data.positionForItem(pickingItem.getKey());
                    dTransform.setPosition(this.coordinatesForItemAt(itemFarmPosition[0], itemFarmPosition[1]));
                    this.destroyerEntity.adaptToDestroyFarmItem(pickingItem);

                    // Set up a delegate so that the destroyer tool gets updates about the item's growth progress
                    this.destroyerDelegate = (updatedFarmItem) -> {
                        if (this.destroyerEntity == null) {
                            return;
                        }
                        if (pickingItem.getKey() == updatedFarmItem) {
                            this.destroyerEntity.setColorForPickable(true);
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
     * Stops picking an item from the farm and removes the {@link Hoverable} component.
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
     * @return {@code true} if the player is destroying an item, otherwise {@code false}.
     */
    public boolean isDestroyingItem() {
        if (this.destroyerEntity == null) {
            return false;
        }
        else {
            return this.destroyerEntity.getDestroyMode();
        }
    }

    /**
     * Checks if a given position at the farm is empty.
     *
     * @param x X coordinate of the "square"
     * @param y Y coordinate of the "square"
     * @return {@code true} if the "square" is empty, <br> otherwise {@code false}
     */
    protected boolean canBePlaced(double x, double y, int w, int h) {
        Point2D coors = this.rescaleCoordinatesToFarm(x, y);
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
        Point2D coors = this.rescaleCoordinatesToFarm(x, y);
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
        Point2D coors = this.rescaleCoordinatesToFarm(x, y);
        int row = (int) coors.getY();
        int column = (int) coors.getX();
        this.audio.play("shovel.wav");
        return this.data.placeItem(item, 0.0, row, column);
    }

    /**
     * Picks(= removes) an Item from a given position defined by the X and Y coordinates.
     *
     * @param x X scene coordinate
     * @param y Y scene coordinate
     */
    public void pickItemAt(double x, double y) {
        pickItemAt(x, y, true);
    }

    /**
     * Picks(= removes) an Item from a given position defined by the X and Y coordinates with an extra flag to check whether you want the item to drop on plant destruction.
     * @param x X scene coordinate
     * @param y Y scene coordinate
     * @param makePickable True if you want the item to be pickable, otherwise false.
     */
    public void pickItemAt(double x, double y, boolean makePickable) {
        Point2D coors = this.rescaleCoordinatesToFarm(x, y);
        int row = (int) coors.getY();
        int column = (int) coors.getX();
        
        double pickableRow = row;
        double pickableColumn = column;

        // Get the farmItem
        FarmItem itemToPick = this.data.itemAt(row, column);
        if (itemToPick == null) {
            return;
        }

        Item pickedItem = itemToPick.get();
        PlantComponent pickedPlantComponent = pickedItem.getComponent(PlantComponent.class);

        // Check if oversized plant, adjust row and column if it is for pickable generation
        if (pickedPlantComponent.width > 1 || pickedPlantComponent.height > 1) {
            int[] pos = this.data.positionForItem(itemToPick);
            pickableRow = pos[0] + pickedPlantComponent.height / 4.0;
            pickableColumn = pos[1] + pickedPlantComponent.width / 4.0;
        }

        // Actually try to pick/destroy the item
        if (isDestroyingItem()) {
            this.data.destroyItemAt(row, column);
            pickedItem = null;
            this.audio.play("slice.wav");
        }
        else {
            pickedItem = this.data.pickItemAt(row, column);
        }

        
        if (pickedItem == null) {
            return;
        }

        this.audio.play("shovel.wav");
        
        if (makePickable) {
            // Player picking the farm item

            // Get the middle of the plant in scene coordinates
            Point2D scenePlantMiddle = this.rescaleCoordinatesToScene(pickableColumn, pickableRow);

            int inventoryID = pickedPlantComponent.grownItemID;
            ThreadLocalRandom randomiser = ThreadLocalRandom.current();
            int numberOfPickables = pickedPlantComponent.minDrop;
            if (pickedPlantComponent.maxDrop > pickedPlantComponent.minDrop) {
                numberOfPickables = randomiser.nextInt(pickedPlantComponent.minDrop, pickedPlantComponent.maxDrop);
            }

            Item inventoryItem = this.itemStore.getItem(inventoryID);
            this.pickables.createPickablesFor(inventoryItem, scenePlantMiddle.getX(), scenePlantMiddle.getY(), numberOfPickables);

        } else {
            // Animal eating the farm item
            this.sendStateUpdate();
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
            Point2D coors = this.rescaleCoordinatesToFarm(x, y);
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
     * Rescales a given X, Y scene coordinates to position coordinates at the farm.
     *
     * @param x X coordinate to be rescaled
     * @param y Y coordinate to be rescaled
     * @return Rescaled point = row and column position at the farm
     */
    public Point2D rescaleCoordinatesToFarm(double x, double y) {
        double startX = this.transform.getWorldPosition().getX() + SQUARE_SIZE;
        double startY = this.transform.getWorldPosition().getY() + SQUARE_SIZE;
        double itemX = x - startX;
        double itemY = y - startY;

        int itemRow = (int) itemY / SQUARE_SIZE;
        int itemColumn = (int) itemX / SQUARE_SIZE;

        return new Point2D(itemColumn, itemRow);
    }

    /**
     * Rescales a given X, Y farm coordinates to position coordinates at the scene.
     *
     * @param x X coordinate to be rescaled
     * @param y Y coordinate to be rescaled
     * @return Rescaled point = row and column position at the scene
     */
    public Point2D rescaleCoordinatesToScene(double x, double y) {
        return new Point2D(this.transform.getWorldPosition().getX() + x * SQUARE_SIZE + SQUARE_SIZE, this.transform.getWorldPosition().getY() + y * SQUARE_SIZE + SQUARE_SIZE);
    }

    public InputKeyAction onKeyPickAction(MouseHoverSystem mouseHoverSystem) {
        return () -> {
            if (this.isPlacingItem()) {
                this.stopPlacingItem(false);
            }

            if (this.isDestroyingItem()) {
                this.stopPickingItem();
                this.startPickingItem(mouseHoverSystem.getCurrentMousePosition());
            
            } else if (this.isPickingItem()) {
                this.stopPickingItem();

            } else {
                this.startPickingItem(mouseHoverSystem.getCurrentMousePosition());
            }
        };
    }

    public InputKeyAction onKeyPickActionDestroy(MouseHoverSystem mouseHoverSystem) {
        return () -> {
            if (this.isPlacingItem()) {
                this.stopPlacingItem(false);
            }

            if (this.isDestroyingItem()) {
                this.stopPickingItem();
            
            } else if (this.isPickingItem()) {
                this.stopPickingItem();
                this.startDestroyingItem(mouseHoverSystem.getCurrentMousePosition());

            } else {
                this.startDestroyingItem(mouseHoverSystem.getCurrentMousePosition());
            }
        };
    }

    public List<Point2D> getGrownItemPositions() {

        List<Point2D> fullyGrownItems = new ArrayList<>();

        ArrayList<ArrayList<FarmItem>> items = data.getItems();

        for (int i = 0; i < items.size(); i++) {
            for (int j = 0; j < items.get(i).size(); j++) {
                FarmItem item = items.get(i).get(j);
                if (item != null && item.isFullyGrown()) {
                    fullyGrownItems.add(rescaleCoordinatesToScene(j, i));
                }
            }
        }

        return fullyGrownItems;
    }

    /**
     * Creates a {@code Clickable} component for the farm entity.
     *
     * @return {@link Clickable} component
     */
    private Clickable makeClickable(Supplier<GameClient> clientFunction) {
        this.clientSupplier = clientFunction;

        Clickable clickable = new Clickable((x, y, button) -> {
            boolean stateChanged = false;

            if (this.isPickingItem()) { 
                this.pickItemAt(x, y); //Picking a plant
                this.stopPickingItem();
                stateChanged = true;
            
            } else if (this.isDestroyingItem()) {
                this.pickItemAt(x, y, true); //Destroying a plant
                this.stopPickingItem();
                stateChanged = true;

            } else if (this.placingItem != null && this.placeItem(placingItem, x, y)) {
                this.stopPlacingItem(true);
                stateChanged = true;
            }

            if (stateChanged) {
                this.sendStateUpdate();
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
     * If possible, sends an updated state of the farm to the server.
     */
    private void sendStateUpdate() {
        if (this.clientSupplier == null) {
            return;
        }

        GameClient client = this.clientSupplier.get();
        if (client != null) {
            try {
                client.send(new GameUpdate(GameUpdateType.FARM_STATE, client.id, this.data.getCurrentState()));

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

}
