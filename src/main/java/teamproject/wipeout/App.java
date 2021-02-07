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
import teamproject.wipeout.engine.component.sound.SoundComponent;
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
        systemUpdater.addSystem(new PhysicsSystem(gameScene));

        GameLoop gl = new GameLoop(systemUpdater, renderer);
        
        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(250,250));
        nge.addComponent(new RectRenderComponent(Color.DARKRED, 20, 20));

        PhysicsComponent ngePhysics = new PhysicsComponent(0f, 0f, 0f, 0f);
        nge.addComponent(ngePhysics);

        InputHandler input = new InputHandler(scene);
        input.addKeyAction(KeyCode.LEFT,
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(50f, 0f),
                () -> ngePhysics.velocity = ngePhysics.velocity.add(50f, 0f));

        input.addKeyAction(KeyCode.RIGHT,
                () -> ngePhysics.velocity = ngePhysics.velocity.add(50f, 0f),
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(50f, 0f));

        input.addKeyAction(KeyCode.UP,
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(0f, 50f),
                () -> ngePhysics.velocity = ngePhysics.velocity.add(0f, 50f));

        input.addKeyAction(KeyCode.DOWN,
                () -> ngePhysics.velocity = ngePhysics.velocity.add(0f, 50f),
                () -> ngePhysics.velocity = ngePhysics.velocity.subtract(0f, 50f));

        input.onMouseClick(MouseButton.PRIMARY,
                (x, y) -> System.out.println("X: " + x +"\nY: " + y));
        
        
        systemUpdater.addSystem(new SoundSystem(gameScene));
        SoundComponent ngeSound = new SoundComponent("src\\main\\java\\teamproject\\wipeout\\sound\\glassSmashing.mp3"); //TODO: path may need changing?
        nge.addComponent(ngeSound);
        
        input.addKeyAction(KeyCode.D, 
        		() -> ngeSound.play(), 
        		() -> {}); //example - pressing the D key will trigger the sound
        
        GameSound gs = new GameSound("src\\main\\java\\teamproject\\wipeout\\sound\\backingTrack.mp3");  //TODO: path may need changing?
        gs.play();
        
        input.addKeyAction(KeyCode.S, 
        		() -> gs.playPause(), 
        		() -> {}); //example - pressing the S key will switch between play and pause
        
        stage.setScene(scene);
        stage.show();
        gl.start();
    }

    public static void main(String[] args) {
        launch();
    }

}