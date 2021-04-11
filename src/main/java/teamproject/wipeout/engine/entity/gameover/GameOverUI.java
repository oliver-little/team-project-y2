package teamproject.wipeout.engine.entity.gameover;

import javafx.scene.control.Label;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.util.resources.ResourceType;

import java.util.HashMap;

public class GameOverUI extends Label {

    public GameOverUI(HashMap<Integer, Player> players) {
        super();

        this.setText("Game Over");

        this.setPrefWidth(150);

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "game-ui.css");
    }
}
