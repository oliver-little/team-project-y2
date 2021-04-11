package teamproject.wipeout.engine.entity.gameclock;

import teamproject.wipeout.engine.entity.gameover.GameOverUI;
import teamproject.wipeout.engine.system.GameSystem;
import teamproject.wipeout.game.player.Player;

import java.util.HashMap;

public class ClockSystem implements GameSystem {

    public final ClockUI clockUI;
    public final GameOverUI gameOverUI;
    public final Long gameStartTime;

    private double timeDifference;

    public ClockSystem(double duration, long startTime, HashMap<Integer, Player> players) {
        this.gameOverUI = new GameOverUI(players);
        this.clockUI = new ClockUI(duration, gameOverUI);
        this.gameStartTime = startTime;
        this.timeDifference = 0.0;
    }

    public void cleanup() {
        this.clockUI.restart();
    }

    public void accept(Double timeStep) {
        this.clockUI.showTime(timeStep);
    }

}
