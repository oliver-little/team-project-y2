package teamproject.wipeout.engine.entity.collector;

import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.event.EntityChangeData;

/**
 * Abstract class providing reusable behaviours for all EntityCollectors
 */
public abstract class BaseEntityCollector implements EntityCollector {

    protected GameScene scene;

    /**
     * Creates a new instance of BaseEntityCollector
     * @param scene The GameScene this EntityCollector is listening to
     */
    public BaseEntityCollector(GameScene scene) {
        this.scene = scene;

        scene.entityChangeEvent.addObserver(this);
    }

    public void cleanup() {
        this.scene.entityChangeEvent.removeObserver(this);
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

    /**
     * Called when a component is added to an entity
     * @param entity The entity that was affected
     */
    protected abstract void addComponent(GameEntity entity);

    /**
     * Called when a component is removed from an entity
     * @param entity The entity that was affected
     */
    protected abstract void removeComponent(GameEntity entity);

    /**
     * Called when an entity is removed from the GameScene
     * @param entity The entity that was affected
     */
    protected abstract void removeEntity(GameEntity entity);
}
