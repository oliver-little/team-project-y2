package teamproject.wipeout.engine.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.physics.Pair;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.FacingDirection;
import teamproject.wipeout.engine.component.physics.GeometryUtil;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;

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
                	Pair<Shape, Shape> p = null;
					if((p  = HitboxComponent.collides(entities.get(i), entities.get(j)))!=null) {
                		//System.out.println("Collision");
						CollisionResolutionComponent.resolveCollision(entities.get(i),entities.get(j), p);
                                        
                	}
            	}
            }
        }
        
    }
    
    


}
