package teamproject.wipeout.engine.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
<<<<<<< HEAD
import javafx.scene.shape.Shape;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.physics.Pair;
import teamproject.wipeout.engine.component.physics.CollisionComponent;
=======
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.physics.Pair;
import teamproject.wipeout.engine.component.physics.Shape;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.FacingDirection;
import teamproject.wipeout.engine.component.physics.GeometryUtil;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
>>>>>>> collisions
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
    
<<<<<<< HEAD
    public void resolveCollision(GameEntity g1, GameEntity g2, Pair<Shape, Shape> p) {
    	Point2D resolutionVector = CollisionComponent.getResolutionVector(p.first,p.second);
    	if (resolutionVector==null) {
    		return;
    	}
    	
    	Transform t1 = g1.getComponent(Transform.class);
    	CollisionComponent c1 = g1.getComponent(CollisionComponent.class);
    	MovementComponent m1 = g1.getComponent(MovementComponent.class);
    	
    	Transform t2 = g2.getComponent(Transform.class);
    	CollisionComponent c2 = g2.getComponent(CollisionComponent.class);
    	MovementComponent m2 = g2.getComponent(MovementComponent.class);
    	    	
    	
    	if(c1.isMoveable) {
        	if(c2.isMoveable) {
        		t1.setPosition(t1.getPosition().add(resolutionVector.multiply(0.5)));
        		t2.setPosition(t2.getPosition().add(resolutionVector.multiply(-0.5)));
        	}
        	else {
        		t1.setPosition(t1.getPosition().add(resolutionVector));
        	}
    	}
    	else if(c2.isMoveable) {
    		t2.setPosition(t2.getPosition().add(resolutionVector.multiply(-1)));
    	}
    }
=======
>>>>>>> collisions
    


}
