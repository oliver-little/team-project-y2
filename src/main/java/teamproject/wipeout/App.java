package teamproject.wipeout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import teamproject.wipeout.engine.component.GravityComponent;
import teamproject.wipeout.engine.component.MoveRightComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.VelocityComponent;
import teamproject.wipeout.engine.component.render.RectRenderComponent;
import teamproject.wipeout.engine.core.GameLoop;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.core.SystemUpdater;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.system.GravitySystem;
import teamproject.wipeout.engine.system.MoveRightSystem;
import teamproject.wipeout.engine.system.RenderSystem;
import teamproject.wipeout.engine.system.VelocitySystem;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(500, 500);
        GameScene gameScene = new GameScene();
        RenderSystem renderer = new RenderSystem(gameScene, canvas);
        SystemUpdater systemUpdater = new SystemUpdater();
        systemUpdater.addSystem(new MoveRightSystem(gameScene));
        systemUpdater.addSystem(new VelocitySystem(gameScene));
        systemUpdater.addSystem(new GravitySystem(gameScene));
        
        GameLoop gl = new GameLoop(systemUpdater, renderer);
        GameEntity ge = gameScene.createEntity();
        ge.addComponent(new Transform(200, 200));
        ge.addComponent(new RectRenderComponent());
        ge.addComponent(new MoveRightComponent(25));
        
        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(250,250));
        nge.addComponent(new RectRenderComponent(Color.DARKRED, 20, 20));
        nge.addComponent(new VelocityComponent(0,-50));
        nge.addComponent(new GravityComponent());

        
        Scene scene = new Scene(new StackPane(canvas), 500, 500);
        stage.setScene(scene);
        stage.show();
        gl.start();
    }

    public static void main(String[] args) {
        launch();
    }

}