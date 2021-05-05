package teamproject.wipeout.engine.entity.event;

import teamproject.wipeout.engine.entity.GameEntity;

/***
 * Event representing changes in the state of an entity
 */
public class EntityChangeEvent implements EntityChangeData {
    protected String change;
    protected GameEntity entity;

    /**
     * Creates a new instance of EntityChangeEvent
     * @param change The change that occurred (one of "COMPONENT_ADDED", "COMPONENT_REMOVED", "ENTITY_REMOVED")
     * @param entity The entity that was affected in this event
     */
    public EntityChangeEvent(String change, GameEntity entity) {
        this.change = change;
        this.entity = entity;
    }

    /**
     * Gets the change that occurred to the entity
     * @return The change that occurred (one of "COMPONENT_ADDED", "COMPONENT_REMOVED", "ENTITY_REMOVED")
     */
    public String getChange() {
        return this.change;
    }

    /**
     * Gets the entity that was affected by the change 
     * @return A GameEntity instance
     */
    public GameEntity getEntity() {
        return this.entity;
    }

}
