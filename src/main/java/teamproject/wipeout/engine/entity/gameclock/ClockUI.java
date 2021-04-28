package teamproject.wipeout.engine.entity.gameclock;

import javafx.scene.control.Label;
import teamproject.wipeout.engine.entity.gameover.GameOverUI;
import teamproject.wipeout.util.resources.ResourceType;

public class ClockUI extends Label {

    public Runnable onEnd;

    private boolean gameEnded;
    private Double time;
    private final Double initialTime;

    public ClockUI(double time, Runnable onEnd) {
        super();
        this.onEnd = onEnd;
        this.gameEnded = false;

        this.time = time;
        this.initialTime = time;
        int min = (int)(this.time / 60);
        String seconds = String.format("%02d", (int)(this.time % 60));
        this.setText("Remaining Time: " + min + ":" + seconds);

        this.setPrefWidth(150);

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "game-ui.css");
    }

    public Double getTime() {
        return this.time;
    }

    public boolean getGameEnded() {
        return this.gameEnded;
    }

    public void showTime(Double timestep) {
        if (gameEnded) {
            return;
        }

        this.time = Math.max(0, this.time - timestep);
        if (this.time < 1.0) {
            this.onEnd.run();
            this.gameEnded = true;
        }
        int min = (int)(this.time / 60);
        String seconds = String.format("%02d", (int)(this.time % 60));
        this.setText("Remaining Time: " + min + ":" + seconds);
    }

    public void restart(){
        this.time = this.initialTime;
    }
}
