package teamproject.wipeout.engine.entity.gameclock;

import teamproject.wipeout.engine.system.GameSystem;

public class ClockSystem implements GameSystem {

    public final ClockUI clockUI;
    public final Long gameStartTime;

    private double timeDifference;

    public ClockSystem(Double time) {
        this.clockUI = new ClockUI(time);
        this.gameStartTime = System.currentTimeMillis();
        this.timeDifference = 0.0;
    }

    public void cleanup() {
        this.clockUI.restart();
    }

    public void accept(Double timeStep) {
        if (this.timeDifference != 0.0) {
            this.clockUI.showTime(timeStep + this.timeDifference);
            this.timeDifference = 0.0;
            return;
        }
        this.clockUI.showTime(timeStep);
    }

    public void setTimeDifference(double newDiff) {
        this.timeDifference = newDiff / 1000.0; // convert it to ms from s
    }

}
