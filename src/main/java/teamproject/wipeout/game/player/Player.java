package teamproject.wipeout.game.player;

import teamproject.wipeout.engine.component.PickableComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.market.MarketItem;
import teamproject.wipeout.game.player.invPair;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.game.market.Market;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import teamproject.wipeout.game.task.Task;
import java.util.Set;

public class Player extends GameEntity {
    public Integer playerID;
    public String playerName;
    public String spriteSheetName;
    public Double money;
    public Integer occupiedSlots;
    
    public InventoryUI invUI;
    public ItemStore itemStore;
    
    public int selectedSlot;
    
    public static int MAX_SIZE = 10; //no. of inventory slots
    
    private ArrayList<invPair> inventory = new ArrayList<>(); //ArrayList used to store inventory
    //private LinkedHashMap<Integer, Integer> inventory = new LinkedHashMap<>();

    public Integer size;

    public ArrayList<Task> tasks;

    private LinkedHashMap<Integer, Integer> soldItems = new LinkedHashMap<>();
    private SignatureEntityCollector pickableCollector;
    /**
     * Creates a new instance of GameEntity
     *
     * @param scene The GameScene this entity is part of
     */
    public Player(GameScene scene, Integer playerID, String playerName, InventoryUI invUI) {
        super(scene);
        this.playerID = playerID;
        this.playerName = playerName;
        this.money = 0.0;
        this.occupiedSlots = 0;
        this.invUI = invUI;
        for (int i = 0; i < MAX_SIZE; i++) {
        	inventory.add(null);
        }
        selectSlot(0);
        this.pickableCollector = new SignatureEntityCollector(this.scene, Set.of(PickableComponent.class, HitboxComponent.class));
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

    // if you want to purchase some task from the market
    public boolean buyTask(Task task) {
        if(task.priceToBuy > this.money) {
            return false;
        }
        tasks.add(task);
        this.money -= task.priceToBuy;
        return true;
    }

    // if the player has the item, removes a single copy of it from the backpack, adds money and returns true
    // if the player does not have the item return false
    public boolean sellItem(Market market, int id, int quantity) {
        if (removeItem(id, quantity)) {
            this.money += market.sellItem(id, quantity);
            this.soldItems.putIfAbsent(id, 0);
            this.soldItems.put(id, this.soldItems.get(id) + 1);
            checkTasks();
            return true;
        }
        return false;
    }

    // Adds single item to inventory
    public boolean acquireItem(Integer itemID) {
        int index = addToInventory(itemID, 1);
        if (index < 0) {
        	System.out.println("No space in inventory for item");
        	return false;
        }
        this.invUI.updateUI(inventory, index);
        System.out.println("Acquired itemID: " + itemID);
        occupiedSlots++;
        return true;
    }
    
    /**
     * Adds item(s) of same id to the inventory
     * @param itemID
     * @param quantity
     * @return true if successful, false if unsuccessful (e.g. no space)
     */
    public boolean acquireItem(Integer itemID, int quantity) {
        int index = addToInventory(itemID, quantity);
        if (index < 0) {
        	System.out.println("No space in inventory for item(s)");
        	return false;
        }
        this.invUI.updateUI(inventory, index);
        System.out.println("Acquired " + quantity + " of itemID " + itemID);
        return true;
    }
    
    /**
     * Adds new pair to inventory if item not present, otherwise increments quantity
     * @param itemID ID of item to be added
     * @param quantity quantity to be added
     * @return index of where item was added
     */
    private int addToInventory(Integer itemID, Integer quantity) {
    	int i = 0;
    	int stackLimit = invUI.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
    	for (invPair pair : inventory) {
    		if((pair != null) && (itemID == pair.itemID) && ((pair.quantity + quantity) <= stackLimit)) {
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
    			occupiedSlots++;
    			return i; //returns index of change
    		}
    		i++;
    	}
    	return -1;
    }

    /**
     * removes item(s) from the inventory
     * @param itemID for item to be removed
     * @param quantity of items to be removed
     * @return true if successfully removed, false if unable to remove
     */
    private boolean removeItem(Integer itemID, int quantity) {
        int i = 0;
        for (invPair pair : inventory) {
        	if((pair != null) && (pair.itemID == itemID) && ((pair.quantity - quantity) >= 0)) {
        		if ((pair.quantity - quantity) == 0) {
        			inventory.set(i, null); //free inventory slot
        			invUI.updateUI(inventory, i);
        			occupiedSlots--; //inventory slot is freed
        			return true;
        		}else {
        			pair.quantity -= quantity;
        			inventory.set(i, pair);
        			invUI.updateUI(inventory, i);
        			return true;
        		}
        	}
        	i++;
        }
        return false;
    }
    
    /**
     * removes item(s) from the inventory from the selected slot
     * @param itemID for item to be removed
     * @param quantity of items to be removed
     * @return true if successfully removed, false if unable to remove
     */
    private boolean removeItemFromSelectedSlot(Integer itemID, int quantity) {
        invPair pair = inventory.get(selectedSlot);
        if((pair != null) && (pair.quantity - quantity) >= 0) {
        	if ((pair.quantity - quantity) == 0) {
    			inventory.set(selectedSlot, null); //free inventory slot
    			invUI.updateUI(inventory, selectedSlot);
    			occupiedSlots--; //inventory slot is freed
    			return true;
    		}else {
    			pair.quantity -= quantity;
    			inventory.set(selectedSlot, pair);
    			invUI.updateUI(inventory, selectedSlot);
    			return true;
    		}
        }
        return false;
    }
    
    /**
     * drops single item from the selected slot in inventory
     * @return the ID of the item dropped
     */
    public int dropItem() {
    	if(inventory.get(selectedSlot) == null) {
    		return -1;
    	}
    	int id = inventory.get(selectedSlot).itemID;
    	removeItemFromSelectedSlot(inventory.get(selectedSlot).itemID, 1); //drops one item from inventory
    	return id;
    }
    
    /**
     * drops multiple items from the selected slot in inventory
     * @return the ID of the item dropped
     */
    public int dropItem(int quantity) {
    	if(inventory.get(selectedSlot) == null) {
    		return -1;
    	}
    	int id = inventory.get(selectedSlot).itemID;
    	removeItem(inventory.get(selectedSlot).itemID, quantity); //drops one item from inventory
    	return id;
    }
    
    /**
     * Called to select which slot/index in the inventory, ready for dropItem() method to use
     * @param slot or index selected
     */
    public void selectSlot(int slot) {
    	selectedSlot = slot;
    	invUI.selectSlot(slot);
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

    public int containsItem(Integer itemID) {
    	int i = 0;
    	for(invPair pair : inventory) {
    		if((pair != null ) && (pair.itemID == itemID)) {
    			return i;
    		}
    		i++;
    	}
    	return -1;
    }

    public LinkedHashMap<Integer, Integer> getSoldItems() {
        return soldItems;
    }

    /**
     * Scan all entities for items the player is standing over, and pick them up, and delete them from the map
     */
    public void pickup() {
        List<GameEntity> entities = this.pickableCollector.getEntities();
        List<GameEntity> removedItems = new ArrayList<>();
        for (GameEntity ge: entities){
            // Check if entity is pickable
            if (ge.hasComponent(PickableComponent.class)){
                if(HitboxComponent.checkCollides(this, ge)) {
                	PickableComponent item = ge.getComponent(PickableComponent.class);
                    if (!this.acquireItem(item.item.id)) {
                    	System.out.println("No space for item with id: " + item.item.id);
                    }else {
                    	removedItems.add(ge);
                    }
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
        this.checkTasks();
        System.out.println("Inventory itemID to count:" + this.getInventory().toString());
    }

    //check what tasks have been completed
    public void checkTasks() {
        for(Task task : tasks) {
            if(task.completed) {
                continue;
            }
            if(task.condition.apply(this)) {
                task.completed = true;
                this.money += task.reward;
                System.out.println("Task completed");
            }
        }
    }

    // get number of completed tasks
    // in the future we can check when all tasks have been completed, to always add new ones
    public int getNumberOfCompletedTasks() {
        int completedTasks = 0;
        for(Task task : tasks) {
            if(task.completed) {
                completedTasks += 1;
            }
        }
        return completedTasks;
    }
}