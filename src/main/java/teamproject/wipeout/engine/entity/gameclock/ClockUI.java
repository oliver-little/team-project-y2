package teamproject.wipeout.engine.entity.gameclock;

import javafx.scene.control.Label;
import teamproject.wipeout.engine.entity.gameover.GameOverUI;
import teamproject.wipeout.util.resources.ResourceType;

/**
 * Shows the Remaining time in a label in the top right of the screen
 */
public class ClockUI extends Label {

    public Runnable onEnd;

    private boolean gameEnded;
    private Double time;
    private final Double initialTime;

    /**
     * Initialises the Clock UI
     * @param time - the time to display in the label
     * @param onEnd - closure to run when the time is up
     */
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

    /**
     * Get the time displayed on the label
     * @return - displayed time
     */
    public Double getTime() {
        return this.time;
    }

    /**
     * Get the information regarding whether the game is still running
     * @return - a boolean for whether the game has ended or not
     */
    public boolean getGameEnded() {
        return this.gameEnded;
    }

    /**
     * Showing the time, updating the UI
     * @param timestep - the timeStep that has passed, with which we update the UI
     */
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
