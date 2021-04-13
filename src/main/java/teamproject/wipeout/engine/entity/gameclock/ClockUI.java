package teamproject.wipeout.engine.entity.gameclock;

import javafx.scene.control.Label;
import teamproject.wipeout.util.resources.ResourceType;

public class ClockUI extends Label {

    private Double time;
    private final Double initialTime;

    public ClockUI(double time) {
        super();

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

    public void showTime(Double timestep) {
        this.time = Math.max(0, this.time - timestep);
        int min = (int)(this.time / 60);
        String seconds = String.format("%02d", (int)(this.time % 60));
        this.setText("Remaining Time: " + min + ":" + seconds);
    }

    public void restart(){
        this.time = this.initialTime;
    }
}
