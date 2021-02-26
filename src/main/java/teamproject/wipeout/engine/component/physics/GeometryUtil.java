package teamproject.wipeout.engine.component.physics;

import javafx.geometry.Point2D;
/**
 * Class containing functions related to geometry in 2D space
 *
 */
public class GeometryUtil
{
	    
    /**
     * Checks whether two circles intersect
     * @param c1 first circle
     * @param c2 second circle
     * @return true if the circles intersect, false otherwise
     */
    public static boolean intersects(Circle c1, Circle c2) {
    	//intersect if the distance between their two centres is less than the sum of their radiuses
    	double radiusSum = c1.getRadius()+c2.getRadius();
    	Point2D centre1 = new Point2D(c1.getCentreX(), c1.getCentreY());
    	Point2D centre2 = new Point2D(c2.getCentreX(), c2.getCentreY());
    	double distanceBetweenCentres = getDistanceBetweenTwoPoints(centre1, centre2);
    	
    	double overlap = radiusSum - distanceBetweenCentres;
    	//System.out.println("overlap: "+overlap);
    	
    	if(distanceBetweenCentres <= radiusSum) {
    		return true;
    	}
    	
    	return false;
    }

    /**
     * Calculates the distance between two points
     * @param p1 first point
     * @param p2 second point
     * @return the distance between the two points
     */
    public static double getDistanceBetweenTwoPoints(Point2D p1, Point2D p2) {
    	return Math.sqrt(Math.pow(p1.getX()-p2.getX(), 2)+(Math.pow(p1.getY()-p2.getY(), 2)));
    }
    
    /**
     * Checks whether two lines intersect
     * @param l1 first line
     * @param l2 second line
     * @return true if the lines intersect, false otherwise.
     */
    public static boolean intersects(Segment l1, Segment l2) {
		if(l1.pointOfIntersection(l2)==null) {
			return false;
		}
		
		return true;
	}

	/**
     * Checks whether a circle and a rectangle collide
     * @param c1 the circle
     * @param r1 the rectangle
     * @return true if the circle and rectangle intersect, false otherwise
     */
    public static boolean intersects(Circle c1, Rectangle r1) {    	
    	Point2D centre = new Point2D(c1.getCentreX(), c1.getCentreY());
    	
    	//System.out.println("centre: "+centre);
    	
    	Segment top = new Segment(r1.getX(), r1.getY(), r1.getX()+r1.getWidth(),r1.getY());
    	double distance = calculateDistanceBetweenPointAndLine(centre, top);
    	if(distance<=c1.getRadius()) {
    		//System.out.println(" 1 distance: "+distance);
    		//System.out.println("top: "+top.toString());
    		return true;
    	}
    	
    	Segment bottom = new Segment(r1.getX(), r1.getY()+r1.getHeight(), r1.getX()+r1.getWidth(),r1.getY()+r1.getHeight());
       	distance = calculateDistanceBetweenPointAndLine(centre, bottom);
    	if(distance<=c1.getRadius()) {
    		//System.out.println(" 2 distance: "+distance);
    		return true;
    	}
    	
    	Segment left = new Segment(r1.getX(), r1.getY(), r1.getX(),r1.getY()+r1.getHeight());
       	distance = calculateDistanceBetweenPointAndLine(centre, left);
    	if(distance<=c1.getRadius()) {
    		//System.out.println(" 3 distance: "+distance);
    		return true;
    	}
    	
    	Segment right = new Segment(r1.getX()+r1.getWidth(), r1.getY(), r1.getX()+r1.getWidth(),r1.getY()+r1.getHeight());
       	distance = calculateDistanceBetweenPointAndLine(centre, right);
    	if(distance<=c1.getRadius()) {
    		//System.out.println(" 4 distance: "+distance);
    		return true;
    	}
    	
    	//System.out.println("not colliding with line");
    	
    	//circle inside rectangle
    	if(r1.contains(centre)) {
    		//System.out.println(" circle inside rectangle ");
    		return true;
    	}
    	
    	//also if rectangle inside circle
    	if(c1.contains(new Point2D(r1.getX(),r1.getY()))) {
    		//System.out.println(" rectangle inside circle");
    		return true;
    	}
    	
    	//System.out.println(" no collision");
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
    	
    	if (r2.contains(tl) || r2.contains(br) || r2.contains(tr) || r2.contains(bl)) {
    		return true;
    	}
    	
    	// the following code will run to check r2 inside r1
    	// can probably be simplified
    	// if r2 completely inside r1, then we only need to check 1 point 
    	
    	//top left, bottom right, top right, bottom left corners of r2
    	tl = new Point2D(r2.getX(),r2.getY());
    	br = tl.add(new Point2D(r2.getWidth(), r2.getHeight()));
    	tr = tl.add(new Point2D(r2.getWidth(), 0));
    	bl = tl.add(new Point2D(0, r2.getHeight())) ;
    	
    	if (r1.contains(tl) || r1.contains(br) || r1.contains(tr) || r1.contains(bl)) {
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * Calculates a unit normal vector of a line connecting 2 points
     * @param p1 start point of line
     * @param p2 end point of line
     * @return a unit normal vector of the line connecting the 2 points provided
     */
    public static Point2D calculateUnitNormal(Point2D p1, Point2D p2) {
    	double dx = p2.getX() - p1.getX();
    	double dy = p2.getY() - p1.getY();
    	
    	Point2D normal = new Point2D(-dy,dx);
    	//other normal:
    	//Point2D normal2 = new Point2D(dy,-dx);
    	
    	double magnitude = normal.magnitude();
    	if (Double.compare(magnitude, 0)!=0) {
    		normal = normal.multiply(1/magnitude);
    	}
        	
    	return normal;
    }
    
    /**
     * Calculates a unit normal vector of the line
     * @param l the line
     * @return a unit normal vector of the line
     */
    public static Point2D calculateUnitNormal(Segment l) {
    	return calculateUnitNormal(new Point2D(l.getStartX(), l.getStartY()),new Point2D(l.getEndX(), l.getEndY()));
    }

    
    
	/**
	 * Calculates the overlap vector of two circles
	 * @param c1 first circle
	 * @param c2 second circle
	 * @return overlap vector of the two circles
	 */
	public static Point2D getResolutionVector(Circle c1, Circle c2) {
		//intersect if the distance between their two centres is less than the sum of their radiuses
		double radiusSum = c1.getRadius()+c2.getRadius();
		Point2D centre1 = new Point2D(c1.getCentreX(), c1.getCentreY());
		Point2D centre2 = new Point2D(c2.getCentreX(), c2.getCentreY());
		double distanceBetweenCentres = getDistanceBetweenTwoPoints(centre1, centre2);
		
		double overlap = radiusSum - distanceBetweenCentres;
		
		Point2D vectorBetweenCentres = centre1.subtract(centre2);
		double magnitude = vectorBetweenCentres.magnitude();
		Point2D overlapVector = new Point2D(0,0);
		
		if(magnitude!=0) {
			overlapVector = vectorBetweenCentres.multiply((overlap)/magnitude);
		}
		
		
		
		return overlapVector;
	}

	/**
	 * Calculates the overlap vector of two rectangles
	 * @param r1 first rectangle
	 * @param r2 second rectangle
	 * @return overlap vector of the two rectangles
	 */
	public static Point2D getResolutionVector(Rectangle r1, Rectangle r2) {
		//resolve in direction of least intersection
		double overlap = Double.MAX_VALUE;
		Point2D p = new Point2D(0,0);
		//check each side of rectangle
		Segment top_r1 = new Segment(r1.getX(), r1.getY(), r1.getX()+r1.getWidth(), r1.getY());
		Segment bottom_r1 = new Segment(r1.getX(), r1.getY()+r1.getHeight(), r1.getX()+r1.getWidth(), r1.getY()+r1.getHeight());
		Segment left_r2 = new Segment(r2.getX(), r2.getY(), r2.getX(), r2.getY()+r2.getHeight());
		Segment right_r1 = new Segment(r1.getX()+r1.getWidth(), r1.getY(), r1.getX()+r1.getWidth(), r1.getY()+r1.getHeight());
		Segment top_r2 = new Segment(r2.getX(), r2.getY(), r2.getX()+r2.getWidth(), r2.getY());
		Segment bottom_r2 = new Segment(r2.getX(), r2.getY()+r2.getHeight(), r2.getX()+r2.getWidth(), r2.getY()+r2.getHeight());
		Segment left_r1 = new Segment(r1.getX(), r1.getY(), r1.getX(), r1.getY()+r1.getHeight());
		Segment right_r2 = new Segment(r2.getX()+r2.getWidth(), r2.getY(), r2.getX()+r2.getWidth(), r2.getY()+r2.getHeight());
	
		if((intersects(left_r2, top_r1) && intersects(left_r2, bottom_r1)) ||
			(intersects(left_r1, top_r2) && intersects(left_r1, bottom_r2))|| 
			(intersects(right_r2, top_r1) && intersects(right_r2, bottom_r1))||
			(intersects(right_r2, top_r1) && intersects(right_r2, bottom_r1))||
			(intersects(right_r1, top_r2) && intersects(right_r1, bottom_r2))||//beyond here is common to both
			(intersects(bottom_r1, left_r2) && intersects(right_r1, top_r2))||
			(intersects(bottom_r2, left_r1) && intersects(right_r2, top_r1))||
			(intersects(top_r1, left_r2) && intersects(right_r1, bottom_r2))||
			(intersects(top_r2, left_r1) && intersects(right_r2, bottom_r1))
			) {
			//calculate 2 different intersections
			double i1 = getDistanceBetweenTwoPoints(new Point2D(r1.getX()+r1.getWidth(),0), new Point2D(r2.getX(),0));
			double i2 =  getDistanceBetweenTwoPoints(new Point2D(r1.getX(),0), new Point2D(r2.getX()+r2.getWidth(),0));
			if (Math.abs(overlap)>i1){
				overlap = -i1;
				p = new Point2D(overlap,0);
			}
			if(Math.abs(overlap)>i2) {
				overlap = i2;
				p = new Point2D(overlap,0);
			}
	
		}
		if((intersects(top_r2, left_r1) && intersects(top_r2, right_r1)) ||
			(intersects(top_r1, left_r2) && intersects(top_r1, right_r2))||
			(intersects(bottom_r1, left_r2) && intersects(bottom_r1, right_r2))||
			(intersects(bottom_r2, left_r1) && intersects(bottom_r2, right_r1))||//beyond here is common to both
			(intersects(bottom_r1, left_r2) && intersects(right_r1, top_r2))||
			(intersects(bottom_r2, left_r1) && intersects(right_r2, top_r1))||
			(intersects(top_r1, left_r2) && intersects(right_r1, bottom_r2))||
			(intersects(top_r2, left_r1) && intersects(right_r2, bottom_r1))
			) {
			//calculate 2 different intersections
			double i1 = getDistanceBetweenTwoPoints(new Point2D(0,r1.getY()+r1.getHeight()), new Point2D(0,r2.getY()));
			double i2 = getDistanceBetweenTwoPoints(new Point2D(0,r1.getY()), new Point2D(0,r2.getY()+r2.getHeight()));
			if (Math.abs(overlap)>i1){
				overlap = -i1;
				p = new Point2D(0,overlap);
			}
			if(Math.abs(overlap)>i2) {
				overlap = i2;
				p = new Point2D(0,overlap);
			}
			
	
		}
		
		return p;
	
		//check rectangle inside other rectangle
	
	}
	
	public static Point2D getResolutionVector(Rectangle r,Circle c) {
		//ISSUE: collision with corner of rectangle will cause sudden movement
		double overlap = Double.MAX_VALUE;
		Point2D p = new Point2D(0,0);
		Point2D centre = new Point2D(c.getCentreX(), c.getCentreY());
		Segment top = new Segment(r.getX(), r.getY(), r.getX()+r.getWidth(), r.getY());
		Segment bottom = new Segment(r.getX(), r.getY()+r.getHeight(), r.getX()+r.getWidth(), r.getY()+r.getHeight());
		Segment right = new Segment(r.getX()+r.getWidth(), r.getY(), r.getX()+r.getWidth(), r.getY()+r.getHeight());
		Segment left = new Segment(r.getX(), r.getY(), r.getX(), r.getY()+r.getHeight());
	
		double minDistance = Double.MAX_VALUE;
		double distanceToTop = calculateDistanceBetweenPointAndLine(centre, top)-c.getRadius();
		if(distanceToTop<minDistance) {
			minDistance=distanceToTop;
			p = new Point2D(0,minDistance);
		}
		double distanceToBottom = calculateDistanceBetweenPointAndLine(centre, bottom)-c.getRadius();
		if(distanceToBottom<minDistance) {
			minDistance=distanceToBottom;
			p = new Point2D(0,-minDistance);
		}
		double distanceToLeft = calculateDistanceBetweenPointAndLine(centre, left)-c.getRadius();
		if(distanceToLeft<minDistance) {
			minDistance=distanceToLeft;
			p = new Point2D(minDistance,0);
		}
		double distanceToRight = calculateDistanceBetweenPointAndLine(centre, right)-c.getRadius();
		if(distanceToRight<minDistance) {
			minDistance=distanceToRight;
			p = new Point2D(-minDistance,0);			
		}
				
		
		//System.out.println("overlap: "+p.toString());
		
		return p;
	}
	
	/**
	 * Calculates the shortest distance between a point and a line
	 * @param p the point
	 * @param l the line
	 * @return the shortest distance between the point and the line
	 */
	public static double calculateDistanceBetweenPointAndLine(Point2D p, Segment l) {
		
		if(l.contains(p)) {
			return 0;
		}
		
		Point2D normal = calculateUnitNormal(l);
		Point2D ac = calculateVector(new Point2D(l.getStartX(), l.getStartY()), p);
		
		double distance = ac.dotProduct(normal);
		//System.out.println("distance: "+distance);
		Point2D d = p.add(normal.multiply(-distance));
		//System.out.println("d: "+d.toString());
		
		distance = Math.abs(distance);
		//check closest point is on segment
		if(l.contains(d)) {
			//System.out.println("point on segment");
			return distance;
		}
		
		//if not, closest point is either start or end of segment
		Point2D bc = calculateVector(new Point2D(l.getEndX(), l.getEndY()), p);
		
		
		//because line is a segment, shortest distance could be from point to end point
		if(ac.magnitude()<bc.magnitude()) {
			distance = ac.magnitude();
		}
		else {
			distance = bc.magnitude();
		}
			
		return distance;
	}
	
	public static Point2D calculateVector(Point2D p1, Point2D p2) {
		return p2.subtract(p1);
	}
	
	/**
	 * Check if two doubles are approximately equal
	 * @param x
	 * @param y
	 * @return true if the doubles are approximately equal.
	 */
	public static boolean approxEquals(double x, double y) {
		double THRESHOLD = 0.000001;
    	if(Double.compare(Math.abs(x-y), THRESHOLD)>0) {
    		return false;
    	}
    	return true;
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




