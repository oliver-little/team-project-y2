package teamproject.wipeout.game.player;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.ai.NavigationSquare;
import teamproject.wipeout.engine.component.ai.SteeringComponent;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.gameclock.ClockSystem;
import teamproject.wipeout.engine.system.ai.PathFindingSystem;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.inventory.InventoryItem;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.game.item.components.SabotageComponent;
import teamproject.wipeout.game.market.MarketItem;
import teamproject.wipeout.game.market.ui.FarmExpansionUI;
import teamproject.wipeout.game.potion.PotionThrowEntity;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Instance adding AI capabilities to the base {@link Player} class.
 */
public class AIPlayer extends Player {

    public static final String[] AI_NAMES = new String[]{"Siri", "Alexa", "Cortana"};

    public static final double COLLISION_RESOLUTION_SPEED = 250.0;
    public static final long COLLISION_RESOLUTION_TIME = 500;

    public static final int IDLE_TIME_SCALING_FACTOR = 4;
    public static final int IDLE_TIME_MINIMUM = 1;

    public static final int[] GOOD_POTIONS = {52, 53, 54, 55, 58, 59};
    public static final int[] MEAN_POTIONS = {51, 56, 57, 60, 61};

    private static final double NOMINAL_SPEED = 300.0;
    private static final double CORNERING_RADIUS = 25.0;

    private final NavigationMesh navMesh;
    private final CollisionResolutionComponent collisionResolution;
    private final ScheduledExecutorService executor;
    private final Random random;

    private final WorldEntity worldEntity;
    private final HashMap<Integer, Integer> boughtItems;

    private FarmEntity aiFarm;
    private Point2D designatedFarmPoint;
    private Point2D designatedMarketPoint;

    private double currentFarmExpansionPrice;

    /**
     * Creates a new {@code AIPlayer} entity.
     *
     * @param scene       The {@link GameScene} this entity is part of
     * @param playerInfo  Player ID and name
     * @param worldEntity Current {@link WorldEntity}
     */
    public AIPlayer(GameScene scene, Pair<Integer, String> playerInfo, String spriteSheet, WorldEntity worldEntity) {
        super(scene, playerInfo, spriteSheet, worldEntity.spriteManager, worldEntity.itemStore);

        this.navMesh = worldEntity.getNavMesh();
        this.collisionResolution = this.createCollisionResolutionComponent();
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.random = ThreadLocalRandom.current();

        this.worldEntity = worldEntity;
        this.boughtItems = new HashMap<Integer, Integer>();

        this.aiFarm = null;
        this.designatedFarmPoint = null;
        this.designatedMarketPoint = null;

        this.currentFarmExpansionPrice = FarmExpansionUI.FARM_EXPANSION_START_PRICE;

        this.removeComponent(CollisionResolutionComponent.class);
        this.addComponent(this.collisionResolution);
    }

    /**
     * Sets position on the map which will be used by the {@code AIPlayer} as the market position.
     *
     * @param designatedMarketPoint {@code AIPlayer}'s market position of type {@link Point2D}
     */
    public void setDesignatedMarketPoint(Point2D designatedMarketPoint) {
        this.designatedMarketPoint = designatedMarketPoint;
    }

    /**
     * Assigns the given farm to the {@code AIPlayer}.
     *
     * @param farm            {@link FarmEntity} to be assigned
     * @param designatedPoint {@link Point2D} position of the farm
     */
    public void assignFarm(FarmEntity farm, Point2D designatedPoint) {
        super.assignFarm(farm);
        this.aiFarm = farm;
        this.designatedFarmPoint = designatedPoint;
    }

    /**
     * Kickstarts the {@code AIPlayer} and its decision process.
     */
    public void start() {
        this.aiMakeDecision();
    }

    /**
     * Main decision making method for the {@code AIPlayer}.
     */
    private void aiMakeDecision() {
        Supplier<ClockSystem> clockSupplier = this.worldEntity.getClockSupplier();
        if (clockSupplier != null && clockSupplier.get().getTime() <= 0.0) {
            return;
        }

        //Make decision on what to do next.
        double probability = Math.random();

        if (probability < 0.85) {
            if (clockSupplier != null && clockSupplier.get().getTime() < 30.0) {
                //Harvest plants near the end of the game
                this.aiHarvestPlants();
                return;
            }

            if (Math.random() < 0.5 || !this.canBuyCheapestSeed()) {
                this.aiHarvestPlants();

            } else {
                this.goToMarket();
            }

        } else {
            double randomiser = Math.random();
            if (randomiser < 0.1 && this.hasEnoughMoney(130)) {
                this.usePotion();

            } else if (randomiser < 0.4) {
                this.aiPathFind();

            } else {
                this.aiIdle(null);
            }
        }
    }

    private Runnable aiDecisionAlgorithm() {
        return () -> this.aiMakeDecision();
    }

    /**
     * Helper function to calculate the path from the AI's current location to its destination,
     * then initiates the traverse method.
     *
     * @param x          Destination X coordinate
     * @param y          Destination Y coordinate
     * @param completion Action executed when the AI reaches its destination
     */
    private void aiTraverse(int x, int y, Runnable completion) {
        Point2D currentPosition = this.getWorldPosition();
        currentPosition = new Point2D((int) currentPosition.getX(), (int) currentPosition.getY());

        Point2D endPosition = new Point2D(x, y);
        if (currentPosition.equals(endPosition)) {
            completion.run();
            return;
        }

        List<Point2D> path = PathFindingSystem.findPath(currentPosition, endPosition, this.navMesh, CORNERING_RADIUS);

        this.collisionResolution.resetControlVariables();
        if (!this.hasComponent(SteeringComponent.class)) {
            this.addComponent(new SteeringComponent(path, completion, AIPlayer.NOMINAL_SPEED));
        } else {
            this.aiMakeDecision();
        }
    }

    /**
     * Idles for a random, short period of time.
     *
     * @param completion Action executed when the AI finishes idling
     */
    private void aiIdle(Runnable completion) {
        long idleTime = (long) (Math.random() * IDLE_TIME_SCALING_FACTOR) + IDLE_TIME_MINIMUM;
        Runnable finishedIdling = () -> Platform.runLater(completion == null ? this.aiDecisionAlgorithm() : completion);
        this.executor.schedule(finishedIdling, idleTime, TimeUnit.SECONDS);
    }

    /**
     * Harvests plants from its farm.
     */
    private void aiHarvestPlants() {
        List<Point2D> fullyGrownItems = this.aiFarm.getGrownItemPositions();

        //If there are no fully grown crops in the selected farm, make decisions again.
        if (fullyGrownItems.size() == 0) {
            this.aiMakeDecision();
            return;
        }

        int rand = this.random.nextInt(fullyGrownItems.size());
        int x = (int) fullyGrownItems.get(rand).getX();
        int y = (int) fullyGrownItems.get(rand).getY();

        Runnable onComplete = () -> {
            double randDestroy = Math.random();
            boolean isTree = this.aiFarm.itemAt(x, y).get().getComponent(PlantComponent.class).isTree;

            if (this.getMoney() > 100.0 && (randDestroy < 0.05 || (randDestroy < 0.1 && isTree))) {
                this.aiFarm.pickItemAt(x, y, false, true);

            } else {
                Integer[] picked = this.aiFarm.pickItemAt(x, y, false, false);
                if (picked != null) {
                    Integer pickedID = picked[0];
                    int pickedQuantity = picked[1];
                    this.acquireItem(pickedID, pickedQuantity);
                    this.sellItem(this.worldEntity.getMarket(), pickedID, pickedQuantity);
                }
            }

            if (fullyGrownItems.size() - 1 > 0) {
                Supplier<ClockSystem> clockSupplier = this.worldEntity.getClockSupplier();
                if (clockSupplier != null && clockSupplier.get().getTime() <= 0.0) {
                    this.aiMakeDecision();
                    return;
                }
                this.aiHarvestPlants();

            } else {
                Platform.runLater(this.aiDecisionAlgorithm());
            }
        };

        this.aiTraverse(x, y, onComplete);
    }

    /**
     * Finds a random point in the world to navigate to.
     */
    private void aiPathFind() {
        int rand = this.random.nextInt(this.navMesh.squares.size());
        NavigationSquare randomSquare = this.navMesh.squares.get(rand);

        int randX = randomInteger((int) randomSquare.topLeft.getX(), (int) randomSquare.bottomRight.getX());
        int randY = randomInteger((int) randomSquare.topLeft.getY(), (int) randomSquare.bottomRight.getY());

        this.aiTraverse(randX, randY, this.aiDecisionAlgorithm());
    }

    /**
     * Tells the {@code AIPlayer} to go to the market and decides what to buy.
     */
    private void goToMarket() {
        aiTraverse((int) this.designatedMarketPoint.getX(), (int) this.designatedMarketPoint.getY(), () -> {

            Runnable buyPlantsAction = () -> this.buyPlants((boughtPlants) -> {
                this.aiIdle(() -> {
                    if (boughtPlants) {
                        this.plantPlants();
                    } else {
                        this.aiMakeDecision();
                    }
                });
            });

            if (Math.random() > 0.8 && !this.aiFarm.isMaxSize()) {
                this.buyAndApplyFarmExpansion((boughtExtension) -> {
                    if (boughtExtension) {
                        this.aiMakeDecision();
                    } else {
                        buyPlantsAction.run();
                    }
                });

            } else {
                buyPlantsAction.run();
            }
        });
    }

    /**
     * {@code AIPlayer} buys plants.
     */
    private void buyPlants(Consumer<Boolean> completion) {
        double emptySpaces = this.aiFarm.getEmptySpaces();
        double currentBalance = this.getMoney();

        if (emptySpaces > 0 && currentBalance > 0.0) {
            boolean boughtSomething = false;
            boolean allowTrees = true;
            while (emptySpaces > 0) {
                boolean canBeTree = Math.random() > 0.6 && allowTrees && this.aiFarm.canFitTree();
                Pair<Integer[], Double> buyPair = this.choosePlantToBuy(currentBalance, canBeTree);
                if (buyPair == null) {
                    break;
                }

                int buyID = buyPair.getKey()[0];
                this.buyItem(this.worldEntity.getMarket(), buyID, 1);
                boughtSomething = true;

                this.boughtItems.merge(buyID, 1, (a, b) -> Integer.sum(a, b));
                emptySpaces -= buyPair.getKey()[1];
                currentBalance -= buyPair.getValue();
                allowTrees = buyPair.getKey()[1] <= 1;
            }
            completion.accept(boughtSomething);

        } else {
            completion.accept(false);
        }
    }

    /**
     * {@code AIPlayer} chooses what to buy.
     *
     * @param withinPrice Highest price
     * @param canBeTree   {@code true} if it can buy a tree, otherwise {@code false}
     */
    private Pair<Integer[], Double> choosePlantToBuy(double withinPrice, boolean canBeTree) {
        AIPlayerHelper aiPlayerHelper = this.worldEntity.aiPlayerHelper;

        MarketItem bestToBuy = null;
        boolean isTree = false;
        double currentPriceDiff = 0.0;

        for (MarketItem marketItem : aiPlayerHelper.seedStockDatabase) {
            int randBoundary = this.random.nextInt(5);
            double currentBuyPrice = marketItem.getCurrentBuyPrice();
            if (currentBuyPrice > withinPrice || this.boughtItems.getOrDefault(marketItem.getID(), 0) > randBoundary) {
                continue;
            }

            Item item = this.itemStore.getItem(marketItem.getID());
            PlantComponent plant = item.getComponent(PlantComponent.class);
            if (!canBeTree && plant.isTree) {
                continue;
            }

            int avgDrop = (plant.minDrop + plant.maxDrop) / 2;
            if (Math.random() > 0.5) {
                double grownItemCurrentSellPrice = aiPlayerHelper.grownUpStockDatabase.get(plant.grownItemID).getCurrentSellPrice();
                double newPriceDiff = (grownItemCurrentSellPrice * avgDrop) - currentBuyPrice;
                if (bestToBuy == null || newPriceDiff > currentPriceDiff) {
                    bestToBuy = marketItem;
                    isTree = plant.isTree;
                    currentPriceDiff = newPriceDiff;
                    continue;
                }
            } else {
                if (bestToBuy == null || bestToBuy.getCurrentBuyPrice() < currentBuyPrice) {
                    bestToBuy = marketItem;
                    isTree = plant.isTree;
                    currentPriceDiff = (marketItem.getCurrentSellPrice() * avgDrop) - currentBuyPrice;
                    continue;
                }
            }
        }

        if (bestToBuy != null) {
            return new Pair<Integer[], Double>(new Integer[]{bestToBuy.getID(), isTree ? 4 : 1}, bestToBuy.getCurrentBuyPrice());
        }
        return null;
    }

    /**
     * @return {@code true} if the {@code AIPlayer} can buy at least the cheapest plant, otherwise {@code false}.
     */
    private boolean canBuyCheapestSeed() {
        MarketItem firstSeed = this.worldEntity.aiPlayerHelper.seedStockDatabase[0];
        return this.hasEnoughMoney(firstSeed.getCurrentBuyPrice());
    }

    /**
     * {@code AIPlayer} buys farm expansion.
     */
    private void buyAndApplyFarmExpansion(Consumer<Boolean> completion) {
        if (this.hasEnoughMoney(this.currentFarmExpansionPrice + 30)) {
            this.setMoney(this.getMoney() - this.currentFarmExpansionPrice);
            this.aiFarm.expandFarmByN(1);
            this.currentFarmExpansionPrice *= FarmExpansionUI.PRICE_MULTIPLIER;
            completion.accept(true);
        } else {
            completion.accept(false);
        }
    }

    /**
     * {@code AIPlayer} plants previously bought plants.
     */
    private void plantPlants() {
        ArrayList<Item> itemsToPlant = new ArrayList<Item>();

        for (InventoryItem inventoryItem : this.inventory) {
            if (inventoryItem != null) {
                Item item = this.itemStore.getItem(inventoryItem.itemID);
                if (item.hasComponent(PlantComponent.class)) {
                    itemsToPlant.add(item);
                }
            }
        }

        if (itemsToPlant.isEmpty()) {
            this.aiMakeDecision();
            return;
        }

        this.aiTraverse(
                (int) this.designatedFarmPoint.getX(),
                (int) this.designatedFarmPoint.getY(),
                () -> {
                    for (Item plantable : itemsToPlant) {
                        this.removeItem(plantable.id, 1);

                        PlantComponent plant = plantable.getComponent(PlantComponent.class);
                        int[] freeSquare = this.aiFarm.firstFreeSquareFor(plant.isTree ? 2 : 1, plant.isTree ? 2 : 1);
                        if (freeSquare == null) {
                            continue;
                        }

                        int row = freeSquare[0];
                        int column = freeSquare[1];
                        this.aiFarm.placeItemAtSquare(plantable, row, column);
                    }
                    this.aiMakeDecision();
                }
        );
    }

    /**
     * {@code AIPlayer} buys and uses a potion.
     */
    private void usePotion() {
        boolean goodOrMean = Math.random() > 0.5;
        ArrayList<Integer> potionPortfolio = new ArrayList<Integer>();

        for (int id : goodOrMean ? GOOD_POTIONS : MEAN_POTIONS) {
            if (this.hasEnoughMoney(this.worldEntity.aiPlayerHelper.potionStockDatabase.get(id).getCurrentBuyPrice())) {
                potionPortfolio.add(id);
            }
        }

        if (potionPortfolio.isEmpty()) {
            this.aiMakeDecision();
            return;
        }

        int potionID = potionPortfolio.get(this.random.nextInt(potionPortfolio.size()));

        this.buyItem(this.worldEntity.getMarket(), potionID, 1);
        this.removeItem(potionID, 1);

        Player useOnPlayer = null;
        List<Player> allPlayers = this.worldEntity.getPlayers();

        if (goodOrMean) {
            useOnPlayer = this;

        } else {
            allPlayers.sort(Comparator.comparing(player -> player.getMoney()));
            useOnPlayer = allPlayers.get(this.random.nextInt(allPlayers.size()));
        }

        Item currentPotion = this.itemStore.getItem(potionID);
        SabotageComponent sc = currentPotion.getComponent(SabotageComponent.class);
        ArrayList<GameEntity> possibleEffectEntities = new ArrayList<GameEntity>();
        if (sc.type == SabotageComponent.SabotageType.SPEED) {
            possibleEffectEntities.addAll(allPlayers);
            possibleEffectEntities.add(this.worldEntity.myAnimal);

        } else if (sc.type == SabotageComponent.SabotageType.GROWTHRATE || sc.type == SabotageComponent.SabotageType.AI) {
            possibleEffectEntities.addAll(this.worldEntity.farms.values());
        }

        Runnable onComplete = this.aiDecisionAlgorithm();
        PotionThrowEntity potionThrow = new PotionThrowEntity(
                this.getScene(),
                this.worldEntity.spriteManager,
                this,
                this.worldEntity.myCurrentPlayer,
                currentPotion,
                possibleEffectEntities,
                onComplete,
                onComplete
        );

        Point2D useOnPlayerPosition = useOnPlayer.getWorldPosition();
        potionThrow.onClick.performMouseClickAction(useOnPlayerPosition.getX(), useOnPlayerPosition.getY(), MouseButton.PRIMARY);
    }

    /**
     * Calculate a random integer.
     *
     * @param min Min value
     * @param max Max value
     * @return The random integer
     */
    private int randomInteger(int min, int max) {
        return this.random.nextInt(Math.abs(max - min)) + min;
    }

    /**
     * @return Created {@link CollisionResolutionComponent} customized for the {@code AIPlayer}.
     */
    private CollisionResolutionComponent createCollisionResolutionComponent() {
        return new CollisionResolutionComponent(true, (resolutionVector) -> {
            SteeringComponent steeringComponent = this.getComponent(SteeringComponent.class);
            if (steeringComponent == null) {
                return (pair) -> {
                    pair.getValue().run();
                };
            }
            steeringComponent.paused = true;

            int vectorX = (int) resolutionVector.getX();
            int vectorY = (int) resolutionVector.getY();

            if (vectorX < 0) {
                if (vectorY < 0) {
                    this.physics.acceleration = new Point2D(COLLISION_RESOLUTION_SPEED, COLLISION_RESOLUTION_SPEED);
                } else if (vectorY > 0) {
                    this.physics.acceleration = new Point2D(COLLISION_RESOLUTION_SPEED, -COLLISION_RESOLUTION_SPEED);
                } else {
                    int oneMultiplier = Math.random() > 0.5 ? 1 : -1;
                    this.physics.acceleration = new Point2D(COLLISION_RESOLUTION_SPEED, oneMultiplier * COLLISION_RESOLUTION_SPEED);
                }

            } else if (vectorX > 0) {
                if (vectorY < 0) {
                    this.physics.acceleration = new Point2D(-COLLISION_RESOLUTION_SPEED, COLLISION_RESOLUTION_SPEED);
                } else if (vectorY > 0) {
                    this.physics.acceleration = new Point2D(-COLLISION_RESOLUTION_SPEED, -COLLISION_RESOLUTION_SPEED);
                } else {
                    int oneMultiplier = Math.random() > 0.5 ? 1 : -1;
                    this.physics.acceleration = new Point2D(-COLLISION_RESOLUTION_SPEED, oneMultiplier * COLLISION_RESOLUTION_SPEED);
                }

            } else {
                int oneMultiplier = Math.random() > 0.5 ? 1 : -1;
                if (vectorY < 0) {
                    this.physics.acceleration = new Point2D(oneMultiplier * COLLISION_RESOLUTION_SPEED, COLLISION_RESOLUTION_SPEED);
                } else if (vectorY > 0) {
                    this.physics.acceleration = new Point2D(oneMultiplier * COLLISION_RESOLUTION_SPEED, -COLLISION_RESOLUTION_SPEED);
                } else {
                    this.physics.acceleration = new Point2D(oneMultiplier * COLLISION_RESOLUTION_SPEED, oneMultiplier * COLLISION_RESOLUTION_SPEED);
                }
            }

            return (pair) -> {
                this.physics.acceleration = new Point2D(0, 0);
                if (pair.getKey() > 4) {
                    steeringComponent.paused = false;
                    steeringComponent.currentPoint = steeringComponent.path.size() - 1;
                    pair.getValue().run();
                    this.aiMakeDecision();

                } else {
                    Runnable oldOnArrive = steeringComponent.onArrive;
                    steeringComponent.onArrive = () -> {
                        pair.getValue().run();
                        oldOnArrive.run();
                    };
                    steeringComponent.paused = false;
                }
            };
        });
    }

}
