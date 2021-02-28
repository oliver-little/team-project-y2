package teamproject.wipeout;

import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.component.ItemComponent;
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
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameLoop;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.core.SystemUpdater;
import teamproject.wipeout.engine.entity.FarmEntity;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.InventoryEntity;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.system.AudioSystem;
import teamproject.wipeout.engine.system.CollisionSystem;
import teamproject.wipeout.engine.system.GrowthSystem;
import teamproject.wipeout.engine.system.MovementSystem;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.PlantableComponent;
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
import java.util.List;
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
    String playerID = UUID.randomUUID().toString();
    GameClient client;
    PlayerStateSystem playerStateSystem;


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

        GameLoop gl = new GameLoop(systemUpdater, renderer);

        GameEntity camera = gameScene.createEntity();
        camera.addComponent(new Transform(0, 0));
        camera.addComponent(new CameraComponent(1));
        camera.addComponent(new TagComponent("MainCamera"));

        // Animated Sprite
        SpriteManager spriteManager = new SpriteManager();
        /*
        this.playerStateSystem = new PlayerStateSystem(gameScene,
                (pState) -> {
                    GameEntity spriteEntity = gameScene.createEntity();
                    spriteEntity.addComponent(new Transform(pState.getPosition(), 0));
                    try {
                        spriteManager.loadSpriteSheet("spritesheet-descriptor.json", "spritesheet.png");
                        Image[] frames = spriteManager.getSpriteSet("player", "walk");
                        spriteEntity.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
                        spriteEntity.addComponent(new HitboxComponent(new Rectangle(34,33)));
                        spriteEntity.addComponent(new CollisionResolutionComponent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    spriteEntity.addComponent(new PlayerStateComponent(pState));
                });
        systemUpdater.addSystem(this.playerStateSystem);
        */
        Player player = gameScene.createPlayer();
        player.addComponent(new Transform(250, 250));
        
        MovementComponent playerPhysics = new MovementComponent(0f, 0f, 0f, 0f);
        player.addComponent(playerPhysics);
        
        PlayerState playerState = new PlayerState(playerID, new Point2D(60, 60));
        player.addComponent(new PlayerStateComponent(playerState));
        player.addComponent(new HitboxComponent(new Rectangle(5, 0, 24, 33)));
        player.addComponent(new CollisionResolutionComponent());
        
        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(20, 20, 0.0,1));

        MovementComponent ngePhysics = new MovementComponent(0f, 0f, 0f, 0f);
        nge.addComponent(ngePhysics);
        nge.addComponent(new HitboxComponent(new Rectangle(5, 0, 24, 33)));
        nge.addComponent(new CollisionResolutionComponent());

        try {
            spriteManager.loadSpriteSheet("player/player-descriptor.json", "player/player-spritesheet.png");
            Image[] frames = spriteManager.getSpriteSet("player", "walk");
            player.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            itemStore = new ItemStore("items.json");
            spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
            spriteManager.loadSpriteSheet("crops/fruit-tree-descriptor.json", "crops/FruitTrees.png");
            spriteManager.loadSpriteSheet("inventory/inventory-fruit-and-vegetable-descriptor.json", "inventory/FruitsAndVeg.png");
        } catch (IOException | ReflectiveOperationException exception) {
            exception.printStackTrace();
        }
        
        List<GameEntity> itemList = new ArrayList<>();

        GameEntity potato = gameScene.createEntity();
        potato.addComponent(new Transform (10, 10));
        potato.addComponent(new HitboxComponent(true, true, new Rectangle(0, 20, 10, 10)));
        Item potatoItem = itemStore.getItem(6); //potato id = 6
        potato.addComponent(new ItemComponent(potatoItem));
        itemList.add(potato);

        GameEntity potato2 = gameScene.createEntity();
        potato2.addComponent(new Transform (200, 300));
        potato2.addComponent(new HitboxComponent(true, true, new Rectangle(0, 20, 200, 300)));
        Item potatoItem2 = itemStore.getItem(6); //potato id = 6
        potato2.addComponent(new ItemComponent(potatoItem2));
        itemList.add(potato2);

        try {
            spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
            Image[] frames = spriteManager.getSpriteSet("crops", "potato");
            potato.addComponent(new RenderComponent(new SpriteRenderable(frames[2])));
            potato2.addComponent(new RenderComponent(new SpriteRenderable(frames[2])));

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        InventoryEntity invEntity;
    	invEntity = new InventoryEntity(gameScene, spriteManager);
    	gameScene.entities.add(invEntity);
    	invEntity.addComponent(new RenderComponent(new InventoryRenderable(invEntity)));
        
        // Input
        InputHandler input = new InputHandler(root.getScene());
        AudioComponent ngeSound = new AudioComponent("glassSmashing2.wav");
        nge.addComponent(ngeSound);

        input.onKeyRelease(KeyCode.D, ngeSound::play); //example - pressing the D key will trigger the sound

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

        farmEntity = new FarmEntity(gameScene, new Point2D(100, 100), "123", spriteManager, itemStore);

        item = itemStore.getItem(14);

        input.onMouseClick(MouseButton.SECONDARY, (x, y) -> {
            item = itemStore.getItem(item.id + 1);
            System.out.println(item.id);
        });

        input.onMouseClick(MouseButton.PRIMARY, (x, y) -> {
            if (farmEntity.isWithinFarm(x, y) != null) {
                try {
                    if (input.mouseHovering == null) {
                        farmEntity.pickItemAt(x, y);
                    } else {
                        farmEntity.putItem(item, x, y);
                    }
                } catch (FileNotFoundException exception) {
                    exception.printStackTrace();
                }
            }
        });

        GameEntity shadow = gameScene.createEntity();
        try {
            PlantableComponent crop = item.getComponent(PlantableComponent.class);
            Image sprite = spriteManager.getSpriteSet(crop.seedSpriteSheetName, crop.seedSpriteSetName)[0];
            shadow.addComponent(new RenderComponent(new SpriteRenderable(sprite)));

            input.onKeyRelease(KeyCode.A, () -> {
                if (input.mouseHovering == null) {
                    input.onMouseHover((x, y) -> {
                        Point2D point = farmEntity.isWithinFarm(x, y);
                        Transform transform = shadow.getComponent(Transform.class);
                        if (transform == null) {
                            shadow.addComponent(new Transform(x, y, 0.0, 2));
                            transform = shadow.getComponent(Transform.class);
                        }
                        if (point == null || !farmEntity.isEmpty(x, y)) {
                            transform.setPosition(new Point2D(x - sprite.getWidth()/3, y - sprite.getHeight()/3));
                        } else {
                            transform.setPosition(point);
                        }
                    });
                } else {
                    shadow.removeComponent(Transform.class);
                    input.removeMouseHover();
                }
            });

        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        }

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
        root = new StackPane(interfaceOverlay, dynamicCanvas, staticCanvas);
		return root;
	}

}