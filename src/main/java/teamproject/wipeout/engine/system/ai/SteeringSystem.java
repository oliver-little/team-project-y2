package teamproject.wipeout.engine.system.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.ai.SteeringComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

/**
 * Allows an entity to traverse a calculated path through a navigation mesh.
 */
public class SteeringSystem implements GameSystem{

    public static final double MAGNITUDE = 10;

    protected SignatureEntityCollector entityCollector;

    private List<GameEntity> toRemove = new ArrayList<>();
    
    public SteeringSystem(GameScene e) {
        this.entityCollector = new SignatureEntityCollector(e, Set.of(Transform.class, MovementComponent.class, SteeringComponent.class));
    }

    public void cleanup(){
        this.entityCollector.cleanup();
    }

    /**
     * Perform the movement through the mesh once the steering component is present.
     */
    public void accept(Double timeStep) {
        List<GameEntity> entities = this.entityCollector.getEntities();
        toRemove.clear();

        for (GameEntity entity : entities) {
            MovementComponent m = entity.getComponent(MovementComponent.class);
            Transform t = entity.getComponent(Transform.class);
            SteeringComponent s = entity.getComponent(SteeringComponent.class);

            Point2D currentPosition = t.getWorldPosition();

            Point2D nextPosition = s.path.get(s.currentPoint);

            Point2D vector = nextPosition.subtract(currentPosition);

            if (vector.magnitude() < MAGNITUDE) {
                if (s.currentPoint == s.path.size() - 1) {
                    m.acceleration = Point2D.ZERO;
                    toRemove.add(entity);
                    continue;
                }
                s.currentPoint ++;
            }

            m.acceleration = vector.normalize().multiply(s.accelerationMultiplier);
        }

        for(GameEntity entity : toRemove) {
            SteeringComponent s = entity.removeComponent(SteeringComponent.class);
            if (s.onArrive != null) {
                s.onArrive.run(); 
            }
        }
    }
}
