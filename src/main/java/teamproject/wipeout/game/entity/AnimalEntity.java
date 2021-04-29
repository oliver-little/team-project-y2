package teamproject.wipeout.game.entity;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.PlayerAnimatorComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.ai.NavigationSquare;
import teamproject.wipeout.engine.component.ai.SteeringComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters.ParticleSimulationSpace;
import teamproject.wipeout.engine.component.render.particle.property.EaseCurve;
import teamproject.wipeout.engine.component.render.particle.property.OvalParticle;
import teamproject.wipeout.engine.component.shape.Rectangle;
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
import teamproject.wipeout.util.SupplierGenerator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AnimalEntity extends GameEntity implements StateUpdatable<AnimalState> {

    public static final OvalParticle FAST_PARTICLE = new OvalParticle(new Color(1, 0.824, 0.004, 1));
    public static final OvalParticle SLOW_PARTICLE = new OvalParticle(new Color(0.001, 1, 0.733, 1));

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

    private ParticleEntity sabotageEffect;

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

            if (newMultiplier != 1) {
                if (newMultiplier < 1) {
                    sabotageEffect.getParameters().setEmissionType(SLOW_PARTICLE);
                }
                else {
                    sabotageEffect.getParameters().setEmissionType(FAST_PARTICLE);
                }

                if (!sabotageEffect.isPlaying()) {
                    sabotageEffect.play();
                }
            }
            else if (sabotageEffect.isPlaying()) {
                sabotageEffect.stop();
            }
        };

        this.addComponent(this.transformComponent);
        this.addComponent(this.movementComponent);
        this.addComponent(new RenderComponent());
        this.addComponent(new HitboxComponent(new Rectangle(0, 0, 32, 32)));

        ParticleParameters parameters = new ParticleParameters(100, true,
                FAST_PARTICLE,
                ParticleSimulationSpace.WORLD,
                SupplierGenerator.rangeSupplier(1.0, 1.75),
                SupplierGenerator.rangeSupplier(1.0, 2.0),
                null,
                SupplierGenerator.staticSupplier(0.0),
                SupplierGenerator.rangeSupplier(new Point2D(-20, -5), new Point2D(20, 0)));

        parameters.setEmissionRate(20);
        parameters.setEmissionPositionGenerator(SupplierGenerator.rangeSupplier(new Point2D(11, 22), new Point2D(20, 32)));
        parameters.addUpdateFunction((particle, percentage, timeStep) -> {
            particle.opacity = EaseCurve.FADE_IN_OUT.apply(percentage);
        });

        this.sabotageEffect = new ParticleEntity(scene, 0, parameters);
        this.sabotageEffect.setParent(this);

        this.animalState = new AnimalState(position, null);

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
        if (!this.animalState.getSpeedMultiplier().equals(newState.getSpeedMultiplier())) {
            Double newSpeedMultiplier = newState.getSpeedMultiplier();
            this.movementComponent.setSpeedMultiplier(newSpeedMultiplier);
            this.animalState.setSpeedMultiplier(newSpeedMultiplier);
            return;
        }
        this.isPuppet = true;

        this.removeComponent(SteeringComponent.class);
        this.transformComponent.setPosition(newState.getPosition());
        this.movementComponent.setSpeedMultiplier(newState.getSpeedMultiplier());

        List<Point2D> path = newState.getPath();
        if (!path.isEmpty()) {
            this.addComponent(new SteeringComponent(path, null, 250));
        }

        this.animalState.updateStateFrom(newState);

        if (newState.getSpeedMultiplier() != 1) {
            if (newState.getSpeedMultiplier() < 1) {
                sabotageEffect.getParameters().setEmissionType(SLOW_PARTICLE);
            }
            else {
                sabotageEffect.getParameters().setEmissionType(FAST_PARTICLE);
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
     * Helper function to calculate the path from the AI's current location to its destination, then initiates the traverse method.
     */
    private void aiTraverse(int x, int y, Runnable callback) {

        Point2D wp = transformComponent.getWorldPosition();

        List<Point2D> path = PathFindingSystem.findPath(new Point2D((int) wp.getX(), (int) wp.getY()), new Point2D(x, y), navMesh, 16);

        this.animalState.setPosition(this.transformComponent.getWorldPosition());
        this.animalState.setPath(path);
        this.sendStateUpdate();

        this.addComponent(new SteeringComponent(path, callback, 250));
    }

    /**
     * Idles for a random, short period of time.
     */
    private void aiIdle() {
        long idleTime = (long) (Math.random() * IDLE_TIME_SCALING_FACTOR) + IDLE_TIME_MINIMUM;

        this.animalState.setPosition(this.transformComponent.getWorldPosition());
        this.animalState.setPath(null);
        this.sendStateUpdate();

        executor.schedule(() -> Platform.runLater(aiDecisionAlgorithm), idleTime, TimeUnit.SECONDS);
    }

    /**
     * Steals crops from a given farm.
     *
     * @param randFarm The farm to steal crops from.
     */
    private void aiStealCrops(FarmEntity randFarm) {
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
            randFarm.pickItemAt(x, y, false, false);
            Platform.runLater(aiDecisionAlgorithm);
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

        if (probability <= 0.1) {
            //Idle
            aiIdle();

        } else if (probability <= stealProbability) {
            //Steal plants
            aiStealCrops(selectedFarm);

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
            client.send(new GameUpdate(GameUpdateType.ANIMAL_STATE, client.getClientID(), this.animalState));
        }
    }

}
