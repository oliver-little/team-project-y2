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
	
	/*
	public CollisionComponent(Shape[] boundingBoxes) {
		this.boundingBoxes=boundingBoxes;
	}
	*/
	
	//varargs constructor. See https://www.baeldung.com/java-varargs for info
	public CollisionComponent(Shape... shapes) {
		this.boundingBoxes = shapes;
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
     * @return true if they collide, false otherwise
     */
    public static boolean collides(GameEntity g1, GameEntity g2) {
    	Transform t1 = g1.getComponent(Transform.class);
    	CollisionComponent c1 = g1.getComponent(CollisionComponent.class);
    	Shape bb1[] = c1.boundingBoxes;
    	
    	Transform t2 = g2.getComponent(Transform.class);
    	CollisionComponent c2 = g2.getComponent(CollisionComponent.class);
    	Shape bb2[] = c2.boundingBoxes;
    	
    	for(int i=0;i<bb1.length;i++) {
    		//System.out.println("bb1["+i+"]: "+bb1[i].toString());
    		Shape s1 = addAbsolutePosition(t1.position, bb1[i]);
    		//System.out.println("s1: "+s1.toString());

        	for(int j=0;j<bb2.length;j++) {
        		//System.out.println("bb1["+j+"]: "+bb1[j].toString());
        		Shape s2 = addAbsolutePosition(t2.position, bb2[j]);
        		//System.out.println("s2: "+s2.toString());
            	if(intersects(s1,s2)) {
            		return true;
            	}
        	}
    	}
    	
    	return false;

    }
    
    /**
     * Calculates the normal vectors of a line connecting 2 points
     * @param p1 start point of line
     * @param p2 end point of line
     * @return normal vectors of the line connecting the 2 points provided
     */
    public Point2D[] getNormalsToLine(Point2D p1, Point2D p2) {
    	double dx = p2.getX() - p1.getX();
    	double dy = p2.getY() - p1.getY();
    	
    	Point2D normals[] = {new Point2D(-dy,dx),new Point2D(dy,-dx)};
    	
    	return normals;
    }
    
    public static Point2D pointOfIntersection(Line l1, Line l2) {
    	double gradient_l1 = calculateGradientOfLine(l1);    
    	double yIntercept_l1 = calculateYInterceptOfLine(l1, gradient_l1);
    	
    	double gradient_l2 = calculateGradientOfLine(l2);
    	double yIntercept_l2 = calculateYInterceptOfLine(l2, gradient_l2);
    	
    	
    	if(Double.compare(gradient_l1, gradient_l2)==0) {
    		if(Double.compare(yIntercept_l1, yIntercept_l2)==0) {
    			//lines overlap
    			//TODO return a point
    			return null;
    		}
    		else {
    			//lines are parallel but not overlapping
    			return null;
    		}
    		
    	}
    	
    	//calculate where lines meet
    	double x = (yIntercept_l2-yIntercept_l1)/(gradient_l1-gradient_l2);
    	double y = gradient_l1*x + yIntercept_l1;
    	
    	//check point (x,y) lies on l1
    	if(x<l1.getStartX()) {
    		return null;
    	}
    	else if(x>l1.getEndX()) {
    		return null;
    	}
    	else if(y<l1.getStartY()) {
    		return null;
    	}
    	else if(x>l1.getEndY()) {
    		return null;
    	}
    	
    	//check point (x,y) lies on l2
    	if(x<l2.getStartX()) {
    		return null;
    	}
    	else if(x>l2.getEndX()) {
    		return null;
    	}
    	else if(y<l2.getStartY()) {
    		return null;
    	}
    	else if(x>l2.getEndY()) {
    		return null;
    	}

    	return new Point2D(x,y);
    }
    
    public static boolean intersects(Line l1, Line l2) {
    	double gradient_l1 = calculateGradientOfLine(l1);    
    	double yIntercept_l1 = calculateYInterceptOfLine(l1, gradient_l1);
    	
    	double gradient_l2 = calculateGradientOfLine(l2);
    	double yIntercept_l2 = calculateYInterceptOfLine(l2, gradient_l2);
    	
    	
    	if(Double.compare(gradient_l1, gradient_l2)==0) {
    		if(Double.compare(yIntercept_l1, yIntercept_l2)==0) {
    			//lines overlap
    			System.out.println("lines overlap");
    			return true;
    		}
    		else {
    			//lines are parallel but not overlapping
    			System.out.println("lines are parallel");
    			return false;
    		}
    		
    	}
    	
    	//calculate where lines meet
    	double x = (yIntercept_l2-yIntercept_l1)/(gradient_l1-gradient_l2);
    	double y = gradient_l1*x + yIntercept_l1;
    	
    	Point2D p = new Point2D(x,y);
    	System.out.println("Lines meet at "+p.toString());
    	
    	Point2D start1 = new Point2D(l1.getStartX(), l1.getStartY());
    	Point2D end1 = new Point2D(l1.getEndX(), l1.getEndY());
    	double lengthOfLine1 = getDistanceBetweenTwoPoints(start1, end1);
    	
    	
    	if(getDistanceBetweenTwoPoints(p,start1)>lengthOfLine1 && getDistanceBetweenTwoPoints(p,end1)>lengthOfLine1) {
    		//point not on l1
    		return false;
    	}
    	
    	Point2D start2 = new Point2D(l2.getStartX(), l2.getStartY());
    	Point2D end2 = new Point2D(l2.getEndX(), l2.getEndY());
    	double lengthOfLine2 = getDistanceBetweenTwoPoints(start2, end2);
    	
    	if(getDistanceBetweenTwoPoints(p,start2)>lengthOfLine2 && getDistanceBetweenTwoPoints(p,end2)>lengthOfLine2) {
    		//point not on l2
    		return false;
    	}
    	

    	return true;
    }
    
    public static double calculateGradientOfLine(Line l) {
    	double dx = l.getEndX() - l.getStartX();
    	//using Double.compare because of imprecision of floating point values
    	if(Double.compare(dx, 0) == 0) {
    		return 0;
    	}
    	double dy = l.getEndY() - l.getStartY();
    	
    	return dy/dx;
    }
    
    public static double calculateYInterceptOfLine(Line l, double gradient) {
    	return l.getStartY()-(gradient*l.getStartY());
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
				return intersects(r1,r2);
			}
			else if(s2 instanceof Circle) {
				Circle c2 = (Circle) s2;
				return intersects(r1,c2);
			}
		}
		else if(s1 instanceof Circle) {
			Circle c1 = (Circle) s1;
			if (s2 instanceof Rectangle) {
				Rectangle r2 = (Rectangle) s2;
				return intersects(c1,r2);
			}
			else if(s2 instanceof Circle) {
				Circle c2 = (Circle) s2;
				return intersects(c1,c2);
			}
		}
		
		System.out.print("Collision not implemented yet between "+ s1.getClass().toString()+ " and "+ s2.getClass().toString());
		
    	return false;
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
     * Checks whether two circles intersect
     * @param c1 first circle
     * @param c2 second circle
     * @return true if the circles intersect, false otherwise
     */
    public static boolean intersects(Circle c1, Circle c2) {
    	//intersect if the distance between their two centres is less than the sum of their radiuses
    	double radiusSum = c1.getRadius()+c2.getRadius();
    	Point2D centre1 = new Point2D(c1.getCenterX(), c1.getCenterY());
    	Point2D centre2 = new Point2D(c2.getCenterX(), c2.getCenterY());
    	double distanceBetweenCentres = getDistanceBetweenTwoPoints(centre1, centre2);
    	
    	if(distanceBetweenCentres <= radiusSum) {
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * Checks whether a circle and a rectangle collide
     * @param c1 the circle
     * @param r1 the rectangle
     * @return true if the circle and rectangle intersect, false otherwise
     */
    public static boolean intersects(Circle c1, Rectangle r1) {
    	//collide if distance between centre of circle and any corner of rectangle is less than
    	// or equal to the radius of the circle
    	// also collide if circle is completely inside the rectangle
    	// this is when centre of circle is contained in the rectangle
    	
    	Point2D centre = new Point2D(c1.getCenterX(), c1.getCenterY());
    	if(isPointInside(centre, r1)) {
    		return true;
    	}
    	
    	Point2D tl = new Point2D(r1.getX(),r1.getY());
    	if (getDistanceBetweenTwoPoints(centre, tl) <= c1.getRadius()) {
    		return true;
    	}
    	
    	Point2D br = tl.add(new Point2D(r1.getWidth(), r1.getHeight()));
    	if (getDistanceBetweenTwoPoints(centre, br) <= c1.getRadius()) {
    		return true;
    	}
    	
    	Point2D tr = tl.add(new Point2D(r1.getWidth(), 0));
    	if (getDistanceBetweenTwoPoints(centre, tr) <= c1.getRadius()) {
    		return true;
    	}
    	
    	Point2D bl = tl.add(new Point2D(0, r1.getHeight())) ;
    	if (getDistanceBetweenTwoPoints(centre, bl) <= c1.getRadius()) {
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * Checks whether a circle and a rectangle collide
     * @param r1 the rectangle
     * @param c1 the circle
     * @return true if the circle and rectangle intersect, false otherwise
     */
    public static boolean intersects(Rectangle r1, Circle c1) {
    	return intersects(c1,r1);
    }
    
    public static double getDistanceBetweenTwoPoints(Point2D p1, Point2D p2) {
    	return Math.sqrt(Math.pow(p1.getX()-p2.getX(), 2)+(Math.pow(p1.getY()-p2.getY(), 2)));
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
    	
    	// the following code will run to check r2 inside r1
    	// can probably be simplified
    	// if r2 completely inside r1, then we only need to check 1 point 
    	
    	//top left, bottom right, top right, bottom left corners of r2
    	tl = new Point2D(r2.getX(),r2.getY());

    	return isPointInside(tl, r1);
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
        		return true;
        	}
    	}
    	
    	return false;
    }
   
    
}

