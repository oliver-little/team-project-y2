package teamproject.wipeout.engine.core;

import java.util.function.Consumer;

import javafx.animation.AnimationTimer;

public class GameLoop extends AnimationTimer {

    public static final double timeStep = 0.01666f;

    private Consumer<Double> _update;
    private Consumer<Double> _renderer;

    private double _lastTime;
    private double _accumulator;

    /**
     * Creates a new GameLoop object
     * @param {Consumer<Double>} A consumer object taking the previous frame time, which will update all logic systems
     * @param {Consumer<Double>} A consumer object taking the previous frame time, which will rerender the scene.
     */
    public GameLoop(Consumer<Double> update, Consumer<Double> renderer) {
        this._update = update;
        this._renderer = renderer;
    }

    @Override
    public void start() {
        this._lastTime = System.nanoTime() / 1000000000.0;
        this._accumulator = 0;
        super.start();
    }

    // TODO: frame interpolation
    @Override
    public void handle(long currentTime) {        
        double newCurrentTime = (currentTime / 1000000000.0);
        this._accumulator += (newCurrentTime - this._lastTime);

        while (this._accumulator > timeStep) {
            this._update.accept(timeStep);
            this._accumulator -= timeStep;
        }

        this._renderer.accept(timeStep);
        this._lastTime = newCurrentTime;
    }

    /**
     * double timeDelta = (currentTime - this._lastTime);
        System.out.printf("%f \n", timeDelta);
        timeDelta /= 1000000000.0;

        this._update.accept(timeDelta);
        this._renderer.accept(timeDelta);
        this._lastTime = currentTime;
     */

    @Override
    public void stop() {
        this._lastTime = 0;
        this._accumulator = 0;
        super.stop();
    }
}
