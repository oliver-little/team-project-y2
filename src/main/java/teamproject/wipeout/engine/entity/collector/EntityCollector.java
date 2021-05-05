package teamproject.wipeout.engine.entity.collector;

import teamproject.wipeout.engine.entity.event.EntityChangeData;
import teamproject.wipeout.util.EventObserver;

/**
 * Interface all EntityCollectors should use - listens for EntityChangeEvents and updates accordingly
 */
public interface EntityCollector extends EventObserver<EntityChangeData> {
    /**
     * Called when the EntityCollector is no longer being used to unsubscribe from the EntityChangeEvent
     */
    public void cleanup();
}
