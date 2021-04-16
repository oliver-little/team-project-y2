package teamproject.wipeout.game.entity;

import teamproject.wipeout.engine.component.TagComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.CameraComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;

public class CameraEntity extends GameEntity {

    public static final float CAMERA_ZOOM = 1.5f;
    public static final String MAIN_CAMERA_TAG = "MainCamera";

    /**
     * Creates a new instance of CameraEntity
     *
     * @param scene The GameScene this entity is part of
     */
    public CameraEntity(GameScene scene) {
        super(scene);
        this.addComponent(new Transform(0.0, 0.0));
        this.addComponent(new CameraComponent(CAMERA_ZOOM));
        this.addComponent(new TagComponent(MAIN_CAMERA_TAG));
    }
}
