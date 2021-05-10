package teamproject.wipeout.engine.component.physics;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.shape.Circle;
import teamproject.wipeout.engine.component.shape.Rectangle;
import teamproject.wipeout.engine.component.shape.Shape;
import teamproject.wipeout.engine.entity.GameEntity;

/**
 * Component that can be used to detect collisions between all entities with this component
 *  Made up of an array of shapes that act as the collision boundaries.
 */
public class HitboxComponent implements GameComponent {

	/**
	 * ArrayList of shapes to act as hitboxes
	 * x,y coords of rectangle represent offset from entities top left corner.
	 * width and height of rectangle are the dimensions of the bounding box.
	 */
	private ArrayList<Shape> hitboxes = new ArrayList<Shape>();
		
	
	//varargs constructor. See https://www.baeldung.com/java-varargs for info
	public HitboxComponent(Shape... shapes) {
		for(int i=0; i<shapes.length;i++) {
			//stop same hitbox getting added twice
			if(!hitboxes.contains(shapes[i])) {
				this.hitboxes.add(shapes[i]);
			}
		}
	}
	
	
	/**
	 * Adds new hitboxes to the hitbox ArrayList
	 * @param shapes hitboxes to add
	 */
	public void addHitboxes(Shape... shapes) {
		for(int i=0; i<shapes.length;i++) {
			//stop same hitbox getting added twice
			if(!hitboxes.contains(shapes[i])) {
				this.hitboxes.add(shapes[i]);
			}
		}		
	}
	
	/**
	 * Removes hitboxes from the hitbox ArrayList if they are there
	 * @param shapes hitboxes to remove from hitbox ArrayList
	 */
	public void removeHitoxes(Shape... shapes) {
		for(int i=0; i<shapes.length;i++) {
			this.hitboxes.remove(shapes[i]);
		}
	}
	
	public ArrayList<Shape> getHitboxes(){
		return this.hitboxes;
	}
	
    public String getType() {
        return "hitbox";
    }
    
    
	/**
	 * Checks whether two game entities collide (whether any of one's hitboxes overlaps with the other's)
	 * @param g1 first game entity
	 * @param g2 second game entity
	 * @return all pairs of shapes from the two GameEntity's hitboxes that collide
	 */
    public static ArrayList<Pair<Shape, Shape>> collides(GameEntity g1, GameEntity g2) {
    	Transform t1 = g1.getComponent(Transform.class);
    	HitboxComponent c1 = g1.getComponent(HitboxComponent.class);
    	ArrayList<Shape> bb1 = c1.hitboxes;
    	
    	Transform t2 = g2.getComponent(Transform.class);
    	HitboxComponent c2 = g2.getComponent(HitboxComponent.class);
    	ArrayList<Shape> bb2 = c2.hitboxes;
    	
    	ArrayList<Pair<Shape, Shape>> collidingPairs = new ArrayList<Pair<Shape, Shape>>();
    	for(int i=0;i<bb1.size();i++) {
    		Shape s1 = addAbsolutePosition(t1.getWorldPosition(), bb1.get(i));
        	for(int j=0;j<bb2.size();j++) {
        		Shape s2 = addAbsolutePosition(t2.getWorldPosition(), bb2.get(j));
            	if(GeometryUtil.intersects(s1,s2)) {
            		collidingPairs.add(new Pair<Shape, Shape>(s1, s2));
            	}
        	}
    	}
    	
    	//No pair of hitboxes collide
    	if (collidingPairs.size()==0) {
    		return null;
    	}
    	return collidingPairs;

    }
    
    /**
     * Checks whether two game entities collide
     * @param g1
     * @param g2
     * @return true if entities collide, false otherwise
     */
    public static boolean checkCollides(GameEntity g1, GameEntity g2) {
    	if(collides(g1,g2)==null) {
    		return false;
    	}
    	return true;
    }
    
      
    
    /**
     * Adds the coordinate of the location of the rectangle to the relative position of the bounding box
     * @param p location of the rectangle
     * @param r the bounding box
     * @return bounding box at correct location
     */
    private static Rectangle addAbsolutePosition(Point2D p, Rectangle r) {
    	return new Rectangle(r.getX()+p.getX(),
    						 r.getY()+p.getY(),
    						 r.getWidth(),
    						 r.getHeight());
    }
    
    private static Circle addAbsolutePosition(Point2D p, Circle c) {
    	return new Circle(c.getCentreX()+p.getX(),
    					  c.getCentreY()+p.getY(),
    					  c.getRadius());
    }
    
    /**
     * Method that takes a generic shape and calls the correct function to add absolute position
     * @param p absolute position
     * @param shape generic shape
     * @return shape at correct location
     */
    private static Shape addAbsolutePosition(Point2D p, Shape shape) {
    	if(shape instanceof Rectangle) {
    		return addAbsolutePosition(p, (Rectangle) shape);
    	}
    	else if(shape instanceof Circle) {
    		return addAbsolutePosition(p, (Circle) shape);
    	}
    	
    	return null;
    }

   
    
}

