package teamproject.wipeout.engine.core;

import java.util.function.Consumer;

import javafx.animation.AnimationTimer;

/**
 * GameLoop implements the core loop used by the game to update the renderer and GameSystems.
 */
public class GameLoop extends AnimationTimer {

    public static final double timeStep = 0.01666f;

    private Consumer<Double> update;
    private Consumer<Double> renderer;

    private double lastTime;
    private double accumulator;

    /**
     * Creates a new GameLoop object
     * @param {Consumer<Double>} A consumer object taking the previous frame time, which will update all logic systems
     * @param {Consumer<Double>} A consumer object taking the previous frame time, which will rerender the scene.
     */
    public GameLoop(Consumer<Double> update, Consumer<Double> renderer) {
        this.update = update;
        this.renderer = renderer;
    }

    /**
     * Called when the GameLoop begins
     */
    @Override
    public void start() {
        this.lastTime = System.nanoTime() / 1000000000.0;
        this.accumulator = 0;
        super.start();
    }

    /**
     * Called at every frame of the GameLoop - updates the renderer every frame and the other GameSystems every time step.
     */
    @Override
    public void handle(long currentTime) {        
        double newCurrentTime = (currentTime / 1000000000.0);
        this.accumulator += (newCurrentTime - this.lastTime);

        while (this.accumulator > timeStep) {
            this.update.accept(timeStep);
            this.accumulator -= timeStep;
        }

        this.renderer.accept(timeStep);
        this.lastTime = newCurrentTime;
    }

    /**
     * Called when the GameLoop stops
     */
    @Override
    public void stop() {
        this.lastTime = 0;
        this.accumulator = 0;
        super.stop();
    }
}
