package teamproject.wipeout;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import teamproject.wipeout.engine.component.TagComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.PhysicsComponent;
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.physics.CollisionComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.CircleRenderComponent;
import teamproject.wipeout.engine.component.render.RectRenderComponent;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.core.*;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.component.render.*;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.input.InputHandler;
import teamproject.wipeout.sound.GameSound;


public class App extends Application {

    public String imgPath = "./assets/";

    @Override
    public void start(Stage stage) {
        double windowWidth = 800;
        double windowHeight = 600;
        Canvas canvas = new Canvas(windowWidth, windowHeight);
        Scene scene = new Scene(new StackPane(canvas), windowWidth, windowHeight);

        GameScene gameScene = new GameScene();
        RenderSystem renderer = new RenderSystem(gameScene, canvas);
        SystemUpdater systemUpdater = new SystemUpdater();
        systemUpdater.addSystem(new PhysicsSystem(gameScene));
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
        staticsprite.addComponent(new Transform(275,250));
        try {
            staticsprite.addComponent(new RenderComponent(new SpriteRenderable(spriteManager.getImage(imgPath + "sprite.png"))));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //the square
        //Wall
        GameEntity bigBall = gameScene.createEntity();
        bigBall.addComponent(new Transform(200, 100));
        bigBall.addComponent(new RectRenderComponent(Color.BLACK, 50, 50));
        bigBall.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        bigBall.addComponent(new CollisionComponent(new Rectangle(50,50)));

        GameEntity bigBall2 = gameScene.createEntity();
        bigBall2.addComponent(new Transform(200, 400));
        bigBall2.addComponent(new RectRenderComponent(Color.GREEN, 50, 50));
        bigBall2.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        bigBall2.addComponent(new CollisionComponent(false, new Rectangle(50,50)));

        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(250,250));
        nge.addComponent(new Transform(100, 100));
        nge.addComponent(new RectRenderComponent(Color.DARKRED, 30, 30));
        nge.addComponent(new CollisionComponent(new Rectangle(30,30)));

        MovementComponent ngePhysics = new MovementComponent(0f, 0f, 0f, 0f);
        nge.addComponent(ngePhysics);

        try {
            spriteManager.loadSpriteSheet(imgPath + "spritesheet-descriptor.json", imgPath + "spritesheet.png");
            Image[] frames = spriteManager.getSpriteSet("player", "walk");
            nge.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Input

        PhysicsComponent ngePhysics = new PhysicsComponent(0, 0, 0, 0);
        nge.addComponent(ngePhysics);

        InputHandler input = new InputHandler(scene);
        input.addKeyAction(KeyCode.LEFT,
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(50, 0),
                () -> ngePhysics.velocity = ngePhysics.velocity.add(50, 0));

        input.addKeyAction(KeyCode.RIGHT,
                () -> ngePhysics.velocity = ngePhysics.velocity.add(50, 0),
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(50, 0));

        input.addKeyAction(KeyCode.UP,
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(0, 50),
                () -> ngePhysics.velocity = ngePhysics.velocity.add(0, 50));

        input.addKeyAction(KeyCode.DOWN,
                () -> ngePhysics.velocity = ngePhysics.velocity.add(0, 50),
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(0, 50));

        input.onMouseClick(MouseButton.PRIMARY,
                (x, y) -> System.out.println("X: " + x +"\nY: " + y));

        AudioComponent ngeSound = new AudioComponent("glassSmashing.mp3");
        nge.addComponent(ngeSound);

        input.onKeyRelease(KeyCode.D, () -> ngeSound.play()); //example - pressing the D key will trigger the sound

        GameAudio ga = new GameAudio("backingTrack.mp3");
        ga.play();

        input.onKeyRelease(KeyCode.S, () -> ga.playPause()); //example - pressing the S key will switch between play and pause


        InputHandler input = new InputHandler(scene);
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
                (x, y) -> System.out.println("X: " + x +"\nY: " + y));

        stage.setScene(scene);
        stage.show();
        gl.start();
    }

    public static void main(String[] args) {
        launch();
    }

}