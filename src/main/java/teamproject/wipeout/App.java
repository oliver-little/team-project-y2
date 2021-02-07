package teamproject.wipeout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import teamproject.wipeout.engine.component.TagComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.PhysicsComponent;
import teamproject.wipeout.engine.component.render.*;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.core.*;
import teamproject.wipeout.engine.system.*;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

public class App extends Application {

    public String imgPath = "./assets/";

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(1980, 1080);
        GameScene gameScene = new GameScene();
        RenderSystem renderer = new RenderSystem(gameScene, canvas);
        SystemUpdater systemUpdater = new SystemUpdater();
        systemUpdater.addSystem(new PhysicsSystem(gameScene));
        SpriteManager spriteManager = new SpriteManager();

        GameLoop gl = new GameLoop(systemUpdater, renderer);

        GameEntity camera = gameScene.createEntity();
        camera.addComponent(new Transform(0, 0));
        camera.addComponent(new CameraComponent(2));
        camera.addComponent(new TagComponent("MainCamera"));
        
        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(10,10));
        //nge.addComponent(new RenderComponent(new RectRenderable(Color.DARKRED, 20, 20)));
        //nge.addComponent(new RenderComponent(new SpriteRenderable(spriteManager.getImage(imgPath + "sprite.png"))));

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

        
        Scene scene = new Scene(new StackPane(canvas), 1920, 1080);
        stage.setScene(scene);
        stage.show();
        gl.start();
    }

    public static void main(String[] args) {
        launch();
    }

}