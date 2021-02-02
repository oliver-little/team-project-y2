package teamproject.wipeout.engine.entity.event;

import teamproject.wipeout.engine.entity.GameEntity;

public class EntityChangeEvent implements EntityChangeData {
    protected String _change;
    protected GameEntity _entity;

    public EntityChangeEvent(String change, GameEntity entity) {
        this._change = change;
        this._entity = entity;
    }

    public String getChange() {
        return this._change;
    }

    public GameEntity getEntity() {
        return this._entity;
    }

}
