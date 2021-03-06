package teamproject.wipeout;

import javafx.geometry.Point2D;
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
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.physics.Rectangle;
import teamproject.wipeout.engine.component.render.AnimatedSpriteRenderable;
import teamproject.wipeout.engine.component.render.CameraComponent;
import teamproject.wipeout.engine.component.render.InventoryRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameLoop;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.core.SystemUpdater;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.InventoryEntity;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.engine.system.farm.GrowthSystem;
import teamproject.wipeout.engine.system.input.MouseClickSystem;
import teamproject.wipeout.engine.system.input.MouseHoverSystem;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.market.entity.MarketEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketPriceUpdater;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.game.task.entity.TaskEntity;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.engine.extension.system.PlayerStateSystem;
import teamproject.wipeout.networking.server.GameServerRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Function;
import java.util.List;


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

    // Temporarily placed variables
    ItemStore itemStore;
    Market market;
    Item item;
    FarmEntity farmEntity;
    TaskEntity taskEntity;

 // Temporarily placed variables
    GameServerRunner server = new GameServerRunner();
    String playerID = UUID.randomUUID().toString();
    GameClient client;
    PlayerStateSystem playerStateSystem;

    // Store systems for cleanup
    RenderSystem renderer;
    SystemUpdater systemUpdater;
    List<EventSystem> eventSystems;

    /**
     * Creates the content to be rendered onto the canvas.
     */
    public void createContent() {
        GameScene gameScene = new GameScene();
        RenderSystem renderer = new RenderSystem(gameScene, dynamicCanvas, staticCanvas);
        SystemUpdater systemUpdater = new SystemUpdater();
        systemUpdater.addSystem(new MovementSystem(gameScene));
        systemUpdater.addSystem(new CollisionSystem(gameScene));
        systemUpdater.addSystem(new AudioSystem(gameScene));
        systemUpdater.addSystem(new GrowthSystem(gameScene));
        systemUpdater.addSystem(new CameraFollowSystem(gameScene));

        GameLoop gl = new GameLoop(systemUpdater, renderer);

        // Input
        InputHandler input = new InputHandler(root.getScene());

        MouseClickSystem mcs = new MouseClickSystem(gameScene, input);
        MouseHoverSystem mhs = new MouseHoverSystem(gameScene, input);
        PlayerAnimatorSystem pas = new PlayerAnimatorSystem(gameScene);
        eventSystems = List.of(mcs, mhs, pas);


        GameEntity camera = gameScene.createEntity();
        camera.addComponent(new Transform(0, 0));
        camera.addComponent(new CameraComponent(1));
        camera.addComponent(new TagComponent("MainCamera"));

        // Animated Sprite
        SpriteManager spriteManager = new SpriteManager();
        Player player = gameScene.createPlayer(1, "Farmer");
        player.addComponent(new Transform(250, 250, 1));

        MovementComponent playerPhysics = new MovementComponent(0f, 0f, 0f, 0f);
        player.addComponent(playerPhysics);

        player.addComponent(new HitboxComponent(new Rectangle(5, 0, 24, 33)));
        player.addComponent(new CollisionResolutionComponent());

        try {
            spriteManager.loadSpriteSheet("player/player-red-descriptor.json", "player/player-red.png");
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
        //camera.addComponent(new CameraFollowComponent(player, camPos));

        try {
            itemStore = new ItemStore("items.json");
            spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
            spriteManager.loadSpriteSheet("crops/fruit-tree-descriptor.json", "crops/FruitTrees.png");
            spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
            spriteManager.loadSpriteSheet("inventory/inventory-tools-descriptor.json", "inventory/Tools.png");
            spriteManager.loadSpriteSheet("inventory/inventory-fruit-and-vegetable-descriptor.json", "inventory/FruitsAndVeg.png");
            spriteManager.loadSpriteSheet("inventory/inventory-vegetables-descriptor.json", "inventory/Vegetables.png");
            spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
        } catch (IOException | ReflectiveOperationException exception) {
            exception.printStackTrace();
        }
        MarketEntity marketStall = new MarketEntity(gameScene, 300, 300, itemStore, player, spriteManager, this.interfaceOverlay);
        new MarketPriceUpdater(marketStall.getMarket());

        List<GameEntity> itemList = new ArrayList<>();
        GameEntity potato = gameScene.createEntity();
        potato.addComponent(new Transform (10, 10));
        potato.addComponent(new HitboxComponent(new Rectangle(0, 20, 10, 10)));
        Item potatoItem = itemStore.getItem(6); //potato id = 6
        potato.addComponent(new PickableComponent(potatoItem));
        itemList.add(potato);

        GameEntity potato2 = gameScene.createEntity();
        potato2.addComponent(new Transform (200, 300));
        potato2.addComponent(new HitboxComponent(new Rectangle(0, 20, 200, 300)));
        Item potatoItem2 = itemStore.getItem(6); //potato id = 6
        potato2.addComponent(new PickableComponent(potatoItem2));
        itemList.add(potato2);

        GameEntity potato3 = gameScene.createEntity();
        potato3.addComponent(new Transform (10, 40));
        potato3.addComponent(new HitboxComponent(new Rectangle(0, 10, 40, 20)));
        Item potatoItem3 = itemStore.getItem(6); //potato id = 6
        potato3.addComponent(new PickableComponent(potatoItem3));
        itemList.add(potato3);

        GameEntity potato4 = gameScene.createEntity();
        potato4.addComponent(new Transform (500, 10));
        potato4.addComponent(new HitboxComponent(new Rectangle(0, 20, 500, 10)));
        Item potatoItem4 = itemStore.getItem(6); //potato id = 6
        potato4.addComponent(new PickableComponent(potatoItem4));
        itemList.add(potato4);

        GameEntity lettuce = gameScene.createEntity();
        lettuce.addComponent(new Transform (500, 40));
        lettuce.addComponent(new HitboxComponent(new Rectangle(0, 20, 500, 40)));
        Item lettuceItem = itemStore.getItem(2); //lettuce id = 2
        lettuce.addComponent(new PickableComponent(lettuceItem));
        itemList.add(lettuce);

        GameEntity lettuce2 = gameScene.createEntity();
        lettuce2.addComponent(new Transform (500, 120));
        lettuce2.addComponent(new HitboxComponent(new Rectangle(0, 20, 500, 120)));
        Item lettuceItem2 = itemStore.getItem(2); //lettuce id = 2
        lettuce2.addComponent(new PickableComponent(lettuceItem2));
        itemList.add(lettuce2);

        try {
            //spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
        	InventoryComponent invComponent = potatoItem.getComponent(InventoryComponent.class);
        	System.out.println("potato: sheet, set: " + invComponent.spriteSheetName + ", " +invComponent.spriteSetName);
            Image[] frames = spriteManager.getSpriteSet(invComponent.spriteSheetName, invComponent.spriteSetName);
            potato.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
            potato2.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
            potato3.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
            potato4.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));

            invComponent = lettuceItem.getComponent(InventoryComponent.class);
            System.out.println("lettuce: sheet, set: " + invComponent.spriteSheetName + ", " +invComponent.spriteSetName);
            frames = spriteManager.getSpriteSet(invComponent.spriteSheetName, invComponent.spriteSetName);
            lettuce.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
            lettuce2.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));

        } catch (Exception e) {
            e.printStackTrace();
        }

        InventoryEntity invEntity;
    	invEntity = new InventoryEntity(gameScene, spriteManager);
    	gameScene.entities.add(invEntity);
    	invEntity.addComponent(new RenderComponent(true, new InventoryRenderable(invEntity)));

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
                () -> playerPhysics.acceleration = playerPhysics.acceleration.subtract(500f, 0f),
                () -> playerPhysics.acceleration = playerPhysics.acceleration.add(500f, 0f));

        input.addKeyAction(KeyCode.RIGHT,
                () -> playerPhysics.acceleration = playerPhysics.acceleration.add(500f, 0f),
                () -> playerPhysics.acceleration = playerPhysics.acceleration.subtract(500f, 0f));

        input.addKeyAction(KeyCode.UP,
                () -> playerPhysics.acceleration = playerPhysics.acceleration.subtract(0f, 500f),
                () -> playerPhysics.acceleration = playerPhysics.acceleration.add(0f, 500f));

        input.addKeyAction(KeyCode.DOWN,
                () -> playerPhysics.acceleration = playerPhysics.acceleration.add(0f, 500f),
                () -> playerPhysics.acceleration = playerPhysics.acceleration.subtract(0f, 500f));

        input.addKeyAction(KeyCode.X,
                () -> {player.pickup();
                	   invEntity.showItems(player.getInventory(), itemStore);
                	   taskEntity.showTasks(player.tasks); },
                () -> {});

        farmEntity = new FarmEntity(gameScene, new Point2D(150, 150), player.playerID, spriteManager, itemStore);

        item = itemStore.getItem(28);

        input.onKeyRelease(KeyCode.A, () -> {
            try {
                if (farmEntity.isPickingItem()) {
                    farmEntity.stopPickingItem();
                }
                if (farmEntity.isPlacingItem()) {
                    farmEntity.stopPlacingItem();
                } else {
                    farmEntity.startPlacingItem(item, mhs.getCurrentMousePosition());
                }
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            }
        });

        input.onKeyRelease(KeyCode.H, () -> {
            if (farmEntity.isPlacingItem()) {
                farmEntity.stopPlacingItem();
            }
            if (farmEntity.isPickingItem()) {
                farmEntity.stopPickingItem();
            } else {
                farmEntity.startPickingItem(mhs.getCurrentMousePosition());
            }
        });

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
                        LinkedHashMap<Integer, Integer> inventory = inputPlayer.getInventory();
                        return inventory.containsKey(itemId) && inventory.get(itemId) == quantityCollected;
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
}