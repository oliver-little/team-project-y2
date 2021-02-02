package teamproject.wipeout.engine.entity.event;

import teamproject.wipeout.engine.entity.GameEntity;

public interface EntityChangeData {
    public String getChange();
    public GameEntity getEntity();
}