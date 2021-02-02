package teamproject.wipeout.engine.entity.collector;

import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.event.EntityChangeData;
import teamproject.wipeout.util.EventObserver;

public abstract class BaseEntityCollector implements EntityCollector, EventObserver<EntityChangeData> {
    
    public void eventCallback(EntityChangeData e) {
        String change = e.getChange();
        GameEntity entity = e.getEntity();
        
        switch (change) {
            case "COMPONENT_ADDED":
                this._addComponent(entity);
                break;
            case "COMPONENT_REMOVED":
                this._removeComponent(entity);
                break;
            case "ENTITY_DELETED":
                this._removeEntity(entity);
                break;
            default:
                System.out.println("Invalid entity change message:" + change);
                break;
        }
    }

    protected abstract void _addComponent(GameEntity entity);
    protected abstract void _removeComponent(GameEntity entity);
    protected abstract void _removeEntity(GameEntity entity);
}
