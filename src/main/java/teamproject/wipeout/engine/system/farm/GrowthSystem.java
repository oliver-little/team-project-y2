package teamproject.wipeout.engine.system.farm;

import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

import java.util.List;
import java.util.Set;

/**
 * System that increments growth property for all crops in the {@link RowGrowthComponent}.
 */
public class GrowthSystem implements GameSystem {

    protected SignatureEntityCollector cropsCollector;

    public GrowthSystem(GameScene scene) {
        this.cropsCollector = new SignatureEntityCollector(scene, Set.of(RowGrowthComponent.class));
    }

    @Override
    public void cleanup() {
        this.cropsCollector.cleanup();
    }

    @Override
    public void accept(Double timeStep) {
        List<GameEntity> entities = this.cropsCollector.getEntities();

        for (GameEntity entity : entities) {
            RowGrowthComponent growthComponent = entity.getComponent(RowGrowthComponent.class);
            growthComponent.updateGrowth(timeStep);
        }
    }

}
