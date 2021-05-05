package teamproject.wipeout;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Pair;
import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.component.render.CameraFollowComponent;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameLoop;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.core.SystemUpdater;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.system.ClockSystem;
import teamproject.wipeout.engine.system.EventSystem;
import teamproject.wipeout.engine.system.PlayerAnimatorSystem;
import teamproject.wipeout.engine.system.SabotageSystem;
import teamproject.wipeout.engine.system.ScriptSystem;
import teamproject.wipeout.engine.system.ai.SteeringSystem;
import teamproject.wipeout.engine.system.audio.AudioSystem;
import teamproject.wipeout.engine.system.audio.MovementAudioSystem;
import teamproject.wipeout.engine.system.farm.FarmSpriteSystem;
import teamproject.wipeout.engine.system.farm.GrowthSystem;
import teamproject.wipeout.engine.system.input.MouseClickSystem;
import teamproject.wipeout.engine.system.input.MouseHoverSystem;
import teamproject.wipeout.engine.system.physics.CollisionSystem;
import teamproject.wipeout.engine.system.physics.MovementSystem;
import teamproject.wipeout.engine.system.render.CameraFollowSystem;
import teamproject.wipeout.engine.system.render.ParticleSystem;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.UI.GameMode;
import teamproject.wipeout.game.UI.GameOverUI;
import teamproject.wipeout.game.UI.Leaderboard;
import teamproject.wipeout.game.UI.StartMenu;
import teamproject.wipeout.game.UI.UIUtil;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.CameraEntity;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.inventory.InventoryUI;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.market.entity.MarketEntity;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.game.player.ui.MoneyUI;
import teamproject.wipeout.game.settings.ui.SettingsUI;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.game.task.TasksHelper;
import teamproject.wipeout.game.task.ui.TaskUI;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.InitContainer;
import teamproject.wipeout.networking.Networker;
import teamproject.wipeout.util.resources.PlayerSpriteSheetManager;

import java.io.IOException;
import java.util.*;

/**
 * App is a class for containing the components for game play.
 * It implements the Controller interface.
 * 
 * Begin by creating an instance, then call init, add App to a scene, then call createContent
 *
 */
public class Gameplay extends StackPane implements Controller {

    private Canvas dynamicCanvas;
    private Canvas staticCanvas;
    private StackPane interfaceOverlay;

    private final long gameDuration;
    private final GameMode gameMode;
    private final long wealthTarget;
    private final long gameStartTime;

    private final PlayerSpriteSheetManager playerSpriteSheetManager;
    private final Pair<Integer, String> playerInfo;
    private final String playerSpriteSheet;
    private final int farmID;

    private ReadOnlyDoubleProperty widthProperty;
    private ReadOnlyDoubleProperty heightProperty;

    private SpriteManager spriteManager;
    private ItemStore itemStore;

    private GameLoop gameLoop;
    private GameScene gameScene;
    private WorldEntity worldEntity;

    private RenderSystem renderer;
    private SystemUpdater systemUpdater;
    private InputHandler inputHandler;

    private AudioSystem audioSystem;
    private MovementAudioSystem movementAudio;
    private GameAudio backingTrack;

    private List<EventSystem> eventSystems;

    private ChangeListener<? super Boolean> focusListener;

    private final Map<String, KeyCode> keyBindings;

    private final Networker networker;

    /**
     * Creates a new instance of Gameplay
     * @param networker The Networker instance to use
     * @param givenGameStartTime The system time the game began at
     * @param initContainer The Game settings
     * @param playerName The name of the current player
     * @param bindings The keybindings to use
     */
    public Gameplay(Networker networker, Long givenGameStartTime, InitContainer initContainer, String playerName, Map<String, KeyCode> bindings) {
        super();

        this.gameStartTime = givenGameStartTime == null ? System.currentTimeMillis() : givenGameStartTime;

        if (playerName == null) {
            this.playerInfo = new Pair<Integer, String>(new Random().nextInt(1024), CurrentPlayer.DEFAULT_NAME);
            this.farmID = 1;
        } else {
            this.playerInfo = new Pair<Integer, String>(initContainer.getClientID(), playerName);
            this.farmID = initContainer.getFarmID();
        }
        
        this.gameMode = initContainer.getGameMode();
        if (this.gameMode == GameMode.WEALTH_MODE) {
            this.gameDuration = -1;
        	this.wealthTarget = initContainer.getGameModeValue();

        } else {
            this.gameDuration = initContainer.getGameModeValue();
            this.wealthTarget = -1;
        }

        if (networker == null) {
            this.playerSpriteSheetManager = new PlayerSpriteSheetManager();
            this.playerSpriteSheet = this.playerSpriteSheetManager.getPlayerSpriteSheet();
        } else {
            this.playerSpriteSheetManager = null;
            this.playerSpriteSheet = initContainer.getClientSpriteSheet();
        }

        this.focusListener = null;

        this.keyBindings = bindings;
        this.networker = networker;
    }

    /** 
     * Sets up resizes and focus properties from the window
     * @param window The window this Game is within
     */
    public Parent getParentWith(Window window) {
        this.widthProperty = window.widthProperty();
        this.heightProperty = window.heightProperty();

        this.focusListener = (observableVal, oldVal, newVal) -> this.inputHandler.setDisableInput(oldVal).run();
        window.focusedProperty().addListener(this.focusListener);

        return this.getContent();
    }

    /**
     * Creates the content to be rendered onto the canvas.
     */
    public void createContent() {
        try {
            this.itemStore = new ItemStore("items.json");
            this.spriteManager = new SpriteManager();
            this.loadSpriteSheets();

        } catch (IOException | ReflectiveOperationException exception) {
            exception.printStackTrace();
        }

        // Scene
        this.gameScene = new GameScene();
        this.renderer = new RenderSystem(this.gameScene, this.dynamicCanvas, this.staticCanvas);
        this.initializeSystemUpdater();
        this.gameLoop = new GameLoop(this.systemUpdater, this.renderer);

        // Input
        this.inputHandler = new InputHandler(this.getScene());

        MouseClickSystem mouseClick = new MouseClickSystem(this.gameScene, this.inputHandler);
        MouseHoverSystem mouseHover = new MouseHoverSystem(this.gameScene, this.inputHandler);
        this.inputHandler.mouseHoverSystem = mouseHover;
        systemUpdater.addSystem(mouseHover);

        // Effects
        PlayerAnimatorSystem playerAnimator = new PlayerAnimatorSystem(this.gameScene);
        SabotageSystem sabotage = new SabotageSystem(this.gameScene);
        this.eventSystems = List.of(mouseClick, playerAnimator, sabotage);

        // Player
    	CurrentPlayer currentPlayer = new CurrentPlayer(this.gameScene, this.playerInfo, this.playerSpriteSheet, this.spriteManager, this.itemStore);

        // Camera
        CameraEntity cameraEntity = new CameraEntity(this.gameScene);
        this.setupCameraFollowing(cameraEntity, currentPlayer);

        // Create tasks
        ArrayList<Task> purchasableTasks = TasksHelper.createTasks(this.itemStore, currentPlayer);

        // Market Entity
        MarketEntity marketEntity = new MarketEntity(this.createMarketPack(currentPlayer, purchasableTasks));
        marketEntity.setOnUIOpen(this.inputHandler.setDisableInput(true));
        marketEntity.setOnUIClose(this.inputHandler.setDisableInput(false));

        // World Entity
        this.worldEntity = new WorldEntity(this.createWorldPack(marketEntity, currentPlayer));
        if (this.playerSpriteSheetManager != null) {
            this.worldEntity.setPlayerSpriteSheetSupplier(this.playerSpriteSheetManager.getPlayerSpriteSheetSupplier());
        }
        currentPlayer.setThrownPotion((potion) ->  this.worldEntity.addPotion(potion));

        // Connect world entity with networker if possible
        if (this.networker != null) {
            this.worldEntity.setClientSupplier(this.networker.clientSupplier);
            this.networker.setWorldEntity(this.worldEntity);
        }

        // Inventory UI
        InventoryUI inventoryUI = new InventoryUI(this.spriteManager, this.itemStore);
        currentPlayer.setInventoryUI(inventoryUI);

        // Money UI
        MoneyUI moneyUI = new MoneyUI(currentPlayer);
        StackPane.setAlignment(moneyUI, Pos.TOP_CENTER);

        // Settings UI
        Runnable returnToMenu = () -> {
            if (this.networker != null && !this.networker.stopServer()) {
                GameClient client = this.networker.getClient();
                if (client != null) {
                    client.closeConnection(true);
                }
            }
    
            this.cleanup();
    
            StartMenu startMenu = new StartMenu();
            this.getScene().setRoot(startMenu.getContent());
            
        };
        this.backingTrack = new GameAudio("backingTrack2.wav", true);
        SettingsUI settingsUI = new SettingsUI(this.audioSystem, this.movementAudio, returnToMenu, this.keyBindings, this.backingTrack);
        
        this.interfaceOverlay.getChildren().addAll(inventoryUI, moneyUI);
        
        // Right UI overlay (Time/Settings/Instructions) based on the chosen game mode
        this.createRightUIOverlay(settingsUI);

        // Task UI
        TaskUI taskUI = new TaskUI(currentPlayer);
        currentPlayer.setTaskUI(taskUI);

        // Left UI overlay (Tasks/Leaderboard) based on the chosen game mode
        this.createLeftUIOverlay(taskUI);

        // Setup networking if possible
        if (this.networker != null) {
            this.setupNetworking();

        } else { // else setup a default farm for the current player
            this.worldEntity.setRandomFarmFor(currentPlayer);
        }

        //currentPlayer.acquireItem(6, 98); //for checking stack/inventory limits
        //currentPlayer.acquireItem(1, 2);
        //currentPlayer.acquireItem(28, 98);
        //currentPlayer.acquireItem( 43, 2);

        // Input bindings
        this.setupKeyInput(currentPlayer, inventoryUI);
        this.setupKeyHotkeys(inventoryUI);

        this.gameLoop.start();
    }


    /**
     * Gets the root node of this class.
     * @return StackPane which contains the canvas.
     */
	@Override
	public Parent getContent() {
		this.dynamicCanvas = new Canvas();
        this.staticCanvas = new Canvas();
        if (this.widthProperty != null) {
            this.dynamicCanvas.widthProperty().bind(this.widthProperty);
            this.staticCanvas.widthProperty().bind(this.widthProperty);

        } else {
            this.dynamicCanvas.setWidth(800);
            this.staticCanvas.setWidth(800);
        }

        if (this.heightProperty != null) {
            this.dynamicCanvas.heightProperty().bind(this.heightProperty);
            this.staticCanvas.heightProperty().bind(this.heightProperty);

        } else {
            this.dynamicCanvas.setHeight(600);
            this.staticCanvas.setHeight(600);
        }

        this.interfaceOverlay = new StackPane();
        AnchorPane anchorPane = new AnchorPane();
        
        anchorPane.getChildren().add(this.interfaceOverlay);
        AnchorPane.setTopAnchor(this.interfaceOverlay, 10.0);
        AnchorPane.setRightAnchor(this.interfaceOverlay, 10.0);
        AnchorPane.setBottomAnchor(this.interfaceOverlay, 10.0);
        AnchorPane.setLeftAnchor(this.interfaceOverlay, 10.0);

        this.getChildren().addAll(staticCanvas, dynamicCanvas, anchorPane);
		return this;
	}

    /**
     * Cleans up the game's threads and dependencies when the game should end.
     */
    public void cleanup() {
        if (this.worldEntity != null) {
            this.worldEntity.cleanup();
        }
        if (this.gameLoop != null) {
            this.gameLoop.stop();
        }
        if (this.renderer != null) {
            this.renderer.cleanup();
        }
        if (this.systemUpdater != null) {
            this.systemUpdater.cleanup();
        }
        if (this.eventSystems != null) {
            for (EventSystem eventSystem : this.eventSystems) {
                eventSystem.cleanup();
            }
        }
        if (this.focusListener != null) {
            this.getScene().getWindow().focusedProperty().removeListener(this.focusListener);
        }

        if (this.backingTrack != null && this.backingTrack.isPlaying()) {
            this.backingTrack.stop();
        }

        // Attempt to clean up networking in case of hard shutdown
        if (this.networker != null && !this.networker.stopServer()) {
            GameClient client = this.networker.getClient();
            if (client != null) {
                client.closeConnection(true);
            }
        }
    }

    /**
     * Loads all spritesheets into the SpriteManager.
     */
    private void loadSpriteSheets() throws IOException {
        PlayerSpriteSheetManager.loadPlayerSpriteSheets(this.spriteManager);
        this.spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
        this.spriteManager.loadSpriteSheet("crops/fruit-tree-descriptor.json", "crops/FruitTrees.png");
        this.spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
        this.spriteManager.loadSpriteSheet("inventory/inventory-tools-descriptor.json", "inventory/Tools.png");
        this.spriteManager.loadSpriteSheet("inventory/inventory-fruit-and-vegetable-descriptor.json", "inventory/FruitsAndVeg.png");
        this.spriteManager.loadSpriteSheet("inventory/inventory-vegetables-descriptor.json", "inventory/Vegetables.png");
        this.spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
        this.spriteManager.loadSpriteSheet("inventory/inventory-animals-and-food-descriptor.json", "inventory/AnimalsAndFood.png");
        this.spriteManager.loadSpriteSheet("inventory/inventory-potions-descriptor.json", "inventory/Potions.png");
        this.spriteManager.loadSpriteSheet("ai/mouse-descriptor.json", "ai/mouse.png");
        this.spriteManager.loadSpriteSheet("ai/rat-descriptor.json", "ai/rat.png");
        this.spriteManager.loadSpriteSheet("gameworld/arrow-descriptor.json", "gameworld/Arrow.png");
    }

    /**
     * Initialises the GameSystems
     */
    private void initializeSystemUpdater() {
        this.systemUpdater = new SystemUpdater();

        this.systemUpdater.addSystem(new MovementSystem(this.gameScene));
        this.systemUpdater.addSystem(new CollisionSystem(this.gameScene));
        this.systemUpdater.addSystem(new CameraFollowSystem(this.gameScene));
        this.systemUpdater.addSystem(new FarmSpriteSystem(this.gameScene, this.spriteManager));
        this.systemUpdater.addSystem(new ParticleSystem(this.gameScene));
        this.systemUpdater.addSystem(new GrowthSystem(this.gameScene));
        this.systemUpdater.addSystem(new SteeringSystem(this.gameScene));
        this.systemUpdater.addSystem(new ScriptSystem(this.gameScene));

        this.audioSystem = new AudioSystem(this.gameScene, 0.05f);
        this.movementAudio = new MovementAudioSystem(this.gameScene, 0.05f);
        this.systemUpdater.addSystem(this.audioSystem);
        this.systemUpdater.addSystem(this.movementAudio);
    }

    /**
     * Sets up the camera to follow the player.
     * @param cameraEntity The CameraEntity to use
     * @param currentPlayer The player to follow
     */
    private void setupCameraFollowing(CameraEntity cameraEntity, CurrentPlayer currentPlayer) {
        // Use JavaFX binding to ensure camera is in correct position even when screen size changes
        RenderComponent targetRenderer = currentPlayer.getComponent(RenderComponent.class);
        Point2D targetDimensions = new Point2D(targetRenderer.getWidth(), targetRenderer.getHeight()).multiply(0.5);
        ObjectBinding<Point2D> camPosBinding = Bindings.createObjectBinding(
                () -> new Point2D(this.widthProperty.doubleValue(), this.heightProperty.doubleValue())
                        .multiply(-0.5).multiply(1/CameraEntity.CAMERA_ZOOM).add(targetDimensions),
                this.widthProperty,
                this.heightProperty
        );
        ObjectProperty<Point2D> cameraPosition = new SimpleObjectProperty<>();
        cameraPosition.bind(camPosBinding);
        cameraEntity.addComponent(new CameraFollowComponent(currentPlayer, cameraPosition));
    }

    /**
     * Creates the UI on the left side of the screen (task and leaderboard)
     */
    private void createLeftUIOverlay(TaskUI taskUI) {
        // UI Overlay
        VBox topLeft = new VBox(3);
        topLeft.setAlignment(Pos.TOP_LEFT);
        topLeft.setPickOnBounds(false);
        topLeft.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(topLeft, Pos.TOP_LEFT);

        VBox leaderboardBox = new VBox();
        Text title = UIUtil.createTitle("Leaderboard:");
        title.setFont(Font.font("Kalam", 30));

        Leaderboard leaderboard = new Leaderboard(this.worldEntity.getPlayers());
        leaderboardBox.getChildren().addAll(title);
        if (this.gameMode == GameMode.WEALTH_MODE) {
            Text target = UIUtil.createTitle("Target: $" + wealthTarget);
            target.setFont(Font.font("Kalam", 20));
            leaderboardBox.getChildren().addAll(target);

            leaderboard.setGameModeValueAction((playerMoney) -> {
                // checks if any player has reached money target
                if (playerMoney >= this.wealthTarget) {
                    this.onGameEnd().run();
                    return true;
                }
                return false;
            });
        }
        leaderboardBox.getChildren().addAll(leaderboard);
        leaderboardBox.setMouseTransparent(true);

        topLeft.setSpacing(20);
        topLeft.getChildren().addAll(leaderboardBox, taskUI);
        this.interfaceOverlay.getChildren().addAll(topLeft);

    }

    /**
     * Creates the UI on the right side of the screen (clock, settings)
     */
    private void createRightUIOverlay(SettingsUI settingsUI) {
        //Clock UI / System
        if (this.gameMode == GameMode.TIME_MODE) {
            ClockSystem clockSystem = new ClockSystem(this.gameDuration, this.gameStartTime, this.onGameEnd());
            this.systemUpdater.addSystem(clockSystem);
            this.worldEntity.setClockSupplier(() -> clockSystem);

            // UI Overlay
            VBox topRight = new VBox(2);
            topRight.setAlignment(Pos.TOP_RIGHT);
            topRight.setPickOnBounds(false);
            topRight.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            StackPane.setAlignment(topRight, Pos.TOP_RIGHT);

            topRight.getChildren().addAll(clockSystem.clockUI, settingsUI);
            this.interfaceOverlay.getChildren().addAll(topRight);

        } else if (this.gameMode == GameMode.WEALTH_MODE) {
            // UI Overlay
            VBox topRight = new VBox(1);
            topRight.setAlignment(Pos.TOP_RIGHT);
            topRight.setPickOnBounds(false);
            topRight.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            StackPane.setAlignment(topRight, Pos.TOP_RIGHT);

            topRight.getChildren().addAll(settingsUI);
            this.interfaceOverlay.getChildren().addAll(topRight);
        }
    }

    /**
     * Sets up the keybindings for moving the player.
     */
    private void setupKeyInput(CurrentPlayer currentPlayer, InventoryUI inventoryUI) {
        inventoryUI.onMouseClick(this.worldEntity);
        this.worldEntity.setupFarmPickingKey(this.keyBindings.get("Harvest"));
        this.worldEntity.setupFarmDestroyingKey(this.keyBindings.get("Destroy"));

        this.inputHandler.addKeyAction(this.keyBindings.get("Move left"), //moving left
                currentPlayer.addAcceleration(-1, 0),
                currentPlayer.addAcceleration(1, 0)
        );
        this.inputHandler.addKeyAction(this.keyBindings.get("Move right"), //moving right
                currentPlayer.addAcceleration(1, 0),
                currentPlayer.addAcceleration(-1, 0)
        );
        this.inputHandler.addKeyAction(this.keyBindings.get("Move up"), //moving up
                currentPlayer.addAcceleration(0, -1),
                currentPlayer.addAcceleration(0, 1)
        );
        this.inputHandler.addKeyAction(this.keyBindings.get("Move down"), //moving down
                currentPlayer.addAcceleration(0, 1),
                currentPlayer.addAcceleration(0, -1)
        );

        this.inputHandler.onKeyRelease(
                this.keyBindings.get("Drop"),
                inventoryUI.dropOnKeyRelease(currentPlayer, this.worldEntity.pickables)
        );
        this.inputHandler.onKeyRelease(
                this.keyBindings.get("Pick-up"),
                () -> this.worldEntity.pickables.picked(currentPlayer.pickup())
        );
    }

    /**
     * Sets up the keybindings for selecting inventory slots.
     */
    private void setupKeyHotkeys(InventoryUI inventoryUI) {
        this.inputHandler.onKeyRelease(KeyCode.DIGIT1, inventoryUI.useSlot(0, this.worldEntity));
        this.inputHandler.onKeyRelease(KeyCode.DIGIT2, inventoryUI.useSlot(1, this.worldEntity));
        this.inputHandler.onKeyRelease(KeyCode.DIGIT3, inventoryUI.useSlot(2, this.worldEntity));
        this.inputHandler.onKeyRelease(KeyCode.DIGIT4, inventoryUI.useSlot(3, this.worldEntity));
        this.inputHandler.onKeyRelease(KeyCode.DIGIT5, inventoryUI.useSlot(4, this.worldEntity));
        this.inputHandler.onKeyRelease(KeyCode.DIGIT6, inventoryUI.useSlot(5, this.worldEntity));
        this.inputHandler.onKeyRelease(KeyCode.DIGIT7, inventoryUI.useSlot(6, this.worldEntity));
        this.inputHandler.onKeyRelease(KeyCode.DIGIT8, inventoryUI.useSlot(7, this.worldEntity));
        this.inputHandler.onKeyRelease(KeyCode.DIGIT9, inventoryUI.useSlot(8, this.worldEntity));
        this.inputHandler.onKeyRelease(KeyCode.DIGIT0, inventoryUI.useSlot(9, this.worldEntity));
    }

    /**
     * Sets up the networker if the game is online.
     */
    private void setupNetworking() {
        CurrentPlayer myCurrentPlayer = this.worldEntity.myCurrentPlayer;

        GameClient currentClient = this.networker.getClient();
        currentClient.setOnDisconnect(() -> Platform.runLater(() -> {
            Scene scene = this.getScene();
            if (scene != null) {
                this.cleanup();
                StartMenu startMenu = new StartMenu();
                scene.setRoot(startMenu.getContent());
                startMenu.disconnectError();
            }
        }));

        currentClient.addCurrentPlayer(myCurrentPlayer);
        currentClient.setFarmEntities(this.worldEntity.farms);
        currentClient.setNewPlayerAction(this.networker.onPlayerConnection(this.gameScene, this.itemStore, this.spriteManager));

        FarmEntity myFarm = this.worldEntity.farms.get(this.farmID);
        this.worldEntity.setFarmFor(myCurrentPlayer, myFarm);

        currentClient.send(new GameUpdate(myCurrentPlayer.getCurrentState()));
    }

    /**
     * Generates a pack of data for MarketEntity.
     * @param currentPlayer The current player
     * @param purchasableTasks All tasks available for purchase
     */
    private Map<String, Object> createMarketPack(CurrentPlayer currentPlayer, ArrayList<Task> purchasableTasks) {
        HashMap<String, Object> marketPack = new HashMap<String, Object>();
        marketPack.put("itemStore", this.itemStore);
        marketPack.put("spriteManager", this.spriteManager);
        marketPack.put("gameScene", this.gameScene);
        marketPack.put("uiContainer", this.interfaceOverlay);
        marketPack.put("networked", this.networker != null);
        marketPack.put("currentPlayer", currentPlayer);
        marketPack.put("tasks", purchasableTasks);
        return marketPack;
    }

    /**
     * Generates a pack of data for WorldEntity
     * @param marketEntity The MarketEntity instance in this game
     * @param currentPlayer The current player
     */
    private Map<String, Object> createWorldPack(MarketEntity marketEntity, CurrentPlayer currentPlayer) {
        HashMap<String, Object> worldPack = new HashMap<String, Object>();
        worldPack.put("itemStore", this.itemStore);
        worldPack.put("spriteManager", this.spriteManager);
        worldPack.put("gameScene", this.gameScene);
        worldPack.put("inputHandler", this.inputHandler);
        worldPack.put("currentPlayer", currentPlayer);
        worldPack.put("marketEntity", marketEntity);
        worldPack.put("singleplayer", this.networker == null);
        return worldPack;
    }

    /**
     * Runnable called when the game ends normally.
     */
    private Runnable onGameEnd() {
        return () -> {
            // Set up onEnd UI
            GameOverUI gameOverUI = new GameOverUI(this, this.networker, this.worldEntity.getPlayers(), () -> this.cleanup());
            StackPane.setAlignment(gameOverUI, Pos.CENTER);
            this.interfaceOverlay.getChildren().clear();
            this.interfaceOverlay.getChildren().addAll(gameOverUI);

            // Clear borders
            AnchorPane.setTopAnchor(this.interfaceOverlay, 0.0);
            AnchorPane.setRightAnchor(this.interfaceOverlay, 0.0);
            AnchorPane.setBottomAnchor(this.interfaceOverlay, 0.0);
            AnchorPane.setLeftAnchor(this.interfaceOverlay, 0.0);

            this.worldEntity.stopAIPlayers();
            this.inputHandler.disableInput(true);
        };
    }

}