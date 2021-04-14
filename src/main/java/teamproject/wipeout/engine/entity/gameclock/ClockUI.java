package teamproject.wipeout.engine.entity.gameclock;

import javafx.scene.control.Label;
import teamproject.wipeout.engine.entity.gameover.GameOverUI;
import teamproject.wipeout.util.resources.ResourceType;

public class ClockUI extends Label {

    public GameOverUI gameOverUI;
    private Double time;
    private final Double INITIAL_TIME;

    public ClockUI(Double time, GameOverUI gameOverUI) {
        super();

        this.time = time;
        this.gameOverUI = gameOverUI;
        this.INITIAL_TIME = time;
        int min = (int)(this.time / 60);
        String seconds = String.format("%02d", (int)(this.time % 60));
        this.setText("Remaining Time: " + min + ":" + seconds);

        this.setPrefWidth(150);

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "game-ui.css");
    }

    public void showTime(Double timestep) {
        this.time = Math.max(0, this.time - timestep);
        if(this.time < 1.0) {
            this.gameOverUI.setVisible(true);
            this.gameOverUI.refreshText();
//            System.out.println("Game over");
        }
        int min = (int)(this.time / 60);
        String seconds = String.format("%02d", (int)(this.time % 60));
        this.setText("Remaining Time: " + min + ":" + seconds);
    }

    public void restart(){
        this.time = this.INITIAL_TIME;
    }
}
