package teamproject.wipeout.engine.system;

import java.util.List;
import java.util.Set;

import teamproject.wipeout.engine.component.MoveRightComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;

public class MoveRightSystem implements GameSystem {
    
    protected SignatureEntityCollector _entityCollector;

    public MoveRightSystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(Transform.class, MoveRightComponent.class));
    }

    public void accept(Double timeStep) {
        List<GameEntity> entities = this._entityCollector.getEntities();

        for (GameEntity entity : entities) {
            Transform t = entity.getComponent(Transform.class);
            MoveRightComponent m = entity.getComponent(MoveRightComponent.class);
            t.position = t.position.add(m.speed * timeStep, 0);
        }
    }
}
