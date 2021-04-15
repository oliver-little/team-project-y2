package teamproject.wipeout;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.component.TagComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.CameraComponent;
import teamproject.wipeout.engine.component.render.CameraFollowComponent;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameLoop;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.core.SystemUpdater;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.gameclock.ClockSystem;
import teamproject.wipeout.engine.entity.gameclock.ClockUI;
import teamproject.wipeout.engine.input.InputHandler;
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
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketPriceUpdater;
import teamproject.wipeout.game.market.entity.MarketEntity;
import teamproject.wipeout.game.player.InventoryItem;
import teamproject.wipeout.game.player.InventoryUI;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.game.player.ui.MoneyUI;
import teamproject.wipeout.game.settings.ui.SettingsUI;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.game.task.ui.TaskUI;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.util.Networker;

import java.io.IOException;
import java.util.*;

/**
 * App is a class for containing the components for game play.
 * It implements the Controller interface.
 * 
 * Begin by creating an instance, then call init, add App to a scene, then call createContent
 *
 */
public class App implements Controller {

    public static final float CAMERA_ZOOM = 1.5f;

    private StackPane root;
    private Canvas dynamicCanvas;
    private Canvas staticCanvas;
    private StackPane interfaceOverlay;

    Double TIME_FOR_GAME = 500.0;
    private long gameStartTime;

    private ReadOnlyDoubleProperty widthProperty;
    private ReadOnlyDoubleProperty heightProperty;

    private SpriteManager spriteManager;
    private ItemStore itemStore;

    private GameScene gameScene;
    private WorldEntity worldEntity;

    private RenderSystem renderer;
    private SystemUpdater systemUpdater;

    private AudioSystem audioSystem;
    private MovementAudioSystem movementAudio;
    private List<EventSystem> eventSystems;

    private LinkedHashMap<String, KeyCode> keyBindings;

    private final Networker networker;

    public App(Networker networker, Long givenGameStartTime, LinkedHashMap<String, KeyCode> bindings) {
        this.networker = networker;
        this.keyBindings = bindings;

        if (givenGameStartTime == null) {
            this.gameStartTime = System.currentTimeMillis();
        } else {
            this.gameStartTime = givenGameStartTime;
        }
    }

    public Parent getParentWith(ReadOnlyDoubleProperty widthProperty, ReadOnlyDoubleProperty heightProperty) {
        this.widthProperty = widthProperty;
        this.heightProperty = heightProperty;
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
        GameLoop gameLoop = new GameLoop(this.systemUpdater, this.renderer);

        // Input
        InputHandler input = new InputHandler(this.root.getScene());

        MouseClickSystem mouseClick = new MouseClickSystem(this.gameScene, input);
        MouseHoverSystem mouseHover = new MouseHoverSystem(this.gameScene, input);
        input.mouseHoverSystem = mouseHover;
        systemUpdater.addSystem(mouseHover);

        // Effects
        PlayerAnimatorSystem playerAnimator = new PlayerAnimatorSystem(this.gameScene);
        SabotageSystem sabotage = new SabotageSystem(this.gameScene);
        this.eventSystems = List.of(mouseClick, playerAnimator, sabotage);

        // Player
        InventoryUI inventoryUI = new InventoryUI(this.spriteManager, this.itemStore);
        int playerID = new Random().nextInt(1024);
    	CurrentPlayer currentPlayer = new CurrentPlayer(this.gameScene, playerID, "Farmer", this.spriteManager, this.itemStore, inventoryUI);

        // Camera
        GameEntity camera = this.gameScene.createEntity();
        camera.addComponent(new Transform(0, 0));
        camera.addComponent(new CameraComponent(CAMERA_ZOOM));
        camera.addComponent(new TagComponent("MainCamera"));

        // Use JavaFX binding to ensure camera is in correct position even when screen size changes
        RenderComponent targetRenderer = currentPlayer.getComponent(RenderComponent.class);
        Point2D targetDimensions = new Point2D(targetRenderer.getWidth(), targetRenderer.getHeight()).multiply(0.5);
        ObjectBinding<Point2D> camPosBinding = Bindings.createObjectBinding(
                () -> new Point2D(this.widthProperty.doubleValue(), this.heightProperty.doubleValue())
                        .multiply(-0.5).multiply(1/CAMERA_ZOOM).add(targetDimensions),
                this.widthProperty,
                this.heightProperty
        );
        ObjectProperty<Point2D> camPos = new SimpleObjectProperty<>();
        camPos.bind(camPosBinding);
        camera.addComponent(new CameraFollowComponent(currentPlayer, camPos));

        // Create tasks
        ArrayList<Task> allTasks = createAllTasks(itemStore);
        ArrayList<Task> playerTasks = new ArrayList<>();
        for(int t = 0; t < 7; t++) {
            playerTasks.add(allTasks.get(t));
        }
        currentPlayer.setTasks(playerTasks);

        ArrayList<Task> purchasableTasks = this.populateTasks(currentPlayer);

        // Market Entity
        MarketEntity marketEntity = new MarketEntity(gameScene, WorldEntity.MARKET_SIZE.getX(), WorldEntity.MARKET_SIZE.getY(),
                itemStore, currentPlayer, spriteManager, this.interfaceOverlay, purchasableTasks);
        marketEntity.setOnUIOpen(() -> input.setDisableInput(true));
        marketEntity.setOnUIClose(() -> input.setDisableInput(false));

        // World Entity
        HashMap<String, Object> worldPack = new HashMap<String, Object>();
        worldPack.put("gameScene", this.gameScene);
        worldPack.put("players", 4);
        worldPack.put("currentPlayer", currentPlayer);
        worldPack.put("marketEntity", marketEntity);
        worldPack.put("itemStore", this.itemStore);
        worldPack.put("spriteManager", this.spriteManager);
        worldPack.put("inputHandler", input);

        this.worldEntity = new WorldEntity(worldPack);
        this.worldEntity.setupFarmPickingKey(keyBindings.get("Harvest"));
        this.worldEntity.setupFarmDestroyingKey(keyBindings.get("Destroy"));
        currentPlayer.setThrownPotion((potion) ->  this.worldEntity.addPotion(potion));

        if (this.networker != null) {
            this.worldEntity.setClientSupplier(this.networker.clientSupplier);
            this.networker.setWorldEntity(this.worldEntity);
        }
        
        this.addKeyboardInput(input, inventoryUI, this.worldEntity);

        // Task UI
        TaskUI taskUI = new TaskUI(currentPlayer);
        StackPane.setAlignment(taskUI, Pos.TOP_LEFT);
        currentPlayer.setTaskUI(taskUI);

        // Money icon
        MoneyUI moneyUI = new MoneyUI(currentPlayer);
        StackPane.setAlignment(moneyUI, Pos.TOP_CENTER);

        //Time left
        ClockSystem clockSystem = new ClockSystem(TIME_FOR_GAME, this.gameStartTime);
        this.systemUpdater.addSystem(clockSystem);
        this.worldEntity.setClockSupplier(() -> clockSystem);

        VBox topRight = new VBox();
        topRight.setAlignment(Pos.TOP_RIGHT);
        topRight.setPickOnBounds(false);
        topRight.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        ClockUI clockUI = clockSystem.clockUI;
        //StackPane.setAlignment(clockUI, Pos.TOP_RIGHT);

        GameAudio gameAudio = new GameAudio("backingTrack2.wav", true);
        gameAudio.play();

        SettingsUI settingsUI = new SettingsUI(this.audioSystem, this.movementAudio, gameAudio);
        //StackPane.setAlignment(settingsUI, Pos.CENTER_RIGHT);

        StackPane.setAlignment(topRight, Pos.TOP_RIGHT);

        topRight.getChildren().addAll(clockUI, settingsUI);
        this.interfaceOverlay.getChildren().addAll(inventoryUI, taskUI, moneyUI, topRight);

        input.addKeyAction(keyBindings.get("Move left"),
                () -> currentPlayer.addAcceleration(-500f, 0f),
                () -> currentPlayer.addAcceleration(500f, 0f)); //moving left

        input.addKeyAction(keyBindings.get("Move right"),
                () -> currentPlayer.addAcceleration(500f, 0f),
                () -> currentPlayer.addAcceleration(-500f, 0f)); //moving right

        input.addKeyAction(keyBindings.get("Move up"),
                () -> currentPlayer.addAcceleration(0f, -500f),
                () -> currentPlayer.addAcceleration(0f, 500f)); //moving up

        input.addKeyAction(keyBindings.get("Move down"),
                () -> currentPlayer.addAcceleration(0f, 500f),
                () -> currentPlayer.addAcceleration(0f, -500f));

        inventoryUI.onMouseClick(this.worldEntity);
        input.onKeyRelease(keyBindings.get("Drop"), inventoryUI.dropOnKeyRelease(currentPlayer, this.worldEntity.pickables));

        input.onKeyRelease(keyBindings.get("Pick-up"), () -> this.worldEntity.pickables.picked(currentPlayer.pickup()));

        if (this.networker != null) {
            this.setUpNetworking();
        }

        //currentPlayer.acquireItem(6, 98); //for checking stack/inventory limits
        //currentPlayer.acquireItem(1, 2);
        //currentPlayer.acquireItem(28, 98);
        //currentPlayer.acquireItem( 43, 2);

        gameLoop.start();
    }

    private ArrayList<Task> populateTasks(CurrentPlayer currentPlayer) {
        ArrayList<Task> allTasks = createAllTasks(this.itemStore);

        ArrayList<Task> playerTasks = new ArrayList<Task>();
        for(int t = 0; t < 7; t++) {
            playerTasks.add(allTasks.get(t));
        }
        currentPlayer.setTasks(playerTasks);

        return allTasks;
    }
    
    private ArrayList<Task> createAllTasks(ItemStore itemStore) {

        ArrayList<Task> tasks = new ArrayList<>();
        ArrayList<Integer> itemIds  = new ArrayList<>();
        for(int i = 1; i < 25; i++) {
            if(itemStore.getItem(i) != null) {
                itemIds.add(i);
            }
        }

        int nrOfTask = 0;
        // Collect tasks
        Integer reward = 5;
        for(Integer itemId : itemIds) {
            String name = itemStore.getItem(itemId).name;
            int quantityCollected = 1;
            Task currentTask =  new Task(nrOfTask, "Collect " + quantityCollected + " " + name, reward * quantityCollected,
                    (CurrentPlayer inputPlayer) ->
                    {
                    	ArrayList<InventoryItem> inventoryList = inputPlayer.getInventory();
                    	int index = inputPlayer.containsItem(itemId);
                    	if(index >= 0 && inventoryList.get(index).quantity >= quantityCollected) {
                    		return true;
                    	}
                    	return false;
                    },
                    itemStore.getItem(itemId)
            );
            tasks.add(currentTask);
            nrOfTask += 1;
        }

        // Sell tasks
        reward = 2;
        for(Integer itemId : itemIds) {
            String name = itemStore.getItem(itemId).name;
            int quantitySold = 1;
            Task currentTask =  new Task(nrOfTask, "Sell " + quantitySold + " " + name, reward * quantitySold,
                    (CurrentPlayer inputPlayer) ->
                    {
                        return inputPlayer.getSoldItems().containsKey(itemId);
                    },
                    itemStore.getItem(itemId)
            );
            tasks.add(currentTask);
            nrOfTask += 1;
        }

        Collections.shuffle(tasks);
        return tasks;
    }

    /**
     * Gets the root node of this class.
     * @return StackPane which contains the canvas.
     */
	@Override
	public Parent getContent() {
		dynamicCanvas = new Canvas();
        staticCanvas = new Canvas();
        if (widthProperty != null) {
            dynamicCanvas.widthProperty().bind(this.widthProperty);
            staticCanvas.widthProperty().bind(this.widthProperty);
        }
        else {
            dynamicCanvas.setWidth(800);
            staticCanvas.setWidth(800);
        }
        if (heightProperty != null) {
            dynamicCanvas.heightProperty().bind(this.heightProperty);
            staticCanvas.heightProperty().bind(this.heightProperty);
        }
        else {
            dynamicCanvas.setHeight(600);
            staticCanvas.setHeight(600);
        }

        interfaceOverlay = new StackPane();
        AnchorPane anchorPane = new AnchorPane();
        
        anchorPane.getChildren().add(interfaceOverlay);
        AnchorPane.setTopAnchor(interfaceOverlay, 10.0);
        AnchorPane.setRightAnchor(interfaceOverlay, 10.0);
        AnchorPane.setBottomAnchor(interfaceOverlay, 10.0);
        AnchorPane.setLeftAnchor(interfaceOverlay, 10.0);

        root = new StackPane(dynamicCanvas, staticCanvas, anchorPane);
		return root;
	}

    public void cleanup() {
        if (renderer != null) {
            renderer.cleanup();
        }
        if (systemUpdater != null) {
            systemUpdater.cleanup();
        }
        if (eventSystems != null) {
            for (EventSystem eventSystem : eventSystems) {
                eventSystem.cleanup();
            }
        }
    }

    private void loadSpriteSheets() throws IOException {
        spriteManager.loadSpriteSheet("player/player-red-descriptor.json", "player/player-red.png");
        spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
        spriteManager.loadSpriteSheet("crops/fruit-tree-descriptor.json", "crops/FruitTrees.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
        spriteManager.loadSpriteSheet("inventory/inventory-tools-descriptor.json", "inventory/Tools.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-and-vegetable-descriptor.json", "inventory/FruitsAndVeg.png");
        spriteManager.loadSpriteSheet("inventory/inventory-vegetables-descriptor.json", "inventory/Vegetables.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
        spriteManager.loadSpriteSheet("inventory/inventory-animals-and-food-descriptor.json", "inventory/AnimalsAndFood.png");
        spriteManager.loadSpriteSheet("inventory/inventory-potions-descriptor.json", "inventory/Potions.png");
        spriteManager.loadSpriteSheet("ai/mouse-descriptor.json", "ai/mouse.png");
        spriteManager.loadSpriteSheet("ai/rat-descriptor.json", "ai/rat.png");
        spriteManager.loadSpriteSheet("gameworld/arrow-descriptor.json", "gameworld/Arrow.png");
    }

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

        this.audioSystem = new AudioSystem(this.gameScene, 0.1f);
        this.movementAudio = new MovementAudioSystem(this.gameScene, 0.05f);
        this.systemUpdater.addSystem(this.audioSystem);
        this.systemUpdater.addSystem(this.movementAudio);
    }

    private void addKeyboardInput(InputHandler input, InventoryUI invUI, WorldEntity world) {
        input.addKeyAction(KeyCode.DIGIT1,
                () -> invUI.useSlot(0, world),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT2,
                () -> invUI.useSlot(1, world),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT3,
                () -> invUI.useSlot(2, world),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT4,
                () -> invUI.useSlot(3, world),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT5,
                () -> invUI.useSlot(4, world),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT6,
                () -> invUI.useSlot(5, world),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT7,
                () -> invUI.useSlot(6, world),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT8,
                () -> invUI.useSlot(7, world),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT9,
                () -> invUI.useSlot(8, world),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT0,
                () -> invUI.useSlot(9, world),
                () -> {});
    }

    private void setUpNetworking() {
        CurrentPlayer myCurrentPlayer = this.worldEntity.myCurrentPlayer;
        Market myMarket = this.worldEntity.getMarket();

        GameClient currentClient = this.networker.getClient();
        currentClient.players.put(myCurrentPlayer.playerID, myCurrentPlayer);
        currentClient.farmEntities = this.worldEntity.farms;
        currentClient.setNewPlayerAction(this.networker.onPlayerConnection(this.gameScene, this.itemStore, this.spriteManager));
        Integer newFarmID = currentClient.myFarmID;

        myMarket.setIsLocal(false);
        this.worldEntity.marketUpdater.stop();

        FarmEntity myFarm = this.worldEntity.farms.get(newFarmID);
        this.worldEntity.setFarmFor(myCurrentPlayer, true, myFarm);

        try {
            currentClient.send(new GameUpdate(myCurrentPlayer.getCurrentState()));

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}