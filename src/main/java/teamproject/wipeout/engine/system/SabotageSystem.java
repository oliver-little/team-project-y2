package teamproject.wipeout.engine.system;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.FunctionalSignatureCollector;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.components.SabotageComponent;

/**
 * SabotageSystem implements sabotage potion effects for players, farms and the AI animal.
 */
public class SabotageSystem implements EventSystem {

    public static final double SPEED_MULTIPLIER_UPPER_LIMIT = 9;
    public static final double SPEED_MULTIPLIER_LOWER_LIMIT = 0.1;

    private FunctionalSignatureCollector entityCollector;

    /**
     * Creates a new instance of SabotageSystem
     * @param scene The GameScene this system is part of
     */
    public SabotageSystem(GameScene scene) {
        entityCollector = new FunctionalSignatureCollector(scene, Set.of(SabotageComponent.class), addSabotage, null, null);
    }

    public void cleanup() {
        entityCollector.cleanup();
    }

    /**
     * Consumer called when a new sabotage entity is added.
     */
    public Consumer<GameEntity> addSabotage = (entity) -> {
        SabotageComponent sabotageComponent = entity.getComponent(SabotageComponent.class);

        if (sabotageComponent.type == SabotageComponent.SabotageType.SPEED && entity.hasComponent(MovementComponent.class)) {
            //Applies a speed multiplier to a player to which the potion is thrown at to slow them down by a constant multiplier for a set period of time (defined in items.JSON)
            
            if ((entity.getComponent(MovementComponent.class).getSpeedMultiplier() * sabotageComponent.multiplier <= SPEED_MULTIPLIER_UPPER_LIMIT) && (entity.getComponent(MovementComponent.class).getSpeedMultiplier() * sabotageComponent.multiplier >= SPEED_MULTIPLIER_LOWER_LIMIT)) {
                Timer timer = new Timer();
                TimerTask speedTask = new TimerTask() {
                    public void run() {
                        cancel();
                        entity.getComponent(MovementComponent.class).divideSpeedMultiplierBy(sabotageComponent.multiplier);
                    }
                };
                entity.getComponent(MovementComponent.class).multiplySpeedMultiplierBy(sabotageComponent.multiplier);

                timer.schedule(speedTask, (long) sabotageComponent.duration * 1000);
            }
            System.out.println(entity.getComponent(MovementComponent.class).getSpeedMultiplier());
            entity.removeComponent(SabotageComponent.class);
        }
        else if (sabotageComponent.type == SabotageComponent.SabotageType.GROWTHRATE && entity instanceof FarmEntity) {
            //Applys a growth rate multiplier to a farm to which the potion is thrown at to either increase or decrease the growth rate of all the items on the farm for a set period of tiem (defined in items.JSON)
     
            FarmEntity farm = (FarmEntity) entity;

            Timer timer = new Timer();
            TimerTask growthTask = new TimerTask() {
                public void run() {
                    cancel();
                    farm.setGrowthMultiplier(farm.getGrowthMultiplier() / sabotageComponent.multiplier);
                }
            };

            farm.setGrowthMultiplier(farm.getGrowthMultiplier() * sabotageComponent.multiplier);

            timer.schedule(growthTask, (long) sabotageComponent.duration * 1000);
            entity.removeComponent(SabotageComponent.class);
        
        }
        else if (sabotageComponent.type == SabotageComponent.SabotageType.AI && entity instanceof FarmEntity) {
            //Applys an AI mutliplier to a farm to which the potion is thrown at to either increase or decrease the likelihood of the rat visiting the farm.

            FarmEntity farm = (FarmEntity) entity;

            Timer timer = new Timer();
            TimerTask AITask = new TimerTask() {
                public void run() {
                    cancel();
                    farm.setAIMultiplier(farm.getAIMultiplier() / sabotageComponent.multiplier);
                }
            };

            farm.setAIMultiplier(farm.getAIMultiplier() * sabotageComponent.multiplier);

            timer.schedule(AITask, (long) sabotageComponent.duration * 1000);
            entity.removeComponent(SabotageComponent.class);
        
        }
        else {
            throw new NoSuchElementException("Sabotage system failed: An item did not have a valid sabotage type.");
        }
    };
}
