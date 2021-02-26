package teamproject.wipeout.game.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teamproject.wipeout.engine.component.ItemComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
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


    // Adds an item to inventory
    public void pickupItem(Item item) {
        Integer itemID = item.id;
        inventory.putIfAbsent(itemID, 0);
        inventory.put(itemID, inventory.get(itemID) + 1);
        System.out.println("Picked up " + item.name);
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

    // Scan all entities for items the player is standing over, and pick them up, and delete them from the map
    public void pickup(List<GameEntity> entities){
        List<GameEntity> removedItems = new ArrayList<>();
        for (GameEntity ge: entities){
            // Check if entity is an item
            if (ge.hasComponent(ItemComponent.class)){
                if(HitboxComponent.checkCollides(this, ge)) {
                    ItemComponent item = ge.getComponent(ItemComponent.class);
                    this.pickupItem(item.item);
                    removedItems.add(ge);
                }
            }
        }
        //cleanup
        for (GameEntity ge: removedItems) {
            entities.remove(ge);
            ge.destroy();
        }

        System.out.println("Inventory itemID to count:" + this.getInventory().toString());
    }
}