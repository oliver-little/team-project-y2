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
import teamproject.wipeout.engine.system.GameSystem;

import java.util.List;
import java.util.Set;

public class MouseHoverSystem implements GameSystem {

    public static final Point2D CLICK_ERROR_OFFSET = MouseClickSystem.CLICK_ERROR_OFFSET;

    private final SignatureEntityCollector collector;
    private final CameraEntityCollector cameraCollector;

    private double currentMouseX;
    private double currentMouseY;
    private double lastFrameWorldX;
    private double lastFrameWorldY;

    protected final InputHoverableAction onHover = (x, y) -> {
        this.currentMouseX = x + CLICK_ERROR_OFFSET.getX();
        this.currentMouseY = y + CLICK_ERROR_OFFSET.getY();
    };

    public MouseHoverSystem(GameScene scene, InputHandler input) {
        this.collector = new SignatureEntityCollector(scene, Set.of(Transform.class, RenderComponent.class, Hoverable.class));
        this.cameraCollector = new CameraEntityCollector(scene);
        this.currentMouseX = 0;
        this.currentMouseY = 0;

        // Setup function to immediately update hoverable when added
        this.collector.onAdd = (entity) -> entity.getComponent(Hoverable.class).onClick.performMouseHoverAction(this.currentMouseX, this.currentMouseY);

        input.onMouseHover(this.onHover);
    }

    public Point2D getCurrentMousePosition() {
        return new Point2D(this.currentMouseX, this.currentMouseY);
    }

    public void cleanup() {
        this.collector.cleanup();
    }

    public void accept(Double timeStep) {
        // Transform mouse click position by camera position and zoom
        double zoom = 1;
        Point2D position = Point2D.ZERO;
        if (this.cameraCollector.getMainCamera() != null) {
            zoom = this.cameraCollector.getCameraComponent().zoom;
            position = this.cameraCollector.getCameraTransform().getWorldPosition();
        }

        double x = (this.currentMouseX / zoom) + position.getX();
        double y = (this.currentMouseY / zoom) + position.getY();

        if (x != lastFrameWorldX || y != lastFrameWorldY) {
            List<GameEntity> entities = this.collector.getEntities();

            for (GameEntity entity : entities) {
                Hoverable hoverable = entity.getComponent(Hoverable.class);
                if (hoverable != null) {
                    hoverable.onClick.performMouseHoverAction(x, y);
                }
            }

            lastFrameWorldX = x;
            lastFrameWorldY = y;
        }
    }
}
