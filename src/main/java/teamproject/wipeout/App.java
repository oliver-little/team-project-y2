package teamproject.wipeout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import teamproject.wipeout.engine.component.MoveRightComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RectRenderComponent;
import teamproject.wipeout.engine.core.GameLoop;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.core.SystemUpdater;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.system.MoveRightSystem;
import teamproject.wipeout.engine.system.RenderSystem;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(1920, 1080);
        GameScene gameScene = new GameScene();
        RenderSystem renderer = new RenderSystem(gameScene, canvas);
        SystemUpdater systemUpdater = new SystemUpdater();
        systemUpdater.addSystem(new MoveRightSystem(gameScene));
        GameLoop gl = new GameLoop(systemUpdater, renderer);
        GameEntity ge = gameScene.createEntity();
        ge.addComponent(new Transform(200, 200));
        ge.addComponent(new RectRenderComponent());
        ge.addComponent(new MoveRightComponent(25));
        Scene scene = new Scene(new StackPane(canvas), 1920, 1080);
        stage.setScene(scene);
        stage.show();
        gl.start();
    }

    public static void main(String[] args) {
        launch();
    }

}