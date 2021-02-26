package teamproject.wipeout.engine.system.render;

import teamproject.wipeout.engine.component.*;
import teamproject.wipeout.engine.component.render.CameraComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.event.EntityChangeData;
import teamproject.wipeout.util.EventObserver;

public class CameraEntityCollector implements EventObserver<EntityChangeData> {

    protected GameEntity _camera;
    protected Transform _cameraTransform;
    protected CameraComponent _cameraComponent;
    protected GameScene _scene;

    public CameraEntityCollector(GameScene scene) {
        this._scene = scene;
        this._scene.entityChangeEvent.addObserver(this);
    }

    public void cleanup() {
        this._scene.entityChangeEvent.removeObserver(this);
    }

    public GameEntity getMainCamera() {
        return this._camera;
    }

    public Transform getCameraTransform() {
        return this._cameraTransform;
    }

    public CameraComponent getCameraComponent() {
        return this._cameraComponent;
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
        if (this._camera == null && entity.hasComponent(TagComponent.class) && entity.getComponent(TagComponent.class).tag == "MainCamera" && entity.hasComponent(Transform.class) && entity.hasComponent(CameraComponent.class)) {
            this._camera = entity;
            this._cameraTransform = entity.getComponent(Transform.class);
            this._cameraComponent = entity.getComponent(CameraComponent.class);
        }
    };

    protected void removeComponent(GameEntity entity) {
        if (entity.getUUID() == this._camera.getUUID() && !(entity.hasComponent(TagComponent.class) && entity.getComponent(TagComponent.class).tag == "MainCamera" && entity.hasComponent(Transform.class) && entity.hasComponent(CameraComponent.class))) {
            this._camera = null;
            this._cameraTransform = null;
            this._cameraComponent = null;
        }
    }

    protected void removeEntity(GameEntity entity) {
        if (entity.getUUID() == this._camera.getUUID()) {
            this._camera = null;
            this._cameraTransform = null;
            this._cameraComponent = null;
        }
    }

}
