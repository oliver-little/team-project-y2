package teamproject.wipeout;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import teamproject.wipeout.game.market.ui.ErrorUI;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.data.InitContainer;
import teamproject.wipeout.networking.Networker;
import teamproject.wipeout.networking.server.GameServer;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * StartMenu is a class which is used for creating and setting up the start menu of the game.
 * It implements the Controller Interface.
 */
public class StartMenu extends StackPane implements Controller {

    private static final List<String> DROPDOWN_ITEMS = List.of("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","UP","DOWN","LEFT","RIGHT");

    private Text title;
    private VBox buttonBox;
    private final VBox menuBox;

    private final LinkedHashMap<String, KeyCode> keyBindings; //maps string describing action to a key
    private final ArrayList<ComboBox<String>> dropDowns;

    private String chosenName;
    private final Networker networker;

    public StartMenu() {
        super();
        this.menuBox = new VBox(30);

        this.dropDowns = new ArrayList<ComboBox<String>>();
        this.keyBindings = new LinkedHashMap<String, KeyCode>();
        this.createDefaultBindings();

        this.chosenName = null;
        this.networker = new Networker();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.cleanupNetworker()));
    }

    public void cleanup() {
        this.chosenName = null;
        this.cleanupNetworker();
    }

    public void disconnectError() {
        this.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        StackPane errorBox = new StackPane();
        new ErrorUI(errorBox, "Error: Game server connection issue", () -> this.createMainMenu());
        menuBox.getChildren().add(errorBox);
        this.getChildren().add(menuBox);
    }

    private void cleanupNetworker() {
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

    /**
     * Creates default key bindings to be passed into game
     */
    private void createDefaultBindings(){
        keyBindings.put("Move left", KeyCode.LEFT);
        keyBindings.put("Move right", KeyCode.RIGHT);
        keyBindings.put("Move up", KeyCode.UP);
        keyBindings.put("Move down", KeyCode.DOWN);
        keyBindings.put("Drop", KeyCode.U);
        keyBindings.put("Pick-up", KeyCode.X);
        keyBindings.put("Destroy", KeyCode.D);
        keyBindings.put("Harvest", KeyCode.H);
    }
    
    private void createSingleplayerMenu() {
        this.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        menuBox.getChildren().addAll(UIUtil.createTitle("Singleplayer"));
    	
        GameModeUI gameModeBox = new GameModeUI();

        Runnable startGameAction = () -> {
            GameMode gameMode = gameModeBox.getGameMode();
            long gameModeValue = (long) gameModeBox.getValue();
            InitContainer initContainer = new InitContainer(gameMode, gameModeValue, null, null, null);
            startLocalGame(null, null, initContainer);
        };

        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Start Game", startGameAction),
                new Pair<String, Runnable>("Back", () -> createMainMenu())
        );
        
        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().addAll(gameModeBox, buttonBox);
        this.getChildren().add(menuBox);
    }

    private void createMultiplayerMenu(){
        this.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        menuBox.getChildren().addAll(UIUtil.createTitle("Multiplayer"));

        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Join Game", () -> createJoinGameMenu()),
                new Pair<String, Runnable>("Host Game", () -> createHostGameMenu()),
                new Pair<String, Runnable>("Back", () -> createMainMenu())
        );
        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);
        this.getChildren().add(menuBox);
    }

    private void createHostGameMenu() {
        this.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        menuBox.getChildren().addAll(UIUtil.createTitle("Host Game"));

        VBox hostPane = new VBox();
        hostPane.setAlignment(Pos.CENTER);
        hostPane.setMaxWidth(400);

        HBox nameBox = new HBox();
        nameBox.setAlignment(Pos.CENTER);
        nameBox.getStyleClass().add("hbox");
        nameBox.setSpacing(37);
        Label nameLabel = new Label("Name: ");
        TextField nameTF = new TextField();
        nameBox.getChildren().addAll(nameLabel,nameTF);

        HBox serverNameBox = new HBox();
        serverNameBox.getStyleClass().add("hbox");
        serverNameBox.setSpacing(3);
        serverNameBox.setAlignment(Pos.CENTER);
        Label serverNameLabel = new Label("Server Name: ");
        TextField serverNameTF = new TextField();
        serverNameBox.getChildren().addAll(serverNameLabel,serverNameTF);
        
        StackPane errorBox = new StackPane();

        GameModeUI gameModeBox = new GameModeUI();
        
        Button hostButton = new Button("Host Server");
        hostButton.setOnAction(((event) -> {
        	if(serverNameTF.getText()==null || serverNameTF.getText().equals("")) {
        		new ErrorUI(errorBox, "Error: No server name entered", null);
        	}
        	else if(serverNameTF.getText().getBytes(StandardCharsets.UTF_8).length > GameServer.SERVER_NAME_BYTE_LENGTH) {
        		new ErrorUI(errorBox, "Error: Server name is too long", null);
        	}
            else if(nameTF.getText()==null || nameTF.getText().equals("")) {
                new ErrorUI(errorBox, "Error: No name entered", null);
            }
        	else {
        	    GameMode gameMode = gameModeBox.getGameMode();
        	    long gameModeValue = (long) gameModeBox.getValue();
        		createServer(serverNameTF.getText(), nameTF.getText(), gameMode, gameModeValue);
        	}
        	
        }));
        
        
        hostPane.getChildren().addAll(nameBox, serverNameBox, gameModeBox, hostButton);

        menuBox.getChildren().addAll(hostPane, errorBox);
        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Back", () -> createMainMenu())
        );

        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);

        this.getChildren().add(menuBox);
        
    }

    
    private void createServer(String serverName, String hostName, GameMode gameMode, long gameModeValue) {
        InetSocketAddress serverAddress = networker.startServer(serverName, gameMode, gameModeValue);

        createLobbyMenu(serverName, hostName, serverAddress, true);
    }

    private void createLobbyMenu(String serverName, String userName, InetSocketAddress serverAddress, boolean isHost) {
        this.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        Consumer<GameClient> startGame = (client) -> Platform.runLater(() -> startLocalGame(networker, client));
        GameClient client = networker.connectClient(serverAddress, userName, startGame);
        if (client == null) {
            StackPane errorBox = new StackPane();
            new ErrorUI(errorBox, "Error: Game server denied connection", () -> this.createJoinGameMenu());
            menuBox.getChildren().add(errorBox);
            this.getChildren().add(menuBox);
            return;
        }
        chosenName = userName;

        menuBox.getChildren().addAll(UIUtil.createTitle(serverName));

        ListView<String> playerList = new ListView<>();
        playerList.setMaxWidth(180);
        playerList.setMaxHeight(120);
        playerList.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        menuBox.getChildren().addAll(playerList);
        ObservableMap<Integer, String> observablePlayers = client.connectedClients.get();
        observablePlayers.addListener((MapChangeListener<? super Integer, ? super String>) (change) -> {
            if (!client.getIsActive()) {
                Platform.runLater(() -> createMainMenu());
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
            createMainMenu();
        });

        List<Pair<String, Runnable>> menuData;
        if (isHost) {
            menuData = Arrays.asList(
                    new Pair<String, Runnable>("Start Game", () -> startServerGame()),
                    backButton
            );
        } else {
            menuData = Arrays.asList(backButton);
        }

        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);

        this.getChildren().add(menuBox);

        for (String player : client.connectedClients.get().values()) {
            playerList.getItems().add(player);
        }
    }

    private void createJoinGameMenu(){
        this.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        menuBox.getChildren().addAll(UIUtil.createTitle("Join Game"));

        HBox playerInfoBox = new HBox();
        playerInfoBox.getStyleClass().add("pane");
        playerInfoBox.setAlignment(Pos.CENTER);
        HBox nameBox = new HBox();
        nameBox.setAlignment(Pos.CENTER);
        Label nameLabel = new Label("Name: ");
        nameLabel.getStyleClass().add("label");
        TextField nameTF = new TextField();
        nameBox.getChildren().addAll(nameLabel,nameTF);

        playerInfoBox.getChildren().addAll(nameBox);

        ObservableMap<String, InetSocketAddress> servers = this.networker.getServerDiscovery().getAvailableServers();

        VBox serverBox = new VBox();
        serverBox.getStyleClass().add("pane");
        serverBox.setAlignment(Pos.CENTER);


        ListView<Server> serverList = new ListView<>();
        serverList.setMaxWidth(180);
        serverList.setMaxHeight(120);
        //list.setMouseTransparent( true );
        serverList.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
        //serverList.getItems().add(new Server("test", null));
        serverBox.getChildren().add(serverList);

        
        StackPane errorBox = new StackPane();
        
        menuBox.getChildren().addAll(playerInfoBox, serverBox, errorBox);

        // https://stackoverflow.com/questions/13264017/getting-selected-element-from-listview
        servers.addListener((MapChangeListener<? super String, ? super InetSocketAddress>) (change) -> {
            Platform.runLater(() -> {
                serverList.getItems().clear();
                for (Map.Entry<String, InetSocketAddress> entry : servers.entrySet()) {
                	serverList.getItems().add(new Server(entry.getKey(), entry.getValue()));
                }
            });
        });

        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Join Server", () -> {
                	Server selectedItem = serverList.getSelectionModel().getSelectedItem();

                    if (selectedItem != null) {
                    	if(nameTF.getText()==null || nameTF.getText().equals("")) {
                    		new ErrorUI(errorBox, "Error: No name entered", null);
                    	}
                    	else {
                    		joinServer(selectedItem.getServerName(), nameTF.getText(), selectedItem.getAddress());
                    	}
                        
                    }
                    else {
                    	new ErrorUI(errorBox, "Error: No Server Selected", null);
                    }
                }),
                new Pair<String, Runnable>("Back", () -> {
                    this.networker.getServerDiscovery().stopLookingForServers();
                    createMainMenu();
                })
        );
        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);

        this.getChildren().add(menuBox);

        try {
            this.networker.getServerDiscovery().startLookingForServers();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
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

    /**
     * Gets key bindings which are taken
     * @return ArrayList of strings representing key bindings which are taken
     */
    private ArrayList<String> getTakenBindings(){
        ArrayList<String> result = new ArrayList<>();
        for(Map.Entry<String, KeyCode> entry2 : keyBindings.entrySet()){
            result.add(entry2.getValue().getName().toUpperCase());
        }

        return result;
    }

    /**
     * Method to grey out items in all of the comboboxes which are already taken
     */
    private void updateDisabledItems(){
        ArrayList<String> takenBindings = getTakenBindings();
        for(ComboBox<String> d : dropDowns){
            d.setCellFactory(lv -> new ListCell<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item.toString());
                        setDisable(takenBindings.contains(item.toString()));
                    }

                }
            });
        }
    }

    /**
     * Creates settings menu for changing key bindings
     */
    private void createSettingsMenu(){
        this.getChildren().remove(menuBox);

        menuBox.getChildren().clear();

        menuBox.getChildren().add(UIUtil.createTitle("How to Play"));

        TilePane instructionsTilePane = new TilePane();
        instructionsTilePane.setMaxSize(600, 100);

        Label goalLabel = new Label("Goal: Make as much money as fast as possible!");
        Label howToPlayLabel = new Label("HOW TO PLAY:");
        Label moveAroundLabel = new Label("1. Move around with the arrow keys to get to the market in the centre.");
        Label marketLabel = new Label("2. Go close to the market and then click on it to buy seeds/tasks/potions.");
        Label selectItemLabel = new Label("3. Rush back to your farm, select the seed you want in the hotbar with the number keys or the mouse.");
        Label plantLabel = new Label("4. Now plant the seeds by clicking somewhere on your farm.");
        Label harvestLabel = new Label("5. Wait for them to grow and then harvest them by pressing H and clicking on the crop.");
        Label pickUpLabel = new Label("6. Pick up the harvested crop by standing on them and pressing X.");
        Label sellItemLabel = new Label("7. Hurry back to the market and sell your crops to make money!");

        instructionsTilePane.getChildren().addAll(goalLabel, howToPlayLabel, moveAroundLabel, marketLabel, selectItemLabel,
                plantLabel, harvestLabel, pickUpLabel, sellItemLabel);

        goalLabel.getStyleClass().add("black-label");
        howToPlayLabel.getStyleClass().add("black-label");
        moveAroundLabel.getStyleClass().add("black-label");
        marketLabel.getStyleClass().add("black-label");
        selectItemLabel.getStyleClass().add("black-label");
        plantLabel.getStyleClass().add("black-label");
        harvestLabel.getStyleClass().add("black-label");
        pickUpLabel.getStyleClass().add("black-label");
        sellItemLabel.getStyleClass().add("black-label");

        instructionsTilePane.setTileAlignment(Pos.TOP_LEFT);
        instructionsTilePane.getStyleClass().add("tile-pane");

        menuBox.getChildren().add(instructionsTilePane);
        
        
        TilePane tilePane = new TilePane();
        tilePane.setAlignment(Pos.TOP_CENTER);
        tilePane.setVgap(5);
        tilePane.setHgap(20);
        tilePane.setMaxSize(600, 300);

        menuBox.getChildren().add(tilePane);    
        tilePane.getStyleClass().add("tile-pane");

        //create a HBox for each drop down menu (key-binding)
        for(Map.Entry<String, KeyCode> entry : keyBindings.entrySet()) {
            String action = entry.getKey();
            KeyCode code = entry.getValue();

            HBox box = new HBox();
            box.setMaxSize(180, 50);
            box.setAlignment(Pos.CENTER_RIGHT);
            box.setPrefWidth(200);

            Label label = new Label(action + ":  ");

            ComboBox<String> dropDown = new ComboBox<String>();

            dropDown.getItems().addAll(DROPDOWN_ITEMS); //adds all possible key bindings
            dropDown.setValue(code.getName().toUpperCase()); //sets default value

            dropDowns.add(dropDown);

            dropDown.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
            {
                public void changed(ObservableValue<? extends String> ov, final String oldvalue, final String newvalue){
                    keyBindings.put(action, KeyCode.valueOf(newvalue));
                    updateDisabledItems(); //greys out taken bindings
                }
            });
            box.getChildren().addAll(label, dropDown);
            tilePane.getChildren().add(box);
            box.getStyleClass().add("my-hbox");
        }
        updateDisabledItems();

        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Back", () -> createMainMenu())
        );

        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);

        this.getChildren().add(menuBox);
    }

    private void createMainMenu(){
        this.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        title = UIUtil.createTitle("Farmageddon");
        menuBox.getChildren().addAll(title);

        buttonBox = UIUtil.createMenu(getMainMenuData());
        menuBox.getChildren().add(buttonBox);

        this.getChildren().add(menuBox);

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

    private List<Pair<String, Runnable>> getMainMenuData() {
        List<Pair<String, Runnable>> menuData = Arrays.asList(
                // (creating content is called separately after so InputHandler has a scene to add listeners to.)
                new Pair<String, Runnable>("Singleplayer", () -> createSingleplayerMenu()),
                new Pair<String, Runnable>("Multiplayer", () -> createMultiplayerMenu()),
                new Pair<String, Runnable>("How to Play", () -> createSettingsMenu()),
                new Pair<String, Runnable>("Exit to Desktop", Platform::exit)
        );
        return menuData;
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
            this.startLocalGame(givenNetworker, client.getGameStartTime(), client.getInitContainer());

        } else {
            this.disconnectError();
        }
    }

    private void startLocalGame(Networker givenNetworker, Long gameStartTime, InitContainer initContainer) {
        Gameplay game = new Gameplay(givenNetworker, gameStartTime, initContainer, this.chosenName, this.keyBindings);

        Parent content = game.getParentWith(this.getScene().getWindow());

        this.getScene().setRoot(content);
        game.createContent();
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
	public Parent getContent() {
		this.createContent();
		return this;
	}
}