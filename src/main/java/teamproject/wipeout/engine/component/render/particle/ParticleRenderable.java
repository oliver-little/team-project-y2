package teamproject.wipeout.engine.component.render.particle;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import teamproject.wipeout.engine.component.render.Renderable;
import teamproject.wipeout.engine.component.render.particle.type.Particle;

/**
 * Renderable for particle systems - renders a list of particles to the screen every frame
 */
public class ParticleRenderable implements Renderable {

    public List<Particle> particles;

    private double width;
    private double height;

    /**
     * Creates a new instance of ParticleRenderable
     */
    public ParticleRenderable() {
        this.particles = new ArrayList<Particle>();
    }
    
    /**
     * Gets the width of the particle renderable
     * @return The width in game units
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * Sets the width of the particle renderable.
     * Used by ParticleSystem at each frame when bounds are recalculated
     * @param width The new width value
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Gets the height of the particle renderable
     * @return The height in game units
     */
    public double getHeight() {
        return this.height;
    }

    /**
     * Sets the height of the particle renderable.
     * Used by ParticleSystem at each frame when bounds are recalculated
     * @param height The new height value
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Renders the particles in the list to the screen
     */
    public void render(GraphicsContext gc, double x, double y, double scale) {
        for (Particle particle : particles) {
            particle.render(gc, x, y, scale);
        }
    }
}
