package teamproject.wipeout.engine.entity.gameclock;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.ScriptComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

import java.util.List;
import java.util.Set;

public class ClockSystem implements GameSystem {
    private Point2D topLeft;
    private Double time;

    private GameEntity textEntity;

    private TextRenderable textRenderable;
    ClockEntity clockEntity;
    protected SignatureEntityCollector entityCollector;


    public ClockSystem(GameScene scene, double x, double y, Double time) {
        this.entityCollector = new SignatureEntityCollector(scene, Set.of());
        clockEntity = new ClockEntity(scene, x, y, time);
        scene.entities.add(clockEntity);
    }

    public void cleanup() {
        this.clockEntity.restart();
        this.entityCollector.cleanup();
    }

    public void accept(Double timeStep) {
        this.clockEntity.showTime(timeStep);
    }
}
