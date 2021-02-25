package teamproject.wipeout.engine.component.physics;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
/**
 * Class containing functions related to geometry in 2D space
 *
 */
public class GeometryUtil
{
	/**
	 * Calculates the gradient of a line
	 * @param l the line
	 * @return the gradient of the line. Double.MAX_VALUE if line is vertical. 0 if line is horizontal.
	 */
    public static double calculateGradient(Line l) {
    	double dx = l.getEndX() - l.getStartX();
    	//using Double.compare because of imprecision of floating point values
    	if(Double.compare(dx, 0) == 0) {
    		return Double.MAX_VALUE;
    	}
    	double dy = l.getEndY() - l.getStartY();
    	
    	double gradient = dy/dx;
    	
    	//stop -0 ever from being returned
    	if(Math.abs(gradient)==0) {
    		return 0;
    	}
    	
    	return gradient;
    }
    
    /**
     * Calculates the y intercept of a line with a known gradient.
     * @param l the line
     * @param gradient gradient of the line l
     * @return the y intercept of the line
     */
    public static double calculateYIntercept(Line l, double gradient) {
    	return l.getStartY()-(gradient*l.getStartX());
    }
    
    /**
     * Calculates the y intercept of a line
     * @param l the line
     * @return the y intercept of the line
     */
    public static double calculateYIntercept(Line l) {
    	return calculateYIntercept(l, calculateGradient(l));
    }
	
    /**
     * Finds the point of intersection of two lines if they meet
     * @param l1 the first line
     * @param l2 the second line
     * @return the point of intersection if they meet, otherwise null.
     */
	public static Point2D pointOfIntersection(Line l1, Line l2) {
		double gradient_l1 = calculateGradient(l1);    
		double yIntercept_l1 = calculateYIntercept(l1, gradient_l1);
		
		double gradient_l2 = calculateGradient(l2);
		double yIntercept_l2 = calculateYIntercept(l2, gradient_l2);
		
		//System.out.println("gradient_l1: "+gradient_l1);
		//System.out.println("gradient_l2: "+gradient_l2);
		//System.out.println("yIntercept_l1: "+yIntercept_l1);
		//System.out.println("yIntercept_l2: "+yIntercept_l2);

		
		if(Double.compare(gradient_l1, gradient_l2)==0) {
			if(Double.compare(yIntercept_l1, yIntercept_l2)==0) {
				//lines overlap
				//TODO make sure point is on both lines
				Point2D p = new Point2D(l1.getStartX(), l1.getStartY());
				if(pointOnSegment(p, l2)) {
					return p;
				}
				p = new Point2D(l2.getStartX(), l2.getStartY());
				if (pointOnSegment(p, l1)){
					return p;
				}
				//otherwise segments do not meet
				return null;
				
			}
			else {
				//lines are parallel but not overlapping
				return null;
			}
			
		}
		
		//calculate where lines meet
		double x=0;
		double y=0;
		if(Double.compare(gradient_l1, Double.MAX_VALUE)==0) {
			x = l1.getStartX();
			y = gradient_l2*x + yIntercept_l2;
		}
		else if(Double.compare(gradient_l2, Double.MAX_VALUE)==0) {
			x = l2.getStartX();
			y = gradient_l1*x + yIntercept_l1;
		}
		else {
			x = (yIntercept_l2-yIntercept_l1)/(gradient_l1-gradient_l2);
			y = gradient_l1*x + yIntercept_l1;
		}
		
		Point2D p = new Point2D(x,y);
		//System.out.println("point : "+p.toString());
		//check point lies on l1 and l2
		if(!pointOnSegment(p, l1) || !pointOnSegment(p, l2)) {
			//System.out.println("not on at least one segment");
			return null;
		}
		return p;
	}
	
	/**
	 * Checks whether a point lies on a segment
	 * @param p the point
	 * @param l the segment
	 * @return true if the point lies on the segment, false otherwise.
	 */
    public static boolean pointOnSegment(Point2D p, Line l) {
    	double m = calculateGradient(l);
    	//System.out.println("m: "+m);
    	if(Double.compare(m, Double.MAX_VALUE)==0) {
    		//x=i
    		if(Double.compare(p.getX(), l.getStartX())!=0) {
    			return false;
    		}
    		
    	}
    	else {
        	double c = calculateYIntercept(l,m);
        	
        	//System.out.println("y: "+p.getY());
        	//System.out.println("mx+c: "+(m*p.getX()+c));
        
        	//y = mx + c
        	double y_new = m*p.getX()+c;
        	if(!approxEquals(y_new, p.getY())) {
        		//System.out.println("not on line");
        		return false;
        	}
    	}
    	
    	//on line
    	if(l.getStartX()<=l.getEndX()) {
        	if(l.getStartY()<=l.getEndY()) {
            	if(p.getX()>=l.getStartX() && p.getY()>=l.getStartY() &&
            	   p.getX()<=l.getEndX() && p.getY()<=l.getEndY()) {
            		return true;
            	}    		
        	}
        	else {
            	if(p.getX()>=l.getStartX() && p.getY()<=l.getStartY() &&
            	   p.getX()<=l.getEndX() && p.getY()>=l.getEndY()) {
            		return true;
                 }  
        	}
    	}
    	else {
        	if(l.getStartY()<=l.getEndY()) {
            	if(p.getX()<=l.getStartX() && p.getY()>=l.getStartY() &&
            	   p.getX()>=l.getEndX() && p.getY()<=l.getEndY()) {
            		return true;
            	}    		
        	}
        	else {
            	if(p.getX()<=l.getStartX() && p.getY()<=l.getStartY() &&
            	   p.getX()>=l.getEndX() && p.getY()>=l.getEndY()) {
            		return true;
                 }  
        	}
    	}
    	return false;
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
    public static boolean intersects(Line l1, Line l2) {
		if(pointOfIntersection(l1,l2)==null) {
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
    	Point2D centre = new Point2D(c1.getCenterX(), c1.getCenterY());
    	
    	//System.out.println("centre: "+centre);
    	
    	Line top = new Line(r1.getX(), r1.getY(), r1.getX()+r1.getWidth(),r1.getY());
    	double distance = calculateDistanceBetweenPointAndLine(centre, top);
    	if(distance<=c1.getRadius()) {
    		//System.out.println(" 1 distance: "+distance);
    		//System.out.println("top: "+top.toString());
    		return true;
    	}
    	
    	Line bottom = new Line(r1.getX(), r1.getY()+r1.getHeight(), r1.getX()+r1.getWidth(),r1.getY()+r1.getHeight());
       	distance = calculateDistanceBetweenPointAndLine(centre, bottom);
    	if(distance<=c1.getRadius()) {
    		//System.out.println(" 2 distance: "+distance);
    		return true;
    	}
    	
    	Line left = new Line(r1.getX(), r1.getY(), r1.getX(),r1.getY()+r1.getHeight());
       	distance = calculateDistanceBetweenPointAndLine(centre, left);
    	if(distance<=c1.getRadius()) {
    		//System.out.println(" 3 distance: "+distance);
    		return true;
    	}
    	
    	Line right = new Line(r1.getX()+r1.getWidth(), r1.getY(), r1.getX()+r1.getWidth(),r1.getY()+r1.getHeight());
       	distance = calculateDistanceBetweenPointAndLine(centre, right);
    	if(distance<=c1.getRadius()) {
    		//System.out.println(" 4 distance: "+distance);
    		return true;
    	}
    	
    	//System.out.println("not colliding with line");
    	
    	//circle inside rectangle
    	if(isPointInside(centre, r1)) {
    		//System.out.println(" circle inside rectangle ");
    		return true;
    	}
    	
    	//also if rectangle inside circle
    	if(isPointInside(new Point2D(r1.getX(),r1.getY()), c1)) {
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
    	
    	if (isPointInside(tl, r2) || isPointInside(br, r2) || isPointInside(tr, r2) || isPointInside(bl, r2)) {
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
    	
    	if (isPointInside(tl, r1) || isPointInside(br, r1) || isPointInside(tr, r1) || isPointInside(bl, r1)) {
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
    public static Point2D calculateUnitNormal(Line l) {
    	return calculateUnitNormal(new Point2D(l.getStartX(), l.getStartY()),new Point2D(l.getEndX(), l.getEndY()));
    }

	/**
     * Checks whether a point is inside a rectangle (inclusive of edges)
     * @param p point to check
     * @param r rectangle
     * @return true if point is inside rectangle, false otherwise.
     */
    public static boolean isPointInside(Point2D p, Rectangle r) {
    	//check point beyond top left corner of rectangle
    	if(p.getX()>=r.getX() && p.getY()>=r.getY()) {
    		//check point before bottom right corner of rectangle
        	if(p.getX()<=(r.getX()+r.getWidth()) && p.getY()<=(r.getY()) + r.getHeight()) {
        		return true;
        	}
    	}
    	
    	return false;
    }
    
    /**
     * Checks whether a point is inside a circle
     * @param p the point
     * @param c the circle
     * @return true if the point is inside, false otherwise
     */
    public static boolean isPointInside(Point2D p, Circle c) {
    	double distance = getDistanceBetweenTwoPoints(p,new Point2D(c.getCenterX(),c.getCenterY()));
    	if(distance<=c.getRadius()) {
    		return true;
    	}
    	return false;
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
		Point2D centre1 = new Point2D(c1.getCenterX(), c1.getCenterY());
		Point2D centre2 = new Point2D(c2.getCenterX(), c2.getCenterY());
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
		Line top_r1 = new Line(r1.getX(), r1.getY(), r1.getX()+r1.getWidth(), r1.getY());
		Line bottom_r1 = new Line(r1.getX(), r1.getY()+r1.getHeight(), r1.getX()+r1.getWidth(), r1.getY()+r1.getHeight());
		Line left_r2 = new Line(r2.getX(), r2.getY(), r2.getX(), r2.getY()+r2.getHeight());
		Line right_r1 = new Line(r1.getX()+r1.getWidth(), r1.getY(), r1.getX()+r1.getWidth(), r1.getY()+r1.getHeight());
		Line top_r2 = new Line(r2.getX(), r2.getY(), r2.getX()+r2.getWidth(), r2.getY());
		Line bottom_r2 = new Line(r2.getX(), r2.getY()+r2.getHeight(), r2.getX()+r2.getWidth(), r2.getY()+r2.getHeight());
		Line left_r1 = new Line(r1.getX(), r1.getY(), r1.getX(), r1.getY()+r1.getHeight());
		Line right_r2 = new Line(r2.getX()+r2.getWidth(), r2.getY(), r2.getX()+r2.getWidth(), r2.getY()+r2.getHeight());
	
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
		Point2D centre = new Point2D(c.getCenterX(), c.getCenterY());
		Line top = new Line(r.getX(), r.getY(), r.getX()+r.getWidth(), r.getY());
		Line bottom = new Line(r.getX(), r.getY()+r.getHeight(), r.getX()+r.getWidth(), r.getY()+r.getHeight());
		Line right = new Line(r.getX()+r.getWidth(), r.getY(), r.getX()+r.getWidth(), r.getY()+r.getHeight());
		Line left = new Line(r.getX(), r.getY(), r.getX(), r.getY()+r.getHeight());
	
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
	public static double calculateDistanceBetweenPointAndLine(Point2D p, Line l) {
		
		if(pointOnSegment(p,l)) {
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
		if(pointOnSegment(d,l)) {
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
}




