package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class RectRenderable implements Renderable {

    public Paint color;
    public float width;
    public float height;
    public Point2D offset;

    public RectRenderable() {
        this.color = Color.BLACK;
        this.width = 10;
        this.height = 10;
        this.offset = Point2D.ZERO;
    }

    public RectRenderable(Paint color, float width, float height) {
        this.color = color;
        this.width = width;
        this.height = height;
        this.offset = Point2D.ZERO;
    } 

    public RectRenderable(Paint color, float width, float height, Point2D offset) {
        this.color = color;
        this.width = width;
        this.height = height;
        this.offset = offset;
    }

    public void render(GraphicsContext gc, double x, double y) {
        gc.setFill(this.color);
        gc.fillRect(x + offset.getX(), y + offset.getY(), this.width, this.height);
    }

}