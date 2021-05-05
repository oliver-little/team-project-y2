package teamproject.wipeout.engine.entity.event;

import teamproject.wipeout.engine.entity.GameEntity;

/**
 * Interface representing some change to a GameEntity in the scene.
 */
public interface EntityChangeData {
    /**
     * Gets the change that occurred to the entity
     * @return The change that occurred (one of "COMPONENT_ADDED", "COMPONENT_REMOVED", "ENTITY_REMOVED")
     */
    public String getChange();

    /**
     * Gets the entity that was affected by the change 
     * @return A GameEntity instance
     */
    public GameEntity getEntity();
}