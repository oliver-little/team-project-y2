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

        double xCoordinate = x * scale;
        double yCoordinate = y * scale;

        gc.fillRoundRect(xCoordinate, yCoordinate, this.width * scale, this.height * scale, this.radius, this.radius);

        gc.setGlobalAlpha(defaultAlpha);
    }

}