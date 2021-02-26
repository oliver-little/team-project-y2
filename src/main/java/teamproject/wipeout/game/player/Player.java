package main.java.teamproject.wipeout.game.player;

import java.util.HashMap;

import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.item.Item;

public class Player extends GameEntity {
    public Integer playerID;
    public String playerName;
    public String spriteSheetName;
    public Integer money;

    private HashMap<Integer, Integer> inventory = new HashMap<>();

    /**
     * Creates a new instance of GameEntity
     *
     * @param scene The GameScene this entity is part of
     */
    public Player(GameScene scene) {
        super(scene);
    }


    public void pickupItem(Integer itemID) {
        inventory.putIfAbsent(itemID, 0);
        inventory.put(itemID, inventory.get(itemID) + 1);
    }

    public boolean useItem(Integer itemID) {
        if (this.inventory.containsKey(itemID)) {
            int count = this.inventory.get(itemID);
            if (count <= 1){
                this.inventory.remove(itemID);
            } else {
                this.inventory.put(itemID, count - 1);
            }
            
            return true;
        } 
    
        return false;
    }

    public HashMap<Integer, Integer> getInventory() {
        return inventory;
    }
}