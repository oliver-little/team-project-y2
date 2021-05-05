package teamproject.wipeout.engine.core;

import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.event.EntityChangeData;
import teamproject.wipeout.util.BasicEvent;

import java.util.ArrayList;
import java.util.List;


/** 
 * This class stores a list of entities that are part of a scene 
 */
public class GameScene {

    public BasicEvent<EntityChangeData> entityChangeEvent;
    public List<GameEntity> entities;


    /**
     * Creates a new instance of GameScene
     */
    public GameScene() {
        this.entityChangeEvent = new BasicEvent<EntityChangeData>();
        this.entities = new ArrayList<GameEntity>();
    }

    /**
     * Creates a new GameEntity in this scene
     */
    public GameEntity createEntity() {
        GameEntity newEntity = new GameEntity(this);
        return newEntity;
    }

    /**
     * Adds a new entity to the list of entities in this scene
     * @param newEntity The entity to add to the scene
     */
    public void addEntity(GameEntity newEntity) {
        if (!this.entities.contains(newEntity)) {
            this.entities.add(newEntity);
        }
    }

    /**
     * Removes an entity from the scene
     * @return Whether the entity was removed successfully
     */
    public boolean removeEntity(GameEntity toRemove) {
        return this.entities.remove(toRemove);
    }
}
