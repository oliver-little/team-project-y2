package teamproject.wipeout.engine.core;

import java.util.ArrayList;
import java.util.List;

import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.event.EntityChangeData;
import teamproject.wipeout.util.BasicEvent;


/** 
 * This class stores a list of entities that are part of a scene 
 */
public class GameScene {

    public BasicEvent<EntityChangeData> entityChangeEvent;
    public List<GameEntity> entities;


    public GameScene() {
        this.entityChangeEvent = new BasicEvent<EntityChangeData>();
        this.entities = new ArrayList<GameEntity>();
    }

    public GameEntity createEntity() {
        GameEntity newEntity = new GameEntity(this);
        return newEntity;
    }

    public void addEntity(GameEntity newEntity) {
        if (!this.entities.contains(newEntity)) {
            this.entities.add(newEntity);
        }
    }

    public boolean removeEntity(GameEntity toRemove) {
        return this.entities.remove(toRemove);
    }
}
