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

    private final SignatureEntityCollector collector;
    private final CameraEntityCollector cameraCollector;

    private double currentMouseX;
    private double currentMouseY;

    protected final InputHoverableAction onHover = (x, y) -> {
        this.currentMouseX = x;
        this.currentMouseY = y;

        Pair<List<GameEntity>, Point2D> hovering = this.getHovered(x, y);
        for (GameEntity entity : hovering.getKey()) {
            Hoverable hoverable = entity.getComponent(Hoverable.class);
            if (hoverable != null) {
                hoverable.onClick.performMouseHoverAction(hovering.getValue().getX(), hovering.getValue().getY());
            }
        }
    };

    public MouseHoverSystem(GameScene scene, InputHandler input) {
        this.collector = new SignatureEntityCollector(scene, Set.of(Transform.class, RenderComponent.class, Hoverable.class));
        this.cameraCollector = new CameraEntityCollector(scene);
        this.currentMouseX = 0;
        this.currentMouseY = 0;

        input.onMouseHover(this.onHover);
    }

    public Point2D getCurrentMousePosition() {
        return new Point2D(this.currentMouseX, this.currentMouseY);
    }

    public void cleanup() {
        this.collector.cleanup();
    }

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
