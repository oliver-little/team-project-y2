package teamproject.wipeout.engine.component.physics;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.entity.GameEntity;

public class CollisionComponent implements GameComponent {

	//each rectangle with attributes x, y, width, height
	// x = horizontal offset from top left corner
	// y = vertical offset from top left corner
	/**
	 * Array of rectangles that are given collision property.
	 * x,y coords of rectangle represent offset from entities top left corner.
	 * width and height of rectangle are the dimensions of the bounding box.
	 */
	public Shape boundingBoxes[];
	
	public boolean isMoveable = true;
	
	/*
	public CollisionComponent(Shape[] boundingBoxes) {
		this.boundingBoxes=boundingBoxes;
	}
	*/
	
	//varargs constructor. See https://www.baeldung.com/java-varargs for info
	public CollisionComponent(Shape... shapes) {
		this.boundingBoxes = shapes;
		this.isMoveable = true;
	}
	
	//varargs constructor. See https://www.baeldung.com/java-varargs for info
	public CollisionComponent(boolean isMoveable, Shape... shapes) {
		this.boundingBoxes = shapes;
		this.isMoveable = isMoveable;
	}
	
	/**
	 * Adds new bounding boxes to the bounding box array
	 * @param shapes bounding boxes to add
	 */
	public void addBoundingBoxes(Shape... shapes) {
		// May want to change data type of boundingBoxes to something more dynamic for efficiency improvement
		Shape bb[] = new Shape[boundingBoxes.length+shapes.length];

		for(int i=0; i<boundingBoxes.length;i++) {
			bb[i]= boundingBoxes[i];
		}
		for(int i=0; i<shapes.length;i++) {
			bb[i+boundingBoxes.length]= shapes[i];
		}
		this.boundingBoxes = bb;
		
	}
	
	/**
	 * Removes bounding boxes from the bounding box array if they are there
	 * @param shapes bounding boxes to remove
	 */
	public void removeBoundingBoxes(Shape... shapes) {
		// May want to change data type of boundingBoxes to something more dynamic for efficiency improvement
		ArrayList<Shape> bb = (ArrayList<Shape>) Arrays.asList(boundingBoxes);

		
		for(int i=0; i<shapes.length;i++) {
			for(int j=0; j<boundingBoxes.length; j++) {
				if(shapes[i].equals(boundingBoxes[j])) {
					bb.remove(j);
				}
			}
		}

		boundingBoxes = (Shape[]) bb.toArray();
	}
	
    public String getType() {
        return "collision";
    }
    
    
    //collision detection
    
    /**
     * Checks whether two game entities collide (whether any of one's bounding boxes overlaps with the other's)
     * @param g1 first game entity
     * @param g2 second game entity
     * @return pair of shapes from the two GameEntity's bounding boxes that collide
     */
    public static Pair<Shape, Shape> collides(GameEntity g1, GameEntity g2) {
    	Transform t1 = g1.getComponent(Transform.class);
    	CollisionComponent c1 = g1.getComponent(CollisionComponent.class);
    	Shape bb1[] = c1.boundingBoxes;
    	
    	Transform t2 = g2.getComponent(Transform.class);
    	CollisionComponent c2 = g2.getComponent(CollisionComponent.class);
    	Shape bb2[] = c2.boundingBoxes;
    	
    	for(int i=0;i<bb1.length;i++) {
    		Shape s1 = addAbsolutePosition(t1.position, bb1[i]);
        	for(int j=0;j<bb2.length;j++) {
        		Shape s2 = addAbsolutePosition(t2.position, bb2[j]);
            	if(intersects(s1,s2)) {
            		return new Pair<Shape, Shape>(s1, s2);
            	}
        	}
    	}
    	
    	//No pair of shapes from the boundng boxes collide
    	return null;

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
    	return new Circle(c.getCenterX()+p.getX(),
    					  c.getCenterY()+p.getY(),
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
    	
    	System.out.print("addAbsolutePosition not implemented yet for "+ shape.getClass().toString());
    	return null;
    }

	/**
	 * Intersects function for generic shapes that calls the appropriate intersects function
	 * @param s1 first shape
	 * @param s2 second shape
	 * @return true if the shapes collide, false otherwise
	 */
	public static boolean intersects(Shape s1, Shape s2) {
		// info on downcasting: https://www.baeldung.com/java-type-casting
		if(s1 instanceof Rectangle) {
			Rectangle r1 = (Rectangle) s1;
			if (s2 instanceof Rectangle) {
				Rectangle r2 = (Rectangle) s2;
				return GeometryUtil.intersects(r1,r2);
			}
			else if(s2 instanceof Circle) {
				Circle c2 = (Circle) s2;
				return GeometryUtil.intersects(c2,r1);
			}
		}
		else if(s1 instanceof Circle) {
			Circle c1 = (Circle) s1;
			if (s2 instanceof Rectangle) {
				Rectangle r2 = (Rectangle) s2;
				return GeometryUtil.intersects(c1,r2);
			}
			else if(s2 instanceof Circle) {
				Circle c2 = (Circle) s2;
				return GeometryUtil.intersects(c1,c2);
			}
		}
		
		System.out.print("Collision not implemented yet between "+ s1.getClass().toString()+ " and "+ s2.getClass().toString());
		
		return false;
	}

	public static Point2D getResolutionVector(Shape s1, Shape s2)
	{
		// info on downcasting: https://www.baeldung.com/java-type-casting
		if(s1 instanceof Rectangle) {
			Rectangle r1 = (Rectangle) s1;
			if (s2 instanceof Rectangle) {
				Rectangle r2 = (Rectangle) s2;
				return GeometryUtil.getResolutionVector(r1,r2);
			}
			else if(s2 instanceof Circle) {
				Circle c2 = (Circle) s2;
				return GeometryUtil.getResolutionVector(r1,c2);
			}
		}
		else if(s1 instanceof Circle) {
			Circle c1 = (Circle) s1;
			if (s2 instanceof Rectangle) {
				Rectangle r2 = (Rectangle) s2;
				return GeometryUtil.getResolutionVector(r2, c1);
			}
			else if(s2 instanceof Circle) {
				Circle c2 = (Circle) s2;
				//System.out.println("calling it");
				return GeometryUtil.getResolutionVector(c1,c2);
			}
		}
		
		System.out.print("Collision not implemented yet between "+ s1.getClass().toString()+ " and "+ s2.getClass().toString());
		
		return null;
		
	}
   
    
}

