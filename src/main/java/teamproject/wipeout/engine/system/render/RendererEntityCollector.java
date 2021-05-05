package teamproject.wipeout.engine.system.render;

import java.util.ArrayList;
import java.util.List;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.BaseEntityCollector;

/**
 * Entity collector implementation for the Renderer
 * Separates entities into static and dynamic renderable entities for the renderer.
 */
public class RendererEntityCollector extends BaseEntityCollector {

    public static final List<Class<? extends GameComponent>> signature = List.of(Transform.class, RenderComponent.class);
    protected List<GameEntity> dynamicEntityList;
    protected List<GameEntity> staticEntityList;

    /**
     * Creates a new instance of RendererEntityCollector
     * @param scene The GameScene this entity collector is part of
     */
    public RendererEntityCollector(GameScene scene) {
        super(scene);

        this.dynamicEntityList = new ArrayList<GameEntity>();
        this.staticEntityList = new ArrayList<GameEntity>();

        // Go through all existing entities once created
        for (GameEntity entity: this.scene.entities) {
            this.addComponent(entity);
        }
    }

    /**
     * Gets the list of dynamic entities in the scene
     * @return A list of GameEntities that contain a Transform and RenderComponent and are dynamic
     */
    public List<GameEntity> getEntities() {
        return this.dynamicEntityList;
    }

    /**
     * Gets the list of static entities in the scene
     * @return A list of GameEntities that contain a Transform and RenderComponent and are static
     */
    public List<GameEntity> getStaticEntities() {
        return this.staticEntityList;
    }

    /**
     * Called when a component is added to an entity in the scene
     * @param entity The GameEntity that changed
     */
    protected void addComponent(GameEntity entity) {
        if (this.testComponent(entity)) {
            RenderComponent rc = entity.getComponent(RenderComponent.class);
            if (rc.isStatic() && !this.staticEntityList.contains(entity)) {
                this.staticEntityList.add(entity);
            }
            else if (!rc.isStatic() && !this.dynamicEntityList.contains(entity)) {
                this.dynamicEntityList.add(entity);
            }
        }
    }

    /**
     * Called when a component is removed from an entity in the scene
     * @param entity The GameEntity that changed
     */
    protected void removeComponent(GameEntity entity) {
        if (!this.testComponent(entity)) {
            this.dynamicEntityList.remove(entity);
            this.staticEntityList.remove(entity);
        }
    }

    /**
     * Called when an entity is removed from the scene
     * @param entity The GameEntity that was removed
     */
    protected void removeEntity(GameEntity entity) {
        this.dynamicEntityList.remove(entity);
        this.staticEntityList.remove(entity);
    }
    

    /**
     * Tests a component to see if it meets the signature needed
     * @param entity The GameEntity to test
     * @return Whether the entity meets the signature.
     */
    private boolean testComponent(GameEntity entity) {
        for (Class<? extends GameComponent> componentClass : signature) {
            if (!entity.hasComponent(componentClass)) {
                return false;
            }
        }
        return true;
    }
}
