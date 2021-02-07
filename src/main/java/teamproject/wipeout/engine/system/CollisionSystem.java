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
    	//top left, bottom right, top right, bottom left corners of r1
    	Point2D tl = new Point2D(r1.getX(),r1.getY());
    	Point2D br = tl.add(new Point2D(r1.getWidth(), r1.getHeight()));
    	Point2D tr = tl.add(new Point2D(r1.getWidth(), 0));
    	Point2D bl = tl.add(new Point2D(0, r1.getHeight())) ;
    	
    	if (isPointInside(tl, r2) || isPointInside(br, r2) || isPointInside(tr, r2) || isPointInside(bl, r2)) {
    		return true;
    	}
    	
    	//the following code will run to check r2 inside r1
    	// can probably be simplified
    	// if r2 completely inside r1, then we only need to check 1 point 
    	
    	//top left, bottom right, top right, bottom left corners of r2
    	tl = new Point2D(r2.getX(),r2.getY());
    	br = tl.add(new Point2D(r2.getWidth(), r2.getHeight()));
    	tr = tl.add(new Point2D(r2.getWidth(), 0));
    	bl = tl.add(new Point2D(0, r2.getHeight())) ;

    	
    	return (isPointInside(tl, r1) || isPointInside(br, r1) || isPointInside(tr, r1) || isPointInside(bl, r1));
    }
    
    /**
     * Checks whether a point is inside a rectangle (inclusive of edges)
     * @param p point to check
     * @param r rectangle
     * @return true if point is inside rectangle, false otherwise.
     */
    private static boolean isPointInside(Point2D p, Rectangle r) {
    	//check point beyond top left corner of rectangle
    	if(p.getX()>=r.getX() && p.getY()>=r.getY()) {
    		//check point before bottom right corner of rectangle
        	if(p.getX()<=(r.getX()+r.getWidth()) && p.getY()<=(r.getY()) + r.getHeight()) {
        		System.out.println(p.toString()+" is inside "+r.toString());
        		return true;
        	}
    	}
    	
    	return false;
    }
   

}
