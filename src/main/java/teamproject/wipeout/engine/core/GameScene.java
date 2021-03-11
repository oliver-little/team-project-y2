package teamproject.wipeout.engine.core;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.event.EntityChangeData;
import teamproject.wipeout.game.player.InventoryUI;
import teamproject.wipeout.game.player.Player;
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
        this.entities.add(newEntity);
        return newEntity;
    }

    public Player createPlayer(Integer id, String name, Point2D position, InventoryUI invUI) {
        Player playerEntity = new Player(this, id, name, position, invUI);
        this.entities.add(playerEntity);
        return playerEntity;
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
