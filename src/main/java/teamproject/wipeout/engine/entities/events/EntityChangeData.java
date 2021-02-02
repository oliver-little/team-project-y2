package teamproject.wipeout.engine.entities.events;

import java.util.Set;

public interface EntityChangeData {
    public String getChange();
    public String getEntityID();
    public Set<String> getComponents();
}