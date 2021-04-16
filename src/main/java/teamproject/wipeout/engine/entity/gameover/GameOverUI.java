package teamproject.wipeout.engine.entity.gameover;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import teamproject.wipeout.StartMenu;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.util.Networker;
import teamproject.wipeout.util.resources.ResourceType;

import java.util.Comparator;
import java.util.List;

public class GameOverUI extends VBox {

    List<Player> players;

    private final ListView<String> list;

    public Networker networker;

    public StackPane root;

    class SortByMoney implements Comparator<Player> {
        // Used for sorting in descending order of
        // money number
        public int compare(Player a, Player b)
        {
            return (int)(a.getMoney() - b.getMoney());
        }
    }

    public GameOverUI(List<Player> players) {
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
            this.list.getItems().add(player.playerName + " " + "$" + player.getMoney());
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

        StartMenu startMenu = new StartMenu();
        Window window = root.getScene().getWindow();
        Parent content = startMenu.getContent();
        root.getScene().setRoot(content);
    }
}
