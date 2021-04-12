package teamproject.wipeout;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.geometry.Pos;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.util.Networker;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * StartMenu is a class which is used for creating and setting up the start menu of the game.
 * It implements the Controller Interface.
 */
public class StartMenu implements Controller {
    
    private Pane root;
    private VBox menuBox;
    private VBox buttonBox;
    private Text title;

    Networker networker;

    private LinkedHashMap<String, KeyCode> keyBindings; //maps string describing action to a key
    private ArrayList<String> dropDownItems = new ArrayList<String>(Arrays.asList("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","UP","DOWN","LEFT","RIGHT"));
    private ArrayList<ComboBox<String>> dropDowns = new ArrayList<>();

    public StartMenu() {
        this.root = new StackPane();
        this.menuBox = new VBox(30);
        this.networker = new Networker();
        createDefaultBindings();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            GameClient client = this.networker.getClient();
            if (client != null) {
                client.closeConnection(true);
            }
            this.networker.stopServer();
        }));
    }

    /**
     * Creates default key bindings to be passed into game
     */
    private void createDefaultBindings(){
        keyBindings = new LinkedHashMap<String, KeyCode>();
        keyBindings.put("Move left", KeyCode.LEFT);
        keyBindings.put("Move right", KeyCode.RIGHT);
        keyBindings.put("Move up", KeyCode.UP);
        keyBindings.put("Move down", KeyCode.DOWN);
        keyBindings.put("Drop", KeyCode.U);
        keyBindings.put("Pick-up", KeyCode.X);
        keyBindings.put("Destroy", KeyCode.D);
        keyBindings.put("Harvest", KeyCode.H);
    }


    public void cleanup() {
    }

    private void createMultiplayerMenu(){
        root.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        menuBox.getChildren().addAll(UIUtil.createTitle("Multiplayer"));

        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Join Game", () -> createJoinGameMenu()),
                new Pair<String, Runnable>("Host Game", () -> createHostGameMenu()),
                new Pair<String, Runnable>("Back", () -> createMainMenu())
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
        //hostPane.getStyleClass().add("vbox");
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

        Button hostButton = new Button("Host Server");
        hostButton.setOnAction(((event) -> createServer(serverNameTF.getText(), nameTF.getText())));


        hostPane.getChildren().addAll(nameBox, serverNameBox, hostButton);


        menuBox.getChildren().addAll(hostPane);
        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Back", () -> createMainMenu())
        );

        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);

        root.getChildren().addAll(menuBox);
    }

    private boolean createServer(String serverName, String hostName){
        InetSocketAddress serverAddress = networker.startServer(serverName);

        createLobbyMenu(serverName, hostName, serverAddress, true);

        return true;
    }

    private void createLobbyMenu(String serverName, String serverHost, InetSocketAddress serverAddress, boolean isHost) {
        root.getChildren().remove(menuBox);
        menuBox.getChildren().clear();

        //addTitle("Lobby");
        menuBox.getChildren().addAll(UIUtil.createTitle(serverName));
        
        ListView<String> playerList = new ListView<>();
        playerList.setMaxWidth(180);
        playerList.setMaxHeight(120);
        //list.setMouseTransparent( true );
        playerList.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
        //serverList.getItems().add(new Server("test", null));

        menuBox.getChildren().addAll(playerList);

        Pair<String, Runnable> backButton = new Pair<String, Runnable>("Back", () -> {
            if (isHost) {
                networker.stopServer();
            } else {
                networker.getClient().closeConnection(true);
            }
            createMainMenu();
        });

        List<Pair<String, Runnable>> menuData;
        if (isHost) {
            menuData =Arrays.asList(
                    new Pair<String, Runnable>("Start Game", () -> startServerGame()),
                    backButton
            );
        } else {
            menuData =Arrays.asList(backButton);
        }

        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);

        root.getChildren().add(menuBox);

        networker.connectClient(serverAddress, serverHost, (gameStartTime) -> Platform.runLater(() -> startLocalGame(networker, gameStartTime)));

        ObservableMap<Integer, String> observablePlayers = networker.getClient().connectedClients.get();

        observablePlayers.addListener((MapChangeListener<? super Integer, ? super String>) (change) -> {
            if (!networker.getClient().getIsActive()) {
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

        menuBox.getChildren().addAll(playerInfoBox, serverBox);

        // TODO use list view instead of toggle group
        // https://stackoverflow.com/questions/13264017/getting-selected-element-from-listview
        servers.addListener((MapChangeListener<? super String, ? super InetSocketAddress>) (change) -> {
            Platform.runLater(() -> {
                serverBox.getChildren().clear();

                for (Map.Entry<String, InetSocketAddress> entry : servers.entrySet()) {
                	serverList.getItems().add(new Server(entry.getKey(), entry.getValue()));
                }
                serverBox.getChildren().add(serverList);
            });
        });

        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Join Server", () -> {
                	Server selectedItem = serverList.getSelectionModel().getSelectedItem();
                	//System.out.println("selectedItem: "+ selectedItem.getServerName());
                    if(selectedItem != null){
                        joinServer(selectedItem.getServerName(), nameTF.getText(), selectedItem.getAddress());
                    }
                }),
                new Pair<String, Runnable>("Back", () -> createMainMenu())
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
    private boolean joinServer(String serverName, String username, InetSocketAddress serverAddress) {
        this.networker.getServerDiscovery().stopLookingForServers();

        createLobbyMenu(serverName, username, serverAddress, false);

        return true;
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
        root.getChildren().remove(menuBox);

        menuBox.getChildren().clear();

        menuBox.getChildren().add(UIUtil.createTitle("Settings"));

        TilePane tilePane = new TilePane();
        tilePane.setAlignment(Pos.TOP_CENTER);
        tilePane.setVgap(20);
        tilePane.setHgap(20);

        //create a HBox for each drop down menu (key-binding)
        for(Map.Entry<String, KeyCode> entry : keyBindings.entrySet()) {
            String action = entry.getKey();
            KeyCode code = entry.getValue();

            HBox box = new HBox();
            box.setAlignment(Pos.CENTER_RIGHT);
            box.setPrefWidth(200);

            Label label = new Label(action + ":  ");
            
            ComboBox<String> dropDown = new ComboBox<String>();

            dropDown.getItems().addAll(dropDownItems); //adds all possible key bindings
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
        }
        updateDisabledItems();

        menuBox.getChildren().add(tilePane);

        List<Pair<String, Runnable>> menuData = Arrays.asList(
                new Pair<String, Runnable>("Back", () -> createMainMenu())
        );
        
        buttonBox = UIUtil.createMenu(menuData);
        menuBox.getChildren().add(buttonBox);

        root.getChildren().add(menuBox);
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
                new Pair<String, Runnable>("Singleplayer", () -> startLocalGame(null, null)),
                new Pair<String, Runnable>("Multiplayer", () -> createMultiplayerMenu()),
                new Pair<String, Runnable>("Settings", () -> createSettingsMenu()),
                new Pair<String, Runnable>("Exit to Desktop", Platform::exit)
        );
        return menuData;
    }

    private void startServerGame() {
        try {
            networker.serverRunner.startGame();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startLocalGame(Networker givenNetworker, Long gameStartTime) {
        App game = new App(givenNetworker, gameStartTime, keyBindings);
        Window window = root.getScene().getWindow();
        Parent content = game.getParentWith(window.widthProperty(), window.heightProperty());
        root.getScene().setRoot(content);
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
	public Parent getContent()
	{
		createContent();
		return root;
	}
}