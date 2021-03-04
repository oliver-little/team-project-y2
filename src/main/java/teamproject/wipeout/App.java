package teamproject.wipeout;

import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.component.PickableComponent;
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
import teamproject.wipeout.engine.component.render.CameraFollowComponent;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.core.GameLoop;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.core.SystemUpdater;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.engine.entity.InventoryEntity;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.engine.system.farm.GrowthSystem;
import teamproject.wipeout.engine.system.input.MouseClickSystem;
import teamproject.wipeout.engine.system.input.MouseHoverSystem;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.game.logic.PlayerState;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.client.ServerDiscovery;
import teamproject.wipeout.networking.engine.extension.component.PlayerStateComponent;
import teamproject.wipeout.networking.engine.extension.system.PlayerStateSystem;
import teamproject.wipeout.networking.server.GameServerRunner;
import teamproject.wipeout.networking.server.ServerRunningException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


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
    Item item;
    FarmEntity farmEntity;
 // Temporarily placed variables
    GameServerRunner server = new GameServerRunner();
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

        GameEntity camera = gameScene.createEntity();
        camera.addComponent(new Transform(0, 0));
        camera.addComponent(new CameraComponent(1.5f));
        camera.addComponent(new TagComponent("MainCamera"));

        
        WorldEntity world = new WorldEntity(gameScene, 4);

        // Animated Sprite
        SpriteManager spriteManager = new SpriteManager();
        Player player = gameScene.createPlayer(1, "Farmer");
        player.addComponent(new Transform(250, 250, 1));
        
        MovementComponent playerPhysics = new MovementComponent(0f, 0f, 0f, 0f);
        player.addComponent(playerPhysics);

        player.addComponent(new HitboxComponent(new Rectangle(5, 0, 24, 33)));
        player.addComponent(new CollisionResolutionComponent());
       
        
        
        try {
            spriteManager.loadSpriteSheet("player/player-descriptor.json", "player/player-spritesheet.png");
            Image[] frames = spriteManager.getSpriteSet("player", "walk");
            player.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //camera follows player
        float cameraZoom = camera.getComponent(CameraComponent.class).zoom; 
        RenderComponent targetRC = player.getComponent(RenderComponent.class);
		Point2D targetDimensions = new Point2D(targetRC.getWidth(), targetRC.getHeight()).multiply(0.5);
        Point2D camPos = new Point2D(windowWidth, windowHeight).multiply(-0.5).multiply(1/cameraZoom).add(targetDimensions);
        camera.addComponent(new CameraFollowComponent(player, camPos));
        
        
        try {
            itemStore = new ItemStore("items.json");
            spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
            spriteManager.loadSpriteSheet("crops/fruit-tree-descriptor.json", "crops/FruitTrees.png");
            spriteManager.loadSpriteSheet("inventory/inventory-fruit-and-vegetable-descriptor.json", "inventory/FruitsAndVeg.png");
            spriteManager.loadSpriteSheet("inventory/inventory-vegetables-descriptor.json", "inventory/Vegetables.png");
        } catch (IOException | ReflectiveOperationException exception) {
            exception.printStackTrace();
        }
        
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
    	invEntity.addComponent(new RenderComponent(new InventoryRenderable(invEntity)));
                
        // Input
        InputHandler input = new InputHandler(root.getScene());

        MouseClickSystem mcs = new MouseClickSystem(gameScene, input);
        MouseHoverSystem mhs = new MouseHoverSystem(gameScene, input);
        eventSystems = List.of(new UISystem(gameScene, interfaceOverlay), mcs, mhs);

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
                () -> {player.pickup(itemList);
                	   invEntity.showItems(player.getInventory(), itemStore);},
                () -> System.out.println(""));

        //farmEntity = new FarmEntity(gameScene, new Point2D(150, 150), player.playerID, spriteManager, itemStore);

        item = itemStore.getItem(28);

        input.onKeyRelease(KeyCode.A, () -> {
            try {
                if (farmEntity.getPlacingItem() == null) {
                    farmEntity.startPlacingItem(item);
                } else {
                    farmEntity.stopPlacingItem();
                }
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            }
        });

        gl.start();
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