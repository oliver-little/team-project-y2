package teamproject.wipeout.engine.system;

import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
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
    	RectRenderComponent r1 = g1.getComponent(RectRenderComponent.class);
    	Point2D dimensionPoint1 = new Point2D(r1.width, r1.height); 
    	
    	Transform t2 = g2.getComponent(Transform.class);
    	RectRenderComponent r2 = g2.getComponent(RectRenderComponent.class);
    	Point2D dimensionPoint2 = new Point2D(r2.width, r2.height); 
    	
    	Point2D minP1 = t1.position;
    	Point2D maxP1 = minP1.add(dimensionPoint1);
    	Point2D minP2 = t2.position;
    	Point2D maxP2 = minP2.add(dimensionPoint2);
    	
    
    	return intersects(minP1, maxP1, minP2, maxP2);

    }
    
    private boolean intersects(Point2D minP1, Point2D maxP1, Point2D minP2, Point2D maxP2) {
    	
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
    	
    	
    	return false;
    }

}
