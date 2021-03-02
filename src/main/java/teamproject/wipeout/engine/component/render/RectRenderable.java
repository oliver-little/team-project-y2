package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/** 
 * Renderable to display a Rectangle
 */
public class RectRenderable implements Renderable {

    public Paint color;
    public float width;
    public float height;
    public Point2D offset;

    /**
     * Creates an instance of RectRenderable with default parameters
     * 
     */
    public RectRenderable() {
        this.color = Color.BLACK;
        this.width = 10;
        this.height = 10;
        this.offset = Point2D.ZERO;
    }

    /**
     * Creates an instance of RectRenderable with a given color, width and height
     * 
     * @param color The color of this Rectangle
     * @param width The width of this Rectangle, in game units
     * @param height The height of this Rectangle, in game units
     */
    public RectRenderable(Paint color, float width, float height) {
        this.color = color;
        this.width = width;
        this.height = height;
        this.offset = Point2D.ZERO;
    } 

    /**
     * Creates an instance of RectRenderable
     * @param color The color of this Rectangle
     * @param width The width of this Rectangle, in game units
     * @param height The height of this Rectangle, in game units
     * @param offset An offset position to display at, from the top left of this object
     */
    public RectRenderable(Paint color, float width, float height, Point2D offset) {
        this.color = color;
        this.width = width;
        this.height = height;
        this.offset = offset;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public void render(GraphicsContext gc, double x, double y, double scale) {
        gc.setFill(this.color);

        
        if (scale != 1) {
            gc.fillRect((x + offset.getX()) * scale, (y + offset.getY()) * scale, this.width * scale, this.height * scale);
        }
        else {
            gc.fillRect(x + offset.getX(), y + offset.getY(), this.width, this.height);
        }
    }
}