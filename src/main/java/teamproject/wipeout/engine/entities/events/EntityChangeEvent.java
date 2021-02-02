package teamproject.wipeout.engine.entities.events;

import java.util.Set;

public class EntityChangeEvent implements EntityChangeData {
    protected String _change;
    protected String _entityID;
    protected Set<String> _components;

    public EntityChangeEvent(String change, String entityID, Set<String> components) {
        this._change = change;
        this._entityID = entityID;
        this._components = components;
    }

    public String getChange() {
        return this._change;
    }

    public String getEntityID() {
        return this._entityID;
    }

    public Set<String> getComponents() {
        return this._components;
    }
}
