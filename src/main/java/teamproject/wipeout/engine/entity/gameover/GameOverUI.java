package teamproject.wipeout.engine.entity.gameover;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.util.resources.ResourceType;

import java.util.*;

public class GameOverUI extends VBox {

    HashMap<Integer, Player> players;
    private ListView<String> list;

    class Sortbymoney implements Comparator<Player> {
        // Used for sorting in descending order of
        // money number
        public int compare(Player a, Player b)
        {
            return (int)(a.getMoney() - b.getMoney());
        }
    }

    public GameOverUI(HashMap<Integer, Player> players) {
        super();
        this.players = players;

//        this.setText(players.size() + "");
//
//        this.setPrefWidth(150);
//
//        this.getStylesheets().add(ResourceType.STYLESHEET.path + "game-ui.css");
        list = new ListView<>();

        list.setMaxWidth(180);
        list.setMaxHeight(240);
        list.setMouseTransparent( true );
        list.setFocusTraversable( false );
        list.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        this.getChildren().addAll(list);
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    public void refreshText() {
        int i = 0;
        list.getItems().clear();
        ArrayList<Player> allPlayers = new ArrayList<>(this.players.values());
        Collections.sort(allPlayers, new Sortbymoney());
        for(Player player: this.players.values()) {
            list.getItems().add(player.playerName);
            i += 1;
        }
//        while(i < MAX_TASKS) {
//            list.getItems().add("");
//            i += 1;
//        }
//        this.setText(this.players.size() + "");
    }
}
