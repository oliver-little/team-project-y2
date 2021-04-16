package teamproject.wipeout.engine.entity.gameover;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import teamproject.wipeout.StartMenu;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.util.Networker;
import teamproject.wipeout.util.resources.ResourceType;

import java.io.IOException;
import java.util.*;

public class GameOverUI extends VBox {

    HashMap<Integer, Player> players;
    private Button closeButton;

    private ListView<String> list;

    public Networker networker;
    public GameScene gameScene;

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
        list = new ListView<>();

        list.setMaxWidth(180);
        list.setMaxHeight(100);
        list.setMouseTransparent( true );
        list.setFocusTraversable( false );
        list.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        this.setSpacing(2);

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "task-ui.css");

        closeButton = new Button("Finish game");
        closeButton.setOnAction(e -> {
            this.endGame();
        });

        this.getChildren().addAll(list, closeButton);
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    public void refreshText() {
        int i = 0;
        list.getItems().clear();
        ArrayList<Player> allPlayers = new ArrayList<>(this.players.values());
        Collections.sort(allPlayers, new Sortbymoney().reversed());
        for(Player player: this.players.values()) {
            list.getItems().add(player.playerName + " " + "$" + player.getMoney());
            i += 1;
        }
    }

    public void endGame() {
        GameClient client = this.networker.getClient();
        if (client != null) {
            System.out.println("Connection closed");
            client.closeConnection(true);
            if (!client.getIsActive()) {
                System.out.println("It's not active");
            }
        }

        this.networker.stopServer();
    }
}
