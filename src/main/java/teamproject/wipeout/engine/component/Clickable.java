package teamproject.wipeout.engine.component;

import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputMouseAction;

public class Clickable implements EntityAwareGameComponent {

    public InputMouseAction onClick;
    
    private GameEntity entity;

    public Clickable(InputMouseAction onClick) {
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
