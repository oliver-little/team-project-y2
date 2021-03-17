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
import teamproject.wipeout.engine.component.shape.Rectangle;
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
    public final int INITIAL_MONEY = 25; //initial amount of money

    public final Integer playerID;
    public String playerName;
    public String spriteSheetName;
    public Integer occupiedSlots;

    public int selectedSlot;

    private ArrayList<InventoryItem> inventory = new ArrayList<>(); //ArrayList used to store inventory

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
        this.money = new SimpleDoubleProperty(INITIAL_MONEY);
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

    // When called with a market item, purchases an item for a player and returns true,
    // otherwise if player has not enough money, returns false
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

    // if the player has the item, removes a single copy of it from the backpack, adds money and returns true
    // if the player does not have the item return false
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

    // Adds single item to inventory
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
    	for(InventoryItem pair : inventory) {
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
    public int countFreeItemSpaces(Integer itemID) {
    	int counter = 0;
    	int stackLimit = invUI.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
    	for(InventoryItem pair : inventory) {
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
        for (InventoryItem pair : inventory) { //adding to item's existing slots
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
	        for (InventoryItem pair : inventory) { //adding to separate slot(s) to hold remaining items
	            if ((pair == null) && (quantity <= stackLimit)) {
	                pair = new InventoryItem(itemID, quantity);
	                inventory.set(i, pair);
	                occupiedSlots++;
	                this.invUI.updateUI(inventory, i);
	                return true; //returns index of change
	            }else if(pair == null) {
	            	pair = new InventoryItem(itemID, stackLimit);
	            	quantity -= stackLimit;
	                inventory.set(i, pair);
	                occupiedSlots++;
	                this.invUI.updateUI(inventory, i);
	            }
	            i++;
	        }
        }
    	return false;
    	/*
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
        */
    }

    /**
     * removes item(s) from the inventory, even if they span multiple slots
     * @param itemID for item to be removed
     * @param quantity of items to be removed
     * @return itemID if successfully removed, negative int if unable to remove
     */
    private int removeItem(int itemID, int quantity) {
    	int noOfThisItem = countItems(itemID);
    	if(quantity > noOfThisItem) {
    		return -1;
    	}
    	
    	int i = 0;
        for (InventoryItem pair : inventory) {
            if((pair != null) && (pair.itemID == itemID)) {
            	if(quantity >= pair.quantity) {
            		quantity -= pair.quantity;
            		inventory.set(i, null); //free inventory slot
                    invUI.updateUI(inventory, i);
                    occupiedSlots--; //inventory slot is freed
                    if(quantity == 0) {
                    	return itemID;
                    }
            	}else {
            		pair.quantity -= quantity;
                    inventory.set(i, pair);
                    invUI.updateUI(inventory, i);
                    return itemID;
            	}
            }
            i++;
        }
    	return itemID;
    	/*
        int i = 0;
        for (invPair pair : inventory) {
            if((pair != null) && (pair.itemID == itemID) && ((pair.quantity - quantity) >= 0)) {
                if ((pair.quantity - quantity) == 0) {
                    inventory.set(i, null); //free inventory slot
                    invUI.updateUI(inventory, i);
                    occupiedSlots--; //inventory slot is freed
                }else {
                    pair.quantity -= quantity;
                    inventory.set(i, pair);
                    invUI.updateUI(inventory, i);
                }
                return itemID;
            }
            i++;
        }
        return -1;
        */
    }

    /**
     * removes item(s) from the inventory from the selected slot
     * @param itemID for item to be removed
     * @param quantity of items to be removed
     * @return true if successfully removed, false if unable to remove
     */
    private boolean removeItemFromSelectedSlot(Integer itemID, int quantity) {
        InventoryItem pair = inventory.get(selectedSlot);
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
    public int selectSlot(int slot) {
        selectedSlot = slot;
        invUI.selectSlot(slot);

        InventoryItem selectedItem = this.inventory.get(slot);
        if (selectedItem != null) {
            return selectedItem.itemID;
        }
        return -1;
    }

    public ArrayList<InventoryItem> getInventory() {
        return inventory;
    }

    //for checking
    private void printInventory() {
        int i = 0;
        for(InventoryItem pair : inventory) {
            if (pair == null) {
                System.out.println(i + ": empty");
            }else {
                System.out.println(i + ": ItemID: "+pair.itemID+" Quantity: "+pair.quantity);
            }
            i++;
        }
    }

    public int containsItem(int itemID) {
        int i = 0;
        for(InventoryItem pair : inventory) {
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
        //System.out.println("Inventory itemID to count:" + this.getInventory().toString());
        //printInventory();
        this.checkTasks();
        System.out.println("Inventory itemID to count:" + this.getInventory().toString());
    }

    //check what tasks have been completed
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
    
    public void clearInventory() {
    	for (int i = 0; i < MAX_SIZE; i++) {
            inventory.set(i, null);
        }
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