package teamproject.wipeout;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.component.PickableComponent;
import teamproject.wipeout.engine.component.PlayerAnimatorComponent;
import teamproject.wipeout.engine.component.TagComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.Rectangle;
import teamproject.wipeout.engine.component.render.CameraComponent;
import teamproject.wipeout.engine.component.render.CameraFollowComponent;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameLoop;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.core.SystemUpdater;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.engine.system.ai.SteeringSystem;
import teamproject.wipeout.engine.system.farm.GrowthSystem;
import teamproject.wipeout.engine.system.input.MouseClickSystem;
import teamproject.wipeout.engine.system.input.MouseHoverSystem;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.AnimalEntity;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.game.player.InventoryUI;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.player.invPair;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.game.task.entity.TaskEntity;
import teamproject.wipeout.util.Networker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Random;


/**
 * App is a class for containing the components for game play.
 * It implements the Controller interface.
 *
 */
public class App implements Controller {

    private StackPane root;
    private Canvas dynamicCanvas;
    private Canvas staticCanvas;
    private StackPane interfaceOverlay;
    private double windowWidth = 800;
    private double windowHeight = 600;

    private ItemStore itemStore;
    private SpriteManager spriteManager;

    TaskEntity taskEntity;

    // Store systems for cleanup
    Networker networker;
    RenderSystem renderer;
    SystemUpdater systemUpdater;
    List<EventSystem> eventSystems;

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
        systemUpdater.addSystem(new SteeringSystem(gameScene));

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
        
        Group inventory = new Group();
        this.root.getChildren().add(inventory);
        inventory.setTranslateX(-(windowWidth/2) + 400);
    	inventory.setTranslateY((windowHeight/2) - 33);

        InventoryUI invUI = new InventoryUI(inventory, spriteManager, itemStore);
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
        Point2D camPos = new Point2D(windowWidth, windowHeight).multiply(-0.5).multiply(1/cameraZoom).add(targetDimensions);
        camera.addComponent(new CameraFollowComponent(player, camPos));

        WorldEntity world = new WorldEntity(gameScene,windowWidth,windowHeight, 2, player, itemStore, spriteManager, this.interfaceOverlay, input);
        world.networker = networker;
        world.setMyPlayer(player);
        networker.worldEntity = world;

        //Currently broken in networking mode.
        new AnimalEntity(gameScene, new Point2D(50, 50), world.getNavMesh(), spriteManager, new ArrayList<>(world.farms.values()));

        // Create tasks
        ArrayList<Task> allTasks = createAllTasks(itemStore);
        player.tasks = allTasks;

        // add task entity
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
        itemIds.add(2); // add letuce
        itemIds.add(6); // add potatos

        // Collect tasks
        for(Integer itemId : itemIds) {
            String name = itemStore.getItem(itemId).name;
            int quantityCollected = 1;
            Task currentTask =  new Task("Collect " + quantityCollected + " " + name, 5 * quantityCollected,
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
        }

        // Sell tasks
        for(Integer itemId : itemIds) {
            String name = itemStore.getItem(itemId).name;
            int quantitySold = 1;
            Task currentTask =  new Task("Sell " + quantitySold + " " + name, 10 * quantitySold,
                    (Player inputPlayer) ->
                    {
                        return inputPlayer.getSoldItems().containsKey(itemId);
                    }
            );
            tasks.add(currentTask);
        }

        return tasks;
    }

    /**
     * Gets the root node of this class.
     * @return StackPane which contains the canvas.
     */
	@Override
	public Parent getContent() {
		dynamicCanvas = new Canvas(windowWidth, windowHeight);
        staticCanvas = new Canvas(windowWidth, windowHeight);
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
        spriteManager.loadSpriteSheet("ai/mouse-descriptor.json", "ai/mouse.png");
        spriteManager.loadSpriteSheet("ai/rat-descriptor.json", "ai/rat.png");
    }

}