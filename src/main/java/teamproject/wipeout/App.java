package teamproject.wipeout;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.CollisionComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.RectRenderComponent;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.core.*;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.input.InputHandler;
import teamproject.wipeout.sound.GameSound;


public class App extends Application {
	
    @Override
    public void start(Stage stage) {
        double windowWidth = 800;
        double windowHeight = 600;
        Canvas canvas = new Canvas(windowWidth, windowHeight);
        Scene scene = new Scene(new StackPane(canvas), windowWidth, windowHeight);

        GameScene gameScene = new GameScene();
        RenderSystem renderer = new RenderSystem(gameScene, canvas);
        SystemUpdater systemUpdater = new SystemUpdater();
        systemUpdater.addSystem(new MovementSystem(gameScene));
        systemUpdater.addSystem(new CollisionSystem(gameScene));

        GameLoop gl = new GameLoop(systemUpdater, renderer);
        
        //the square

        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(250, 250));
        nge.addComponent(new RectRenderComponent(Color.DARKRED, 20, 20));
        nge.addComponent(new CollisionComponent(new Rectangle(20,20)));

        MovementComponent ngePhysics = new MovementComponent(0f, 0f, 0f, 0f);
        nge.addComponent(ngePhysics);

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
        
        
        // GameSound gs = new GameSound("src\\main\\java\\teamproject\\wipeout\\sound\\backingTrack.mp3");
        // gs.play();
        
        // input.addKeyAction(KeyCode.S, 
        // 		() -> gs.playPause(), 
        // 		() -> {});
        
        //Wall
        GameEntity platformBottom = gameScene.createEntity();
        platformBottom.addComponent(new Transform(50, 520));
        platformBottom.addComponent(new RectRenderComponent(Color.BLACK, 600, 50));
        platformBottom.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        platformBottom.addComponent(new CollisionComponent(new Rectangle(600,50)));

        GameEntity platformUp = gameScene.createEntity();
        platformUp.addComponent(new Transform(50, 30));
        platformUp.addComponent(new RectRenderComponent(Color.RED, 600, 50));
        platformUp.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        platformUp.addComponent(new CollisionComponent(new Rectangle(600,50)));

        GameEntity platformLeft = gameScene.createEntity();
        platformLeft.addComponent(new Transform(50, 100));
        platformLeft.addComponent(new RectRenderComponent(Color.BLUE, 50, 400));
        platformLeft.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        platformLeft.addComponent(new CollisionComponent(new Rectangle(50,400)));

        GameEntity platformRight = gameScene.createEntity();
        platformRight.addComponent(new Transform(700, 80));
        platformRight.addComponent(new RectRenderComponent(Color.GREEN, 50, 400));
        platformRight.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        platformRight.addComponent(new CollisionComponent(new Rectangle(50,400)));

        stage.setScene(scene);
        stage.show();
        gl.start();
    }

    public static void main(String[] args) {
        launch();
    }

}