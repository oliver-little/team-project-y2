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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.CollisionComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.CircleRenderComponent;
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
        //Wall
        GameEntity bigBall = gameScene.createEntity();
        bigBall.addComponent(new Transform(200, 100));
        bigBall.addComponent(new RectRenderComponent(Color.BLACK, 50, 50));
        bigBall.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        bigBall.addComponent(new CollisionComponent(new Circle(25,25,25)));
        
        GameEntity bigBall2 = gameScene.createEntity();
        bigBall2.addComponent(new Transform(200, 400));
        bigBall2.addComponent(new RectRenderComponent(Color.GREEN, 50, 50));
        bigBall2.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        bigBall2.addComponent(new CollisionComponent(false, new Circle(25,25,25)));
        
        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(100, 100));
        nge.addComponent(new RectRenderComponent(Color.DARKRED, 50, 50));
        nge.addComponent(new CollisionComponent(new Circle(25,25,25)));

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
        
        
      


        stage.setScene(scene);
        stage.show();
        gl.start();
    }

    public static void main(String[] args) {
        launch();
    }

}