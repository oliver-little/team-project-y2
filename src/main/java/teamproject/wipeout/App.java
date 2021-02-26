package teamproject.wipeout;

import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.component.TagComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.physics.CollisionComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.*;
import teamproject.wipeout.engine.core.GameLoop;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.core.SystemUpdater;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.InventoryEntity;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.system.AudioSystem;
import teamproject.wipeout.engine.system.CollisionSystem;
import teamproject.wipeout.engine.system.MovementSystem;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.logic.PlayerState;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.client.ServerDiscovery;
import teamproject.wipeout.networking.engine.extension.component.PlayerStateComponent;
import teamproject.wipeout.networking.engine.extension.system.PlayerStateSystem;
import teamproject.wipeout.networking.server.GameServerRunner;
import teamproject.wipeout.networking.server.ServerRunningException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;


/**
 * App is a class for containing the components for game play.
 * It implements the Controller interface.
 *
 */
public class App implements Controller {

    private StackPane root;
    private Canvas canvas;
    private double windowWidth = 800;
    private double windowHeight = 600;

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
        RenderSystem renderer = new RenderSystem(gameScene, canvas);
        SystemUpdater systemUpdater = new SystemUpdater();
        systemUpdater.addSystem(new AudioSystem(gameScene));
        systemUpdater.addSystem(new MovementSystem(gameScene));
        systemUpdater.addSystem(new CollisionSystem(gameScene));

        GameLoop gl = new GameLoop(systemUpdater, renderer);

        GameEntity camera = gameScene.createEntity();
        camera.addComponent(new Transform(0, 0));
        camera.addComponent(new CameraComponent(1));
        camera.addComponent(new TagComponent("MainCamera"));

        //GameEntity bigBall = gameScene.createEntity();
        //bigBall.addComponent(new Transform(25, 125));
        //bigBall.addComponent(new CircleRenderComponent(Color.BLACK, 50, 50));
        //bigBall.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        //bigBall.addComponent(new CollisionComponent(new Circle(25,25,25)));

        
        GameEntity rec = gameScene.createEntity();
        rec.addComponent(new Transform(100, 125));
        rec.addComponent(new RenderComponent(new RectRenderable(Color.BLACK, 40, 60)));
        rec.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        rec.addComponent(new CollisionComponent(new Rectangle(40,60)));
        
        GameEntity rec2 = gameScene.createEntity();
        rec2.addComponent(new Transform(200, 70));
        rec2.addComponent(new RenderComponent(new RectRenderable(Color.RED, 100, 20)));
        rec2.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        rec2.addComponent(new CollisionComponent(new Rectangle(100,20)));
        
        GameEntity rec3 = gameScene.createEntity();
        rec3.addComponent(new Transform(300, 300));
        rec3.addComponent(new RenderComponent(new RectRenderable(Color.GREEN, 150, 150)));
        rec3.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        rec3.addComponent(new CollisionComponent(false, new Rectangle(150,150)));
        
        
        // Animated Sprite
        SpriteManager spriteManager = new SpriteManager();

        this.playerStateSystem = new PlayerStateSystem(gameScene,
                (pState) -> {
                    GameEntity spriteEntity = gameScene.createEntity();
                    spriteEntity.addComponent(new Transform(pState.getPosition(), 0));
                    try {
                        spriteManager.loadSpriteSheet("spritesheet-descriptor.json", "spritesheet.png");
                        Image[] frames = spriteManager.getSpriteSet("player", "walk");
                        spriteEntity.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
                        spriteEntity.addComponent(new CollisionComponent(new Rectangle(34,33)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    spriteEntity.addComponent(new PlayerStateComponent(pState));
                });
        systemUpdater.addSystem(this.playerStateSystem);


        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(250, 250));

        MovementComponent ngePhysics = new MovementComponent(0f, 0f, 0f, 0f);
        nge.addComponent(ngePhysics);
        nge.addComponent(new CollisionComponent(new Rectangle(34, 33)));
        PlayerState playerState = new PlayerState(playerID, new Point2D(60, 60));
        nge.addComponent(new PlayerStateComponent(playerState));


        try {
            spriteManager.loadSpriteSheet("spritesheet-descriptor.json", "spritesheet.png");
            Image[] frames = spriteManager.getSpriteSet("player", "walk");
            nge.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        GameEntity potato = gameScene.createEntity();
        potato.addComponent(new Transform (10, 10));
        potato.addComponent(new CollisionComponent(true, true, new Rectangle(10, 10)));

        try {
            spriteManager.loadSpriteSheet("crops-descriptor.json", "crops.png");
            Image[] frames = spriteManager.getSpriteSet("crops", "potato");
            potato.addComponent(new RenderComponent(new SpriteRenderable(frames[0])));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        InventoryEntity[] inventorySlots = new InventoryEntity[10];
        
        for(int i = 0; i < 10; i++) {
	        InventoryEntity inventory = new InventoryEntity(gameScene, 20 + (75*i), 500);
	        inventorySlots[i] = inventory;
	        gameScene.entities.add(inventory);
        }
        
        // Input
        InputHandler input = new InputHandler(root.getScene());

        AudioComponent ngeSound = new AudioComponent("glassSmashing2.wav");
        //nge.addComponent(ngeSound);

        input.onKeyRelease(KeyCode.D, ngeSound::play); //example - pressing the D key will trigger the sound
        
        GameAudio ga = new GameAudio("backingTrack2.wav");
        input.onKeyRelease(KeyCode.P, ga::stopStart); //example - pressing the P key will switch between stop and start
        
        input.addKeyAction(KeyCode.LEFT,
                () -> ngePhysics.acceleration = ngePhysics.acceleration.subtract(500f, 0f),
                () -> ngePhysics.acceleration = ngePhysics.acceleration.add(500f, 0f));

        input.addKeyAction(KeyCode.RIGHT,
                () -> ngePhysics.acceleration = ngePhysics.acceleration.add(500f, 0f),
                () -> ngePhysics.acceleration = ngePhysics.acceleration.subtract(500f, 0f));

        input.addKeyAction(KeyCode.UP,
                () -> ngePhysics.acceleration = ngePhysics.acceleration.subtract(0f, 500f),
                () -> ngePhysics.acceleration = ngePhysics.acceleration.add(0f, 500f));

        input.addKeyAction(KeyCode.DOWN,
                () -> ngePhysics.acceleration = ngePhysics.acceleration.add(0f, 500f),
                () -> ngePhysics.acceleration = ngePhysics.acceleration.subtract(0f, 500f));

        
        gl.start();

        input.onKeyRelease(KeyCode.S, () -> {
            try {
                if (this.server.isServerActive()) {
                    this.server.stopServer();
                } else {
                    this.server.startServer("ServerName");
                    System.out.println("Server Started");
                }
            } catch (ServerRunningException | IOException exception) {
                exception.printStackTrace();
            }
        });

        input.onKeyRelease(KeyCode.Q, () -> {
            try {
                this.client.closeConnection(true);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        try {
            ServerDiscovery serverDiscovery = new ServerDiscovery((name, address) -> {
                System.out.println(name + " : " + address.getHostAddress());
            });

            input.onKeyRelease(KeyCode.C, () -> {
                try {
                    if (serverDiscovery.getIsActive()) {
                        serverDiscovery.stopLookingForServers();
                        InetSocketAddress foundAddress = (InetSocketAddress) serverDiscovery.getFoundServers().values().toArray()[0];
                        this.client = GameClient.openConnection(playerState, foundAddress);
                        this.playerStateSystem.setClient(this.client);
                    } else {
                        serverDiscovery.startLookingForServers();
                    }
                } catch (IOException | ClassNotFoundException exception) {
                    exception.printStackTrace();
                }
            });
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    /**
     * Gets the root node of this class.
     * @return StackPane which contains the canvas.
     */
	@Override
	public Parent getContent()
	{
		canvas = new Canvas(windowWidth, windowHeight);
        root = new StackPane(canvas);
		return root;
	}
}