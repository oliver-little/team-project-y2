package teamproject.wipeout.engine.entity.gameclock;

import teamproject.wipeout.engine.system.GameSystem;

public class ClockSystem implements GameSystem {

    public final ClockUI clockUI;
    public final Long gameStartTime;

    public ClockSystem(double duration, long startTime) {
        this.clockUI = new ClockUI(duration);
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
