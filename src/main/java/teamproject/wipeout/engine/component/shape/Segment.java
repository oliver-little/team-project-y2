package teamproject.wipeout.engine.component.shape;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.physics.GeometryUtil;

/**
 * Class for a line that connects two points
 */
public class Segment extends Shape
{

	private Point2D start;
	private Point2D end;
	
	public Segment(Point2D start, Point2D end) {
		this.start = start;
		this.end = end;
	}
	
	public Segment(double startX, double startY, double endX, double endY) {
		this.start = new Point2D(startX, startY);
		this.end = new Point2D(endX, endY);
	}

	public Point2D getStart()
	{
		return start;
	}
	
	public double getStartX()
	{
		return start.getX();
	}

	public double getStartY()
	{
		return start.getY();
	}
	
	public void setStart(Point2D start)
	{
		this.start = start;
	}

	public Point2D getEnd()
	{
		return end;
	}
	
	public double getEndX()
	{
		return end.getX();
	}

	public double getEndY()
	{
		return end.getY();
	}

	public void setEnd(Point2D end)
	{
		this.end = end;
	}

	/**
	 * Checks whether a point lies on a segment
	 * @param p the point
	 * @return true if the point lies on the segment, false otherwise.
	 */
    public boolean contains(Point2D p) {
    	double m = calculateGradient();

    	if(Double.compare(m, Double.MAX_VALUE)==0) {
    		//x=i
    		if(Double.compare(p.getX(), this.getStartX())!=0) {
    			return false;
    		}
    		
    	}
    	else {
        	double c = calculateYIntercept(m);
        
        	//y = mx + c
        	double y_new = m*p.getX()+c;
        	if(!GeometryUtil.approxEquals(y_new, p.getY())) {
        		return false;
        	}
    	}
    	
    	//on line
    	if(this.getStartX()<=this.getEndX()) {
        	if(this.getStartY()<=this.getEndY()) {
            	if(p.getX()>=this.getStartX() && p.getY()>=this.getStartY() &&
            	   p.getX()<=this.getEndX() && p.getY()<=this.getEndY()) {
            		return true;
            	}    		
        	}
        	else {
            	if(p.getX()>=this.getStartX() && p.getY()<=this.getStartY() &&
            	   p.getX()<=this.getEndX() && p.getY()>=this.getEndY()) {
            		return true;
                 }  
        	}
    	}
    	else {
        	if(this.getStartY()<=this.getEndY()) {
            	if(p.getX()<=this.getStartX() && p.getY()>=this.getStartY() &&
            	   p.getX()>=this.getEndX() && p.getY()<=this.getEndY()) {
            		return true;
            	}    		
        	}
        	else {
            	if(p.getX()<=this.getStartX() && p.getY()<=this.getStartY() &&
            	   p.getX()>=this.getEndX() && p.getY()>=this.getEndY()) {
            		return true;
                 }  
        	}
    	}
    	return false;
    }
    
    /**
	 * Calculates the gradient of a line
	 * @return the gradient of the line. Double.MAX_VALUE if line is vertical. 0 if line is horizontal.
	 */
    public double calculateGradient() {
    	double dx = this.getEndX() - this.getStartX();
    	//using Double.compare because of imprecision of floating point values
    	if(Double.compare(dx, 0) == 0) {
    		return Double.MAX_VALUE;
    	}
    	double dy = this.getEndY() - this.getStartY();
    	
    	double gradient = dy/dx;
    	
    	//stop -0 ever from being returned
    	if(Math.abs(gradient)==0) {
    		return 0;
    	}
    	
    	return gradient;
    }
    
    /**
     * Calculates the y intercept of a line with a known gradient.
     * @param gradient gradient of the line
     * @return the y intercept of the line
     */
    public double calculateYIntercept(double gradient) {
    	return this.getStartY()-(gradient*this.getStartX());
    }
    
    /**
     * Calculates the y intercept of a line
     * @return the y intercept of the line
     */
    public double calculateYIntercept() {
    	return calculateYIntercept(calculateGradient());
    }
	
    /**
     * Finds the point of intersection of two lines if they meet
     * @param l2 the second line
     * @return the point of intersection if they meet, otherwise null.
     */
	public Point2D pointOfIntersection(Segment l2) {
		double gradient_l1 = this.calculateGradient();    
		double yIntercept_l1 = this.calculateYIntercept(gradient_l1);
		
		double gradient_l2 = l2.calculateGradient();
		double yIntercept_l2 = l2.calculateYIntercept(gradient_l2);
		
		if(Double.compare(gradient_l1, gradient_l2)==0) {
			if(Double.compare(yIntercept_l1, yIntercept_l2)==0) {
				//lines overlap
				Point2D p = new Point2D(this.getStartX(), this.getStartY());
				if(l2.contains(p)) {
					return p;
				}
				p = new Point2D(l2.getStartX(), l2.getStartY());
				if (this.contains(p)){
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
			x = this.getStartX();
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

		//check point lies on l1 and l2
		if(!this.contains(p) || !l2.contains(p)) {
			return null;
		}
		return p;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Segment other = (Segment) obj;
		if (end == null)
		{
			if (other.end != null)
				return false;
		}
		else if (!end.equals(other.end))
			return false;
		if (start == null)
		{
			if (other.start != null)
				return false;
		}
		else if (!start.equals(other.start))
			return false;
		return true;
	}
	
}
