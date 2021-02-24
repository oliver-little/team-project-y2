package teamproject.wipeout.engine.component;

import teamproject.wipeout.engine.entity.GameEntity;

/**
 * Implements a GameComponent with knowledge of the GameEntity it is connected to
 */
public interface EntityAwareGameComponent extends GameComponent {
    /**
     * Gets the entity this GameComponent is connected to
     * 
     * @return The entity this GameComponent is connected to
     */
    public GameEntity getEntity();

    /**
     * Sets the entity this GameComponent is connected to
     * @param connectedEntity The new entity to connect this GameComponent to
     */
    public void setEntity(GameEntity connectedEntity);
}
