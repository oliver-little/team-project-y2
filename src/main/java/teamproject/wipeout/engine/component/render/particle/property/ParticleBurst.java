package teamproject.wipeout.engine.component.render.particle.property;

import java.util.function.Supplier;

public class ParticleBurst {
    public double burstTime;
    public Supplier<Integer> burstAmount;

    public ParticleBurst(double burstTime, Supplier<Integer> burstAmount) {
        this.burstTime = burstTime;
        this.burstAmount = burstAmount;
    }
}
