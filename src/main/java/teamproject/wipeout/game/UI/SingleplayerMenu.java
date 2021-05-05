package teamproject.wipeout.game.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import teamproject.wipeout.networking.data.InitContainer;

import java.util.Arrays;

public class SingleplayerMenu {

    private final StartMenu parentMenu;
    private final VBox menuBox;
    private final Runnable backToMainMenu;

    public SingleplayerMenu(StartMenu parentMenu, VBox menuBox) {
        this.parentMenu = parentMenu;
        this.menuBox = menuBox;
        this.backToMainMenu = () -> this.parentMenu.createMainMenu();
    }

    public Runnable getMenu() {
        return () -> this.createSingleplayerMenu();
    }

    /**
     * Creates the UI for starting a singleplayer game
     */
    private void createSingleplayerMenu() {
        menuBox.getChildren().clear();

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setMaxSize(600, 100);
        vBox.getStyleClass().add("tile-pane");

        menuBox.getChildren().addAll(UIUtil.createTitle("Singleplayer"));

        GameModeUI gameModeBox = new GameModeUI();
        vBox.getChildren().add(gameModeBox);

        Runnable startGameAction = () -> {
            GameMode gameMode = gameModeBox.getGameMode();
            long gameModeValue = (long) gameModeBox.getValue();
            InitContainer initContainer = new InitContainer(gameMode, gameModeValue, null, null, null);
            this.parentMenu.startLocalGame(null, null, null, initContainer);
        };

        VBox startBox = UIUtil.createMenu(Arrays.asList(new Pair<String, Runnable>("Start Game", startGameAction)));
        startBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(startBox);

        VBox backBox = UIUtil.createMenu(Arrays.asList(new Pair<String, Runnable>("Back", this.backToMainMenu)));
        menuBox.getChildren().addAll(vBox, backBox);
    }

}