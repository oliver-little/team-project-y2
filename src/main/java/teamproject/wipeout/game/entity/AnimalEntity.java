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

    public Supplier<GameClient> clientSupplier;

    private boolean isPuppet;

    private NavigationMesh navMesh;

    private Transform transformComponent;
    private MovementComponent movementComponent;

    private AnimalState animalState;

    private ScheduledExecutorService executor;

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

    public AnimalState getCurrentState() {
        return this.animalState;
    }

    public void updateFromState(AnimalState newState) {
        this.isPuppet = true;
        this.transformComponent.setPosition(newState.getPosition());

        int[] traverseTo = newState.getTraveseTo();
        if (traverseTo != null) {
            int eatAt = newState.getEatAt();
            if (eatAt < 0) {
                this.aiTraverse(traverseTo[0], traverseTo[1], () -> {});
            } else {
                this.aiStealCrops(new int[]{eatAt, traverseTo[0], traverseTo[1]});
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
     * Finds a random farm to steal crops from.
     */
    private void aiStealCrops(int[] eatAt) {
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

        FarmEntity randFarm = farms.get(randomInteger(0, farms.size()));

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

        //Is the animal on a farm, if so, try to harvest some vegetables.

        double probability = Math.random();

        if (probability <= 0.2) {
            //Idle
            aiIdle();

        } else if (probability <= 0.6) {
            //Steal plants
            aiStealCrops(null);

        } else {
            //Pick random point
            aiPathFind();
        }
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
