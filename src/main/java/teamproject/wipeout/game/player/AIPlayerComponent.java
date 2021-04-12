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
import teamproject.wipeout.engine.component.physics.MovementComponent;
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

    public ArrayList<Player> allPlayers;
    public AnimalEntity theAnimal;
    public List<FarmEntity> otherFarms;
    private Player myPlayer;
    private FarmEntity myFarm;

    private NavigationMesh navMesh;

    private Transform transformComponent;
    private Market market;

    private ScheduledExecutorService executor;

    public Supplier<ClockSystem> clock;

    /**
     * Creates a new animal entity, taking a game scene, starting position, a navigation mesh and a sprite manager.
     */
    public AIPlayerComponent(Player player, Market market, NavigationMesh navMesh, FarmEntity myFarm) {
        this.myPlayer = player;
        this.myFarm = myFarm;
        this.market = market;

        this.navMesh = navMesh;

        this.executor = Executors.newSingleThreadScheduledExecutor();

        this.transformComponent = player.getComponent(Transform.class);

        this.aiDecisionAlgorithm().run();
    }

    /**
     * Helper function to calculate the path from the AI's current location to its destination, then initiates the traverse method.
     */
    private void aiTraverse(int x, int y, Runnable callback) {
        Point2D wp = transformComponent.getWorldPosition();

        List<Point2D> path = PathFindingSystem.findPath(new Point2D((int) wp.getX(), (int) wp.getY()), new Point2D(x, y), navMesh, 100);

        this.myPlayer.addComponent(new SteeringComponent(path, callback, 250));
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
            if (this.myPlayer.getMoney() > 100.0 && (randDestroy < 0.05 || (randDestroy < 0.1 && this.myFarm.itemAt(x, y).get().getComponent(PlantComponent.class).isTree))) {
                System.out.println("Destroy");
                this.myFarm.pickItemAt(x, y, false, true);
            } else {
                System.out.println("Harvest");
                Integer[] picked = this.myFarm.pickItemAt(x, y, false, false);
                if (picked != null) {
                    Integer pickedID = picked[0];
                    int pickedQuantity = picked[1];
                    this.myPlayer.acquireItem(pickedID, pickedQuantity);
                    this.myPlayer.sellItem(this.market, pickedID, pickedQuantity);
                }
            }
            System.out.println(this.myPlayer.getMoney());
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
                System.out.println("STOP");
                System.out.println("AI: " + this.myPlayer.getMoney());
                System.out.println("AI: " + this.myPlayer.getInventory().toString());
                return;
            }

            //Make decision on what to do next.
            double probability = Math.random();

            if (probability < 0.75) {
                if (this.clock != null && this.clock.get().clockUI.getTime() < 30.0) {
                    System.out.println("Harvest STOP");
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
                        if (this.myPlayer.hasEnoughMoney(expansionPrice + 30)) {
                            System.out.println("Expand");
                            this.myPlayer.setMoney(this.myPlayer.getMoney() - expansionPrice);
                            this.myFarm.expandFarmBy(1);
                            this.aiDecisionAlgorithm().run();
                        } else {
                            System.out.println("Buy 2");
                            aiTraverse((int) this.myFarm.getWorldPosition().getX(), (int) this.myFarm.getWorldPosition().getY(), () -> {
                                this.buyPlants();
                                System.out.println(this.myPlayer.getMoney());
                                this.aiDecisionAlgorithm().run();
                            });
                        }
                    } else {
                        System.out.println("Buy 1");
                        aiTraverse((int) this.myFarm.getWorldPosition().getX(), (int) this.myFarm.getWorldPosition().getY(), () -> {
                            this.buyPlants();
                            System.out.println(this.myPlayer.getMoney());
                            this.aiDecisionAlgorithm().run();
                        });
                    }
                }

            } else {
                double randomiser = Math.random();
                if (randomiser < 0.2 && this.myPlayer.hasEnoughMoney(130)) {
                    System.out.println("Potion");
                    usePotions();

                } else if (randomiser < 0.5) {
                    //Pick random point
                    System.out.println("Wander");
                    aiPathFind();
                } else {
                    //Idle
                    System.out.println("Idle");
                    aiIdle();
                }
            }
        };
    }

    private void buyPlants() {
        double emptySpaces = this.myFarm.getEmptySpaces();
        double currentBalance = this.myPlayer.getMoney();

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
                this.myPlayer.buyItem(this.market, boughtID, 1);
                this.myPlayer.removeItem(boughtID, 1);

                Item item = this.myPlayer.itemStore.getItem(boughtID);
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
            if (this.myPlayer.hasEnoughMoney(this.market.stockDatabase.get(id).getCurrentBuyPrice())) {
                potionPortfolio.add(id);
            }
        }

        if (potionPortfolio.isEmpty()) {
            this.aiDecisionAlgorithm().run();
            return;
        }

        int potionID = potionPortfolio.get(new Random().nextInt(potionPortfolio.size()));

        this.myPlayer.buyItem(this.market, potionID, 1);
        this.myPlayer.removeItem(potionID, 1);

        Player useOnPlayer;
        if (goodOrMean) {
            useOnPlayer = this.myPlayer;
        } else {
            if (allPlayers == null) {
                return;
            }
            allPlayers.sort(Comparator.comparing(player -> player.getMoney())); // does it give me the richest???
            useOnPlayer = allPlayers.get(0);
        }

        Item currentPotion = this.myPlayer.itemStore.getItem(potionID);
        SabotageComponent sc = currentPotion.getComponent(SabotageComponent.class);
        List<GameEntity> possibleEffectEntities = null;
        if (sc.type == SabotageComponent.SabotageType.SPEED) {
            possibleEffectEntities = List.of(useOnPlayer, this.theAnimal);
        }
        else if (sc.type == SabotageComponent.SabotageType.GROWTHRATE || sc.type == SabotageComponent.SabotageType.AI) {
            possibleEffectEntities = List.copyOf(this.otherFarms);
        }

        Runnable onComplete = () -> this.aiDecisionAlgorithm().run();

        PotionThrowEntity potionThrow = new PotionThrowEntity(this.myPlayer.getScene(), this.myFarm.spriteManager, this.myPlayer, currentPotion, possibleEffectEntities, onComplete, onComplete);
        potionThrow.onClick.performMouseClickAction(useOnPlayer.getPosition().getX(), useOnPlayer.getPosition().getY(), MouseButton.PRIMARY);
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

            Item item = this.myPlayer.itemStore.getItem(marketItem.getID());
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
