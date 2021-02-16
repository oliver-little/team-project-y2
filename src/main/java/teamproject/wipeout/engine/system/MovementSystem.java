package teamproject.wipeout.engine.system;

import java.util.List;
import java.util.Set;

import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;

public class MovementSystem implements GameSystem {
    
    protected SignatureEntityCollector _entityCollector;

    public MovementSystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(Transform.class, MovementComponent.class));
    }

    public void cleanup() {
        this._entityCollector.cleanup();
    }

    public void cleanup() {
        this._entityCollector.cleanup();
    }

    public void accept(Double timeStep) {
        List<GameEntity> entities = this._entityCollector.getEntities();

        for (GameEntity entity : entities) {
            Transform t = entity.getComponent(Transform.class);
            MovementComponent m = entity.getComponent(MovementComponent.class);
            m.updateVelocity(timeStep);
            t.position = t.position.add(m.velocity.multiply(timeStep));
            
            m.updateFacingDirection();
        }
    }
}
