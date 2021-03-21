package teamproject.wipeout.engine.entity.gameover;

import javafx.scene.control.Label;
import teamproject.wipeout.util.resources.ResourceType;

public class GameOverUI extends Label {

    public GameOverUI() {
        super();

        this.setText("Game Over");

        this.setPrefWidth(150);

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "game-ui.css");
    }
}
