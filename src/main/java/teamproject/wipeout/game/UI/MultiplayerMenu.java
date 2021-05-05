package teamproject.wipeout.game.UI;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import teamproject.wipeout.ServerListItem;
import teamproject.wipeout.game.market.ui.ErrorUI;
import teamproject.wipeout.networking.Networker;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.server.GameServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Class that constructs the multiplayer UI screens
 */
public class MultiplayerMenu {

    private final Networker networker;

    private final StartMenu parentMenu;
    private final VBox menuBox;
    private final Runnable backToMainMenu;

    private String chosenName;

    public MultiplayerMenu(StartMenu parentMenu, VBox menuBox) {
        this.networker = new Networker();

        this.parentMenu = parentMenu;
        this.menuBox = menuBox;
        this.backToMainMenu = () -> this.parentMenu.createMainMenu();

        this.chosenName = null;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.cleanupNetworker()));
    }

    public Runnable getMenu() {
        return () -> this.createMultiplayerMenu();
    }

    public void cleanupNetworker() {
        if (!this.networker.stopServer()) {
            GameClient client = this.networker.getClient();
            if (client != null) {
                client.closeConnection(true);
            }
        }
        if (this.networker.getServerDiscovery().getIsActive()) {
            this.networker.getServerDiscovery().stopLookingForServers();
        }
    }

    private void createMultiplayerMenu() {
        menuBox.getChildren().clear();

        menuBox.getChildren().addAll(UIUtil.createTitle("Multiplayer"));

        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Join Game", () -> createJoinGameMenu()),
                new Pair<String, Runnable>("Host Game", () -> createHostGameMenu()),
                new Pair<String, Runnable>("Back", this.backToMainMenu)
        );

        VBox buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);
    }

    private void createHostGameMenu() {
        menuBox.getChildren().clear();

        menuBox.getChildren().addAll(UIUtil.createTitle("Host Game"));

        StackPane stackPane = new StackPane();
        stackPane.setMaxSize(600, 100);
        stackPane.setAlignment(Pos.CENTER);
        stackPane.getStyleClass().add("tile-pane");

        VBox hostPane = new VBox();
        hostPane.setSpacing(10);
        hostPane.setPadding(new Insets(10, 10, 10, 10));
        hostPane.setAlignment(Pos.CENTER);
        hostPane.setMaxWidth(600);

        GridPane namesPane = new GridPane();
        namesPane.setAlignment(Pos.CENTER);
        namesPane.setHgap(10);
        namesPane.setVgap(20);

        Label nameLabel = new Label("Name:");
        nameLabel.getStyleClass().add("black-label");
        TextField nameTF = new TextField();
        namesPane.add(nameLabel, 0, 0, 1, 1);
        namesPane.add(nameTF, 1, 0, 2, 1);

        Label serverNameLabel = new Label("Server Name:");
        serverNameLabel.getStyleClass().add("black-label");
        TextField serverNameTF = new TextField();
        namesPane.add(serverNameLabel, 0, 1, 1, 1);
        namesPane.add(serverNameTF, 1, 1, 2, 1);

        GameModeUI gameModeBox = new GameModeUI();

        Button hostButton = new Button("Host Server");

        hostButton.setOnAction(((event) -> {
            if(serverNameTF.getText()==null || serverNameTF.getText().equals("")) {
                new ErrorUI(stackPane, "Error: No server name entered", null);

            } else if(serverNameTF.getText().getBytes(StandardCharsets.UTF_8).length > GameServer.SERVER_NAME_BYTE_LENGTH) {
                new ErrorUI(stackPane, "Error: Server name is too long", null);

            } else if(nameTF.getText()==null || nameTF.getText().equals("")) {
                new ErrorUI(stackPane, "Error: No name entered", null);

            } else {
                GameMode gameMode = gameModeBox.getGameMode();
                long gameModeValue = (long) gameModeBox.getValue();
                createServer(serverNameTF.getText(), nameTF.getText(), gameMode, gameModeValue);
            }
        }));

        hostPane.getChildren().addAll(namesPane, gameModeBox, hostButton);
        stackPane.getChildren().add(hostPane);

        menuBox.getChildren().addAll(stackPane);
        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Back", () -> createMultiplayerMenu())
        );

        VBox buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);
    }

    private void createJoinGameMenu() {
        menuBox.getChildren().clear();
        menuBox.getChildren().add(UIUtil.createTitle("Join Game"));

        StackPane stackPane = new StackPane();
        stackPane.setMaxWidth(600);
        stackPane.setAlignment(Pos.CENTER);
        stackPane.getStyleClass().add("tile-pane");

        VBox joinPane = new VBox();
        joinPane.setSpacing(20);
        joinPane.setPadding(new Insets(10, 10, 10, 10));
        joinPane.setAlignment(Pos.CENTER);
        joinPane.setMaxWidth(600);

        HBox playerInfoBox = new HBox();
        playerInfoBox.setAlignment(Pos.CENTER);

        HBox nameBox = new HBox();
        nameBox.setAlignment(Pos.CENTER);
        Label nameLabel = new Label("Name: ");
        nameLabel.getStyleClass().add("black-label");
        nameLabel.getStyleClass().add("label");
        TextField nameTF = new TextField();
        nameBox.getChildren().addAll(nameLabel,nameTF);

        playerInfoBox.getChildren().addAll(nameBox);

        ObservableMap<String, InetSocketAddress> servers = this.networker.getServerDiscovery().getAvailableServers();

        ListView<ServerListItem> serverList = new ListView<>();
        serverList.setMaxWidth(180);
        serverList.setMaxHeight(120);
        serverList.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        joinPane.getChildren().addAll(playerInfoBox, serverList);

        // https://stackoverflow.com/questions/13264017/getting-selected-element-from-listview
        servers.addListener((MapChangeListener<? super String, ? super InetSocketAddress>) (change) -> {
            Platform.runLater(() -> {
                serverList.getItems().clear();
                for (Map.Entry<String, InetSocketAddress> entry : servers.entrySet()) {
                    serverList.getItems().add(new ServerListItem(entry.getKey(), entry.getValue()));
                }
            });
        });

        List<Pair<String, Runnable>> joinData = Arrays.asList(
                new Pair<String, Runnable>("Join Server", () -> {
                    ServerListItem selectedItem = serverList.getSelectionModel().getSelectedItem();

                    if (selectedItem != null) {
                        if(nameTF.getText()==null || nameTF.getText().equals("")) {
                            new ErrorUI(stackPane, "Error: No name entered", null);
                        }
                        else {
                            joinServer(selectedItem.getServerName(), nameTF.getText(), selectedItem.getAddress());
                        }

                    }
                    else {
                        new ErrorUI(stackPane, "Error: No Server Selected", null);
                    }
                })
        );

        List<Pair<String, Runnable>> backData = Arrays.asList(
                new Pair<String, Runnable>("Back", () -> {
                    this.networker.getServerDiscovery().stopLookingForServers();
                    createMultiplayerMenu();
                })
        );

        VBox joinBox = UIUtil.createMenu(joinData);
        VBox backBox = UIUtil.createMenu(backData);

        joinPane.getChildren().add(joinBox);
        stackPane.getChildren().add(joinPane);
        menuBox.getChildren().addAll(stackPane, backBox);

        try {
            this.networker.getServerDiscovery().startLookingForServers();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void createLobbyMenu(String serverName, String userName, InetSocketAddress serverAddress, boolean isHost) {
        menuBox.getChildren().clear();

        VBox vBox = new VBox();
        vBox.setMaxWidth(600);
        vBox.setAlignment(Pos.CENTER);
        vBox.getStyleClass().add("tile-pane");
        vBox.setSpacing(10);

        Consumer<GameClient> startGame = (client) -> Platform.runLater(() -> startLocalGame(networker, client));
        GameClient client = networker.connectClient(serverAddress, userName, startGame);
        if (client == null) {
            StackPane errorBox = new StackPane();
            new ErrorUI(errorBox, "Error: Game server denied connection", () -> this.createJoinGameMenu());
            menuBox.getChildren().add(errorBox);
            return;
        }
        chosenName = userName;

        menuBox.getChildren().add(UIUtil.createTitle(serverName));

        ListView<String> playerList = new ListView<>();
        playerList.setMaxWidth(180);
        playerList.setMaxHeight(120);
        playerList.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        vBox.getChildren().add(playerList);
        ObservableMap<Integer, String> observablePlayers = client.connectedClients.get();
        observablePlayers.addListener((MapChangeListener<? super Integer, ? super String>) (change) -> {
            if (!client.getIsActive()) {
                Platform.runLater(() -> createMultiplayerMenu());
                return;
            }

            Platform.runLater(() -> {
                playerList.getItems().clear();
                for (String player : observablePlayers.values()) {
                    playerList.getItems().add(player);
                }
            });
        });

        Pair<String, Runnable> backButton = new Pair<String, Runnable>("Back", () -> {
            if (isHost) {
                this.networker.stopServer();
            } else {
                this.networker.getClient().closeConnection(true);
            }
            createMultiplayerMenu();
        });

        if (isHost) {
            Pair<String, Runnable> startButton = new Pair<String, Runnable>("Start Game", () -> startServerGame());
            VBox startBox = UIUtil.createMenu(Arrays.asList(startButton));
            startBox.setPadding(new Insets(10, 10, 10, 10));;
            vBox.getChildren().add(startBox);
        }

        VBox backBox = UIUtil.createMenu(Arrays.asList(backButton));
        menuBox.getChildren().addAll(vBox, backBox);

        for (String player : client.connectedClients.get().values()) {
            playerList.getItems().add(player);
        }
    }

    private void createServer(String serverName, String hostName, GameMode gameMode, long gameModeValue) {
        InetSocketAddress serverAddress = networker.startServer(serverName, gameMode, gameModeValue);
        createLobbyMenu(serverName, hostName, serverAddress, true);
    }

    /**
     * Connects player to server.
     *
     * @param serverName Chosen server name
     * @param username Chosen player name
     * @param serverAddress {@link InetSocketAddress} of the server we want to connect to
     */
    private void joinServer(String serverName, String username, InetSocketAddress serverAddress) {
        this.networker.getServerDiscovery().stopLookingForServers();
        createLobbyMenu(serverName, username, serverAddress, false);
    }

    private void startServerGame() {
        try {
            this.networker.serverRunner.startGame();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startLocalGame(Networker givenNetworker, GameClient client) {
        if (client != null) {
            this.parentMenu.startLocalGame(givenNetworker, this.chosenName, client.getGameStartTime(), client.getInitContainer());

        } else {
            this.parentMenu.disconnectError();
        }
    }
}
