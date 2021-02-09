package teamproject.wipeout.engine.system.render;

import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import teamproject.wipeout.engine.component.*;
import teamproject.wipeout.engine.component.render.*;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

/**
 * System to render relevant GameEntities to the canvas
 */
public class RenderSystem implements GameSystem {
    
    public static final Set<Class<? extends GameComponent>> renderSignaturePattern = Set.of(Transform.class, RenderComponent.class);

    private Affine identityTransform;

    protected Canvas canvas;

    protected GraphicsContext gc;

    protected SignatureEntityCollector renderableEntityCollector;
    protected CameraEntityCollector cameraCollector;

    /** 
     * Creates a new instance of RenderSystem
     * 
     * @param scene The GameScene this System is part of
     * @param canvas The canvas this RenderSystem should draw to
     */
    public RenderSystem(GameScene scene, Canvas canvas) {
        this.renderableEntityCollector = new SignatureEntityCollector(scene, renderSignaturePattern);
        this.cameraCollector = new CameraEntityCollector(scene);

        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

        // Disable antialiasing to get pixel perfect sprites
        this.gc.setImageSmoothing(false);

        this.identityTransform = new Affine();
    }

    public void cleanup() {
        this.cameraCollector.cleanup();
        this.renderableEntityCollector.cleanup();
    }

    /**
     * Renders all renderable objects in view of the current camera.
     * 
     * @param timeStep The amount of time, in seconds, that passed between the last frame and now
     */
    public void accept(Double timeStep) {
        // Reset the GraphicsContext back to the origin
        this.gc.setTransform(this.identityTransform);
        
        Point2D cameraPos = Point2D.ZERO;
        double width = this.canvas.getWidth();
        double height = this.canvas.getHeight();
        double zoom = 1;

        GameEntity camera = this.cameraCollector.getMainCamera();
        // If the camera is present, apply various transforms
        if (camera != null) {
            Transform cameraTransform = camera.getComponent(Transform.class);
            CameraComponent cameraData = camera.getComponent(CameraComponent.class);
            zoom = cameraData.zoom;

            cameraPos = cameraTransform.position;

            if (cameraPos.getX() != 0 || cameraPos.getY() != 0) {
                this.gc.translate(-cameraPos.getX() * zoom, -cameraPos.getY() * zoom);
            }

            if (Double.compare(zoom, 1f) != 0 && zoom > 0) {
                // Scale the camera's virtual size by the zoom amount
                width /= zoom;
                height /= zoom;
            }
        }

        Point2D cameraPosBottomRight = cameraPos.add(width, height);

        // Clear the screen ready for rendering
        this.gc.clearRect(0, 0, width, height);
        List<GameEntity> entities = this.renderableEntityCollector.getEntities();
        for (GameEntity entity : entities) {
            Transform t = entity.getComponent(Transform.class);
            RenderComponent r = entity.getComponent(RenderComponent.class);

            Point2D entityTopLeft = t.position;
            Point2D entityBottomRight = t.position.add(r.getWidth(), r.getHeight());
            
            // Test if the entity is actually visible on the camera view
            if (this.calculateBoxIntersects(cameraPos, cameraPosBottomRight, entityTopLeft, entityBottomRight)) {
                // Render the entity, scaled according to the camera view
                r.render(this.gc, entityTopLeft.getX() * zoom, entityTopLeft.getY() * zoom, zoom);
            }
        }
    }

    /**
     * Calculates the intersection of two bounding boxes
     * Used to calculate whether a given entity should be rendered to the screen
     * based on whether it would actually be visible to the camera.
     * 
     * @param b1TL Top left coordinates of the first bounding box
     * @param b1BR Bottom right coordinates of the first bounding box
     * @param b2TL Top left coordinates of the second bounding box
     * @param b2BR Bottom right coordinates of the second bounding box
     * @return Whether the two boxes intersect
     */
    private boolean calculateBoxIntersects(Point2D b1TL, Point2D b1BR, Point2D b2TL, Point2D b2BR) {
        return !(b2TL.getX() > b1BR.getX() || b2BR.getX() < b1TL.getX() || b2TL.getY() > b1BR.getY() || b2BR.getY() < b1TL.getY());
    }
}
