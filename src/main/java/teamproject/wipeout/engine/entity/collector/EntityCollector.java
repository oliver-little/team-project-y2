package teamproject.wipeout.engine.entity.collector;

import teamproject.wipeout.engine.entity.event.EntityChangeData;
import teamproject.wipeout.util.EventObserver;

public interface EntityCollector extends EventObserver<EntityChangeData> {
    public void cleanup();
}
