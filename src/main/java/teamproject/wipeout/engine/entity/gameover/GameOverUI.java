package teamproject.wipeout.engine.entity.gameover;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import teamproject.wipeout.StartMenu;
import teamproject.wipeout.UIUtil;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.util.Networker;
import teamproject.wipeout.util.resources.ResourceType;

import java.util.Comparator;
import java.util.List;

public class GameOverUI extends StackPane {

    public static final String[] ORDINAL_STRINGS = new String[]{"1st", "2nd", "3rd", "4th", "5th", "6th"};

    private final StackPane root;
    private final Networker networker;
    private final Runnable onClose;

    private final VBox content;
    private final List<Player> players;
    private final Text winner;
    private final ListView<String> list;

    static class SortByMoney implements Comparator<Player> {
        // Used for sorting in descending order of money number
        public int compare(Player a, Player b)
        {
            return (int)(a.getMoney() - b.getMoney());
        }
    }

    public GameOverUI(StackPane root, Networker networker, List<Player> players, Runnable onClose) {
        super();
        this.root = root;
        this.networker = networker;
        this.onClose = onClose;

        this.players = players;
        this.content = new VBox(2);
        this.content.setAlignment(Pos.CENTER);
        this.list = new ListView<>();
        this.getStylesheets().add(ResourceType.STYLESHEET.path + "task-ui.css");

        winner = UIUtil.createTitle("");
        winner.setFont(Font.font("Kalam", 40));

        Text title = UIUtil.createTitle("Game Over!");
        title.setFont(Font.font("Kalam", 80));

        list.setMaxWidth(180);
        list.setMaxHeight(100);
        list.setMouseTransparent( true );
        list.setFocusTraversable( false );
        list.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        Button closeButton = new Button("Go back to menu");
        closeButton.setOnAction(e -> this.endGame());

        Region titleSpacer = new Region();
        titleSpacer.setPrefHeight(40);
        VBox.setVgrow(titleSpacer, Priority.ALWAYS);

        Region subtitleSpacer = new Region();
        subtitleSpacer.setPrefHeight(40);
        VBox.setVgrow(subtitleSpacer, Priority.ALWAYS);

        content.getChildren().addAll(title, titleSpacer, winner, subtitleSpacer, list, closeButton);
        content.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        this.setBackground(new Background(new BackgroundFill(new Color(0, 0, 0, 0.75), CornerRadii.EMPTY, Insets.EMPTY)));
        this.getChildren().addAll(content);

        this.refreshText();

        // Setup animation
        this.setOpacity(0);
        winner.setOpacity(0);
        list.setOpacity(0);
        closeButton.setOpacity(0);

        FadeTransition bgFade = new FadeTransition(Duration.seconds(0.5), this);
        bgFade.setFromValue(0);
        bgFade.setToValue(1);
        bgFade.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition winnerFade = new FadeTransition(Duration.seconds(0.5), winner);
        winnerFade.setDelay(Duration.seconds(0.5));
        winnerFade.setFromValue(0);
        winnerFade.setToValue(1);
        winnerFade.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition listFade = new FadeTransition(Duration.seconds(0.5), list);
        listFade.setFromValue(0);
        listFade.setToValue(1);
        listFade.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition buttonFade = new FadeTransition(Duration.seconds(0.5), closeButton);
        buttonFade.setFromValue(0);
        buttonFade.setToValue(1);
        buttonFade.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition pt = new ParallelTransition(listFade, buttonFade);
        pt.setDelay(Duration.seconds(0.5));

        SequentialTransition st = new SequentialTransition(bgFade, winnerFade, pt);
        st.play();
    }

    public void refreshText() {
        this.list.getItems().clear();
        this.players.sort(new SortByMoney().reversed());

        String winnerName = this.players.get(0).playerName;
        String winString = winnerName + " wins!";
        if (winnerName.equals(this.getCurrentPlayerName())) {
            winString = "You win!";
        }

        winner.setText(winString);

        for (int i = 0; i < this.players.size(); i++) {
            Player player = this.players.get(i);
            this.list.getItems().add(ORDINAL_STRINGS[i] + ": " + player.playerName + " " + "$" + String.format("%.2f",  player.moneyProperty().getValue()));
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

        this.onClose.run();

        StartMenu startMenu = new StartMenu();
        this.root.getScene().setRoot(startMenu.getContent());
    }

    private String getCurrentPlayerName() {
        GameClient client = this.networker.getClient();
        if (client == null) {
            return CurrentPlayer.DEFAULT_NAME;
        } else {
            return client.clientName;
        }
    }
}
