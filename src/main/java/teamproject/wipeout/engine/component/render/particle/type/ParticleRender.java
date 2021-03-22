package teamproject.wipeout.engine.component.render.particle.type;

import javafx.scene.canvas.GraphicsContext;

@FunctionalInterface
/**
 * Provides a functional interface for the render function of particles to be defined
 */
public interface ParticleRender {
    /**
     * Renders a particle
     * 
     * @param gc The GraphicsContext to render to
     * @param x The x coordinate to offset rendering from
     * @param y The y coordinate to offset rendering from
     * @param scale The scale to render at
     */
    void render(GraphicsContext gc, double x, double y, double width, double height);
}
