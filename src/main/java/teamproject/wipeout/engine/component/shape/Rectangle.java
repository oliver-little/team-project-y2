package teamproject.wipeout.engine.component.shape;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.shape.Shape;

public class Rectangle extends Shape
{
	private double x;
	private double y;
	private double width;
	private double height;
	
	public Rectangle(double width, double height) {
		this.x=0;
		this.y=0;
		this.width=width;
		this.height=height;
	}
	
	public Rectangle(double x, double y, double width, double height) {
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
	}
	
	public Rectangle(Point2D xy, double width, double height) {
		this.x = xy.getX();
		this.y = xy.getY();
		this.width=width;
		this.height=height;
	}
	
	public double getX()
	{
		return x;
	}

	public void setX(double x)
	{
		this.x = x;
	}

	public double getY()
	{
		return y;
	}

	public void setY(double y)
	{
		this.y = y;
	}

	public double getWidth()
	{
		return width;
	}

	public void setWidth(double width)
	{
		this.width = width;
	}

	public double getHeight()
	{
		return height;
	}

	public void setHeight(double height)
	{
		this.height = height;
	}

	/**
     * Checks whether a point is inside a rectangle (inclusive of edges)
     * @param p point to check
     * @return true if point is inside rectangle, false otherwise.
     */
    public boolean contains(Point2D p) {
    	//check point beyond top left corner of rectangle
    	if(p.getX()>=this.getX() && p.getY()>=this.getY()) {
    		//check point before bottom right corner of rectangle
        	if(p.getX()<=(this.getX()+this.getWidth()) && p.getY()<=(this.getY()) + this.getHeight()) {
        		return true;
        	}
    	}
    	
    	return false;
    }

}
