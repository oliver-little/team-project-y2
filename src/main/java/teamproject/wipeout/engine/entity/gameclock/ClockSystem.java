package teamproject.wipeout.engine.entity.gameclock;

import teamproject.wipeout.engine.entity.gameover.GameOverUI;
import teamproject.wipeout.engine.system.GameSystem;
import teamproject.wipeout.game.player.Player;

import java.util.List;

public class ClockSystem implements GameSystem {

    public final ClockUI clockUI;
    public final GameOverUI gameOverUI;
    public final Long gameStartTime;

    public ClockSystem(double duration, long startTime, List<Player> players) {
        this.gameOverUI = new GameOverUI(players);
        this.clockUI = new ClockUI(duration, gameOverUI);
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
