package teamproject.wipeout.engine.system.physics;

import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

/**
 * System that updates an entities velocity and position based on their velocity and acceleration
 */
public class MovementSystem implements GameSystem {
    
    protected SignatureEntityCollector _entityCollector;

    public MovementSystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(Transform.class, MovementComponent.class));
    }

    public void cleanup() {
        this._entityCollector.cleanup();
    }

    public void accept(Double timeStep) {
        List<GameEntity> entities = this._entityCollector.getEntities();

        for (GameEntity entity : entities) {
            Transform t = entity.getComponent(Transform.class);
            MovementComponent m = entity.getComponent(MovementComponent.class);

            boolean wasMoving = !m.velocity.equals(Point2D.ZERO);

            m.updateVelocity(timeStep);
            t.setPosition(t.getPosition().add(m.velocity.multiply(timeStep)));

            m.updateFacingDirection();

            boolean isStationary = m.velocity.equals(Point2D.ZERO);
            if (m.stopCallback != null && wasMoving && isStationary) {
                m.stopCallback.accept(t.getWorldPosition());
            }
        }
    }
}
