package teamproject.wipeout.engine.component.render;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface representing any object that can be rendered to the game view.
 */
public interface Renderable {
    public void render(GraphicsContext gc, double x, double y);
}
