package teamproject.wipeout.engine.entities;

import java.util.Set;

import teamproject.wipeout.engine.entities.events.EntityChangeData;
import teamproject.wipeout.util.EventObserver;

public abstract class BaseEntityCollector implements EntityCollector, EventObserver<EntityChangeData> {
    
    public void eventCallback(EntityChangeData e) {
        String change = e.getChange();
        String entityID = e.getEntityID();
        Set<String> components = e.getComponents();
        
        switch (change) {
            case "COMPONENT_ADDED":
                this._addComponent(entityID, components);
                break;
            case "COMPONENT_REMOVED":
                this._removeComponent(entityID, components);
                break;
            case "ENTITY_DELETED":
                this._removeEntity(entityID);
                break;
            default:
                System.out.println("Invalid entity change message:" + change);
                break;
        }
    }

    protected abstract void _addComponent(String entityID, Set<String> components);
    protected abstract void _removeComponent(String entityID, Set<String> components);
    protected abstract void _removeEntity(String entityID);
}
