package teamproject.wipeout.engine.component.render.particle;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import teamproject.wipeout.engine.component.render.Renderable;
import teamproject.wipeout.engine.component.render.particle.type.Particle;

public class ParticleRenderable implements Renderable {

    public List<Particle> particles;

    private double width;
    private double height;

    public ParticleRenderable() {
        this.particles = new ArrayList<Particle>();
    }
    
    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public void render(GraphicsContext gc, double x, double y, double scale) {
        for (Particle particle : particles) {
            particle.render(gc, x, y, scale);
        }
    }
}
