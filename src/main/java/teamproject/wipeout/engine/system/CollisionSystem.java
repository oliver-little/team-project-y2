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
import teamproject.wipeout.engine.component.render.RectRenderComponent;
import teamproject.wipeout.engine.component.physics.CollisionComponent;
import teamproject.wipeout.engine.component.physics.FacingDirection;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;

public class CollisionSystem implements GameSystem {
    
    protected SignatureEntityCollector _entityCollector;

    public CollisionSystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(Transform.class, CollisionComponent.class));
    }

    public void accept(Double timeStep) {
        List<GameEntity> entities = this._entityCollector.getEntities();

        for(int i=0; i < entities.size(); i++) {
            for(int j=i+1; j < entities.size(); j++) {
            	if(i!=j) {
                	Pair<Shape, Shape> p = null;
					if((p  = CollisionComponent.collides(entities.get(i), entities.get(j)))!=null) {
                		//System.out.println("Collision");
						resolveCollision(entities.get(i),entities.get(j), p);
                                        
                	}
            	}
            }
        }
        
    }
    
    public void resolveCollision(GameEntity g1, GameEntity g2, Pair<Shape, Shape> p) {
    	Point2D resolutionVector = CollisionComponent.getResolutionVector(p.first,p.second);
    	if (resolutionVector==null) {
    		return;
    	}
    	
    	Transform t1 = g1.getComponent(Transform.class);
    	CollisionComponent c1 = g1.getComponent(CollisionComponent.class);
    	
    	Transform t2 = g2.getComponent(Transform.class);
    	CollisionComponent c2 = g2.getComponent(CollisionComponent.class);
    	
    	
    	if(c1.isMoveable) {
        	if(c2.isMoveable) {
        		t1.position = t1.position.add(resolutionVector.multiply(0.5));
        		t2.position = t2.position.add(resolutionVector.multiply(-0.5));
        	}
        	else {
        		t1.position = t1.position.add(resolutionVector);
        	}
    	}
    	else if(c2.isMoveable) {
    		t2.position = t2.position.add(resolutionVector.multiply(-1));
    	}
    }
    


}
