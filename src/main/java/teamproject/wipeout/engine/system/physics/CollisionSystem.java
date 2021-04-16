package teamproject.wipeout.engine.system.physics;

import javafx.util.Pair;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.shape.Shape;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * System that checks and resolves collisions between entities with the collision component
 *
 */
public class CollisionSystem implements GameSystem {
    
    protected SignatureEntityCollector _entityCollector;

    public CollisionSystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(Transform.class, HitboxComponent.class, CollisionResolutionComponent.class));
    }

	@Override
	public void cleanup() {
		this._entityCollector.cleanup();
	}

	@Override
    public void accept(Double timeStep) {
        List<GameEntity> entities = this._entityCollector.getEntities();

        for(int i=0; i < entities.size(); i++) {
            for(int j=i+1; j < entities.size(); j++) {
            	if(i!=j) {
                	ArrayList<Pair<Shape, Shape>> p = null;
					if((p = HitboxComponent.collides(entities.get(i), entities.get(j))) != null) {
						CollisionResolutionComponent.resolveCollision(entities.get(i),entities.get(j), p);
                	}
            	}
            }
        }
        
    }

}
