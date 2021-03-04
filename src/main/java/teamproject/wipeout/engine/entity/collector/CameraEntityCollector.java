package teamproject.wipeout.engine.entity.collector;

import teamproject.wipeout.engine.component.*;
import teamproject.wipeout.engine.component.render.CameraComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.event.EntityChangeData;
import teamproject.wipeout.util.EventObserver;

public class CameraEntityCollector implements EventObserver<EntityChangeData> {

    protected GameEntity camera;
    protected Transform cameraTransform;
    protected CameraComponent cameraComponent;
    protected GameScene scene;

    public CameraEntityCollector(GameScene scene) {
        this.scene = scene;
        this.scene.entityChangeEvent.addObserver(this);

        // Go through all existing entities once created
        for (GameEntity entity: this.scene.entities) {
            this.addComponent(entity);
        }
    }

    public void cleanup() {
        this.scene.entityChangeEvent.removeObserver(this);
    }

    public GameEntity getMainCamera() {
        return this.camera;
    }

    public Transform getCameraTransform() {
        return this.cameraTransform;
    }

    public CameraComponent getCameraComponent() {
        return this.cameraComponent;
    }

    public void eventCallback(EntityChangeData e) {
        String change = e.getChange();
        GameEntity entity = e.getEntity();

        switch (change) {
            case "COMPONENT_ADDED":
                this.addComponent(entity);
                break;
            case "COMPONENT_REMOVED":
                this.removeComponent(entity);
                break;
            case "ENTITY_REMOVED":
                this.removeEntity(entity);
                break;
            default:
                System.out.println("Invalid entity change message:" + change);
                break;
        }
    }

    protected void addComponent(GameEntity entity) {
        if (this.camera == null && entity.hasComponent(TagComponent.class) && entity.getComponent(TagComponent.class).tag == "MainCamera" && entity.hasComponent(Transform.class) && entity.hasComponent(CameraComponent.class)) {
            this.camera = entity;
            this.cameraTransform = entity.getComponent(Transform.class);
            this.cameraComponent = entity.getComponent(CameraComponent.class);
        }
    };

    protected void removeComponent(GameEntity entity) {
        if (entity.getUUID() == this.camera.getUUID() && !(entity.hasComponent(TagComponent.class) && entity.getComponent(TagComponent.class).tag == "MainCamera" && entity.hasComponent(Transform.class) && entity.hasComponent(CameraComponent.class))) {
            this.camera = null;
            this.cameraTransform = null;
            this.cameraComponent = null;
        }
    }

    protected void removeEntity(GameEntity entity) {
        if (entity.getUUID() == this.camera.getUUID()) {
            this.camera = null;
            this.cameraTransform = null;
            this.cameraComponent = null;
        }
    }

}