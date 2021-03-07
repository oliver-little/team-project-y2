package teamproject.wipeout.engine.entity.gameclock;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;


public class ClockEntity extends GameEntity {
    private Point2D topLeft;
    private Double time;
    private final Double INITIAL_TIME;

    private GameEntity textEntity;

    private TextRenderable textRenderable;

    public ClockEntity(GameScene scene, double x, double y, Double time) {
        super(scene);

        this.addComponent(new Transform(x, y));
        this.addComponent(new RenderComponent(new RectRenderable(Color.BLACK, 120, 15)));

        this.topLeft = new Point2D(x, y);
        this.time = time;
        this.INITIAL_TIME = time;
        textEntity = scene.createEntity();
        textEntity.addComponent(new Transform (x + 10, y + 10));
        textRenderable = new TextRenderable("Remaining time: " + this.time.toString());
        textEntity.addComponent(new RenderComponent(textRenderable));
    }

    public void showTime(Double timestep) {
        this.time -= timestep;
        textRenderable.setText("Remaining time: " + this.time.toString());
    }

    public void restart(){
        this.time = this.INITIAL_TIME;
    }
}
