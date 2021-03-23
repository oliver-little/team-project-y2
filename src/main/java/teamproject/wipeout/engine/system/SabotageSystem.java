package teamproject.wipeout.engine.system;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.FunctionalSignatureCollector;
import teamproject.wipeout.game.item.components.SabotageComponent;
import teamproject.wipeout.game.player.Player;

public class SabotageSystem implements EventSystem {

    public Player player;

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
            //This needs to apply a speed multiplier to a player to which the potion is thrown at to slow them down by a constant multiplier for a set period of time (defined in items.JSON)
            //The code for the player is done, it already calculates acceleration (speed) based on the speed multiplier.
            //TODO - Need to apply the potion's multiplier to the selected player for the potion's duration.

            Timer timer = new Timer();
            TimerTask speedTask = new TimerTask() {
                public void run() {
                    cancel();
                    player.setSpeedMultiplier(1.0);
                    player.removeTimer(timer);
                }
            };

            player.setSpeedMultiplier(sabotageComponent.multiplier);

            timer.schedule(speedTask, (long) sabotageComponent.duration);
            player.addTimer(timer);
            entity.removeComponent(SabotageComponent.class);
        }
        else if (sabotageComponent.type == SabotageComponent.SabotageType.GROWTHRATE) {
            //This needs to apply a growth rate multiplier to a farm to which the potion is thrown at to either increase or decrease the growth rate of all the items on the farm for a set period of tiem (defined in items.JSON)
            //The multiplier for growth rate has been added into FarmEntity but does NOT do anything at the moment.
            //TODO - Need to apply the potion's multiplier to the selected farm AND need to implement code to change the growth rate of every item for the potion's duration.
        }
        else if (sabotageComponent.type == SabotageComponent.SabotageType.AI) {
            //This needs to apply an AI mutliplier to a farm to which the potion is thrown at to either increase or decrease the likelihood of the rat visiting the farm.
            //The code for the rat is done, it already can look at all the FarmEntity's AI multipliers and make decisions based on it.
            //TODO - Need to apply the potion's multiplier to the selected farm (FarmEntity) for the potion's duration.
        }
        else {
            throw new NoSuchElementException("Sabotage system failed: An item did not have a valid sabotage type.");
        }
    };
}
