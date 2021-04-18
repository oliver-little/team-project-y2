package teamproject.wipeout.engine.entity.gameover;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import teamproject.wipeout.StartMenu;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.util.Networker;
import teamproject.wipeout.util.resources.ResourceType;

import java.util.Comparator;
import java.util.List;

public class GameOverUI extends VBox {

    private final StackPane root;
    private final Networker networker;
    private final Runnable onEnd;

    private final List<Player> players;
    private final ListView<String> list;

    static class SortByMoney implements Comparator<Player> {
        // Used for sorting in descending order of money number
        public int compare(Player a, Player b)
        {
            return (int)(a.getMoney() - b.getMoney());
        }
    }

    public GameOverUI(StackPane root, Networker networker, List<Player> players, Runnable onEnd) {
        super();
        this.root = root;
        this.networker = networker;
        this.onEnd = onEnd;

        this.players = players;
        list = new ListView<>();

        list.setMaxWidth(180);
        list.setMaxHeight(100);
        list.setMouseTransparent( true );
        list.setFocusTraversable( false );
        list.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        this.setSpacing(2);

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "task-ui.css");

        Button closeButton = new Button("Go back to menu");
        closeButton.setOnAction(e -> {
            this.endGame();
        });

        this.getChildren().addAll(list, closeButton);
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    public void refreshText() {
        this.list.getItems().clear();
        this.players.sort(new SortByMoney().reversed());

        for (Player player : this.players) {
            this.list.getItems().add(player.playerName + " " + "$" + String.format("%.2f",  player.moneyProperty().getValue()));
        }
    }

    public void endGame() {
        if (this.networker != null) {
            GameClient client = this.networker.getClient();
            if (client != null) {
                client.closeConnection(true);
            }

            this.networker.stopServer();
        }

        this.onEnd.run();

        StartMenu startMenu = new StartMenu();
        this.root.getScene().setRoot(startMenu.getContent());
    }
}
