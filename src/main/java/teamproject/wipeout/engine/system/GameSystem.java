package teamproject.wipeout.engine.system;

import java.util.function.Consumer;

public interface GameSystem extends Consumer<Double> {

    public void cleanup();

    public void accept(Double timeStep);
}