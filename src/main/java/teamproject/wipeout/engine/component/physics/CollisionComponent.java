package teamproject.wipeout.engine.component.physics;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import teamproject.wipeout.engine.component.GameComponent;

public class CollisionComponent implements GameComponent {

	//each rectangle with attributes x, y, width, height
	// x = horizontal offset from top left corner
	// y = vertical offset from top left corner
	/**
	 * Array of rectangles that are given collision property.
	 * x,y coords of rectangle represent offset from entities top left corner.
	 * width and height of rectangle are the dimensions of the bounding box.
	 */
	public Rectangle boundingBoxes[]; 
	
	/*
	public CollisionComponent(Shape[] boundingBoxes) {
		this.boundingBoxes=boundingBoxes;
	}
	*/
	
	//varargs constructor. See https://www.baeldung.com/java-varargs for info
	public CollisionComponent(Rectangle... rectangles) {
		this.boundingBoxes = rectangles;
	}
	
    public String getType() {
        return "collision";
    }
}

