package teamproject.wipeout.engine.system;

import teamproject.wipeout.game.UI.ClockUI;

/**
 * The Clock System that is responsible for updating the ClockUI
 */
public class ClockSystem implements GameSystem {

    public final ClockUI clockUI;
    public final Long gameStartTime;

    /**
     * Initialise the clock system
     * @param duration - the duration of the game
     * @param startTime - the initial time
     * @param onEnd - closure to run when the time is up
     */
    public ClockSystem(double duration, long startTime, Runnable onEnd) {
        this.clockUI = new ClockUI(duration, onEnd);
        this.gameStartTime = startTime;
    }

    public double getTime() {
        return this.clockUI.getTime();
    }

    public void cleanup() {
        this.clockUI.restart();
    }

    public void accept(Double timeStep) {
        this.clockUI.showTime(timeStep);
    }

}
