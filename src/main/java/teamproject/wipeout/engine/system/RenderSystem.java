package teamproject.wipeout.engine.system;

import java.util.List;
import java.util.Set;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import teamproject.wipeout.engine.component.*;
import teamproject.wipeout.engine.component.render.*;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.MultipleSignatureCollector;

public class RenderSystem implements GameSystem {

    public static final List<Set<Class<? extends GameComponent>>> signaturePattern = List.of(Set.of(Transform.class, RectRenderComponent.class));
    
    protected Canvas _canvas;

    protected GraphicsContext _gc;
    protected MultipleSignatureCollector _scene;

    public RenderSystem(GameScene scene, Canvas canvas) { 
        this._scene = new MultipleSignatureCollector(scene, signaturePattern);
        this._canvas = canvas;
        this._gc = canvas.getGraphicsContext2D();
    }

    public void accept(Double timeStep) {
        this._gc.clearRect(0, 0, this._canvas.getWidth(), this._canvas.getHeight());
        for (Set<Class<? extends GameComponent>> signature : signaturePattern) {
            List<GameEntity> entities = this._scene.getEntitiesForSignature(signature);
            for (GameEntity entity : entities) {
                Transform t = entity.getComponent(Transform.class);
                Renderable r = entity.getComponent(RectRenderComponent.class);
                r.render(this._gc, t.position.getX(), t.position.getY());
            }
        }
    }
}
