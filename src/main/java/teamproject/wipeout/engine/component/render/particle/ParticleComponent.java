package teamproject.wipeout.engine.component.render.particle;

import teamproject.wipeout.engine.component.GameComponent;

/**
 * Represents a Particle Effect on a GameEntity
 * Add this and call play to run the effect at the entity's position
 */
public class ParticleComponent implements GameComponent {

    public ParticleParameters parameters;
    public double time;
    public double lastEmissionTime;
    public int nextBurst;

    private boolean playing;
    
    /**
     * Creates a new instance of ParticleComponent
     * @param parameters The ParticleParameters object to reference for how the effect should look
     */
    public ParticleComponent(ParticleParameters parameters) {
        this.parameters = parameters;
        this.playing = false;
    }

    /**
     * Plays the particle effect. Call again to restart playback immediately
     */
    public void play() {
        this.playing = true;
        this.time = 0;
        this.lastEmissionTime = this.time;
        this.nextBurst = 0;
    }

    /**
     * Stops playback immediately - should only be called by ParticleSystem or particles will not clean up correctly
     */
    public void stop() {
        this.playing = false;
    }

    /**
     * Whether this ParticleComponent is playing
     * @return A boolean representing whether this effect is playing
     */
    public boolean isPlaying() {
        return this.playing;
    }

    public String getType() {
        return "particle";
    }
}
