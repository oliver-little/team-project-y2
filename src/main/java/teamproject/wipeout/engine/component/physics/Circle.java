package teamproject.wipeout.engine.component.physics;

import javafx.geometry.Point2D;

public class Circle extends Shape
{

	private double centreX;
	private double centreY;
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
	
    
}
