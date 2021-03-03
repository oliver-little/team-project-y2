package teamproject.wipeout.engine.system.input;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.input.Hoverable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.CameraEntityCollector;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.input.InputHoverableAction;
import teamproject.wipeout.engine.system.EventSystem;

import java.util.List;
import java.util.Set;

public class MouseHoverSystem implements EventSystem {

    public static final Set<Class<? extends GameComponent>> signature = Set.of(Transform.class, RenderComponent.class, Hoverable.class);

    private SignatureEntityCollector collector;
    private CameraEntityCollector cameraCollector;

    public MouseHoverSystem(GameScene scene, InputHandler input) {
        this.collector = new SignatureEntityCollector(scene, this.signature);
        this.cameraCollector = new CameraEntityCollector(scene);
        input.onMouseHover(this.onHover);
    }

    public void cleanup() {
        this.collector.cleanup();
    }

    protected InputHoverableAction onHover = (x, y) -> {
        Pair<List<GameEntity>, Point2D> hovering = this.getHovered(x, y);
        for (GameEntity entity : hovering.getKey()) {
            Hoverable hoverable = entity.getComponent(Hoverable.class);
            if (hoverable != null) {
                hoverable.onClick.performMouseHoverAction(hovering.getValue().getX(), hovering.getValue().getY());
            }
        }
    };

    protected Pair<List<GameEntity>, Point2D> getHovered(double x, double y) {
        // Transform mouse click position by camera position and zoom
        double zoom = 1;
        Point2D position = Point2D.ZERO;
        if (this.cameraCollector.getMainCamera() != null) {
            zoom = this.cameraCollector.getCameraComponent().zoom;
            position = this.cameraCollector.getCameraTransform().getWorldPosition();
        }

        x = (x / zoom) + position.getX();
        y = (y / zoom) + position.getY();

        return new Pair<List<GameEntity>, Point2D>(this.collector.getEntities(), new Point2D(x, y));
    }

}
