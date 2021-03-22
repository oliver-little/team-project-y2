package teamproject.wipeout.engine.component.render.particle.property;

import java.util.function.Supplier;

/**
 * Data container for particle burst effects
 */
public class ParticleBurst {
    public double burstTime;
    public Supplier<Integer> burstAmount;

    /**
     * Creates a new instance of ParticleBurst
     * @param burstTime The time for the burst to occur at (s)
     * @param burstAmount A generator for the amount of particles to create
     */
    public ParticleBurst(double burstTime, Supplier<Integer> burstAmount) {
        this.burstTime = burstTime;
        this.burstAmount = burstAmount;
    }
}
