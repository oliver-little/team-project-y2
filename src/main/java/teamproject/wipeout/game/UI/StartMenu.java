package teamproject.wipeout.game.UI;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import teamproject.wipeout.Controller;
import teamproject.wipeout.Gameplay;
import teamproject.wipeout.game.market.ui.ErrorUI;
import teamproject.wipeout.networking.Networker;
import teamproject.wipeout.networking.data.InitContainer;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/**
 * StartMenu is a class which is used for creating and setting up the start menu of the game.
 * It implements the Controller Interface.
 */
public class StartMenu extends StackPane implements Controller {

    private final VBox menuBox;

    private Text title;
    private VBox buttonBox;

    private final SingleplayerMenu singleplayerMenu;
    private final MultiplayerMenu multiplayerMenu;
    private final SettingsMenu settingsMenu;

    /**
     * Creates a new instance of StartMenu
     */
    public StartMenu() {
        super();
        this.menuBox = new VBox(30);

        this.singleplayerMenu = new SingleplayerMenu(this, this.menuBox);
        this.multiplayerMenu = new MultiplayerMenu(this, this.menuBox);
        this.settingsMenu = new SettingsMenu(this.menuBox, () -> this.createMainMenu());
    }

    /**
     * Cleans up active threads when StartMenu is closed.
     */
    public void cleanup() {
        this.multiplayerMenu.cleanupNetworker();
    }

    /**
     * Displays an error message (usually if a game ends unexpectedly or does not start correctly)
     */
    public void createMainMenu() {
        this.getChildren().remove(menuBox);

        menuBox.getChildren().clear();

        title = UIUtil.createTitle("Farmageddon");
        menuBox.getChildren().addAll(title);

        buttonBox = UIUtil.createMenu(getMainMenuData());
        menuBox.getChildren().add(buttonBox);

        this.getChildren().add(menuBox);

    }

    public void startLocalGame(Networker givenNetworker, String chosenName, Long gameStartTime, InitContainer initContainer) {
        Gameplay game = new Gameplay(givenNetworker, gameStartTime, initContainer, chosenName, this.settingsMenu.getKeyBindings());

        Parent content = game.getParentWith(this.getScene().getWindow());

        this.getScene().setRoot(content);
        game.createContent();
    }

    private List<Pair<String, Runnable>> getMainMenuData() {
        List<Pair<String, Runnable>> menuData = Arrays.asList(
                // (creating content is called separately after so InputHandler has a scene to add listeners to.)
                new Pair<String, Runnable>("Singleplayer", this.singleplayerMenu.getMenu()),
                new Pair<String, Runnable>("Multiplayer", this.multiplayerMenu.getMenu()),
                new Pair<String, Runnable>("How to Play", this.settingsMenu.getMenu()),
                new Pair<String, Runnable>("Exit to Desktop", Platform::exit)
        );

        return menuData;
    }

    public void disconnectError() {
        menuBox.getChildren().clear();

        StackPane errorBox = new StackPane();
        new ErrorUI(errorBox, "Error: Game server connection issue", () -> this.createMainMenu());
        menuBox.getChildren().add(errorBox);
    }

    /**
     * A method to animate the menu items.
     */
    private void startAnimation() {
        double titleDuration = 0.5;
        double buttonDuration = 0.5;

        // Set initial values
        title.setScaleX(2);
        title.setScaleY(2);
        buttonBox.setOpacity(0);

        ScaleTransition st = new ScaleTransition(Duration.seconds(titleDuration), title);
        st.setFromX(2);
        st.setFromY(2);
        st.setToX(1);
        st.setToY(1);

        FadeTransition ft = new FadeTransition(Duration.seconds(buttonDuration), buttonBox);
        ft.setFromValue(0);
        ft.setToValue(1);

        SequentialTransition full = new SequentialTransition(st, ft);
        full.setDelay(Duration.seconds(1.25));
        full.setInterpolator(Interpolator.EASE_BOTH);
        full.play();
    }

    /**
     * Creates the content to be rendered onto the canvas.
     */
    private void createContent() {
        this.setPrefSize(800, 600);

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "start-menu.css");

        FileInputStream imgFile = null;
        try {
            imgFile = new FileInputStream(ResourceLoader.get(ResourceType.UI, "background.png"));
            ImageView imageView = UIUtil.createBackground(imgFile, this);
            this.getChildren().add(imageView);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        menuBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(menuBox, Pos.CENTER);

        createMainMenu();
        startAnimation();
    }

    /**
     * Creates the content of the menu and then gets the root node of this class.
     *
     * @return StackPane (root) which contains all the menu components in the scene graph.
     */
	@Override
	public Parent getContent() {
		this.createContent();
		return this;
	}
}