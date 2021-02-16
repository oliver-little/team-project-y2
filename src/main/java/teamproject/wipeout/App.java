package teamproject.wipeout;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
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
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.system.AudioSystem;
import teamproject.wipeout.engine.system.CollisionSystem;
import teamproject.wipeout.engine.system.MovementSystem;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;


/**
 * App is a class for containing the components for game play.
 * It implements the Controller interface.
 *
 */
public class App implements Controller {

    public String imgPath = "./assets/";
    private StackPane root;
    private Canvas canvas;
    private double windowWidth = 800;
    private double windowHeight = 600;
    
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

        // Animated Sprite
        SpriteManager spriteManager = new SpriteManager();

        GameEntity staticsprite = gameScene.createEntity();
        staticsprite.addComponent(new Transform(275, 250));
        try {
            staticsprite.addComponent(new RenderComponent(new SpriteRenderable(spriteManager.getImage(imgPath + "sprite.png"))));
            staticsprite.addComponent(new CollisionComponent(false, new Rectangle(50,50)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(250, 250));

        MovementComponent ngePhysics = new MovementComponent(0f, 0f, 0f, 0f);
        nge.addComponent(ngePhysics);
        nge.addComponent(new CollisionComponent(new Rectangle(50, 50)));

        try {
            spriteManager.loadSpriteSheet(imgPath + "spritesheet-descriptor.json", imgPath + "spritesheet.png");
            Image[] frames = spriteManager.getSpriteSet("player", "walk");
            nge.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Input

        nge.addComponent(ngePhysics);

        InputHandler input = new InputHandler(root.getScene());


        AudioComponent ngeSound = new AudioComponent("glassSmashing2.wav");
        nge.addComponent(ngeSound);

        input.onKeyRelease(KeyCode.D, ngeSound::play); //example - pressing the D key will trigger the sound
        
        GameAudio ga = new GameAudio("backingTrack2.wav");
        ga.play();
        input.onKeyRelease(KeyCode.S, ga::stopStart); //example - pressing the S key will switch between stop and start
        
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

        input.onMouseClick(MouseButton.PRIMARY,
                (x, y) -> System.out.println("X: " + x + "\nY: " + y));
        gl.start();
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