package teamproject.wipeout.game.player;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.PickableComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.audio.MovementAudioComponent;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.physics.Rectangle;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.task.ui.TaskUI;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.networking.state.StateUpdatable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class Player extends GameEntity implements StateUpdatable<PlayerState> {

    public final int MAX_SIZE = 10; //no. of inventory slots

    public final Integer playerID;
    public String playerName;
    public String spriteSheetName;
    public Integer occupiedSlots;

    public int selectedSlot;

    private ArrayList<invPair> inventory = new ArrayList<>(); //ArrayList used to store inventory

    public InventoryUI invUI;
    public ItemStore itemStore;
    public GameClient client;

    public Integer size;

    public ArrayList<Task> tasks;
    private TaskUI taskUI;

    private LinkedHashMap<Integer, Integer> soldItems = new LinkedHashMap<>();

    private SignatureEntityCollector pickableCollector;

    private DoubleProperty money;

    private final PlayerState playerState;
    private final Transform position;
    private final MovementComponent physics;

    /**
     * Creates a new instance of GameEntity
     *
     * @param scene The GameScene this entity is part of
     */
    public Player(GameScene scene, Integer playerID, String playerName, Point2D position, InventoryUI invUI) {
        super(scene);
        this.playerID = playerID;
        this.playerName = playerName;
        this.money = new SimpleDoubleProperty(100.0);
        this.occupiedSlots = 0;

        this.playerState = new PlayerState(this.playerID, position, Point2D.ZERO, this.money.getValue());

        this.position = new Transform(position, 0.0, 1);
        this.physics = new MovementComponent(0f, 0f, 0f, 0f);
        this.physics.stopCallback = (newPosition) -> {
            this.playerState.setPosition(newPosition);
            this.sendPlayerStateUpdate();
        };
        this.addComponent(this.position);
        this.addComponent(this.physics);
        this.addComponent(new MovementAudioComponent(this.getComponent(MovementComponent.class), "steps.wav"));

        this.addComponent(new HitboxComponent(new Rectangle(20, 12, 24, 16)));
        this.addComponent(new CollisionResolutionComponent());

        this.invUI = invUI;
        if (invUI != null) {
            for (int i = 0; i < MAX_SIZE; i++) {
                inventory.add(null);
            }
            selectSlot(0);

            this.pickableCollector = new SignatureEntityCollector(scene, Set.of(PickableComponent.class, HitboxComponent.class));
        }
    }

    public void setTaskUI(TaskUI taskUI) {
        this.taskUI = taskUI;
        this.checkTasks();
    }

    public double getMoney() {
        return this.money.getValue();
    }

    public void setMoney(double value) {
        this.money.set(value);
        this.playerState.setMoney(value);
    }

    public DoubleProperty moneyProperty() {
        return this.money;
    }

    /**
     * Adds acceleration to the physics component of the Player
     *
     * @param x X axis acceleration
     * @param y Y axis acceleration
     */
    public void addAcceleration(float x, float y) {
        this.physics.acceleration = this.physics.acceleration.add(x, y);
        this.playerState.setPosition(this.position.getWorldPosition());
        this.playerState.setAcceleration(this.physics.acceleration);
        this.sendPlayerStateUpdate();
    }

    public PlayerState getCurrentState() {
        return this.playerState;
    }

    public void updateFromState(PlayerState newState) {
        this.physics.acceleration = newState.getAcceleration();
        if (newState.getAcceleration().equals(Point2D.ZERO)) {
            this.position.setPosition(newState.getPosition());
        }
        this.money.set(newState.getMoney());
        this.playerState.updateStateFrom(newState);
    }

    /**
     * When called with a market item, purchases an item for a player and returns true, otherwise if player has not enough money, returns false
     * @param market - from which item is bought
     * @param id - of item to buy
     * @param quantity - of items to buy
     * @return true if successful, false if unsuccessful
     */
    public boolean buyItem(Market market, int id, int quantity) {
        if (market.calculateTotalCost(id, quantity, true) > this.money.getValue()) {
            return false;
        }
        if (!this.acquireItem(id, quantity)) {
        	return false;
        };
        this.setMoney(this.money.getValue() - market.buyItem(id, quantity));
        return true;
    }

    // if you want to purchase some task from the market
    public boolean buyTask(Task task) {
        if(task.priceToBuy > this.money.getValue()) {
            return false;
        }
        tasks.add(task);
        this.setMoney(this.money.getValue() - task.priceToBuy);
        return true;
    }

    /**
     * if the player has the item(s), removes them from the inventory, adds money and returns true, otherwise returns false
     * @param market - to which item is sold
     * @param id - of item to sell
     * @param quantity - of item to sell
     * @return true if successful, false if unsuccessful
     */
    public boolean sellItem(Market market, int id, int quantity) {
        if (removeItem(id, quantity) < 0) {
            return false;
        }
        this.setMoney(this.money.getValue() + market.sellItem(id, quantity));

        this.soldItems.putIfAbsent(id, 0);
        this.soldItems.put(id, this.soldItems.get(id) + quantity);
        checkTasks();
        return true;
    }

    /**
     * Adds a single item to the inventory
     * @param itemID
     * @return true if successful, false if unsuccessful (e.g. no space)
     */
    public boolean acquireItem(Integer itemID) {
        return acquireItem(itemID, 1);
    }

    /**
     * Adds item(s) of same id to the inventory
     * @param itemID
     * @param quantity
     * @return true if successful, false if unsuccessful (e.g. no space)
     */
    public boolean acquireItem(Integer itemID, int quantity) {
        if (!addToInventory(itemID, quantity)) {
            System.out.println("No space in inventory for item(s)");
            return false;
        }
        System.out.println("Acquired " + quantity + " of itemID " + itemID);
        return true;
    }
    
    /**
     * Method to count the number of items with a specific itemID
     * @param itemID - id of item to be counted
     * @return - quantity of item in inventory
     */
    public int countItems(Integer itemID) {
    	int counter = 0;
    	for(invPair pair : inventory) {
    		if((pair != null) && (pair.itemID == itemID)) {
    			counter += pair.quantity;
    		}
    	}
    	return counter;
    }
    
    /**
     * Method to count how many items with a specific itemID there is space for in the inventory
     * @param itemID - of item to be counted
     * @return - number of free spaces available for item
     */
    private int countFreeItemSpaces(Integer itemID) {
    	int counter = 0;
    	int stackLimit = invUI.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
    	for(invPair pair : inventory) {
    		if((pair != null) && (pair.itemID == itemID)) {
    			counter += stackLimit - pair.quantity;
    		}else if(pair == null) {
    			counter += stackLimit;
    		}
    	}
    	return counter;
    }

    /**
     * Adds new pair to inventory if item not present, otherwise increments quantity
     * @param itemID ID of item to be added
     * @param quantity quantity to be added
     * @return index of where item was added
     */
    private boolean addToInventory(int itemID, Integer quantity) {
    	if(quantity > countFreeItemSpaces(itemID)) {
    		return false; //not enough space for this many items
    	}
    	
    	int i = 0;
        int stackLimit = invUI.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
        for (invPair pair : inventory) { //adding to item's existing slots 
            if((pair != null) && (itemID == pair.itemID) && ((pair.quantity + quantity) <= stackLimit)) {
                pair.quantity += quantity;
                inventory.set(i, pair);
                this.invUI.updateUI(inventory, i);
                return true; //returns index of change
            }else if((pair != null) && (itemID == pair.itemID)) {
            	quantity -= stackLimit - pair.quantity;
            	pair.quantity = stackLimit;
            	inventory.set(i, pair);
            	this.invUI.updateUI(inventory, i);
            }
            i++;
        }
        if(quantity != 0) { 
	        i = 0;
	        for (invPair pair : inventory) { //adding to separate slot(s) to hold remaining items
	            if ((pair == null) && (quantity <= stackLimit)) {
	                pair = new invPair(itemID, quantity);
	                inventory.set(i, pair);
	                occupiedSlots++;
	                this.invUI.updateUI(inventory, i);
	                return true; //returns index of change
	            }else if(pair == null) {
	            	pair = new invPair(itemID, stackLimit);
	            	quantity -= stackLimit;
	                inventory.set(i, pair);
	                occupiedSlots++;
	                this.invUI.updateUI(inventory, i);
	            }
	            i++;
	        }
        }
    	return false;
    }
    
    /**
     * Finds the index of the slot with the least number of items with a specific item id.
     * @param itemID - of items to search for
     * @param stackLimit - max number of this item per slot
     * @return - index of slot holding the least of this item
     */
    private int findIndexOfLeast(Integer itemID, int stackLimit) {
    	int index = -1;
    	int current = stackLimit;
    	int i = 0;
    	for (invPair pair : inventory) {
    		if((pair != null) && (pair.itemID == itemID)) {
    			if(pair.quantity < current) {
    				current = i;
    				index = i;
    			}
    		}
    		i++;
    	}
    	return index;
    }
    
    /**
     * Rearranges items with a specific itemID to ensure they use the minimum number of slots possible
     * @param itemID - of item to be rearranged
     * @param quantity - quantity of item to be rearranged
     */
    private void rearrangeItems(Integer itemID, int quantity, int slotWithSpace, int stackLimit) {
    	if (quantity <= 1) {
    		return;
    	}else if(slotWithSpace < 0) {
    		System.out.println("logic issue - wasted slots but no slot with space");
    		return;
    	}
    	stackLimit = invUI.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
    	int indexOfExtraSlot = findIndexOfLeast(itemID, stackLimit);
    	System.out.println("index of extra slot = " + indexOfExtraSlot);
    	invPair withSpacePair = inventory.get(slotWithSpace);
    	withSpacePair.quantity += inventory.get(indexOfExtraSlot).quantity;
    	inventory.set(slotWithSpace, withSpacePair);
    	invUI.updateUI(inventory, slotWithSpace);
    	inventory.set(indexOfExtraSlot, null);
    	invUI.updateUI(inventory, indexOfExtraSlot);
    	
    }
    
    /**
     * counts the number of slots occupied by items with a specific id
     * @param itemID - of item of interest
     * @return - number of slots occupied by this item
     */
    public int countSlotsOccupiedBy(Integer itemID) {
    	int count = 0;
    	for (invPair pair : inventory) {
    		if((pair != null) && (pair.itemID == itemID)) {
    			count++;
    		}
    	}
    	return count;
    }

    /**
     * removes item(s) from the inventory, even if they span multiple slots, starting from the right-most slot
     * @param itemID for item to be removed
     * @param quantity of items to be removed
     * @return itemID if successfully removed, negative int if unable to remove
     */
    public int removeItem(int itemID, int quantity) {
    	int noOfThisItem = countItems(itemID);
    	int stackLimit = invUI.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
    	if(quantity > noOfThisItem) {
    		System.out.println("There isn't " + quantity + " of itemID " + itemID + " in the inventory");
    		
    		return -1;
    	}
    	int endQuantity = noOfThisItem - quantity;
    	int slotWithSpace = -1;
    	invPair pair;
        for (int i = MAX_SIZE - 1; i >= 0; i--) {
        	pair = inventory.get(i);
            if((pair != null) && (pair.itemID == itemID)) {
            	if(quantity >= pair.quantity) {
            		quantity -= pair.quantity;
            		inventory.set(i, null); //free inventory slot
                    invUI.updateUI(inventory, i);
                    occupiedSlots--; //inventory slot is freed
                    if(quantity == 0) {
                    	break;
                    }
            	}else {
            		pair.quantity -= quantity;
                    inventory.set(i, pair);
                    invUI.updateUI(inventory, i);
                    slotWithSpace = i;
                    break;
            	}
            }
        }
        int itemOccupiedSlots = countSlotsOccupiedBy(itemID);
        int minRequiredSlots = (int) (((double) (endQuantity + stackLimit -1))/((double) stackLimit));
        if(itemOccupiedSlots > minRequiredSlots) {
        	System.out.println("items being rearranged");
        	rearrangeItems(itemID, endQuantity, slotWithSpace, stackLimit); //rearranges items if slots are being wasted
        }
        System.out.println("Removed " + (noOfThisItem - endQuantity) + " of itemID " + itemID);
    	return itemID;
    }

    /**
     * removes item(s) from the inventory from the selected slot
     * @param quantity of items to be removed
     * @return true if successfully removed, false if unable to remove
     */
    public boolean removeItemFromSelectedSlot(int quantity) {
        invPair pair = inventory.get(selectedSlot);
        if((pair != null) && (pair.quantity - quantity) >= 0) {
            if ((pair.quantity - quantity) == 0) {
                inventory.set(selectedSlot, null); //free inventory slot
                invUI.updateUI(inventory, selectedSlot);
                occupiedSlots--; //inventory slot is freed
                System.out.println("Removed " + quantity + " items from selected slot");
                return true;
            }else {
                pair.quantity -= quantity;
                inventory.set(selectedSlot, pair);
                invUI.updateUI(inventory, selectedSlot);
                System.out.println("Removed " + quantity + " items from selected slot");
                return true;
            }
        }
        System.out.println("There isn't " + quantity + " items in the selected slot");
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
        removeItemFromSelectedSlot(1); //drops one item from selected slot in inventory
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
    public int selectSlot(int slot) {
        selectedSlot = slot;
        invUI.selectSlot(slot);

        invPair selectedItem = this.inventory.get(slot);
        if (selectedItem != null) {
            return selectedItem.itemID;
        }
        return -1;
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
    
    /**
     * Checks if the inventory contains item(s) with a specific id
     * @param itemID - of item to be checked
     * @return - index of the first occurence of this item
     */
    public int containsItem(int itemID) {
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
        return this.soldItems;
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

        this.checkTasks();
        System.out.println("Inventory itemID to count:" + this.getInventory().toString());
    }

    /**
     * Checks what tasks have been completed
     */
    public void checkTasks() {
        System.out.println("Checking tasks");
        for(Task task : tasks) {
            if(task.completed) {
                continue;
            }
            if(task.condition.apply(this)) {
                task.completed = true;
                this.money.set(this.money.getValue() + task.reward);
                System.out.println("Task is completed");
            }
        }
        taskUI.showTasks(tasks);
    }
    

    public int getNumberOfCompletedTasks() {
        int completedTasks = 0;
        for(Task task : tasks) {
            if(task.completed) {
                completedTasks += 1;
            }
        }
        return completedTasks;
    }
    
    /**
     * method to clear/empty the inventory
     */
    public void clearInventory() {
    	for (int i = 0; i < MAX_SIZE; i++) {
            inventory.set(i, null);
            invUI.updateUI(inventory, i);
        }
    	this.occupiedSlots = 0;
    }

    private void sendPlayerStateUpdate() {
        if (this.client != null) {
            try {
                this.client.send(this.playerState);

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}