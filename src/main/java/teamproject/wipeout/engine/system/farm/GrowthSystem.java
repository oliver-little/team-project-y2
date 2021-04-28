package teamproject.wipeout.engine.system.farm;

import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

import java.util.List;
import java.util.Set;

/**
 * System that increments growth property for all plants in the {@link RowGrowthComponent}.
 *
 * @see GameSystem
 */
public class GrowthSystem implements GameSystem {

    private final SignatureEntityCollector plantCollector;

    /**
     * Creates an instance of a {@code GrowthSystem}.
     *
     * @param scene The {@link GameScene} this system is part of
     */
    public GrowthSystem(GameScene scene) {
        this.plantCollector = new SignatureEntityCollector(scene, Set.of(RowGrowthComponent.class));
    }

    /**
     * Cleans up the plant collector instance of type {@link SignatureEntityCollector}.
     */
    public void cleanup() {
        this.plantCollector.cleanup();
    }

    /**
     * Receives the change in time in the form of the given {@code timeStep} of type {@code Double}.
     *
     * @param timeStep {@code Double} value of delta time
     */
    public void accept(Double timeStep) {
        List<GameEntity> entities = this.plantCollector.getEntities();

        for (GameEntity entity : entities) {
            RowGrowthComponent growthComponent = entity.getComponent(RowGrowthComponent.class);
            growthComponent.updateGrowth(timeStep);
        }
    }

}
