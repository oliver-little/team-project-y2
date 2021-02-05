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
        nge.addComponent(new Transform(200,400));
        nge.addComponent(new RectRenderComponent(Color.MEDIUMPURPLE, 20, 20));
        nge.addComponent(new MovementComponent(80f, 0, -20f, 60f));
        nge.addComponent(new CollisionComponent());

        //add more entities here
        GameEntity platform = gameScene.createEntity();
        platform.addComponent(new Transform(300,300));
        platform.addComponent(new RectRenderComponent(Color.DARKGRAY, 200, 200));
        platform.addComponent(new MovementComponent(0, 0, 0, 0));
        platform.addComponent(new CollisionComponent());
        
        GameEntity nge2 = gameScene.createEntity();
        nge2.addComponent(new Transform(250,300));
        nge2.addComponent(new RectRenderComponent(Color.DARKRED, 20, 20));
        nge2.addComponent(new MovementComponent(50f, -100f, 0, 80f));
        nge2.addComponent(new CollisionComponent());
        
        Scene scene = new Scene(new StackPane(canvas), 800, 600);
        stage.setScene(scene);
        stage.show();
        gl.start();
    }

    public static void main(String[] args) {
        launch();
    }

}