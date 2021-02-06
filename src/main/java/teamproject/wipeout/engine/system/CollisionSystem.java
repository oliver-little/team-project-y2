package teamproject.wipeout.engine.system;

import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.RectRenderComponent;
import teamproject.wipeout.engine.component.physics.CollisionComponent;
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
            for(int j=0; j < entities.size(); j++) {
            	if(i!=j) {
                	if(collides(entities.get(i), entities.get(j))) {
                		
                		//TODO make sure components behave correctly now we have detected the collision
                		//This is a quick hacky thing just to see if it works
                		//Only works because both components have a gravity and velocity component 
                		//at the moment a collision causes both objects to freeze
                		MovementComponent m1 = entities.get(i).getComponent(MovementComponent.class);
                        m1.velocity= new Point2D(0,0);
                        m1.acceleration = new Point2D(0,0);
                        
                        MovementComponent m2 = entities.get(j).getComponent(MovementComponent.class);
                        m2.velocity= new Point2D(0,0);
                        m2.acceleration = new Point2D(0,0);
                        
                        
                	}
            	}
            }
        }
        
    }
    
    private boolean collides(GameEntity g1, GameEntity g2) {
    	Transform t1 = g1.getComponent(Transform.class);
    	CollisionComponent c1 = g1.getComponent(CollisionComponent.class);
    	Rectangle bb1[] = c1.boundingBoxes;
    	
    	Transform t2 = g2.getComponent(Transform.class);
    	CollisionComponent c2 = g2.getComponent(CollisionComponent.class);
    	Rectangle bb2[] = c2.boundingBoxes;
    	
    	for(int i=0;i<bb1.length;i++) {
    		//add absolute coord of top left corner
    		Rectangle r1 = new Rectangle(bb1[i].getX()+t1.position.getX(),
    									 bb1[i].getY()+t1.position.getY(),
    									 bb1[i].getWidth(),
    									 bb1[i].getHeight());

        	for(int j=0;j<bb2.length;j++) {
        		//add absolute coord of top left corner
        		Rectangle r2 = new Rectangle(bb2[j].getX()+t2.position.getX(),
        									 bb2[j].getY()+t2.position.getY(),
        									 bb2[j].getWidth(),
        									 bb2[j].getHeight());
        		
            	if(intersects(r1,r2)) {
            		return true;
            	}
        	}
    	}
    	
    	return false;

    }
    
    public static boolean intersects(Rectangle r1, Rectangle r2) {
    	Point2D d1 = new Point2D(r1.getWidth(), r1.getHeight());
    	Point2D d2 = new Point2D(r2.getWidth(), r2.getHeight());
    	Point2D minP1 = new Point2D(r1.getX(),r1.getY());
    	Point2D maxP1 = minP1.add(d1);
    	Point2D minP2 = new Point2D(r2.getX(),r2.getY());
    	Point2D maxP2 = minP2.add(d2);
    	
    	if(maxP1.getX()>=minP2.getX() && maxP1.getY()>=minP2.getY() && maxP1.getX()<=maxP2.getX() && maxP1.getY()<=maxP2.getY()) {
    		return true;
    	}
    	else if(minP1.getX()>=minP2.getX() && minP1.getY()>=minP2.getY() && minP1.getX()<=maxP2.getX() && minP1.getY()<=maxP2.getY()) {
    		return true;
    	}
    	else if(minP1.getX()>=minP2.getX() && maxP1.getY()>=minP2.getY() && minP1.getX()<=maxP2.getX() && maxP1.getY()<=maxP2.getY()) {
    		return true;
    	}
    	else if(maxP1.getX()>=minP2.getX() && minP1.getY()>=minP2.getY() && maxP1.getX()<=maxP2.getX() && minP1.getY()<=maxP2.getY()) {
    		return true;
    	}
    	
    	else if(maxP2.getX()>=minP1.getX() && maxP2.getY()>=minP1.getY() && maxP2.getX()<=maxP1.getX() && maxP2.getY()<=maxP1.getY()) {
    		return true;
    	}
    	else if(minP2.getX()>=minP1.getX() && minP2.getY()>=minP1.getY() && minP2.getX()<=maxP1.getX() && minP2.getY()<=maxP1.getY()) {
    		return true;
    	}
    	else if(minP2.getX()>=minP1.getX() && maxP2.getY()>=minP1.getY() && minP2.getX()<=maxP1.getX() && maxP2.getY()<=maxP1.getY()) {
    		return true;
    	}
    	else if(maxP2.getX()>=minP1.getX() && minP2.getY()>=minP1.getY() && maxP2.getX()<=maxP1.getX() && minP2.getY()<=maxP1.getY()) {
    		return true;
    	}
    	
    	
    	return false;
    }
   

}
