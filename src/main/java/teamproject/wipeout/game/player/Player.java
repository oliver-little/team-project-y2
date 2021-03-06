package teamproject.wipeout.game.player;

import teamproject.wipeout.engine.component.PickableComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.market.MarketItem;
import teamproject.wipeout.engine.entity.InventoryEntity;
import teamproject.wipeout.game.player.invPair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Player extends GameEntity {
    public Integer playerID;
    public String playerName;
    public String spriteSheetName;
    public Double money;
    public Integer size;
    public InventoryEntity invEntity;
    public int selectedSlot;
    
    public static int MAX_SIZE = 10;
    
    private ArrayList<invPair> inventory = new ArrayList<>();
    //private LinkedHashMap<Integer, Integer> inventory = new LinkedHashMap<>();

    /**
     * Creates a new instance of GameEntity
     *
     * @param scene The GameScene this entity is part of
     */
    public Player(GameScene scene, Integer playerID, String playerName, InventoryEntity invEntity) {
        super(scene);
        this.playerID = playerID;
        this.playerName = playerName;
        this.money = 0.0;
        this.size = 0;
        this.invEntity = invEntity;
        for (int i = 0; i < MAX_SIZE; i++) {
        	inventory.add(null);
        }
        selectSlot(0);
        
    }

    // When called with a market item, purchases an item for a player and returns true,
    // otherwise if player has not enough money, returns false
    public boolean buyItem(MarketItem item) {
        if (item.getCurrentBuyPrice() > this.money) {
            return false;
        }
        this.money -= item.getCurrentBuyPrice();
        this.acquireItem(item.getID());
        return true;
    }

    // if the player has the item, removes a single copy of it from the backpack, adds money and returns true
    // if the player does not have the item return false
    public boolean sellItem(MarketItem item) {
        if (removeItem(item.getID())) {
            this.money += item.getCurrentSellPrice();
            return true;
        }
        return false;
    }

    // Adds an item to inventory
    public void acquireItem(Integer itemID) {
        int index = addToInventory(itemID, 1);
        this.invEntity.updateUI(inventory, index);
        System.out.println("Acquired itemID: " + itemID);
        size++;
    }
    
    /**
     * Adds new pair to inventory if item not present, otherwise increments quantity
     * @param itemID ID of item to be added
     * @param quantity quantity to be added
     */
    private Integer addToInventory(Integer itemID, Integer quantity) {
    	int i = 0;
    	for (invPair pair : inventory) {
    		if((pair != null) && itemID == pair.itemID) {
    			pair.quantity += quantity;
    			inventory.set(i, pair);
    			return i; //returns index of change
    		}
    		i++;
    	}
    	i = 0;
    	for (invPair pair : inventory) {
    		if (pair == null) {
    			pair = new invPair(itemID, quantity);
    			inventory.set(i, pair);
    			return i; //returns index of change
    		}
    		i++;
    	}
    	return null;
    }

    // removes a SINGLE copy of an item from the players backpack and returns true
    // if player does not have the item, returns false;
    public boolean removeItem(Integer itemID) {
        int i = 0;
        for (invPair pair : inventory) {
        	if((pair != null) && pair.itemID == itemID) {
        		if (pair.quantity == 1) {
        			inventory.set(i, null);
        			invEntity.updateUI(inventory, i);
        			size--;
        			return true;
        		}else {
        			pair.quantity--;
        			inventory.set(i, pair);
        			invEntity.updateUI(inventory, i);
        			size--;
        			return true;
        		}
        	}
        	i++;
        }
        return false;
    }
    
    public int dropItem() {
    	if(inventory.get(selectedSlot) == null) {
    		return -1;
    	}
    	int id = inventory.get(selectedSlot).itemID;
    	removeItem(inventory.get(selectedSlot).itemID);
    	return id;
    }
    
    public void selectSlot(int slot) {
    	selectedSlot = slot;
    	invEntity.selectSlot(slot);
    }

    public ArrayList<invPair> getInventory() {
        return inventory;
    }
    
    //for checking
    private void printInventory() {
    	int i = 0;
    	for(invPair pair : inventory) {
    		if (pair == null) {
    			System.out.println(i + ": empty");
    		}else {
    			System.out.println(i + ": ItemID: "+pair.itemID+" Quantity: "+pair.quantity);
    		}
    		i++;
    	}
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

        //System.out.println("Inventory itemID to count:" + this.getInventory().toString());
        //printInventory();
    }
}