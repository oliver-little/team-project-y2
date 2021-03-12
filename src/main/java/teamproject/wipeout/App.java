package teamproject.wipeout;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
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
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.engine.system.farm.GrowthSystem;
import teamproject.wipeout.engine.system.input.MouseClickSystem;
import teamproject.wipeout.engine.system.input.MouseHoverSystem;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.player.InventoryUI;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.player.invPair;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.game.task.entity.TaskEntity;
import teamproject.wipeout.util.Networker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


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

    private ItemStore itemStore;
    private SpriteManager spriteManager;

    private ReadOnlyDoubleProperty widthProperty;
    private ReadOnlyDoubleProperty heightProperty;

    TaskEntity taskEntity;

    // Store systems for cleanup
    Networker networker;
    RenderSystem renderer;
    SystemUpdater systemUpdater;
    List<EventSystem> eventSystems;

    public Parent init(ReadOnlyDoubleProperty widthProperty, ReadOnlyDoubleProperty heightProperty) {
        this.widthProperty = widthProperty;
        this.heightProperty = heightProperty;
        Parent contentRoot = this.getContent();
        return contentRoot;
    }

    /**
     * Creates the content to be rendered onto the canvas.
     */
    public void createContent() {
        this.networker = new Networker();

        try {
            this.itemStore = new ItemStore("items.json");
            this.spriteManager = new SpriteManager();
            this.loadSpriteSheets();

        } catch (IOException | ReflectiveOperationException exception) {
            exception.printStackTrace();
        }

        GameScene gameScene = new GameScene();
        RenderSystem renderer = new RenderSystem(gameScene, dynamicCanvas, staticCanvas);
        SystemUpdater systemUpdater = new SystemUpdater();
        systemUpdater.addSystem(new MovementSystem(gameScene));
        systemUpdater.addSystem(new CollisionSystem(gameScene));
        systemUpdater.addSystem(new AudioSystem(gameScene));
        systemUpdater.addSystem(new GrowthSystem(gameScene));
        systemUpdater.addSystem(new CameraFollowSystem(gameScene));
        systemUpdater.addSystem(new ScriptSystem(gameScene));

        GameLoop gl = new GameLoop(systemUpdater, renderer);

        // Input
        InputHandler input = new InputHandler(root.getScene());

        MouseClickSystem mcs = new MouseClickSystem(gameScene, input);
        MouseHoverSystem mhs = new MouseHoverSystem(gameScene, input);
        PlayerAnimatorSystem pas = new PlayerAnimatorSystem(gameScene);
        eventSystems = List.of(mcs, mhs, pas);

        input.mouseHoverSystem = mhs;

        GameEntity camera = gameScene.createEntity();
        camera.addComponent(new Transform(0, 0));
        camera.addComponent(new CameraComponent(1.5f));
        camera.addComponent(new TagComponent("MainCamera"));
        

        InventoryUI invUI = new InventoryUI(spriteManager, itemStore);
        this.interfaceOverlay.getChildren().add(invUI);
        
        addInvUIInput(input, invUI);
        

    	Player player = gameScene.createPlayer(new Random().nextInt(1024), "Farmer", new Point2D(250, 250), invUI);
        
        player.acquireItem(6, 98); //for checking stack/inventory limits
        player.acquireItem(1, 2);
        player.acquireItem(28, 98);
        player.acquireItem( 43, 2);


        try {
            player.addComponent(new RenderComponent());
            player.addComponent(new PlayerAnimatorComponent(
                spriteManager.getSpriteSet("player-red", "walk-up"), 
                spriteManager.getSpriteSet("player-red", "walk-right"), 
                spriteManager.getSpriteSet("player-red", "walk-down"), 
                spriteManager.getSpriteSet("player-red", "walk-left"), 
                spriteManager.getSpriteSet("player-red", "idle")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //camera follows player
        float cameraZoom = camera.getComponent(CameraComponent.class).zoom;
        RenderComponent targetRC = player.getComponent(RenderComponent.class);
		Point2D targetDimensions = new Point2D(targetRC.getWidth(), targetRC.getHeight()).multiply(0.5);

        // Use JavaFX binding to ensure camera is in correct position even when screen size changes
        ObjectBinding<Point2D> camPosBinding = Bindings.createObjectBinding(() -> {return new Point2D(this.widthProperty.doubleValue(), this.heightProperty.doubleValue()).multiply(-0.5).multiply(1/cameraZoom).add(targetDimensions);}, this.widthProperty, this.heightProperty);
        ObjectProperty<Point2D> camPos = new SimpleObjectProperty<>();
        camPos.bind(camPosBinding);
        camera.addComponent(new CameraFollowComponent(player, camPos));

        WorldEntity world = new WorldEntity(gameScene, this.widthProperty.doubleValue(), this.heightProperty.doubleValue(), 2, player, itemStore, spriteManager, this.interfaceOverlay, input);
        world.networker = networker;
        world.setMyPlayer(player);
        networker.worldEntity = world;

        // Tasks
        ArrayList<Task> allTasks = createAllTasks(itemStore);
//        ArrayList<Task> playerTasks = new ArrayList<>();
        player.tasks = allTasks;

        taskEntity = new TaskEntity(gameScene, 10, 100, player);

        gameScene.entities.add(taskEntity);

        AudioComponent playerSound = new AudioComponent("glassSmashing2.wav");
        player.addComponent(playerSound);

        input.onKeyRelease(KeyCode.D, playerSound::play); //example - pressing the D key will trigger the sound

        GameAudio ga = new GameAudio("backingTrack2.wav");
        input.onKeyRelease(KeyCode.P, ga::stopStart); //example - pressing the P key will switch between stop and start

        input.addKeyAction(KeyCode.LEFT,
                () -> player.addAcceleration(-500f, 0f),
                () -> player.addAcceleration(500f, 0f));

        input.addKeyAction(KeyCode.RIGHT,
                () -> player.addAcceleration(500f, 0f),
                () -> player.addAcceleration(-500f, 0f));

        input.addKeyAction(KeyCode.UP,
                () -> player.addAcceleration(0f, -500f),
                () -> player.addAcceleration(0f, 500f));

        input.addKeyAction(KeyCode.DOWN,
                () -> player.addAcceleration(0f, 500f),
                () -> player.addAcceleration(0f, -500f));


        input.onKeyRelease(KeyCode.S, networker.startServer("ServerName"));
        input.onKeyRelease(KeyCode.C, networker.initiateClient(gameScene, spriteManager));


        invUI.onMouseClick(world);
        input.onKeyRelease(KeyCode.U, invUI.dropOnKeyRelease(gameScene, player));
        
        input.addKeyAction(KeyCode.X,
                () -> {player.pickup();
                	   taskEntity.showTasks(player.tasks); },
                () -> {});

        gl.start();
    }

    
    public ArrayList<Task> createAllTasks(ItemStore itemStore) {

        ArrayList<Task> tasks = new ArrayList<>();
        ArrayList<Integer> itemIds  = new ArrayList<>();
        itemIds.add(2);
        itemIds.add(6);

        int nrOfTask = 0;
        // Collect taskk
        for(Integer itemId : itemIds) {
            String name = itemStore.getItem(itemId).name;
            int quantityCollected = 1;
            Task currentTask =  new Task(nrOfTask, "Collect " + quantityCollected + " " + name, 5 * quantityCollected,
                    (Player inputPlayer) ->
                    {
                    	ArrayList<invPair> inventoryList = inputPlayer.getInventory();
                        //LinkedHashMap<Integer, Integer> inventory = inputPlayer.getInventory();  //inventory is now an ArrayList
                    	int index = inputPlayer.containsItem(itemId);
                    	if(index >= 0 && inventoryList.get(index).quantity >= quantityCollected) {
                    		return true;
                    	}
                    	return false;
                        //return inventory.containsKey(itemId) && inventory.get(itemId) == quantityCollected;
                    }
            );
            tasks.add(currentTask);
            nrOfTask += 1;
        }

        // Sell tasks
        for(Integer itemId : itemIds) {
            String name = itemStore.getItem(itemId).name;
            int quantitySold = 1;
            Task currentTask =  new Task(nrOfTask, "Sell " + quantitySold + " " + name, 10 * quantitySold,
                    (Player inputPlayer) ->
                    {
                        return inputPlayer.getSoldItems().containsKey(itemId);
                    }
            );
            tasks.add(currentTask);
            nrOfTask += 1;
        }

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
        root = new StackPane(dynamicCanvas, staticCanvas, interfaceOverlay);
		return root;
	}

    public void cleanup() {
        try {
            this.networker.getClient().closeConnection(true);
            this.networker.stopServer();

        } catch (IOException exception) {
            exception.printStackTrace();
        }

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
    }
    
    private void addInvUIInput(InputHandler input, InventoryUI invUI) {
        input.addKeyAction(KeyCode.DIGIT1,
                () -> invUI.selectSlot(0),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT2,
                () -> invUI.selectSlot(1),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT3,
                () -> invUI.selectSlot(2),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT4,
                () -> invUI.selectSlot(3),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT5,
                () -> invUI.selectSlot(4),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT6,
                () -> invUI.selectSlot(5),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT7,
                () -> invUI.selectSlot(6),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT8,
                () -> invUI.selectSlot(7),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT9,
                () -> invUI.selectSlot(8),
                () -> {});
        input.addKeyAction(KeyCode.DIGIT0,
                () -> invUI.selectSlot(9),
                () -> {});
    }

}