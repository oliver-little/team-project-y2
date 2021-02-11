package teamproject.wipeout;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import teamproject.wipeout.engine.component.TagComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.PhysicsComponent;
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.core.*;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.component.render.*;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.core.*;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

public class App implements Controller {

    public String imgPath = "./assets/";
    private StackPane root;
    private Canvas canvas;
    private double windowWidth = 800;
    private double windowHeight = 600;
    
    public void createContent() {
    	/*
    	double windowWidth = 800;
        double windowHeight = 600;
    	
    	canvas = new Canvas(windowWidth, windowHeight);
        //Scene scene = new Scene(new StackPane(canvas), windowWidth, windowHeight);
        root = new StackPane(canvas);
        */
        GameScene gameScene = new GameScene();
        RenderSystem renderer = new RenderSystem(gameScene, canvas);
        SystemUpdater systemUpdater = new SystemUpdater();
        systemUpdater.addSystem(new PhysicsSystem(gameScene));
        systemUpdater.addSystem(new AudioSystem(gameScene));

        GameLoop gl = new GameLoop(systemUpdater, renderer);

        GameEntity camera = gameScene.createEntity();
        camera.addComponent(new Transform(0, 0));
        camera.addComponent(new CameraComponent(1));
        camera.addComponent(new TagComponent("MainCamera"));
        
        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(250,250));


        // Animated Sprite
        SpriteManager spriteManager = new SpriteManager();

        try {
            spriteManager.loadSpriteSheet(imgPath + "spritesheet.png", 32, 32);
            Image[][] spriteSheet = spriteManager.getSpriteSheet(imgPath + "spritesheet.png");
            Image[] frames = new Image[spriteSheet.length];
            for (int row = 0; row < spriteSheet.length; row++) {
                frames[row] = spriteSheet[row][2];
            }
            
            nge.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Input

        PhysicsComponent ngePhysics = new PhysicsComponent(0, 0, 0, 0);
        nge.addComponent(ngePhysics);

        InputHandler input = new InputHandler(root.getScene());
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
        

        //stage.setScene(scene);
        //stage.show();
        gl.start();
    }
    
	@Override
	public Parent getContent()
	{
		canvas = new Canvas(windowWidth, windowHeight);
        root = new StackPane(canvas);
		return root;
	}
	

}