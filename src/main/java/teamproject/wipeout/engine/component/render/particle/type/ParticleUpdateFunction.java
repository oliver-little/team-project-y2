package teamproject.wipeout.engine.component.render.particle.type;

/**
 * Interface for "over time" effects on particles - i.e.: effects that apply over the course of a particle's lifetime
 */
@FunctionalInterface
public interface ParticleUpdateFunction {
    public void update(Particle p, double percentage, double timeStep);
}
