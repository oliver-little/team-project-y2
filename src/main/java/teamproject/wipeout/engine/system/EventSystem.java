package teamproject.wipeout.engine.system;

/**
 * Interface for all systems in the game that don't need to be called every frame, but need to interact with entities in some way
 */
public interface EventSystem {
    /**
     * Cleans up dependencies of the GameSystem when the game shuts down
     */
    public void cleanup();
}