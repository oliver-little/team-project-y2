package teamproject.wipeout.engine.system.input;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.input.Clickable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.CameraEntityCollector;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.input.InputClickableAction;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.system.EventSystem;
import teamproject.wipeout.util.sort.InsertionSort;
import teamproject.wipeout.util.sort.RenderOrderComparator;

import java.util.List;
import java.util.Set;

public class MouseClickSystem implements EventSystem {

    public static final Set<Class<? extends GameComponent>> signature = Set.of(Transform.class, RenderComponent.class, Clickable.class);

    private static final RenderOrderComparator comparator = new RenderOrderComparator();
    private SignatureEntityCollector collector;
    private CameraEntityCollector cameraCollector;
    
    public MouseClickSystem(GameScene scene, InputHandler input) {
        this.collector = new SignatureEntityCollector(scene, signature);
        this.cameraCollector = new CameraEntityCollector(scene);
        input.onMouseClick(this.onClick);
    }

    public MouseClickSystem(GameScene scene, InputHandler input, CameraEntityCollector cameraCollector) {
        this.collector = new SignatureEntityCollector(scene, signature);
        this.cameraCollector = cameraCollector;
        input.onMouseClick(this.onClick);
    }

    public void cleanup() {
        this.collector.cleanup();
    }

    public InputClickableAction onClick = (x, y, button) -> {
        Pair<GameEntity, Point2D> clicked = this.getClicked(x, y);
        if (clicked != null) {
            Clickable entityClickable = clicked.getKey().getComponent(Clickable.class);
            if (entityClickable != null) {
                entityClickable.onClick.performMouseClickAction(clicked.getValue().getX(), clicked.getValue().getY(), button);
            }
        }
    };

    protected Pair<GameEntity, Point2D> getClicked(double x, double y) {
        // Transform mouse click position by camera position and zoom
        double zoom = 1;
        Point2D position = Point2D.ZERO;
        if (cameraCollector.getMainCamera() != null) {
            zoom = cameraCollector.getCameraComponent().zoom;
            position = cameraCollector.getCameraTransform().getWorldPosition();
        }

        x = (x / zoom) + position.getX();
        y = (y / zoom) + position.getY();

        List<GameEntity> entities = this.collector.getEntities();
        entities = InsertionSort.sort(entities, comparator);
        int i = entities.size() - 1;
        GameEntity entity;
        Point2D worldPosition;
        RenderComponent renderComponent;

        while (i >= 0) {
            entity = entities.get(i);
            worldPosition = entity.getComponent(Transform.class).getWorldPosition();
            renderComponent = entity.getComponent(RenderComponent.class);
            if (x > worldPosition.getX() && y > worldPosition.getY() && x < worldPosition.getX() + renderComponent.getWidth() && y < worldPosition.getY() + renderComponent.getHeight()) {
                return new Pair<GameEntity, Point2D>(entity, new Point2D(x, y));
            } 
            i--;
        }

        return null;
    }
}
