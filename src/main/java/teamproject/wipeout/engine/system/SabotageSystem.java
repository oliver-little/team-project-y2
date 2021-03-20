package teamproject.wipeout.engine.system;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.EntityCollector;
import teamproject.wipeout.engine.entity.collector.FunctionalSignatureCollector;
import teamproject.wipeout.game.item.components.SabotageComponent;

public class SabotageSystem implements EventSystem{

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
            //TODO - This needs to apply a speed multiplier to a player to which the potion is thrown at to slow them down by a constant multiplier for a set period of time (defined in items.JSON)
        }
        else if (sabotageComponent.type == SabotageComponent.SabotageType.GROWTHRATE) {
            //TODO - This needs to apply a growth rate multiplier to a farm to which the potion is thrown at to either increase or decrease the growth rate of all the items on the farm for a set period of tiem (defined in items.JSON)
        }
        else if (sabotageComponent.type == SabotageComponent.SabotageType.REPUTATION) {
            //TODO - This needs to apply a reputation multiplier to a player to which the potion is thrown at to either increase/decrease the market prices for that player by a constant multiplier for a set period of time (defined in items.JSON)
        }
        else if (sabotageComponent.type == SabotageComponent.SabotageType.AI) {
            //TODO - Jamie & Ollie.
        }
        else {
            throw new NoSuchElementException("Sabotage system failed: An item did not have a valid sabotage type.");
        }
    };
}
