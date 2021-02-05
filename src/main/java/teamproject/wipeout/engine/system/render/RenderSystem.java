package teamproject.wipeout.engine.system.render;

import java.util.List;
import java.util.Set;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import teamproject.wipeout.engine.component.*;
import teamproject.wipeout.engine.component.render.*;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

public class RenderSystem implements GameSystem {

    public static final Set<Class<? extends GameComponent>> cameraSignaturePattern = Set.of(Transform.class, CameraComponent.class);
    public static final Set<Class<? extends GameComponent>> renderSignaturePattern = Set.of(Transform.class, RectRenderComponent.class);

    private Affine _identityTransform;

    protected Canvas _canvas;

    protected GraphicsContext _gc;

    protected SignatureEntityCollector _renderableEntityCollector;
    protected CameraEntityCollector _cameraCollector;

    protected GameScene _scene;

    public RenderSystem(GameScene scene, Canvas canvas) {
        this._renderableEntityCollector = new SignatureEntityCollector(scene, renderSignaturePattern);
        this._cameraCollector = new CameraEntityCollector(scene);

        this._scene = scene;
        this._canvas = canvas;
        this._gc = canvas.getGraphicsContext2D();
        this._identityTransform = new Affine();
    }

    public void cleanup() {
        this._cameraCollector.cleanup();
        this._renderableEntityCollector.cleanup();
    }

    public void accept(Double timeStep) {
        this._gc.setTransform(this._identityTransform);

        GameEntity camera = this._cameraCollector.getMainCamera();
        if (camera != null) {
            Transform cameraTransform = camera.getComponent(Transform.class);
            CameraComponent cameraData = camera.getComponent(CameraComponent.class);
            float zoom = cameraData.zoom;

            double x = -cameraTransform.position.getX();
            double y = -cameraTransform.position.getY();

            if (x != 0 || y != 0) {
                this._gc.translate(-cameraTransform.position.getX(), -cameraTransform.position.getY());
            }
            
            if (cameraTransform.rotation != 0) {
                this._gc.rotate(-cameraTransform.rotation);
            }

            if (Float.compare(zoom, 1f) != 0) {
                this._gc.scale(zoom, zoom);
            }
        }

        this._gc.clearRect(0, 0, this._canvas.getWidth(), this._canvas.getHeight());
        List<GameEntity> entities = this._renderableEntityCollector.getEntities();
        for (GameEntity entity : entities) {
            Transform t = entity.getComponent(Transform.class);
            Renderable r = entity.getComponent(RectRenderComponent.class);
            r.render(this._gc, t.position.getX(), t.position.getY());
        }
    }
}
