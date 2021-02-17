package teamproject.wipeout.engine.component.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import teamproject.wipeout.engine.component.GameComponent;

public class CircleRenderComponent implements GameComponent, Renderable {

    public Paint color;
    public float width;
    public float height;

    public CircleRenderComponent() {
        this.color = Color.BLACK;
        this.width = 10;
        this.height = 10;
    }

    public CircleRenderComponent(Paint color, float width, float height) {
        this.color = color;
        this.width = width;
        this.height = height;
    }

    public void render(GraphicsContext gc, double x, double y) {
        gc.setFill(this.color);
        gc.fillOval(x, y, this.width, this.height);
    }

    public String getType() {
        return "render-circle";
    }

    @Override
    public double getWidth() {
        return this.width;
    }

    @Override
    public double getHeight() {
        return this.height;
    }

    @Override
    public void render(GraphicsContext gc, double x, double y, double scale) {
        gc.setFill(this.color);
        gc.fillOval(x, y, this.width, this.height);
    }
}