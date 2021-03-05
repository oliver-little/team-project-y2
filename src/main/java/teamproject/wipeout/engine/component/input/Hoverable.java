package teamproject.wipeout.engine.component.input;

import teamproject.wipeout.engine.component.EntityAwareGameComponent;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputHoverableAction;

public class Hoverable implements EntityAwareGameComponent {

    public InputHoverableAction onClick;

    private GameEntity entity;

    public Hoverable(InputHoverableAction onClick) {
        this.onClick = onClick;
    }

    public GameEntity getEntity() {
        return this.entity;
    }

    public void setEntity(GameEntity newEntity) {
        this.entity = newEntity;
    }

    public String getType() {
        return "hoverable";
    }

}
