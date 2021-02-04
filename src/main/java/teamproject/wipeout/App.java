package teamproject.wipeout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.CollisionComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.RectRenderComponent;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.core.*;
import teamproject.wipeout.engine.system.*;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);
        GameScene gameScene = new GameScene();
        RenderSystem renderer = new RenderSystem(gameScene, canvas);
        SystemUpdater systemUpdater = new SystemUpdater();
        systemUpdater.addSystem(new MovementSystem(gameScene));
        systemUpdater.addSystem(new CollisionSystem(gameScene));

        GameLoop gl = new GameLoop(systemUpdater, renderer);
        
        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(250,250));
        nge.addComponent(new RectRenderComponent(Color.DARKRED, 20, 20));
        nge.addComponent(new MovementComponent(80f, -100f, -20f, 40f));
        nge.addComponent(new CollisionComponent());

        //add more entities here
        GameEntity platform = gameScene.createEntity();
        platform.addComponent(new Transform(300,300));
        platform.addComponent(new RectRenderComponent(Color.DARKRED, 200, 200));
        platform.addComponent(new MovementComponent(0, 0, 0, 0));
        platform.addComponent(new CollisionComponent());
        
        Scene scene = new Scene(new StackPane(canvas), 800, 600);
        stage.setScene(scene);
        stage.show();
        gl.start();
    }

    public static void main(String[] args) {
        launch();
    }

}