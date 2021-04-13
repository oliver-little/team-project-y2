package teamproject.wipeout.game.player;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.ai.NavigationSquare;
import teamproject.wipeout.engine.component.ai.SteeringComponent;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.gameclock.ClockSystem;
import teamproject.wipeout.engine.system.ai.PathFindingSystem;
import teamproject.wipeout.game.entity.AnimalEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.game.item.components.SabotageComponent;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketItem;
import teamproject.wipeout.game.market.ui.FarmExpansionUI;
import teamproject.wipeout.game.potion.PotionThrowEntity;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AIPlayerComponent implements GameComponent  {

    public static final int[] GOOD_POTIONS = {52, 53, 54, 55, 58, 59};
    public static final int[] MEAN_POTIONS = {51, 56, 57, 60, 61};

    public static final int IDLE_TIME_SCALING_FACTOR = 3;

    public static final int IDLE_TIME_MINIMUM = 2;

    public List<CurrentPlayer> allCurrentPlayers;
    public AnimalEntity theAnimal;
    public List<FarmEntity> otherFarms;
    private CurrentPlayer myCurrentPlayer;
    private FarmEntity myFarm;

    private NavigationMesh navMesh;

    private Transform transformComponent;
    private Market market;

    private ScheduledExecutorService executor;

    public Supplier<ClockSystem> clock;

    /**
     * Creates a new animal entity, taking a game scene, starting position, a navigation mesh and a sprite manager.
     */
    public AIPlayerComponent(CurrentPlayer currentPlayer, Market market, NavigationMesh navMesh, FarmEntity myFarm) {
        this.myCurrentPlayer = currentPlayer;
        this.myFarm = myFarm;
        this.market = market;

        this.navMesh = navMesh;

        this.executor = Executors.newSingleThreadScheduledExecutor();

        this.transformComponent = currentPlayer.getComponent(Transform.class);

        this.aiDecisionAlgorithm().run();
    }

    /**
     * Helper function to calculate the path from the AI's current location to its destination, then initiates the traverse method.
     */
    private void aiTraverse(int x, int y, Runnable callback) {
        Point2D wp = transformComponent.getWorldPosition();

        List<Point2D> path = PathFindingSystem.findPath(new Point2D((int) wp.getX(), (int) wp.getY()), new Point2D(x, y), navMesh, 100);

        this.myCurrentPlayer.addComponent(new SteeringComponent(path, callback, 250));
    }

    /**
     * Idles for a random, short period of time.
     */
    private void aiIdle() {
        long idleTime = (long) (Math.random() * IDLE_TIME_SCALING_FACTOR) + IDLE_TIME_MINIMUM;

        executor.schedule(() -> Platform.runLater(this.aiDecisionAlgorithm()), idleTime, TimeUnit.SECONDS);
    }

    /**
     * Steals crops from a given farm.
     */
    private void aiHarvestCrops() {
        List<Point2D> fullyGrownItems = this.myFarm.getGrownItemPositions();

        //If there are no fully grown crops in the selected farm, just find a random location instead.
        if (fullyGrownItems.size() == 0) {
            this.aiDecisionAlgorithm().run();
            return;
        }

        int rand = new Random().nextInt(fullyGrownItems.size());
        int x = (int) fullyGrownItems.get(rand).getX();
        int y = (int) fullyGrownItems.get(rand).getY();

        Runnable onComplete = () ->  {
            double randDestroy = Math.random();
            if (this.myCurrentPlayer.getMoney() > 100.0 && (randDestroy < 0.05 || (randDestroy < 0.1 && this.myFarm.itemAt(x, y).get().getComponent(PlantComponent.class).isTree))) {
                System.out.println("Destroy " + this.myCurrentPlayer.playerID);
                this.myFarm.pickItemAt(x, y, false, true);
            } else {
                Integer[] picked = this.myFarm.pickItemAt(x, y, false, false);
                if (picked != null) {
                    Integer pickedID = picked[0];
                    int pickedQuantity = picked[1];
                    this.myCurrentPlayer.acquireItem(pickedID, pickedQuantity);
                    this.myCurrentPlayer.sellItem(this.market, pickedID, pickedQuantity);
                }
            }

            if (fullyGrownItems.size() - 1 > 0) {
                if (this.clock != null && this.clock.get().clockUI.getTime() <= 0.0) {
                    this.aiDecisionAlgorithm().run();
                    return;
                }
                this.aiHarvestCrops();
            } else {
                Platform.runLater(this.aiDecisionAlgorithm());
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

        aiTraverse(randX, randY, this.aiDecisionAlgorithm());
    }

    /**
     * Runnable method which runs when the animal arrives at its destination, in this case, steals vegetables, picks a new destination to go to or idles.
     */
    private Runnable aiDecisionAlgorithm() {
        return () -> {
            if (this.clock != null && this.clock.get().clockUI.getTime() <= 0.0) {
                System.out.println("STOP " + this.myCurrentPlayer.playerID);
                System.out.println("AI: " + this.myCurrentPlayer.getMoney());
                System.out.println("AI: " + this.myCurrentPlayer.getInventory().toString());
                return;
            }

            //Make decision on what to do next.
            double probability = Math.random();

            if (probability < 0.8) {
                if (this.clock != null && this.clock.get().clockUI.getTime() < 30.0) {
                    aiHarvestCrops();
                    return;
                }

                if (Math.random() < 0.65) {
                    //Steal plants
                    aiHarvestCrops();

                } else {
                    if (Math.random() > 0.8) {
                        double expansionAddition = (this.myFarm.getExpansionLevel() * FarmExpansionUI.PRICE_MULTIPLIER);
                        double expansionPrice = 100 * (expansionAddition == 0.0 ? 1.0 : expansionAddition);
                        if (this.myCurrentPlayer.hasEnoughMoney(expansionPrice + 30)) {
                            System.out.println("Expand " + this.myCurrentPlayer.playerID);
                            this.myCurrentPlayer.setMoney(this.myCurrentPlayer.getMoney() - expansionPrice);
                            this.myFarm.expandFarmBy(1);
                            this.aiDecisionAlgorithm().run();
                        } else {
                            aiTraverse((int) this.myFarm.getWorldPosition().getX(), (int) this.myFarm.getWorldPosition().getY(), () -> {
                                this.buyPlants();
                                this.aiDecisionAlgorithm().run();
                            });
                        }
                    } else {
                        aiTraverse((int) this.myFarm.getWorldPosition().getX(), (int) this.myFarm.getWorldPosition().getY(), () -> {
                            this.buyPlants();
                            this.aiDecisionAlgorithm().run();
                        });
                    }
                }

            } else {
                double randomiser = Math.random();
                if (randomiser < 0.2 && this.myCurrentPlayer.hasEnoughMoney(130)) {
                    usePotions();

                } else if (randomiser < 0.4) {
                    //Pick random point
                    aiPathFind();

                } else {
                    //Idle
                    aiIdle();
                }
            }
        };
    }

    private void buyPlants() {
        double emptySpaces = this.myFarm.getEmptySpaces();
        double currentBalance = this.myCurrentPlayer.getMoney();

        if (emptySpaces > 0 && currentBalance > 0.0) {
            ArrayList<Integer> boughtPlants = new ArrayList<>();
            boolean allowTrees = true;
            while (emptySpaces > 0) {
                boolean canBeTree = Math.random() > 0.6 && allowTrees && this.myFarm.canPlaceTree();
                Pair<Integer[], Double> buyPair = this.chooseItemToBuy(currentBalance, canBeTree);
                if (buyPair == null) {
                    break;
                }

                int buyID = buyPair.getKey()[0];
                this.boughtItems.merge(buyID, 1, (a, b) -> Integer.sum(a, b));
                emptySpaces -= buyPair.getKey()[1];
                currentBalance -= buyPair.getValue();
                boughtPlants.add(buyID);
                allowTrees = buyPair.getKey()[1] <= 1;
            }
            if (boughtPlants.isEmpty()) {
                return;
            }

            for (Integer boughtID : boughtPlants) {
                this.myCurrentPlayer.buyItem(this.market, boughtID, 1);
                this.myCurrentPlayer.removeItem(boughtID, 1);

                Item item = this.myCurrentPlayer.itemStore.getItem(boughtID);
                PlantComponent plant = item.getComponent(PlantComponent.class);

                int[] freeSquare = this.myFarm.firstFreeSquareFor(plant.isTree ? 2 : 1, plant.isTree ? 2 : 1);

                if (freeSquare == null) {
                    continue;
                }
                int row = freeSquare[0];
                int column = freeSquare[1];
                this.myFarm.placeItemAtSquare(item, row, column);
            }
        }
    }

    private void usePotions() {
        boolean goodOrMean = Math.random() > 0.5;
        ArrayList<Integer> potionPortfolio = new ArrayList<Integer>();

        for (int id : goodOrMean ? GOOD_POTIONS : MEAN_POTIONS) {
            if (this.myCurrentPlayer.hasEnoughMoney(this.market.stockDatabase.get(id).getCurrentBuyPrice())) {
                potionPortfolio.add(id);
            }
        }

        if (potionPortfolio.isEmpty()) {
            this.aiDecisionAlgorithm().run();
            return;
        }

        int potionID = potionPortfolio.get(new Random().nextInt(potionPortfolio.size()));

        this.myCurrentPlayer.buyItem(this.market, potionID, 1);
        this.myCurrentPlayer.removeItem(potionID, 1);

        CurrentPlayer useOnCurrentPlayer;
        if (goodOrMean) {
            useOnCurrentPlayer = this.myCurrentPlayer;
        } else {
            if (allCurrentPlayers == null) {
                return;
            }
            allCurrentPlayers.sort(Comparator.comparing(player -> player.getMoney())); // does it give me the richest???
            useOnCurrentPlayer = allCurrentPlayers.get(0);
        }

        Item currentPotion = this.myCurrentPlayer.itemStore.getItem(potionID);
        SabotageComponent sc = currentPotion.getComponent(SabotageComponent.class);
        List<GameEntity> possibleEffectEntities = null;
        if (sc.type == SabotageComponent.SabotageType.SPEED) {
            possibleEffectEntities = List.of(useOnCurrentPlayer, this.theAnimal);
        }
        else if (sc.type == SabotageComponent.SabotageType.GROWTHRATE || sc.type == SabotageComponent.SabotageType.AI) {
            possibleEffectEntities = List.copyOf(this.otherFarms);
        }

        Runnable onComplete = () -> this.aiDecisionAlgorithm().run();

        System.out.println("Potion from " + this.myCurrentPlayer.playerID);
        System.out.println("used on " + useOnCurrentPlayer.playerID);

        PotionThrowEntity potionThrow = new PotionThrowEntity(this.myCurrentPlayer.getScene(), this.myFarm.spriteManager, this.myCurrentPlayer, currentPotion, possibleEffectEntities, onComplete, onComplete);
        potionThrow.onClick.performMouseClickAction(useOnCurrentPlayer.getPosition().getX(), useOnCurrentPlayer.getPosition().getY(), MouseButton.PRIMARY);
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

    private HashMap<Integer, Integer> boughtItems = new HashMap<>();
    private Pair<Integer[], Double> chooseItemToBuy(double withinPrice, boolean canBeTree) {
        MarketItem bestToBuy = null;
        boolean isTree = false;
        double currentPriceDiff = 0.0;
        MarketItem[] seedStockDatabase = this.market.stockDatabase.values().stream().filter((mItm) -> mItm.getID() > 28 && mItm.getID() < 50).toArray((arrSize) -> new MarketItem[arrSize]);;
        for (MarketItem marketItem : seedStockDatabase) {
            int randBoundary = new Random().nextInt(5);
            double currentBuyPrice = marketItem.getCurrentBuyPrice();
            if (currentBuyPrice > withinPrice || this.boughtItems.getOrDefault(marketItem.getID(), 0) > randBoundary) {
                continue;
            }

            Item item = this.myCurrentPlayer.itemStore.getItem(marketItem.getID());
            PlantComponent plant = item.getComponent(PlantComponent.class);
            if (!canBeTree && plant.isTree) {
                continue;
            }

            int avgDrop = (plant.minDrop + plant.maxDrop) / 2;
            if (Math.random() > 0.5) {
                double newPriceDiff = (marketItem.getCurrentSellPrice() * avgDrop) - currentBuyPrice;
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

    public String getType() {
        return "ai-player";
    }

}
