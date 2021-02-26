package teamproject.wipeout.engine.component.physics;


import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.entity.GameEntity;

/**
 * Component that, when added to an entity, will resolve collisions between other entities with this component.
 * Has a boolean flag isMoveable that indicates whether an entity should be affected by collisions.
 *  Made up of an array of shapes that act as the collision boundaries.
 */
public class CollisionResolutionComponent implements GameComponent {
	
	/**
	 * Boolean flag that indicates whether this entity should be affected by collisions
	 */
	public boolean isMoveable = true;

	
	public CollisionResolutionComponent() {
		this.isMoveable = true;
	}
	
	//varargs constructor. See https://www.baeldung.com/java-varargs for info
	public CollisionResolutionComponent(boolean isMoveable) {
		this.isMoveable = isMoveable;
	}
	

	
    public String getType() {
        return "collisionResolution";
    }
    
   
    
    public static void resolveCollision(GameEntity g1, GameEntity g2, Pair<Shape, Shape> p) {
    	Point2D resolutionVector = GeometryUtil.getResolutionVector(p.first,p.second);
    	if (resolutionVector==null) {
    		return;
    	}
    	
    	Transform t1 = g1.getComponent(Transform.class);
    	//HitboxComponent h1 = g1.getComponent(HitboxComponent.class);
    	CollisionResolutionComponent c1 = g1.getComponent(CollisionResolutionComponent.class);
    	
    	Transform t2 = g2.getComponent(Transform.class);
    	//HitboxComponent h2 = g2.getComponent(HitboxComponent.class);
    	CollisionResolutionComponent c2 = g2.getComponent(CollisionResolutionComponent.class);
    	    	
    	
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

   
    
}

