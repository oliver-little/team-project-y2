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
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.game.item.components.SabotageComponent;
import teamproject.wipeout.game.market.MarketItem;
import teamproject.wipeout.game.market.ui.FarmExpansionUI;
import teamproject.wipeout.game.potion.PotionThrowEntity;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AIPlayer extends Player  {

    public static final int[] GOOD_POTIONS = {52, 53, 54, 55, 58, 59};
    public static final int[] MEAN_POTIONS = {51, 56, 57, 60, 61};

    public static final int IDLE_TIME_SCALING_FACTOR = 3;

    public static final int IDLE_TIME_MINIMUM = 2;

    private Point2D designatedMarketPoint;

    private FarmEntity aiFarm;
    private Point2D designatedFarmPoint;
    private double currentFarmExpansionPrice;
    private final HashMap<Integer, Integer> boughtItems;

    private final WorldEntity worldEntity;

    private final CollisionResolutionComponent collisionResolution;
    private final NavigationMesh navMesh;
    private final ScheduledExecutorService executor;

    /**
     * Creates a new animal entity, taking a game scene, starting position, a navigation mesh and a sprite manager.
     */
    public AIPlayer(GameScene scene, int playerID, String playerName, Point2D position, WorldEntity worldEntity) {
        super(scene, playerID, playerName, position, worldEntity.spriteManager, worldEntity.itemStore);

        this.aiFarm = null;
        this.currentFarmExpansionPrice = FarmExpansionUI.FARM_EXPANSION_START_PRICE;
        this.boughtItems = new HashMap<Integer, Integer>();

        this.worldEntity = worldEntity;

        this.navMesh = worldEntity.getNavMesh();
        this.executor = Executors.newSingleThreadScheduledExecutor();

        this.removeComponent(CollisionResolutionComponent.class);

        this.collisionResolution = new CollisionResolutionComponent(true, (resolutionVector) -> {
            SteeringComponent steeringComponent = this.removeComponent(SteeringComponent.class);

            if (resolutionVector.getX() == 0) {
                if (resolutionVector.getY() < 0) {
                    this.physics.acceleration = new Point2D(250, 50);
                } else {
                    this.physics.acceleration = new Point2D(-250, -50);
                }
            } else {
                if (resolutionVector.getX() < 0) {
                    this.physics.acceleration = new Point2D(50, -250);
                } else {
                    this.physics.acceleration = new Point2D(-50, 250);
                }
            }

            return (pair) -> {
                this.physics.acceleration = new Point2D(0, 0);
                if (pair.getKey() > 4) {
                    pair.getValue().run();
                    this.aiDecisionAlgorithm();

                } else {
                    Runnable oldOnArrive = steeringComponent.onArrive;
                    steeringComponent.onArrive = () -> {
                        pair.getValue().run();
                        oldOnArrive.run();
                    };
                    this.addComponent(steeringComponent);
                }
            };
        });
        this.addComponent(this.collisionResolution);
    }

    public void setDesignatedMarketPoint(Point2D designatedMarketPoint) {
        this.designatedMarketPoint = designatedMarketPoint;
    }

    public void assignFarm(FarmEntity farm, Point2D designatedPoint) {
        super.assignFarm(farm);
        this.aiFarm = farm;
        this.designatedFarmPoint = designatedPoint;
    }

    public void start() {
        this.aiDecisionAlgorithm();
    }

    /**
     * Helper function to calculate the path from the AI's current location to its destination, then initiates the traverse method.
     */
    private void aiTraverse(int x, int y, Runnable callback) {
        Point2D wp = this.getWorldPosition();
        wp = new Point2D((int) wp.getX(), (int) wp.getY());
        Point2D endPosition = new Point2D(x, y);
        if (wp.equals(endPosition)) {
            callback.run();
            return;
        }

        List<Point2D> path = PathFindingSystem.findPath(wp, endPosition, this.navMesh, 25);

        this.collisionResolution.resetControlVariables();
        this.addComponent(new SteeringComponent(path, callback, 300));
    }

    /**
     * Idles for a random, short period of time.
     */
    private void aiIdle(Runnable completion) {
        long idleTime = (long) (Math.random() * IDLE_TIME_SCALING_FACTOR) + IDLE_TIME_MINIMUM;
        Runnable finishedIdling;
        if (completion == null) {
            finishedIdling = () -> Platform.runLater(() -> this.aiDecisionAlgorithm());
        } else {
            finishedIdling = () -> Platform.runLater(completion);
        }
        this.executor.schedule(finishedIdling, idleTime, TimeUnit.SECONDS);
    }

    /**
     * Steals crops from a given farm.
     */
    private void aiHarvestCrops() {
        List<Point2D> fullyGrownItems = this.aiFarm.getGrownItemPositions();

        //If there are no fully grown crops in the selected farm, just find a random location instead.
        if (fullyGrownItems.size() == 0) {
            this.aiDecisionAlgorithm();
            return;
        }

        int rand = new Random().nextInt(fullyGrownItems.size());
        int x = (int) fullyGrownItems.get(rand).getX();
        int y = (int) fullyGrownItems.get(rand).getY();

        Runnable onComplete = () ->  {
            double randDestroy = Math.random();
            if (this.getMoney() > 100.0 && (randDestroy < 0.05 || (randDestroy < 0.1 && this.aiFarm.itemAt(x, y).get().getComponent(PlantComponent.class).isTree))) {
                System.out.println("Destroy " + this.playerID);
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
                    this.aiDecisionAlgorithm();
                    return;
                }
                this.aiHarvestCrops();

            } else {
                Platform.runLater(() -> this.aiDecisionAlgorithm());
            }
        };

        aiTraverse(x, y, onComplete);
    }

    /**
     * Finds a random point in the world to navigate to.
     */
    private void aiPathFind() {
        //Go to random point
        int rand = new Random().nextInt(navMesh.squares.size());

        NavigationSquare randomSquare = navMesh.squares.get(rand);

        int randX = randomInteger((int) randomSquare.topLeft.getX(), (int) randomSquare.bottomRight.getX());

        int randY = randomInteger((int) randomSquare.topLeft.getY(), (int) randomSquare.bottomRight.getY());

        aiTraverse(randX, randY, () -> this.aiDecisionAlgorithm());
    }

    /**
     * Runnable method which runs when the animal arrives at its destination, in this case, steals vegetables, picks a new destination to go to or idles.
     */
    private void aiDecisionAlgorithm() {
        Supplier<ClockSystem> clockSupplier = this.worldEntity.getClockSupplier();
        if (clockSupplier != null && clockSupplier.get().getTime() <= 0.0) {
            System.out.println("STOP " + this.playerID);
            System.out.println("AI: " + this.getMoney());
            System.out.println("AI: " + this.inventory.toString());
            return;
        }

        //Make decision on what to do next.
        double probability = Math.random();

        if (probability < 0.85) {
            if (clockSupplier != null && clockSupplier.get().getTime() < 30.0) {
                aiHarvestCrops();
                return;
            }

            if (Math.random() < 0.5 || !this.canBuyCheapestSeed()) {
                //Steal plants
                aiHarvestCrops();

            } else {
                goToMarket();
            }

        } else {
            double randomiser = Math.random();
            if (randomiser < 0.2 && this.hasEnoughMoney(130)) {
                usePotions();

            } else if (randomiser < 0.4) {
                //Pick random point
                aiPathFind();

            } else {
                //Idle
                aiIdle(null);
            }
        }
    }

    private void goToMarket() {
        aiTraverse((int) this.designatedMarketPoint.getX(), (int) this.designatedMarketPoint.getY(), () -> {

            Runnable buyPlantsAction = () -> this.buyPlants((boughtPlants) -> {
                this.aiIdle(() -> {
                    if (boughtPlants) {
                        this.plantPlants();
                    } else {
                        this.aiDecisionAlgorithm();
                    }
                });
            });

            if (Math.random() > 0.8 && !this.aiFarm.isMaxSize()) {
                this.buyAndApplyFarmExpansion((boughtExtension) -> {
                    if (boughtExtension) {
                        this.aiDecisionAlgorithm();
                    } else {
                        buyPlantsAction.run();
                    }
                });
            } else {
                buyPlantsAction.run();
            }
        });
    }

    private void buyPlants(Consumer<Boolean> completion) {
        double emptySpaces = this.aiFarm.getEmptySpaces();
        double currentBalance = this.getMoney();

        if (emptySpaces > 0 && currentBalance > 0.0) {
            boolean boughtSomething = false;
            boolean allowTrees = true;
            while (emptySpaces > 0) {
                boolean canBeTree = Math.random() > 0.6 && allowTrees && this.aiFarm.canPlaceTree();
                Pair<Integer[], Double> buyPair = this.chooseItemToBuy(currentBalance, canBeTree);
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

    private void buyAndApplyFarmExpansion(Consumer<Boolean> completion) {
        if (this.hasEnoughMoney(this.currentFarmExpansionPrice + 30)) {
            System.out.println("Expand " + this.playerID);
            this.setMoney(this.getMoney() - this.currentFarmExpansionPrice);
            this.aiFarm.expandFarmBy(1);
            this.currentFarmExpansionPrice *= FarmExpansionUI.PRICE_MULTIPLIER;
            completion.accept(true);
        } else {
            completion.accept(false);
        }
    }

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
            this.aiDecisionAlgorithm();
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
                    this.aiDecisionAlgorithm();
                }
        );
    }

    private void usePotions() {
        boolean goodOrMean = Math.random() > 0.5;
        ArrayList<Integer> potionPortfolio = new ArrayList<Integer>();

        for (int id : goodOrMean ? GOOD_POTIONS : MEAN_POTIONS) {
            if (this.hasEnoughMoney(this.worldEntity.aiPlayerHelper.potionStockDatabase.get(id).getCurrentBuyPrice())) {
                potionPortfolio.add(id);
            }
        }

        if (potionPortfolio.isEmpty()) {
            this.aiDecisionAlgorithm();
            return;
        }

        int potionID = potionPortfolio.get(new Random().nextInt(potionPortfolio.size()));

        this.buyItem(this.worldEntity.getMarket(), potionID, 1);
        this.removeItem(potionID, 1);

        Player useOnPlayer = null;
        List<Player> allPlayers = this.worldEntity.getPlayers();

        if (goodOrMean) {
            useOnPlayer = this;
        } else {
            allPlayers.sort(Comparator.comparing(player -> player.getMoney())); // does it give me the richest???
            useOnPlayer = allPlayers.get(new Random().nextInt(allPlayers.size()));
        }

        Item currentPotion = this.itemStore.getItem(potionID);
        SabotageComponent sc = currentPotion.getComponent(SabotageComponent.class);
        ArrayList<GameEntity> possibleEffectEntities = new ArrayList<GameEntity>();
        if (sc.type == SabotageComponent.SabotageType.SPEED) {
            possibleEffectEntities.addAll(allPlayers);
            possibleEffectEntities.add(this.worldEntity.myAnimal);
        }
        else if (sc.type == SabotageComponent.SabotageType.GROWTHRATE || sc.type == SabotageComponent.SabotageType.AI) {
            possibleEffectEntities.addAll(this.worldEntity.farms.values());
        }

        Runnable onComplete = () -> this.aiDecisionAlgorithm();

        System.out.println("Potion from " + this.playerID);
        System.out.println("used on " + useOnPlayer.playerID);

        PotionThrowEntity potionThrow = new PotionThrowEntity(this.getScene(), this.worldEntity.spriteManager, this, this.worldEntity.myCurrentPlayer, currentPotion, possibleEffectEntities, onComplete, onComplete);

        Point2D useOnPlayerPosition = useOnPlayer.getWorldPosition();
        potionThrow.onClick.performMouseClickAction(useOnPlayerPosition.getX(), useOnPlayerPosition.getY(), MouseButton.PRIMARY);
    }

    /**
     * Calculate a random integer.
     * @param min Min Value.
     * @param max Max value.
     * @return The random integer.
     */
    private int randomInteger(int min, int max) {
        return new Random().nextInt(Math.abs(max - min)) + min;
    }

    private Pair<Integer[], Double> chooseItemToBuy(double withinPrice, boolean canBeTree) {
        AIPlayerHelper aiPlayerHelper = this.worldEntity.aiPlayerHelper;

        MarketItem bestToBuy = null;
        boolean isTree = false;
        double currentPriceDiff = 0.0;

        for (MarketItem marketItem : aiPlayerHelper.seedStockDatabase) {
            int randBoundary = new Random().nextInt(5);
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

    private boolean canBuyCheapestSeed() {
        MarketItem firstSeed = this.worldEntity.aiPlayerHelper.seedStockDatabase[0];
        return this.hasEnoughMoney(firstSeed.getCurrentBuyPrice());
    }

}
