package teamproject.wipeout.engine.component.shape;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.physics.GeometryUtil;

/**
 * Class for a Circle with a radius about a point in 2D space
 */
public class Circle extends Shape {

	/**
	 * x coordinate of centre of circle
	 */
	private double centreX;
	/**
	 * y coordinate of centre of circle
	 */
	private double centreY;
	/**
	 * radius of circle
	 */
	private double radius;
	
	public Circle(double radius) {
		this.radius=radius;
	}
	
	
	public Circle(double centreX, double centreY, double radius) {
		this.centreX = centreX;
		this.centreY = centreY;
		this.radius = radius;
	}
	
	public Circle(Point2D centre, double radius) {
		this.centreX = centre.getX();
		this.centreY = centre.getY();
		this.radius = radius;
	}


	public double getCentreX()
	{
		return centreX;
	}


	public void setCentreX(double centreX)
	{
		this.centreX = centreX;
	}


	public double getCentreY()
	{
		return centreY;
	}


	public void setCentreY(double centreY)
	{
		this.centreY = centreY;
	}


	public double getRadius()
	{
		return radius;
	}


	public void setRadius(double radius)
	{
		this.radius = radius;
	}
	
    /**
     * Checks whether a point is inside a circle
     * @param p the point
     * @return true if the point is inside, false otherwise
     */
    public boolean contains(Point2D p) {
    	double distance = GeometryUtil.getDistanceBetweenTwoPoints(p,new Point2D(this.getCentreX(),this.getCentreY()));
    	if(distance<=this.getRadius()) {
    		return true;
    	}
    	return false;
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
		Circle other = (Circle) obj;
		if (Double.doubleToLongBits(centreX) != Double.doubleToLongBits(other.centreX))
			return false;
		if (Double.doubleToLongBits(centreY) != Double.doubleToLongBits(other.centreY))
			return false;
		if (Double.doubleToLongBits(radius) != Double.doubleToLongBits(other.radius))
			return false;
		return true;
	}
	
    
}
