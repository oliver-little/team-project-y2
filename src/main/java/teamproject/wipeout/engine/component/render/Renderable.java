package teamproject.wipeout.engine.component.render;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface representing any object that can be rendered to the game view.
 */
public interface Renderable {
    /**
     * Returns the width, in game units, of this Renderable
     * @return The width of this Renderable
     */
    public double getWidth();

    /**
     * Returns the height, in game units, of this Renderable
     * @return The height of this Renderable
     */
    public double getHeight();

    /**
     * Renders this renderable to the given GraphicsContext
     * 
     * @param gc The GraphicsContext to Render to
     * @param x The x position to render at
     * @param y The y position to render at
     * @param scale The scale to render this Renderable at (1 for normal size)
     */
    public void render(GraphicsContext gc, double x, double y, double scale);
}
