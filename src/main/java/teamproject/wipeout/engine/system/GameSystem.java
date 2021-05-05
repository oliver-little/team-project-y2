package teamproject.wipeout.engine.system;

import java.util.function.Consumer;

/**
 * Interface for all systems in the game that need to do something every frame.
 */
public interface GameSystem extends Consumer<Double> {

    /**
     * Cleans up dependencies of the GameSystem when the game shuts down
     */
    public void cleanup();

    /**
     * Called every frame - causes the system to perform one update step
     * @param timeStep The time between the this update cycle and the last (s)
     */
    public void accept(Double timeStep);
}