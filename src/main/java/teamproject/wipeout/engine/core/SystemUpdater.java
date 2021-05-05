package teamproject.wipeout.engine.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import teamproject.wipeout.engine.system.GameSystem;

/**
 * Stores a list of GameSystems, and updates them at each timeStep in the GameLoop.
 */
public class SystemUpdater implements Consumer<Double> {

    private List<GameSystem> systems;
    
    /**
     * Creates a new instance of SystemUpdater
     */
    public SystemUpdater() {
        this.systems = new ArrayList<GameSystem>();
    }

    /**
     * Calls the cleanup function for every GameSystem this SystemUpdater controls
     */
    public void cleanup() {
        for (GameSystem system : this.systems) {
            system.cleanup();
        }
    }

    /**
     * Adds a new GameSystem to be updated each timeStep
     */
    public void addSystem(GameSystem g) {
        this.systems.add(g);
    }

    /**
     * Removes a GameSystem from this SystemUpdater
     * @return Whether the system was removed successfully
     */
    public boolean removeSystem(GameSystem g) {
        return this.systems.remove(g);
    }

    /**
     * Called each timeStep - this calls the update function on each system in this SystemUpdater's control
     */
    public void accept(Double timeStep) {
        for (GameSystem g : this.systems) {
            g.accept(timeStep);
        }
    }
}
