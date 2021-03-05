package teamproject.wipeout.game.player;

import teamproject.wipeout.engine.component.PickableComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.market.Market;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Player extends GameEntity {
    public Integer playerID;
    public String playerName;
    public String spriteSheetName;
    public Double money;
    public Integer size;
    
    public static int MAX_SIZE = 10;

    private LinkedHashMap<Integer, Integer> inventory = new LinkedHashMap<>();

    /**
     * Creates a new instance of GameEntity
     *
     * @param scene The GameScene this entity is part of
     */
    public Player(GameScene scene, Integer playerID, String playerName) {
        super(scene);
        this.playerID = playerID;
        this.playerName = playerName;
        this.money = 1000.0;
    }

    // When called with a market item, purchases an item for a player and returns true,
    // otherwise if player has not enough money, returns false
    public boolean buyItem(Market market, int id, int quantity) {
        if (market.calculateTotalCost(id, quantity, true) > this.money) {
            return false;
        }
        this.money -= market.buyItem(id, quantity);
        this.acquireItem(id, quantity);
        return true;
    }

    // if the player has the item, removes a single copy of it from the backpack, adds money and returns true
    // if the player does not have the item return false
    public boolean sellItem(Market market, int id, int quantity) {
        if (removeItem(id, quantity)) {
            this.money += market.sellItem(id, quantity);
            return true;
        }
        return false;
    }

    // Adds an item to inventory
    public void acquireItem(int itemID) {
        inventory.putIfAbsent(itemID, 0);
        inventory.put(itemID, inventory.get(itemID) + 1);
        System.out.println("Acquired itemID: " + itemID);
    }

    // Adds a given quantity of an item to the inventory
    public void acquireItem(int itemID, int quantity) {
        inventory.putIfAbsent(itemID, 0);
        inventory.put(itemID, inventory.get(itemID) + quantity);
        System.out.println("Acquired " + quantity + " of itemID " + itemID);
    }

    // removes a SINGLE copy of an item from the players backpack and returns true
    // if player does not have the item, returns false;
    public boolean removeItem(int itemID) {
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

    // Removes a given quantity of an item from the player's backpack and returns true
    // Returns false if the player doesn't have the item or have enough of the item
    public boolean removeItem(int itemID, int quantity) {
        if (this.inventory.containsKey(itemID)) {
            int count = this.inventory.get(itemID);
            if (count < quantity) {
                return false;
            }
            else if (count == quantity) {
                this.inventory.remove(itemID);
            }
            else {
                this.inventory.put(itemID, count - quantity);
            }
            return true;
        }
        return false;
    }

    public LinkedHashMap<Integer, Integer> getInventory() {
        return inventory;
    }

    // Scan all entities for items the player is standing over, and pick them up, and delete them from the map
    public void pickup(List<GameEntity> entities){
        List<GameEntity> removedItems = new ArrayList<>();
        for (GameEntity ge: entities){
            // Check if entity is an item
            if (ge.hasComponent(PickableComponent.class)){
                if(HitboxComponent.checkCollides(this, ge)) {
                	PickableComponent item = ge.getComponent(PickableComponent.class);
                    this.acquireItem(item.item.id);
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