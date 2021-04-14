package teamproject.wipeout.engine.entity.gameover;

import javafx.scene.control.Label;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.util.resources.ResourceType;

import java.util.HashMap;

public class GameOverUI extends Label {

    HashMap<Integer, Player> players;
    public GameOverUI(HashMap<Integer, Player> players) {
        super();
        this.players = players;
        this.setText(players.size() + "");

        this.setPrefWidth(150);

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "game-ui.css");
    }

    public void refreshText() {
        this.setText(this.players.size() + "");
    }
}
