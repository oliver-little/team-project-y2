package teamproject.wipeout.engine.system;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.FunctionalSignatureCollector;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.components.SabotageComponent;
import teamproject.wipeout.game.player.Player;

public class SabotageSystem implements EventSystem {

    public Player player;

    public FarmEntity farm;

    private FunctionalSignatureCollector entityCollector;

    public SabotageSystem(GameScene scene) {
        entityCollector = new FunctionalSignatureCollector(scene, Set.of(SabotageComponent.class), addSabotage, null, null);
    }

    public void cleanup() {
        entityCollector.cleanup();
    }

    public Consumer<GameEntity> addSabotage = (entity) -> {
        SabotageComponent sabotageComponent = entity.getComponent(SabotageComponent.class);

        if (sabotageComponent.type == SabotageComponent.SabotageType.SPEED) {
            //Applys a speed multiplier to a player to which the potion is thrown at to slow them down by a constant multiplier for a set period of time (defined in items.JSON)

            Timer timer = new Timer();
            TimerTask speedTask = new TimerTask() {
                public void run() {
                    cancel();
                    player.setSpeedMultiplier(player.getSpeedMultiplier() / sabotageComponent.multiplier);
                    player.removeTimer(timer);
                }
            };

            player.setSpeedMultiplier(sabotageComponent.multiplier);

            timer.schedule(speedTask, (long) sabotageComponent.duration);
            player.addTimer(timer);
            entity.removeComponent(SabotageComponent.class);
        }
        else if (sabotageComponent.type == SabotageComponent.SabotageType.GROWTHRATE) {
            //Applys a growth rate multiplier to a farm to which the potion is thrown at to either increase or decrease the growth rate of all the items on the farm for a set period of tiem (defined in items.JSON)
     
            Timer timer = new Timer();
            TimerTask growthTask = new TimerTask() {
                public void run() {
                    cancel();
                    farm.setGrowthMultiplier(farm.getGrowthMultiplier() / sabotageComponent.multiplier);
                    farm.removeTimer(timer);
                }
            };

            farm.setGrowthMultiplier(sabotageComponent.multiplier);

            timer.schedule(growthTask, (long) sabotageComponent.duration);
            farm.addTimer(timer);
            entity.removeComponent(SabotageComponent.class);
        
        }
        else if (sabotageComponent.type == SabotageComponent.SabotageType.AI) {
            //Applys an AI mutliplier to a farm to which the potion is thrown at to either increase or decrease the likelihood of the rat visiting the farm.

            Timer timer = new Timer();
            TimerTask AITask = new TimerTask() {
                public void run() {
                    cancel();
                    farm.setAIMultiplier(farm.getAIMultiplier() / sabotageComponent.multiplier);
                    farm.removeTimer(timer);
                }
            };

            farm.setAIMultiplier(sabotageComponent.multiplier);

            timer.schedule(AITask, (long) sabotageComponent.duration);
            farm.addTimer(timer);
            entity.removeComponent(SabotageComponent.class);
        
        }
        else {
            throw new NoSuchElementException("Sabotage system failed: An item did not have a valid sabotage type.");
        }
    };
}
