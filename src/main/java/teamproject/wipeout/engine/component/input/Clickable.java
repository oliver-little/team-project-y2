package teamproject.wipeout.engine.component.input;

import teamproject.wipeout.engine.component.EntityAwareGameComponent;
import teamproject.wipeout.engine.entity.GameEntity;

public class Clickable implements EntityAwareGameComponent {

    public EntityClickAction onClick;

    private GameEntity entity;

    public Clickable(EntityClickAction onClick) {
        this.onClick = onClick;
    }

    public GameEntity getEntity() {
        return this.entity;
    }    

    public void setEntity(GameEntity newEntity) {
        this.entity = newEntity;
    }

    public String getType() {
        return "clickable";
    }
}
