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
    
    /**
     * Checks whether two game entities collide (whether any of one's bounding boxes overlaps with the other's)
     * @param g1 first game entity
     * @param g2 second game entity
     * @return true if they collide, false otherwise
     */
    private boolean collides(GameEntity g1, GameEntity g2) {
    	Transform t1 = g1.getComponent(Transform.class);
    	CollisionComponent c1 = g1.getComponent(CollisionComponent.class);
    	Shape bb1[] = c1.boundingBoxes;
    	
    	Transform t2 = g2.getComponent(Transform.class);
    	CollisionComponent c2 = g2.getComponent(CollisionComponent.class);
    	Shape bb2[] = c2.boundingBoxes;
    	
    	for(int i=0;i<bb1.length;i++) {
    		Rectangle r1 = addAbsolutePosition(t1.position, castToShape(bb1[i]));

        	for(int j=0;j<bb2.length;j++) {
        		Rectangle r2 = addAbsolutePosition(t2.position, castToShape(bb2[j]));
            	if(intersects(r1,r2)) {
            		return true;
            	}
        	}
    	}
    	
    	return false;

    }
    
    /**
     * Converts a generic shape to specific
     * @param <T>
     * @param s shape to cast
     * @return casted shape
     */
    // https://stackoverflow.com/a/450874
    private <T extends Shape> T castToShape(Shape s) {
    	if(s.getClass()==Rectangle.class) {
    		return (T) s;
    	}
    	else if(s.getClass()==Circle.class) {
    		return (T) s;
    	}
    	return null;
    }
    
    /**
     * Takes an array of different shapes and returns just the rectangles
     * @param shapes array of different shapes
     * @return just the rectangles from the array
     */
    private Rectangle[] getRectangles(Shape[] shapes) {
    	ArrayList<Rectangle> r = new ArrayList<Rectangle>();
    	for(int i=0; i<shapes.length;i++) {
    		if(shapes[i].getClass()==Rectangle.class) {
    			r.add((Rectangle) shapes[i]);
    		}
    	}
    	return (Rectangle[]) r.toArray();
    }
    
    /**
     * Adds the coordinate of the location of the rectangle to the relative position of the bounding box
     * @param p location of the rectangle
     * @param r the bounding box
     * @return bounding box at correct location
     */
    private Rectangle addAbsolutePosition(Point2D p, Rectangle r) {
    	return new Rectangle(r.getX()+p.getX(),
    						 r.getY()+p.getY(),
    						 r.getWidth(),
    						 r.getHeight());
    }
    
    /**
     * Checks whether two circles intersect
     * @param c1 first circle
     * @param c2 second circle
     * @return true if the circles intersect, false otherwise
     */
    public static boolean intersects(Circle c1, Circle c2) {
    	//intersect if the distance between their two centres is less than the sum of their radiuses
    	double radiusSum = c1.getRadius()+c2.getRadius();
    	double distanceBetweenCentres = Math.sqrt(Math.pow(c1.getCenterX()-c2.getCenterX(), 2)+(Math.pow(c1.getCenterY()-c2.getCenterY(), 2)));
    	
    	if(distanceBetweenCentres <= radiusSum) {
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * Checks whether two rectangles intersect
     * @param r1 first rectangle
     * @param r2 second rectangle
     * @return true if the rectangles intersect, false otherwise
     */
    public static boolean intersects(Rectangle r1, Rectangle r2) {
    	Point2D d1 = new Point2D(r1.getWidth(), r1.getHeight());
    	Point2D d2 = new Point2D(r2.getWidth(), r2.getHeight());
    	Point2D minP1 = new Point2D(r1.getX(),r1.getY());
    	Point2D maxP1 = minP1.add(d1);
    	Point2D minP2 = new Point2D(r2.getX(),r2.getY());
    	Point2D maxP2 = minP2.add(d2);
    	
    	//this may be able to be reduced
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
