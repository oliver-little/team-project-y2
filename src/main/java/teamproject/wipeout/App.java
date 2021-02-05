package teamproject.wipeout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.PhysicsComponent;
import teamproject.wipeout.engine.component.render.RectRenderComponent;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.core.*;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.input.InputHandler;

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
        systemUpdater.addSystem(new PhysicsSystem(gameScene));

        GameLoop gl = new GameLoop(systemUpdater, renderer);
        
        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(250,250));
        nge.addComponent(new RectRenderComponent(Color.DARKRED, 20, 20));

        PhysicsComponent ngePhysics = new PhysicsComponent(0f, 0f, 0f, 0f);
        nge.addComponent(ngePhysics);

        InputHandler input = new InputHandler(scene);
        input.addKeyAction(KeyCode.LEFT,
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(60f, 0f),
                () -> ngePhysics.velocity = ngePhysics.velocity.add(60f, 0f));

        input.addKeyAction(KeyCode.RIGHT,
                () -> ngePhysics.velocity = ngePhysics.velocity.add(60f, 0f),
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(60f, 0f));

        input.addKeyAction(KeyCode.UP,
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(0f, 60f),
                () -> ngePhysics.velocity = ngePhysics.velocity.add(0f, 60f));

        input.addKeyAction(KeyCode.DOWN,
                () -> ngePhysics.velocity = ngePhysics.velocity.add(0f, 60f),
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(0f, 60f));

        input.onMouseClick(MouseButton.PRIMARY,
                (x, y) -> System.out.println("+X: " + x +"\n+Y: " + y));

        input.onMouseDrag(MouseButton.SECONDARY,
                (x, y) -> System.out.println("_X: " + x +"\n_Y: " + y),
                (x, y) -> System.out.println("X: " + x +"\nY: " + y),
                (x, y) -> System.out.println("*X: " + x +"\n*Y: " + y));

        stage.setScene(scene);
        stage.show();
        gl.start();
    }

    public static void main(String[] args) {
        launch();
    }

}