package teamproject.wipeout.engine.system.render;

import java.util.List;

import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import teamproject.wipeout.engine.component.*;
import teamproject.wipeout.engine.component.render.*;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.system.GameSystem;
import teamproject.wipeout.util.InsertionSort;

/**
 * System to render relevant GameEntities to the canvas
 */
public class RenderSystem implements GameSystem {
    private Affine identityTransform;

    protected Canvas dynamicCanvas;
    protected Canvas staticCanvas;

    protected GraphicsContext dynamicGC;
    protected GraphicsContext staticGC;
    protected boolean renderStatic = false;

    protected RendererEntityCollector renderableEntityCollector;
    protected CameraEntityCollector cameraCollector;

    private TransformComparator yPosComparator;
    private Point2D lastCameraPos;
    private double lastZoom;

    private static final double DOUBLE_COMPARE = 0.00001f;

    /** 
     * Creates a new instance of RenderSystem
     * 
     * @param scene The GameScene this System is part of
     * @param dynamicCanvas The canvas this RenderSystem should draw to
     */
    public RenderSystem(GameScene scene, Canvas staticCanvas, Canvas dynamicCanvas) {
        this.renderableEntityCollector = new RendererEntityCollector(scene);
        this.cameraCollector = new CameraEntityCollector(scene);
        this.yPosComparator = new TransformComparator();

        this.staticCanvas = staticCanvas;
        this.staticGC = staticCanvas.getGraphicsContext2D();
        this.dynamicCanvas = dynamicCanvas;
        this.dynamicGC = dynamicCanvas.getGraphicsContext2D();

        // Disable antialiasing to get pixel perfect sprites
        this.dynamicGC.setImageSmoothing(false);
        this.staticGC.setImageSmoothing(false);

        this.identityTransform = new Affine();
        this.lastCameraPos = Point2D.ZERO;
        this.lastZoom = 0;
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
        this.dynamicGC.setTransform(this.identityTransform);
        
        Point2D cameraPos = Point2D.ZERO;
        double width = this.dynamicCanvas.getWidth();
        double height = this.dynamicCanvas.getHeight();
        double zoom = 1;

        // Clear the screen ready for rendering
        this.dynamicGC.clearRect(0, 0, width, height);

        GameEntity camera = this.cameraCollector.getMainCamera();
        // If the camera is present, apply various transforms
        if (camera != null) {
            Transform cameraTransform = camera.getComponent(Transform.class);
            CameraComponent cameraData = camera.getComponent(CameraComponent.class);
            zoom = cameraData.zoom;

            cameraPos = cameraTransform.getWorldPosition();
            
            if (cameraPos.getX() != 0 || cameraPos.getY() != 0) {
                this.dynamicGC.translate(-cameraPos.getX() * zoom, -cameraPos.getY() * zoom);
            }

            if (Double.compare(zoom, 1f) != 0 && zoom > 0) {
                // Scale the camera's virtual size by the zoom amount
                width /= zoom;
                height /= zoom;
            }

            if (!cameraPos.equals(lastCameraPos) || Double.compare(zoom, lastZoom) > DOUBLE_COMPARE) {
                lastZoom = zoom;
                lastCameraPos = cameraPos;
                this.staticGC.clearRect(0, 0, width, height);
                // Copy in the now calculated transform
                this.staticGC.setTransform(this.dynamicGC.getTransform());
                this.renderStatic = true;
            }
        }

        BoundingBox cameraBox = new BoundingBox(cameraPos.getX(), cameraPos.getY(), width, height);

        // Get the list of renderable entities
        List<GameEntity> entities = this.renderableEntityCollector.getEntities();
        
        // Sort it - use an insertion sort because insertion sort is fast for nearly sorted lists:
        // On most frames this list will be sorted or very nearly sorted.
        InsertionSort.sort(entities, yPosComparator);

        for (GameEntity entity : entities) {
            Transform t = entity.getComponent(Transform.class);
            RenderComponent r = entity.getComponent(RenderComponent.class);

            // Test if the entity is actually visible on the camera view
            Point2D tWorldPosition = t.getWorldPosition();
            if (cameraBox.intersects(tWorldPosition.getX(), tWorldPosition.getY(), r.getWidth(), r.getHeight())) {
                // Render the entity, scaled according to the camera view
                r.render(this.dynamicGC, tWorldPosition.getX(), tWorldPosition.getY(), zoom);
            }
        }

        // Renderable static entities if needed
        if (this.renderStatic) {
            this.renderStatic = false;
            // Get the list of renderable entities
            entities = this.renderableEntityCollector.getStaticEntities();
        
            // Sort it - use an insertion sort because insertion sort is fast for nearly sorted lists:
            // On most frames this list will be sorted or very nearly sorted.
            InsertionSort.sort(entities, yPosComparator);

            for (GameEntity entity : entities) {
                Transform t = entity.getComponent(Transform.class);
                RenderComponent r = entity.getComponent(RenderComponent.class);

                // Test if the entity is actually visible on the camera view
                Point2D tWorldPosition = t.getWorldPosition();
                if (cameraBox.intersects(tWorldPosition.getX(), tWorldPosition.getY(), r.getWidth(), r.getHeight())) {
                    // Render the entity, scaled according to the camera view
                    r.render(this.staticGC, tWorldPosition.getX(), tWorldPosition.getY(), zoom);
            }
        }
        }
    }
}
