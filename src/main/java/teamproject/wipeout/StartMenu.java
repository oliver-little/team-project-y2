package teamproject.wipeout;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.geometry.Pos;
import teamproject.wipeout.networking.server.GameServer;
import teamproject.wipeout.util.Networker;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * StartMenu is a class which is used for creating and setting up the start menu of the game.
 * It implements the Controller Interface.
 */
public class StartMenu implements Controller {
    
    private Pane root = new StackPane();
    private VBox menuBox = new VBox(30);
    private VBox buttonBox;
    private Text title;

    Networker networker = new Networker();

    public void cleanup() {
        try {
            this.networker.getClient().closeConnection(true);
            this.networker.stopServer();

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void createMultiplayerMenu(){
        root.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        menuBox.getChildren().addAll(UIUtil.createTitle("Multiplayer"));

        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Join Game", () -> {createJoinGameMenu();}), // (creating content is called separately after so InputHandler has a scene to add listeners to.)
                new Pair<String, Runnable>("Host Game", () -> {createHostGameMenu();}),
                new Pair<String, Runnable>("Back", () -> {createMainMenu();})
        );
        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);
        root.getChildren().add(menuBox);
    }

    private void createHostGameMenu(){
        root.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        menuBox.getChildren().addAll(UIUtil.createTitle("Host Game"));

        VBox hostPane = new VBox();
        hostPane.setAlignment(Pos.CENTER);
        hostPane.getStyleClass().add("pane");

        HBox nameBox = new HBox();
        nameBox.setAlignment(Pos.CENTER);
        Label nameLabel = new Label("Name: ");
        TextField nameTF = new TextField();
        nameBox.getChildren().addAll(nameLabel,nameTF);

        HBox serverNameBox = new HBox();
        serverNameBox.setAlignment(Pos.CENTER);
        Label serverNameLabel = new Label("Server Name: ");
        TextField serverNameTF = new TextField();
        nameBox.getChildren().addAll(serverNameLabel,serverNameTF);

        Button hostButton = new Button("Host Server");
        hostButton.setOnAction(((event) -> createServer(serverNameTF.getText(), nameTF.getText())));


        hostPane.getChildren().addAll(nameBox, serverNameBox, hostButton);


        menuBox.getChildren().addAll(hostPane);
        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Back", () -> {createMainMenu();})
        );

        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);

        root.getChildren().addAll(menuBox);
    }

    private boolean createServer(String serverName, String hostName){
        InetSocketAddress serverAddress = networker.startServer(serverName);

        createLobbyMenu(serverName, hostName, serverAddress);

        return true;
    }

    private void createLobbyMenu(String serverName, String serverHost, InetSocketAddress serverAddress) {
    private void createLobbyMenu(String serverName, String serverHost, boolean isHost){
        root.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        //addTitle("Lobby");
        menuBox.getChildren().addAll(UIUtil.createTitle(serverName));
        VBox players = new VBox();
        players.getStyleClass().add("pane");
        players.setAlignment(Pos.CENTER);
        // TODO get all players connected
        Label host = new Label(serverHost+" (HOST)");

        players.getChildren().addAll(host);

        menuBox.getChildren().addAll(players);

        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Start Game", () -> this.startGame(this.networker)),
                new Pair<String, Runnable>("Back", () -> {
                    if(isHost){
                        networker.stopServer();
                    }
                    createMainMenu();
                })
        );
        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);

        root.getChildren().add(menuBox);
    }

    private void createJoinGameMenu(){
        root.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        menuBox.getChildren().addAll(UIUtil.createTitle("Join Game"));

        HBox playerInfoBox = new HBox();
        playerInfoBox.getStyleClass().add("pane");
        playerInfoBox.setAlignment(Pos.CENTER);
        HBox nameBox = new HBox();
        nameBox.setAlignment(Pos.CENTER);
        Label nameLabel = new Label("Name: ");
        TextField nameTF = new TextField();
        nameBox.getChildren().addAll(nameLabel,nameTF);

        playerInfoBox.getChildren().addAll(nameBox);

        ObservableMap<String, InetSocketAddress> servers = this.networker.getServerDiscovery().getFoundServers();

        VBox serverBox = new VBox();
        serverBox.getStyleClass().add("pane");
        serverBox.setAlignment(Pos.CENTER);
        //toggle groups are so only one can be selected at a time
        ToggleGroup serverGroup = new ToggleGroup();

        menuBox.getChildren().addAll(playerInfoBox, serverBox);

        servers.addListener((MapChangeListener<? super String, ? super InetSocketAddress>) (change) -> {
            Platform.runLater(() -> {
                serverBox.getChildren().clear();

                for (Map.Entry<String, InetSocketAddress> entry : servers.entrySet()) {
                    ToggleButton serverButton = new ToggleButton(entry.getKey());
                    serverButton.setUserData(entry.getValue());
                    serverButton.setToggleGroup(serverGroup);
                    serverBox.getChildren().add(serverButton);
                }
            });
        });

        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Join Server", () -> {
                    ToggleButton s = (ToggleButton) serverGroup.getSelectedToggle();
                    if(s != null){
                        joinServer(s.getText(), "test player", (InetSocketAddress) s.getUserData());
                    }
                }),
                new Pair<String, Runnable>("Back", () -> {createMainMenu();})
        );
        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);

        root.getChildren().add(menuBox);

        try {
            this.networker.getServerDiscovery().startLookingForServers();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * connects player to server
     * @param serverName
     * @param username
     * @return true if player joins successfully, false otherwise
     */
    private boolean joinServer(String serverName, String username, InetSocketAddress serverAddress){
        createLobbyMenu(serverName, username, serverAddress);

        return true;
    }

    private void createMainMenu(){
        root.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        title = UIUtil.createTitle("Farmageddon");
        menuBox.getChildren().addAll(title);

        buttonBox = UIUtil.createMenu(getMainMenuData());
        menuBox.getChildren().add(buttonBox);

        root.getChildren().add(menuBox);

    }

    /**
     * Creates the content to be rendered onto the canvas.
     */
    private void createContent() {
        root.setPrefSize(800, 600);

        root.getStylesheets().add(ResourceType.STYLESHEET.path + "start-menu.css");

        FileInputStream imgFile = null;
        try {
            imgFile = new FileInputStream(ResourceLoader.get(ResourceType.UI, "background.png"));
            ImageView imageView = UIUtil.createBackground(imgFile, root);
            root.getChildren().add(imageView);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        

        menuBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(menuBox, Pos.CENTER);

        createMainMenu();
        startAnimation();
    }

    private List<Pair<String, Runnable>> getMainMenuData(){
        List<Pair<String, Runnable>> menuData = Arrays.asList(
                // (creating content is called separately after so InputHandler has a scene to add listeners to.)
                new Pair<String, Runnable>("Singleplayer", () -> this.startGame(null)),
                new Pair<String, Runnable>("Multiplayer", this::createMultiplayerMenu),
                new Pair<String, Runnable>("Settings", () -> {}),
                new Pair<String, Runnable>("Exit to Desktop", Platform::exit)
        );
        return menuData;
    }

    private void startGame(Networker givenNetworker) {
        App app = new App();
        Window window = root.getScene().getWindow();
        Parent content = app.init(window.widthProperty(), window.heightProperty(), givenNetworker);
        root.getScene().setRoot(content);
        app.createContent();
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
     * Creates the content of the menu and then gets the root node of this class.
     * @return StackPane (root) which contains all the menu components in the scene graph.
     */
	@Override
	public Parent getContent()
	{
		createContent();
		return root;
	}
}