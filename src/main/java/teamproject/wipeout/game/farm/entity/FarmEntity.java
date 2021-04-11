package teamproject.wipeout.game.farm.entity;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.input.Clickable;
import teamproject.wipeout.engine.component.input.Hoverable;
import teamproject.wipeout.engine.component.render.FarmRenderer;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.particle.ParticleComponent;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters.ParticleSimulationSpace;
import teamproject.wipeout.engine.component.render.particle.property.EaseCurve;
import teamproject.wipeout.engine.component.render.particle.property.OvalParticle;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputHoverableAction;
import teamproject.wipeout.engine.input.InputKeyAction;
import teamproject.wipeout.engine.system.input.MouseHoverSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.ParticleEntity;
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
import teamproject.wipeout.util.SupplierGenerator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Defines a farm in the game.
 */
public class FarmEntity extends GameEntity {

    public static final int MAX_EXPANSION_SIZE = 5;

    public static final OvalParticle FAST_CROP_PARTICLE = new OvalParticle(new Color(0.001, 1, 0.003, 1));
    public static final OvalParticle SLOW_CROP_PARTICLE = new OvalParticle(new Color(1, 0.003, 0.003, 1));
    public static final OvalParticle AI_ATTRACT_PARTICLE = new OvalParticle(Color.YELLOW);
    public static final OvalParticle AI_REPEL_PARTICLE = new OvalParticle(new Color(0.25, 0.25, 0.25, 1));

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

    private ParticleEntity sabotageEffect;

    private DestroyerEntity destroyerEntity;
    private Pair<FarmItem, ChangeListener<? super Number>> destroyerListener;

    private Supplier<GameClient> clientSupplier;

    private final AudioComponent audio;

    private final Pickables pickables;

    private final FarmRenderer farmRenderer;

    private final Supplier<Double> growthMultiplierSupplier = () -> this.data.getGrowthMultiplier();

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
        this.spriteManager = spriteManager;
        this.itemStore = itemStore;

        this.farmID = farmID;
        this.data = new FarmData(-13, null, this.expandFarmBy(), this.itemStore);

        this.transform = new Transform(location, 0.0, -1);

        Point2D squareGrid = new Point2D(this.data.farmColumns, this.data.farmRows);
        this.size = squareGrid.multiply(SQUARE_SIZE).add(SQUARE_SIZE * 2, SQUARE_SIZE * 2);

        this.rowEntities = new ArrayList<ItemsRowEntity>();

        this.placingItem = null;
        this.seedEntity = null;
        this.destroyerEntity = null;
        this.destroyerListener = null;

        this.addComponent(this.transform);

        this.farmRenderer = new FarmRenderer(this.size, this.spriteManager);
        this.addComponent(new RenderComponent(false, this.farmRenderer));

        audio = new AudioComponent();
        this.addComponent(audio);

        //Create row entities for the rows of the farm
        for (int r = 0; r < this.data.getNumberOfRows(); r++) {
            ItemsRowEntity rowEntity = new ItemsRowEntity(this.scene, this.data.getItemsInRow(r), this.growthMultiplierSupplier);
            Point2D rowPoint = new Point2D(0, (SQUARE_SIZE / 1.5) + (SQUARE_SIZE * r));
            rowEntity.addComponent(new Transform(rowPoint, 0.0, 1));

            rowEntity.setParent(this);
            this.addChild(rowEntity);
            this.rowEntities.add(rowEntity);
        }

        this.pickables = pickables;

        ParticleParameters parameters = new ParticleParameters(100, true, 
            FAST_CROP_PARTICLE, 
            ParticleSimulationSpace.WORLD, 
            SupplierGenerator.rangeSupplier(1.0, 7.0), 
            SupplierGenerator.rangeSupplier(1.0, 5.0), 
            null, 
            SupplierGenerator.staticSupplier(0.0), 
            SupplierGenerator.rangeSupplier(new Point2D(-5, -5), new Point2D(5, 0)));

        parameters.setEmissionRate(10);
        parameters.setEmissionPositionGenerator(SupplierGenerator.rangeSupplier(Point2D.ZERO, this.getSize()));
        parameters.addUpdateFunction((particle, percentage, timeStep) -> {
            particle.opacity = 1.0 * EaseCurve.FADE_IN_OUT.apply(percentage);
        });

        this.sabotageEffect = new ParticleEntity(scene, 0, parameters);
        this.sabotageEffect.setParent(this);
    }

    public static double scaleFactorToFitWidth(int squareScale, double w, double h) {
        return squareScale * FarmEntity.SQUARE_SIZE / w;
    }

    public Point2D getWorldPosition() {
        return this.transform.getWorldPosition();
    }

    public Point2D getSize() {
        return this.size;
    }

    public void assignPlayer(Integer playerID, boolean activePlayer, Supplier<GameClient> clientFunction) {
        this.removePlayer();

        this.data = new FarmData(this.farmID, playerID, this.expandFarmBy(), this.itemStore);

        int row = 0;
        for (ItemsRowEntity rowEntity : this.rowEntities) {
            rowEntity.setFarmRow(this.data.getItemsInRow(row));
            row += 1;
        }

        this.clientSupplier = clientFunction;

        if (activePlayer) {
            //this.farmUI = new FarmUI(this.data, spriteManager);
            //this.farmUI.setParent(uiContainer);
            this.addComponent(this.makeClickable());
        }
    }

    private void removePlayer() {
        this.data = new FarmData(-13, null, this.expandFarmBy(), this.itemStore);

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

        if (farmState.getAIMultiplier() != 1 || farmState.getGrowthMultiplier() != 1) {
            if (farmState.getGrowthMultiplier() > 1) {
                sabotageEffect.getParameters().setEmissionType(FAST_CROP_PARTICLE);
            }
            else if (farmState.getGrowthMultiplier() < 1) {
                sabotageEffect.getParameters().setEmissionType(SLOW_CROP_PARTICLE);
            }
            else if (farmState.getAIMultiplier() > 1) {
                sabotageEffect.getParameters().setEmissionType(AI_ATTRACT_PARTICLE);
            }
            else {
                sabotageEffect.getParameters().setEmissionType(AI_REPEL_PARTICLE);
            }
            
            if (!sabotageEffect.isPlaying()) {
                sabotageEffect.play();
            }
        }
        else if (sabotageEffect.isPlaying()) {
            sabotageEffect.stop();
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
                // Removes existing growth delegates
                if (this.destroyerListener != null) {
                    this.destroyerListener.getKey().growth.removeListener(this.destroyerListener.getValue());
                }

            } else {
                // Is within the farm. But is there any item at that coordinates?...
                Pair<FarmItem, Boolean> pickingItem = this.canBePicked(x, y);
                if (pickingItem == null) {
                    //...NO :( there is no item
                    dTransform.setPosition(point);
                    this.destroyerEntity.adaptToFarmItem(null);
                    // Removes existing growth delegates
                    if (this.destroyerListener != null) {
                        this.destroyerListener.getKey().growth.removeListener(this.destroyerListener.getValue());
                    }

                } else {
                    //...YES :) there is an item
                    FarmItem onFarmItem = pickingItem.getKey();

                    int[] itemFarmPosition = this.data.positionForItem(onFarmItem);
                    dTransform.setPosition(this.coordinatesForItemAt(itemFarmPosition[0], itemFarmPosition[1]));
                    this.destroyerEntity.adaptToFarmItem(pickingItem);

                    // Set up a delegate so that the destroyer tool gets updates about the item's growth progress
                    if (this.destroyerListener == null || !this.destroyerListener.getKey().equals(onFarmItem)) {
                        this.destroyerListener = new Pair<FarmItem, ChangeListener<? super Number>>(onFarmItem, (observable, oldVal, newVal) -> {
                            if (this.destroyerEntity == null) {
                                return;
                            }
                            if (onFarmItem.isFullyGrown()) {
                                this.destroyerEntity.setColorForPickable(true);
                            }
                        });
                        onFarmItem.growth.addListener(this.destroyerListener.getValue());
                    }
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

            // Removes existing growth delegate
            if (this.destroyerListener != null) {
                this.destroyerListener.getKey().growth.removeListener(this.destroyerListener.getValue());
            }

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
                    this.destroyerEntity.setColorForPickable(false);
                } else {
                    //...YES :) there is an item
                    FarmItem onFarmItem = pickingItem.getKey();
                    
                    int[] itemFarmPosition = this.data.positionForItem(onFarmItem);
                    dTransform.setPosition(this.coordinatesForItemAt(itemFarmPosition[0], itemFarmPosition[1]));
                    this.destroyerEntity.adaptToDestroyFarmItem(pickingItem);
                    this.destroyerEntity.setColorForPickable(true);
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
        if (this.destroyerListener != null) {
            this.destroyerListener.getKey().growth.removeListener(this.destroyerListener.getValue());
        }
        if (this.destroyerEntity != null) {
            this.destroyerEntity.destroy();
            this.destroyerEntity = null;
        }
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
        } else {
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

        boolean placed = this.data.placeItem(item, 0.0, row, column);

        if (placed) {
            this.audio.play("shovel.wav");
            PlantComponent pc = item.getComponent(PlantComponent.class);
            Point2D startPos = this.rescaleCoordinatesToScene(column, row).add((pc.width * FarmEntity.SQUARE_SIZE / 2), (pc.height * FarmEntity.SQUARE_SIZE / 1.8));
            new PlantParticleEntity(this.getScene(), startPos.getX(), startPos.getY());
        }

        this.sendStateUpdate();
        return placed;
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
        } else {
            pickedItem = this.data.pickItemAt(row, column);
        }
        this.sendStateUpdate();
        
        if (pickedItem == null) {
            return;
        }

        
        
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
            this.audio.play("shovel.wav");
        } else {
            // Animal eating the farm item
            this.audio.play("chomp.wav");
        }
    }

    /**
     * Function to expand the size of a farm.
     *
     * @param expandBy Amount to expand the farm by.
     */
    public void expandFarmBy(int expandBy) {
        this.data.expandFarm(expandBy);
    }

    /**
     * Function to expand the size of a farm.
     * @return {@code Consumer<Integer>} which expands the entity.
     */
    public Consumer<Integer> expandFarmBy() {
        return (expandBy) -> {
            double expandByPoints = SQUARE_SIZE * expandBy;

            this.size = this.size.add(expandByPoints, expandByPoints);
            this.farmRenderer.setFarmSize(this.size);

            Point2D addedDimensions;
            int row;
            switch (this.farmID) {
                case 1:
                    addedDimensions = new Point2D(expandByPoints, expandByPoints);
                    this.transform.setPosition(this.transform.getWorldPosition().subtract(addedDimensions));
                    row = 1;
                    break;
                case 2:
                    addedDimensions = new Point2D(0, expandByPoints);
                    this.transform.setPosition(this.transform.getWorldPosition().subtract(addedDimensions));
                    row = 1;
                    break;
                case 3:
                    addedDimensions = new Point2D(expandByPoints, 0);
                    this.transform.setPosition(this.transform.getWorldPosition().subtract(addedDimensions));
                default:
                    row = 0;
                    break;
            }

            for (ItemsRowEntity rowEntity : this.rowEntities) {
                Point2D rowPoint = new Point2D(0, (SQUARE_SIZE / 1.5) + (SQUARE_SIZE * row));
                rowEntity.getComponent(Transform.class).setPosition(rowPoint);
                rowEntity.setFarmRow(this.data.getItemsInRow(row));
                row += 1;
            }

            Point2D newRowPoint;
            int newRowIndex;
            switch (this.farmID) {
                case 1:
                case 2:
                    newRowPoint = new Point2D(0, SQUARE_SIZE / 1.5);
                    newRowIndex = 0;
                    break;
                default:
                    newRowPoint = new Point2D(0, (SQUARE_SIZE / 1.5) + (SQUARE_SIZE * row));
                    newRowIndex = this.data.farmRows - 1;
                    break;
            }
            ItemsRowEntity newRowEntity = new ItemsRowEntity(this.scene, this.data.getItemsInRow(newRowIndex), this.growthMultiplierSupplier);
            newRowEntity.addComponent(new Transform(newRowPoint, 0.0, 1));

            newRowEntity.setParent(this);
            this.addChild(newRowEntity);
            this.rowEntities.add(0, newRowEntity);

            this.sendStateUpdate();

            sabotageEffect.getComponent(ParticleComponent.class).parameters.setEmissionPositionGenerator(SupplierGenerator.rangeSupplier(Point2D.ZERO, this.getSize()));
        };
    }

    /**
     * Function to check has the farm reached its maximum size.
     * @return True if hit maximum, False if under maximum.
     */
    public boolean isMaxSize() {
        return this.data.getExpansionLevel() >= MAX_EXPANSION_SIZE;
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
    private Clickable makeClickable() {
        Clickable clickable = new Clickable((x, y, button) -> {
            if (this.isPickingItem()) { 
                this.pickItemAt(x, y); //Picking a plant
                this.stopPickingItem();

            } else if (this.isDestroyingItem()) {
                this.pickItemAt(x, y, true); //Destroying a plant
                this.stopPickingItem();

            } else if (this.placingItem != null && this.placeItem(placingItem, x, y)) {
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
     * If possible, sends an updated state of the farm to the server.
     */
    private void sendStateUpdate() {
        if (this.clientSupplier == null) {
            return;
        }

        GameClient client = this.clientSupplier.get();
        if (client != null) {
            try {
                client.send(new GameUpdate(GameUpdateType.FARM_STATE, client.getID(), this.data.getCurrentState()));

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Getter for the global growth multiplier for a farm, this multiplies the growth rate of all plants in a farm.
     * @return The growth multiplier for a farm.
     */
    public double getGrowthMultiplier() {
        return this.data.getGrowthMultiplier();
    }

    /**
     * Setter for the global growth multiplier for a farm, this multiplies the growth rate of all plants in a farm by the given constant.
     * @param growthMultiplier The new growth multiplier for a farm.
     */
    public void setGrowthMultiplier(double growthMultiplier) {
        this.data.setGrowthMultiplier(growthMultiplier);
        this.sendStateUpdate();

        if (growthMultiplier != 1) {
            if (growthMultiplier > 1) {
                sabotageEffect.getParameters().setEmissionType(FAST_CROP_PARTICLE);
            }
            else {
                sabotageEffect.getParameters().setEmissionType(SLOW_CROP_PARTICLE);
            }

            if (!sabotageEffect.isPlaying()) {
                sabotageEffect.play();
            }
        }
        else if (sabotageEffect.isPlaying()) {
            sabotageEffect.stop();
        }
    }

    /**
     * Getter for the AI multiplier for a farm, the higher the value, the higher the chance of the AI stealing plants from a farm.
     * @return The AI multiplier
     */
    public double getAIMultiplier() {
        return this.data.getAIMultiplier();
    }

    /**
     * Setter for the AI multiplier for a farm, the higher the value, the higher the chance of the AI stealing plants from a farm.
     * @param AIMultiplier The new AI multiplier for a farm.
     */
    public void setAIMultiplier(double AIMultiplier) {
        this.data.setAIMultiplier(AIMultiplier);
        this.sendStateUpdate();

        if (AIMultiplier != 1) {
            if (AIMultiplier > 1) {
                sabotageEffect.getParameters().setEmissionType(AI_ATTRACT_PARTICLE);
            }
            else {
                sabotageEffect.getParameters().setEmissionType(AI_REPEL_PARTICLE);
            }

            if (!sabotageEffect.isPlaying()) {
                sabotageEffect.play();
            }
        }
        else if (sabotageEffect.isPlaying()) {
            sabotageEffect.stop();
        }
    }
}
