package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

/** 
 * Renderable to display a Rectangle
 */
public class RectRenderable implements Renderable {

    public Paint color;
    public double width;
    public double height;
    public double radius;
    public double alpha;
    public Point2D offset;

    /**
     * Creates an instance of RectRenderable with a given color, width and height
     * 
     * @param color The color of this Rectangle
     * @param width The width of this Rectangle, in game units
     * @param height The height of this Rectangle, in game units
     */
    public RectRenderable(Paint color, double width, double height) {
        this.color = color;
        this.width = width;
        this.height = height;
        this.radius = 0.0;
        this.alpha = 1.0;
        this.offset = Point2D.ZERO;
    } 

    /**
     * Creates an instance of RectRenderable
     * @param color The color of this Rectangle
     * @param width The width of this Rectangle, in game units
     * @param height The height of this Rectangle, in game units
     * @param offset An offset position to display at, from the top left of this object
     */
    public RectRenderable(Paint color, double width, double height, Point2D offset) {
        this.color = color;
        this.width = width;
        this.height = height;
        this.radius = 0.0;
        this.alpha = 1.0;
        this.offset = offset;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public void render(GraphicsContext gc, double x, double y, double scale) {
        double defaultAlpha = gc.getGlobalAlpha();

        gc.setFill(this.color);
        gc.setGlobalAlpha(this.alpha);

        double xCoordinate = (x + offset.getX()) * scale;
        double yCoordinate = (y + offset.getY()) * scale;

        gc.fillRoundRect(xCoordinate, yCoordinate, this.width * scale, this.height * scale, this.radius, this.radius);

        gc.setGlobalAlpha(defaultAlpha);
    }

}