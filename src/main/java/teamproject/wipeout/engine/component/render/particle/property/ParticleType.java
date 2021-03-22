package teamproject.wipeout.engine.component.render.particle.property;

import teamproject.wipeout.engine.component.render.particle.type.ParticleRender;

/**
 * Interface representing a type of particle that can be emitted by a Particle Effect
 */
public interface ParticleType {
    /**
     * A factory function used by the ParticleRenderable to actually render the individual particle to the screen.
     * @return The ParticleRender interface implementation
     */
    public ParticleRender renderFactory();
}
