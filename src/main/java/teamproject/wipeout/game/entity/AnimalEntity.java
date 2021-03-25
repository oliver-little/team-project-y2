package teamproject.wipeout.game.entity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.PlayerAnimatorComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.ai.NavigationSquare;
import teamproject.wipeout.engine.component.ai.SteeringComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.shape.Rectangle;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.system.ai.PathFindingSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.state.AnimalState;
import teamproject.wipeout.networking.state.StateUpdatable;

public class AnimalEntity extends GameEntity implements StateUpdatable<AnimalState> {

    public static final int IDLE_TIME_SCALING_FACTOR = 5;

    public static final int IDLE_TIME_MINIMUM = 2;

    public static final double DOUBLE_COMPARE = 0.0000001;

    private boolean isPuppet;

    private NavigationMesh navMesh;

    private Transform transformComponent;
    private MovementComponent movementComponent;

    private ScheduledExecutorService executor;

    private Supplier<GameClient> clientSupplier;
    private AnimalState animalState;
    private List<FarmEntity> farms;

    /**
     * Creates a new animal entity, taking a game scene, starting position, a navigation mesh and a sprite manager.
     */
    public AnimalEntity(GameScene scene, Point2D position, NavigationMesh navMesh, SpriteManager spriteManager, List<FarmEntity> farms) {
        super(scene);

        this.isPuppet = false;

        this.navMesh = navMesh;
        this.farms = farms;

        this.executor = Executors.newSingleThreadScheduledExecutor();

        this.transformComponent = new Transform(position.getX(), position.getY(), 1);
        this.movementComponent = new MovementComponent();
        this.movementComponent.speedMultiplierChanged = (newMultiplier) -> {
            this.animalState.setSpeedMultiplier(newMultiplier);
            this.sendStateUpdate();
        };

        this.addComponent(this.transformComponent);
        this.addComponent(this.movementComponent);
        this.addComponent(new RenderComponent(new Point2D(-16, -16)));
        this.addComponent(new HitboxComponent(new Rectangle(-16, -16, 32, 32)));

        this.animalState = new AnimalState(position, null, -1);

        try {
            this.addComponent(new PlayerAnimatorComponent(
                spriteManager.getSpriteSet("mouse", "up"),
                spriteManager.getSpriteSet("mouse", "right"),
                spriteManager.getSpriteSet("mouse", "down"),
                spriteManager.getSpriteSet("mouse", "left"),
                spriteManager.getSpriteSet("mouse", "idle")));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.aiDecisionAlgorithm.run();
    }

    public void setClientSupplier(Supplier<GameClient> clientSupplier) {
        this.clientSupplier = clientSupplier;
    }

    public AnimalState getCurrentState() {
        return this.animalState;
    }

    public void updateFromState(AnimalState newState) {
        this.isPuppet = true;
        this.removeComponent(SteeringComponent.class);
        this.transformComponent.setPosition(newState.getPosition());
        this.movementComponent.setSpeedMultiplier(newState.getSpeedMultiplier());

        int[] traverseTo = newState.getTraveseTo();
        if (traverseTo != null) {
            int eatAt = newState.getEatAt();
            if (eatAt < 0) {
                this.aiTraverse(traverseTo[0], traverseTo[1], () -> {});
            } else {
                this.aiStealCrops(new int[]{eatAt, traverseTo[0], traverseTo[1]}, null);
            }
        }
        this.animalState.updateStateFrom(newState);
    }

    /**
     * Helper function to calculate the path from the AI's current location to its destination, then initiates the traverse method.
     */
    private void aiTraverse(int x, int y, Runnable callback) {

        Point2D wp = transformComponent.getWorldPosition();

        List<Point2D> path = PathFindingSystem.findPath(new Point2D((int) wp.getX(), (int) wp.getY()), new Point2D(x, y), navMesh, 16);

        this.addComponent(new SteeringComponent(path, callback, 250));
    }

    /**
     * Idles for a random, short period of time.
     */
    private void aiIdle() {
        long idleTime = (long) (Math.random() * IDLE_TIME_SCALING_FACTOR) + IDLE_TIME_MINIMUM;

        this.animalState.setPosition(this.transformComponent.getWorldPosition());
        this.animalState.setTraveseTo(null);
        this.animalState.setEatAt(-1);
        this.sendStateUpdate();

        executor.schedule(() -> Platform.runLater(aiDecisionAlgorithm), idleTime, TimeUnit.SECONDS);
    }

    /**
     * Steals crops from a given farm.
     * @param eatAt Location used for networking.
     * @param randFarm The farm to steal crops from.
     */
    private void aiStealCrops(int[] eatAt, FarmEntity randFarm) {
        if (eatAt != null) {
            FarmEntity theFarm = (FarmEntity) farms.stream().filter((farm) -> farm.farmID.equals(eatAt[0])).toArray()[0];
            int theX = eatAt[1];
            int theY = eatAt[2];
            Runnable onComplete = () ->  {
                theFarm.pickItemAt(theX, theY, false);
            };

            aiTraverse(theX, theY, onComplete);
            return;
        }
        List<Point2D> fullyGrownItems = new ArrayList<>();

        fullyGrownItems = randFarm.getGrownItemPositions();

        //If there are no fully grown crops in the selected farm, just find a random location instead.
        if (fullyGrownItems.size() == 0) {
            aiPathFind();
            return;
        }

        int rand = new Random().nextInt(fullyGrownItems.size());

        int x = (int) fullyGrownItems.get(rand).getX();

        int y = (int) fullyGrownItems.get(rand).getY();

        Runnable onComplete = () ->  {
            randFarm.pickItemAt(x, y, false);
            Platform.runLater(aiDecisionAlgorithm);
        };

        this.animalState.setPosition(this.transformComponent.getWorldPosition());
        this.animalState.setTraveseTo(new int[]{x, y});
        this.animalState.setEatAt(randFarm.farmID);
        this.sendStateUpdate();

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

        this.animalState.setPosition(this.transformComponent.getWorldPosition());
        this.animalState.setTraveseTo(new int[]{randX, randY});
        this.animalState.setEatAt(-1);
        this.sendStateUpdate();

        aiTraverse(randX, randY, aiDecisionAlgorithm);
    }

    /**
     * Runnable method which runs when the animal arrives at its destination, in this case, steals vegetables, picks a new destination to go to or idles.
     */
    private Runnable aiDecisionAlgorithm = () -> {
        if (this.isPuppet) {
            return;
        }

        //Establish whether rat repellent or cheese has been used on a farm.
        double totalWeight = 0;

        double stealProbability = 0.6; //Probability of stealing crops.

        FarmEntity selectedFarm;

        for (FarmEntity farm : farms) {
            totalWeight += farm.getAIMultiplier();
        }

        //If at least one farm has cheese on it, calculate a farm to visit.
        if (!(Math.abs(totalWeight - farms.size()) < DOUBLE_COMPARE)) {
            double farmProbability = Math.random() * totalWeight;
            double weightTracker = 0;
            int farmIndex = -1;

            while (weightTracker < farmProbability && farmIndex < farms.size()) {
                farmIndex++;
                weightTracker += farms.get(farmIndex).getAIMultiplier();
            }

            stealProbability = 0.9; //80% chance of stealing crops.
            selectedFarm = farms.get(farmIndex);

        } else {
            selectedFarm = farms.get(randomInteger(0, farms.size()));
        }
        
        //Make decision on what to do next.
        double probability = Math.random();

        /*if (probability <= 0.1) {
            //Idle
            aiIdle();

        } else if (probability <= stealProbability) {*/
            //Steal plants
            aiStealCrops(null, selectedFarm);

        /*} else {
            //Pick random point
            aiPathFind();
        }*/
    };

    /**
     * Calculate a random integer.
     * @param min Min Value.
     * @param max Max value.
     * @return The random integer.
     */
    private int randomInteger(int min, int max) {
        return new Random().nextInt(Math.abs(max - min)) + min;
    }

    private void sendStateUpdate() {
        if (clientSupplier == null) {
            return;
        }
        GameClient client = this.clientSupplier.get();
        if (client != null) {
            try {
                client.send(new GameUpdate(GameUpdateType.ANIMAL_STATE, client.id, this.animalState));

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

}
