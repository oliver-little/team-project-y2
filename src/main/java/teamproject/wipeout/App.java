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
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.core.*;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.audio.GameAudio;

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
        
        
        systemUpdater.addSystem(new AudioSystem(gameScene));
        AudioComponent ngeSound = new AudioComponent("glassSmashing2.wav");
        nge.addComponent(ngeSound);
        
        input.onKeyRelease(KeyCode.D, () -> ngeSound.play()); //example - pressing the D key will trigger the sound
        
        GameAudio ga = new GameAudio("backingTrack2.wav");
        ga.play();
        
        input.onKeyRelease(KeyCode.S, () -> ga.stopStart()); //example - pressing the S key will switch between play and pause
        
        stage.setScene(scene);
        stage.show();
        gl.start();
    }

    public static void main(String[] args) {
        launch();
    }

}