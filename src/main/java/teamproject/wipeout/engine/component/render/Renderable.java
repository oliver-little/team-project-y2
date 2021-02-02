package teamproject.wipeout.engine.component.render;

import javafx.scene.canvas.GraphicsContext;

public interface Renderable {
    public void render(GraphicsContext gc, double x, double y);
}
