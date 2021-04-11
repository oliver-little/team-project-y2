package teamproject.wipeout.engine.system;

import java.util.List;
import java.util.Set;

import teamproject.wipeout.engine.component.ScriptComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;

public class ScriptSystem implements GameSystem {
    
    protected SignatureEntityCollector entityCollector;

    public ScriptSystem(GameScene scene) {
        this.entityCollector = new SignatureEntityCollector(scene, Set.of(ScriptComponent.class));
    }

    public void cleanup() {
        this.entityCollector.cleanup();
    }

    public void accept(Double timeStep) {
        List<GameEntity> entities = this.entityCollector.getEntities();

        int i = 0;
        while (i < entities.size()) {
            GameEntity entity = entities.get(i);
            ScriptComponent sc = entity.getComponent(ScriptComponent.class);
            sc.onStep.accept(timeStep);
            if (sc.requestDeletion) {
                entity.removeComponent(ScriptComponent.class);
            }
            else {
                i++;
            }
        }
    }
}
