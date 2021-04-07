package teamproject.wipeout.game.player;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.ai.NavigationSquare;
import teamproject.wipeout.engine.component.ai.SteeringComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.system.ai.PathFindingSystem;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AIPlayerComponent implements GameComponent  {

    public static final int IDLE_TIME_SCALING_FACTOR = 5;

    public static final int IDLE_TIME_MINIMUM = 2;

    private Player myPlayer;
    private FarmEntity myFarm;

    private NavigationMesh navMesh;

    private Transform transformComponent;
    private Market market;

    private ScheduledExecutorService executor;

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
            aiDecisionAlgorithm().run();
            return;
        }

        int rand = new Random().nextInt(fullyGrownItems.size());

        int x = (int) fullyGrownItems.get(rand).getX();

        int y = (int) fullyGrownItems.get(rand).getY();

        Runnable onComplete = () ->  {
            Integer pickedID = this.myFarm.pickItemAt(x, y, false);
            if (pickedID != null) {
                this.myPlayer.sellItem(this.market, pickedID, 1);
            }
            Platform.runLater(this.aiDecisionAlgorithm());
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

        aiTraverse(randX, randY, aiDecisionAlgorithm());
    }

    /**
     * Runnable method which runs when the animal arrives at its destination, in this case, steals vegetables, picks a new destination to go to or idles.
     */
    private Runnable aiDecisionAlgorithm() {
        return () -> {
            double stealProbability = 0.3; //Probability of stealing crops.
            double buyProbability = 0.8; //Probability of stealing crops.

            //Make decision on what to do next.
            double probability = Math.random();

            if (probability <= 0.3) {

                if (Math.random() < 0.5) {
                    //Idle
                    aiIdle();
                } else {
                    //Pick random point
                    aiPathFind();
                }

            } else {
                if (Math.random() <= 0.6) {
                    //Steal plants
                    aiHarvestCrops();

                } else  {
                    aiTraverse((int) this.myFarm.getWorldPosition().getX(), (int) this.myFarm.getWorldPosition().getY(), () -> this.buyPlants());
                }
            }
        };
    }

    private void buyPlants() {
        double emptySpaces = this.myFarm.getEmptySpaces();
        if (emptySpaces > 0 && this.myPlayer.getMoney() > 0.0) {
            Integer buyID = this.chooseItemToBuy(this.myPlayer.getMoney(), emptySpaces >= 4);
            if (buyID == null) {
                this.aiDecisionAlgorithm().run();
                return;
            }
            this.myPlayer.buyItem(this.market, buyID, 1);
            this.myPlayer.removeItem(buyID, 1);
            this.boughtItems.merge(buyID, 1, (a, b) -> Integer.sum(a, b));
            Item item = this.myPlayer.itemStore.getItem(buyID);
            PlantComponent plant = item.getComponent(PlantComponent.class);

            int row = 0;
            int column = 0;
            while (!this.myFarm.canBePlacedAtSquare(row, column, plant.isTree ? 2 : 1, plant.isTree ? 2 : 1)) {
                if (column < this.myFarm.getPointSize().getX() - 1) {
                    column += 1;
                } else if (row < this.myFarm.getPointSize().getY() - 2) {
                    column = 0;
                    row += 1;
                } else {
                    break;
                }
            }

            this.myFarm.placeItemAtSquare(item, row, column);
            this.aiDecisionAlgorithm().run();
        }
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
    private Integer chooseItemToBuy(double withinPrice, boolean canBeTree) {
        MarketItem bestToBuy = null;
        double currentPriceDiff = 0.0;
        MarketItem[] seedStockDatabase = this.market.stockDatabase.values().stream().filter((mItm) -> mItm.getID() > 28 && mItm.getID() < 50).toArray((arrSize) -> new MarketItem[arrSize]);;
        for (MarketItem marketItem : seedStockDatabase) {
            double currentBuyPrice = marketItem.getCurrentBuyPrice();
            if (currentBuyPrice > withinPrice || this.boughtItems.getOrDefault(marketItem.getID(), 0) > 1) {
                continue;
            }

            Item item = this.myPlayer.itemStore.getItem(marketItem.getID());
            PlantComponent plant = item.getComponent(PlantComponent.class);
            if (!canBeTree && plant.isTree) {
                continue;
            }
            int avgDrop = (plant.minDrop + plant.maxDrop) / 2;
            double newPriceDiff = (marketItem.getCurrentSellPrice() * avgDrop) - currentBuyPrice;
            if (bestToBuy == null) {
                bestToBuy = marketItem;
                currentPriceDiff = newPriceDiff;
                continue;
            }

            if (newPriceDiff > currentPriceDiff) {
                bestToBuy = marketItem;
                continue;
            }
        }

        if (bestToBuy != null) {
            return bestToBuy.getID();
        }
        return null;
    }

    public String getType() {
        return "ai-player";
    }

}
