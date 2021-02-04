package teamproject.wipeout.engine.system;

import java.util.List;
import java.util.Set;

import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.PhysicsComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;

public class PhysicsSystem implements GameSystem {
    
    protected SignatureEntityCollector _entityCollector;

    public PhysicsSystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(Transform.class, PhysicsComponent.class));
    }

    public void accept(Double timeStep) {
        List<GameEntity> entities = this._entityCollector.getEntities();

        for (GameEntity entity : entities) {
            Transform t = entity.getComponent(Transform.class);
            PhysicsComponent m = entity.getComponent(PhysicsComponent.class);
            m.velocity = m.velocity.add(m.acceleration.multiply(timeStep));
            t.position = t.position.add(m.velocity.multiply(timeStep));
        }
    }
}
