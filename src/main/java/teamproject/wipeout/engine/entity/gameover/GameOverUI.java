package teamproject.wipeout.engine.entity.gameover;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import teamproject.wipeout.game.UI.Leaderboard;
import teamproject.wipeout.game.UI.StartMenu;
import teamproject.wipeout.game.UI.UIUtil;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.Networker;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.util.resources.ResourceType;

import java.util.List;

/**
 * Creates the Game Over UI that is displayed once the game finishes
 */
public class GameOverUI extends StackPane {

    private final StackPane root;
    private final Networker networker;
    private final Runnable onClose;

    private final List<Player> players;
    private final Text winner;
    private final Leaderboard leaderboard;

    /**
     * Initializes the GameOverUI
     * @param root - in order to help us return to start menu
     * @param networker - the networker responsible for helping us retrieve the current player's name, and stop the server once the game ends
     * @param players - for creating the leaderboard shown at the end
     * @param onClose - a closure that is run once the player wants to return to start menu
     */
    public GameOverUI(StackPane root, Networker networker, List<Player> players, Runnable onClose) {
        super();
        this.root = root;
        this.networker = networker;
        this.players = players;
        this.onClose = onClose;

        VBox content = new VBox(2);
        content.setAlignment(Pos.CENTER);
        this.getStylesheets().add(ResourceType.STYLESHEET.path + "task-ui.css");

        winner = UIUtil.createTitle("");
        winner.setFont(Font.font("Kalam", 40));

        Text title = UIUtil.createTitle("Game Over!");
        title.setFont(Font.font("Kalam", 80));

        leaderboard = new Leaderboard(players);
        leaderboard.setAlignment(Pos.CENTER);

        Button closeButton = new Button("Go back to menu");
        closeButton.setOnAction(e -> this.endGame());

        Region titleSpacer = new Region();
        titleSpacer.setPrefHeight(40);
        VBox.setVgrow(titleSpacer, Priority.ALWAYS);

        Region subtitleSpacer = new Region();
        subtitleSpacer.setPrefHeight(40);
        VBox.setVgrow(subtitleSpacer, Priority.ALWAYS);

        content.getChildren().addAll(title, titleSpacer, winner, subtitleSpacer, leaderboard, closeButton);
        content.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        this.setBackground(new Background(new BackgroundFill(new Color(0, 0, 0, 0.75), CornerRadii.EMPTY, Insets.EMPTY)));
        this.getChildren().addAll(content);

        this.refreshText();

        // Setup animation
        this.setOpacity(0);
        winner.setOpacity(0);
        leaderboard.setOpacity(0);
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

        FadeTransition listFade = new FadeTransition(Duration.seconds(0.5), leaderboard);
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

    /**
     * Shows the text of who wins
     */
    public void refreshText() {
        List<Player> sortedPlayers = this.leaderboard.update(this.players);

        String winnerName = sortedPlayers.get(0).playerName;
        String winString = winnerName + " wins!";
        if (winnerName.equals(this.getCurrentPlayerName())) {
            winString = "You win!";
        }

        winner.setText(winString);
    }

    /**
     * When the player clicks on return to start menu
     */
    public void endGame() {
        if (this.networker != null && !this.networker.stopServer()) {
            GameClient client = this.networker.getClient();
            if (client != null) {
                client.closeConnection(true);
            }
        }

        this.onClose.run();

        StartMenu startMenu = new StartMenu();
        this.root.getScene().setRoot(startMenu.getContent());
    }

    /**
     * Get current player's name
     * @return - player's name
     */
    private String getCurrentPlayerName() {
        if (this.networker == null) {
            return CurrentPlayer.DEFAULT_NAME;
        }

        GameClient client = this.networker.getClient();
        if (client == null) {
            return CurrentPlayer.DEFAULT_NAME;
        } else {
            return client.clientName;
        }
    }
}
