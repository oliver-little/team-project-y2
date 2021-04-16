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
import teamproject.wipeout.engine.component.PlayerAnimatorComponent;
import teamproject.wipeout.engine.component.TagComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.render.CameraComponent;
import teamproject.wipeout.engine.component.render.CameraFollowComponent;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameLoop;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.core.SystemUpdater;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.gameclock.ClockSystem;
import teamproject.wipeout.engine.entity.gameclock.ClockUI;
import teamproject.wipeout.engine.entity.gameover.GameOverUI;
import teamproject.wipeout.engine.system.audio.AudioSystem;
import teamproject.wipeout.engine.system.audio.MovementAudioSystem;
import teamproject.wipeout.engine.system.physics.CollisionSystem;
import teamproject.wipeout.engine.system.physics.MovementSystem;
import teamproject.wipeout.engine.system.render.CameraFollowSystem;
import teamproject.wipeout.engine.system.render.ParticleSystem;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.engine.system.ai.SteeringSystem;
import teamproject.wipeout.engine.system.farm.FarmSpriteSystem;
import teamproject.wipeout.engine.system.farm.GrowthSystem;
import teamproject.wipeout.engine.system.input.MouseClickSystem;
import teamproject.wipeout.engine.system.input.MouseHoverSystem;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.ItemStore;

import java.io.IOException;

import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.player.InventoryUI;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.player.InventoryItem;
import teamproject.wipeout.game.player.ui.MoneyUI;
import teamproject.wipeout.game.settings.ui.SettingsUI;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.game.task.ui.TaskUI;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.util.Networker;
//<<<<<<< HEAD
//
//import java.util.*;
//=======
import java.util.*;
//>>>>>>> develop

/**
 * App is a class for containing the components for game play.
 * It implements the Controller interface.
 *
 * Begin by creating an instance, then call init, add App to a scene, then call createContent
 *
 */
public class App implements Controller {

    private StackPane root;
    private Canvas dynamicCanvas;
    private Canvas staticCanvas;
    private StackPane interfaceOverlay;

    Double TIME_FOR_GAME = 10.0;

    // Temporarily placed variables
    ItemStore itemStore;
    private long gameStartTime;

    private ReadOnlyDoubleProperty widthProperty;
    private ReadOnlyDoubleProperty heightProperty;

    private SpriteManager spriteManager;

    private GameScene gameScene;
    private WorldEntity worldEntity;

    // Store systems for cleanup
    private final Networker networker;
    RenderSystem renderer;
    SystemUpdater systemUpdater;
    List<EventSystem> eventSystems;

    private LinkedHashMap<String, KeyCode> keyBindings;

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

        // Input
        InputHandler input = new InputHandler(root.getScene());

        this.gameScene = new GameScene();
        RenderSystem renderer = new RenderSystem(gameScene, dynamicCanvas, staticCanvas);
        SystemUpdater systemUpdater = new SystemUpdater();
        MovementAudioSystem mas = new MovementAudioSystem(gameScene, 0.05f);
        MouseHoverSystem mhs = new MouseHoverSystem(gameScene, input);
        AudioSystem audioSys = new AudioSystem(gameScene, 0.1f);
        systemUpdater.addSystem(new MovementSystem(gameScene));
        systemUpdater.addSystem(new CollisionSystem(gameScene));
        systemUpdater.addSystem(new CameraFollowSystem(gameScene));
        systemUpdater.addSystem(new FarmSpriteSystem(gameScene, spriteManager));
        systemUpdater.addSystem(mhs);
        systemUpdater.addSystem(new ParticleSystem(gameScene));
        systemUpdater.addSystem(audioSys);
        systemUpdater.addSystem(new GrowthSystem(gameScene));
        systemUpdater.addSystem(mas);
        systemUpdater.addSystem(new SteeringSystem(gameScene));
        systemUpdater.addSystem(new ScriptSystem(gameScene));
        GameLoop gl = new GameLoop(systemUpdater, renderer);

        MouseClickSystem mcs = new MouseClickSystem(gameScene, input);
        PlayerAnimatorSystem pas = new PlayerAnimatorSystem(gameScene);
        SabotageSystem sas = new SabotageSystem(gameScene);
        eventSystems = List.of(mcs, pas, sas);

        input.mouseHoverSystem = mhs;

        GameEntity camera = gameScene.createEntity();
        camera.addComponent(new Transform(0, 0));
        camera.addComponent(new CameraComponent(1.5f));
        camera.addComponent(new TagComponent("MainCamera"));

        // Inventory
        InventoryUI invUI = new InventoryUI(spriteManager, itemStore);

        // Player
        Player player = new Player(gameScene, new Random().nextInt(1024), "Farmer", new Point2D(250, 250), invUI, spriteManager);

        try {
            player.addComponent(new RenderComponent(new Point2D(0, -3)));
            player.addComponent(new PlayerAnimatorComponent(
                    spriteManager.getSpriteSet("player-red", "walk-up"),
                    spriteManager.getSpriteSet("player-red", "walk-right"),
                    spriteManager.getSpriteSet("player-red", "walk-down"),
                    spriteManager.getSpriteSet("player-red", "walk-left"),
                    spriteManager.getSpriteSet("player-red", "idle")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Camera follows player
        float cameraZoom = camera.getComponent(CameraComponent.class).zoom;
        RenderComponent targetRC = player.getComponent(RenderComponent.class);
        Point2D targetDimensions = new Point2D(targetRC.getWidth(), targetRC.getHeight()).multiply(0.5);

        // Use JavaFX binding to ensure camera is in correct position even when screen size changes
        ObjectBinding<Point2D> camPosBinding = Bindings.createObjectBinding(() -> {return new Point2D(this.widthProperty.doubleValue(), this.heightProperty.doubleValue()).multiply(-0.5).multiply(1/cameraZoom).add(targetDimensions);}, this.widthProperty, this.heightProperty);
        ObjectProperty<Point2D> camPos = new SimpleObjectProperty<>();
        camPos.bind(camPosBinding);
        camera.addComponent(new CameraFollowComponent(player, camPos));

        // Create tasks
        ArrayList<Task> allTasks = createAllTasks(itemStore);
        ArrayList<Task> playerTasks = new ArrayList<>();
        for(int t = 0; t < 7; t++) {
            playerTasks.add(allTasks.get(t));
        }
        player.setTasks(playerTasks);

        // Purchasable tasks
        ArrayList<Task> purchasableTasks = allTasks;

        //World Entity
        this.worldEntity = new WorldEntity(gameScene, 4, player, itemStore, spriteManager, this.interfaceOverlay, input, purchasableTasks);
        this.worldEntity.setupFarmPickingKey(keyBindings.get("Harvest"));
        this.worldEntity.setupFarmDestroyingKey(keyBindings.get("Destroy"));
        player.setThrownPotion((potion) ->  this.worldEntity.addPotion(potion));

        if (this.networker != null) {
            this.worldEntity.setClientSupplier(this.networker.clientSupplier);
            this.networker.setWorldEntity(this.worldEntity);
        }

        addInvUIInput(input, invUI, this.worldEntity);

        // Task UI
        TaskUI taskUI = new TaskUI(player);
        StackPane.setAlignment(taskUI, Pos.TOP_LEFT);
        player.setTaskUI(taskUI);

        // Money icon
        MoneyUI moneyUI = new MoneyUI(player);
        StackPane.setAlignment(moneyUI, Pos.TOP_CENTER);

        //Clock system
        HashMap<Integer, Player> playersNotNull = new HashMap<Integer, Player>();

        if (this.networker != null) {
            GameClient client =  this.networker.getClient();
            if(client != null) {
                playersNotNull = this.networker.getClient().players;
            }
        }
        ClockSystem clockSystem = new ClockSystem(TIME_FOR_GAME, this.gameStartTime, playersNotNull);
        systemUpdater.addSystem(clockSystem);

        // Game over UI
        GameOverUI gameOverUI = clockSystem.gameOverUI;
        gameOverUI.setVisible(false);
        gameOverUI.networker = this.networker;
        gameOverUI.gameScene = this.gameScene;
        StackPane.setAlignment(gameOverUI, Pos.CENTER);

        // Top right UI - Clock + Settings
        VBox topRight = new VBox();
        topRight.setAlignment(Pos.TOP_RIGHT);
        topRight.setPickOnBounds(false);
        topRight.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        ClockUI clockUI = clockSystem.clockUI;

        GameAudio ga = new GameAudio("backingTrack2.wav", true);
        ga.play();

        // Settings
        SettingsUI settingsUI = new SettingsUI(audioSys, mas, ga);

        StackPane.setAlignment(topRight, Pos.TOP_RIGHT);

        topRight.getChildren().addAll(clockUI, settingsUI);
        this.interfaceOverlay.getChildren().addAll(invUI, taskUI, moneyUI, topRight, gameOverUI);

        // Input key actions
        input.addKeyAction(keyBindings.get("Move left"),
                () -> player.addAcceleration(-500f, 0f),
                () -> player.addAcceleration(500f, 0f)); //moving left

        input.addKeyAction(keyBindings.get("Move right"),
                () -> player.addAcceleration(500f, 0f),
                () -> player.addAcceleration(-500f, 0f)); //moving right

        input.addKeyAction(keyBindings.get("Move up"),
                () -> player.addAcceleration(0f, -500f),
                () -> player.addAcceleration(0f, 500f)); //moving up

        input.addKeyAction(keyBindings.get("Move down"),
                () -> player.addAcceleration(0f, 500f),
                () -> player.addAcceleration(0f, -500f));

        invUI.onMouseClick(this.worldEntity);
        input.onKeyRelease(keyBindings.get("Drop"), invUI.dropOnKeyRelease(player, this.worldEntity.pickables));

        input.onKeyRelease(keyBindings.get("Pick-up"), () -> {
            this.worldEntity.pickables.picked(player.pickup());
        });

        // Networker setup
        if (this.networker != null) {
            this.setUpNetworking();
        }

        gl.start();
    }


    public ArrayList<Task> createAllTasks(ItemStore itemStore) {
        // All tasks Items
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
                    (Player inputPlayer) ->
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
                    (Player inputPlayer) ->
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


    private void addInvUIInput(InputHandler input, InventoryUI invUI, WorldEntity world) {
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
        Player myPlayer = this.worldEntity.myPlayer;
        Market myMarket = this.worldEntity.market.getMarket();

        GameClient currentClient = this.networker.getClient();
        myPlayer.setName(currentClient.clientName);
        currentClient.players.put(myPlayer.playerID, myPlayer);
        currentClient.farmEntities = this.worldEntity.farms;
        currentClient.setNewPlayerAction(this.networker.onPlayerConnection(this.gameScene, this.spriteManager));
        Integer newFarmID = currentClient.myFarmID;


        myMarket.setIsLocal(false);
        this.worldEntity.marketUpdater.stop();

        FarmEntity myFarm = this.worldEntity.farms.get(newFarmID);
        this.worldEntity.setMyFarm(myFarm);

        try {
            currentClient.send(new GameUpdate(myPlayer.getCurrentState()));

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}