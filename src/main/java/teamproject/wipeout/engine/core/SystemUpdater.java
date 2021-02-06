package teamproject.wipeout.engine.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import teamproject.wipeout.engine.system.GameSystem;

public class SystemUpdater implements Consumer<Double> {

    private List<GameSystem> _systems;
    
    public SystemUpdater() {
        this._systems = new ArrayList<GameSystem>();
    }

    public void cleanup() {
        for (GameSystem system : this._systems) {
            system.cleanup();
        }
    }

    public void addSystem(GameSystem g) {
        this._systems.add(g);
    }

    public boolean removeSystem(GameSystem g) {
        return this._systems.remove(g);
    }

    public void accept(Double timeStep) {
        for (GameSystem g : this._systems) {
            g.accept(timeStep);
        }
    }
}
