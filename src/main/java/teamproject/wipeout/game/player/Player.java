package teamproject.wipeout.game.player;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.PickableComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.audio.MovementAudioComponent;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters.ParticleSimulationSpace;
import teamproject.wipeout.engine.component.render.particle.property.EaseCurve;
import teamproject.wipeout.engine.component.render.particle.property.OvalParticle;
import teamproject.wipeout.engine.component.shape.Rectangle;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.game.entity.ParticleEntity;
import teamproject.wipeout.game.farm.Pickables;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.ui.ErrorUI.ERROR_TYPE;
import teamproject.wipeout.game.potion.PotionEntity;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.game.task.ui.TaskUI;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.networking.state.StateUpdatable;
import teamproject.wipeout.util.SupplierGenerator;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Player extends GameEntity implements StateUpdatable<PlayerState> {

    public static final OvalParticle FAST_PARTICLE = new OvalParticle(new Color(1, 0.824, 0.004, 1));
    public static final OvalParticle SLOW_PARTICLE = new OvalParticle(new Color(0.001, 1, 0.733, 1));

    public final int MAX_SIZE = 10; //no. of inventory slots
    public final int INITIAL_MONEY = 2500; //initial amount of money
    public final int MAX_TASK_SIZE = 10; //no. of task slots

    public final Integer playerID;
    public String playerName;
    public String spriteSheetName;
    public Integer occupiedSlots;

    public int selectedSlot;

    private ArrayList<InventoryItem> inventory = new ArrayList<>(); //ArrayList used to store inventory

    public InventoryUI invUI;
    public ItemStore itemStore;

    public Integer size;

    public ArrayList<Task> tasks;
    public LinkedHashMap<Integer, Integer> currentAvailableTasks = new LinkedHashMap<>();
    private TaskUI taskUI;

    private LinkedHashMap<Integer, Integer> soldItems = new LinkedHashMap<>();

    private SignatureEntityCollector pickableCollector;

    private DoubleProperty money;

    private Consumer<PotionEntity> thrownPotion;
    private Supplier<GameClient> clientSupplier;
    private final PlayerState playerState;
    private final Transform position;
    private final MovementComponent physics;
    private final AudioComponent audio;

    private ParticleEntity sabotageEffect;

    /**
     * Creates a new instance of GameEntity
     *
     * @param scene The GameScene this entity is part of
     */
    public Player(GameScene scene, Integer playerID, String playerName, Point2D position, ItemStore itemStore, InventoryUI invUI) {
        super(scene);
        this.playerID = playerID;
        this.playerName = playerName;
        this.money = new SimpleDoubleProperty(INITIAL_MONEY);
        this.occupiedSlots = 0;

        this.itemStore = itemStore;

        this.playerState = new PlayerState(this.playerID, position, Point2D.ZERO, this.money.getValue());

        ParticleParameters parameters = new ParticleParameters(100, true, 
            FAST_PARTICLE, 
            ParticleSimulationSpace.WORLD, 
            SupplierGenerator.rangeSupplier(0.5, 1.5), 
            SupplierGenerator.rangeSupplier(1.0, 4.0), 
            null, 
            SupplierGenerator.staticSupplier(0.0), 
            SupplierGenerator.rangeSupplier(new Point2D(-25, -30), new Point2D(25, -10)));

        parameters.setEmissionRate(20);
        parameters.setEmissionPositionGenerator(SupplierGenerator.rangeSupplier(new Point2D(12, 0), new Point2D(52, 40)));
        parameters.addUpdateFunction((particle, percentage, timeStep) -> {
            particle.opacity = EaseCurve.FADE_IN_OUT.apply(percentage);
        });

        this.sabotageEffect = new ParticleEntity(scene, 0, parameters);
        this.sabotageEffect.setParent(this);

        this.position = new Transform(position, 0.0, 1);
        this.physics = new MovementComponent(0f, 0f, 0f, 0f);
        this.physics.stopCallback = (newPosition) -> {
            this.playerState.setPosition(newPosition);
            this.sendPlayerStateUpdate();
        };
        this.physics.speedMultiplierChanged = (newMultiplier) -> {
            // Updates local player when sabotage applied
            if (newMultiplier != 1) {
                if (newMultiplier < 1) {
                    sabotageEffect.getParameters().setEmissionType(SLOW_PARTICLE);
                }
                else {
                    sabotageEffect.getParameters().setEmissionType(FAST_PARTICLE);
                }

                if (!sabotageEffect.isPlaying()) {
                    sabotageEffect.play();
                }
            }
            else if (sabotageEffect.isPlaying()) {
                sabotageEffect.stop();
            }

            this.playerState.setSpeedMultiplier(newMultiplier);
            this.sendPlayerStateUpdate();
        };

        this.addComponent(this.position);
        this.addComponent(this.physics);
        this.addComponent(new MovementAudioComponent(this.getComponent(MovementComponent.class), "steps.wav"));

        this.addComponent(new HitboxComponent(new Rectangle(20, 45, 24, 16)));
        //this.addComponent(new CollisionResolutionComponent());

        this.audio = new AudioComponent();
        this.addComponent(this.audio);

        for (int i = 0; i < MAX_SIZE; i++) {
            this.inventory.add(null);
        }

        this.invUI = invUI;
        if (invUI != null) {
            selectSlot(0);
            this.pickableCollector = new SignatureEntityCollector(scene, Set.of(PickableComponent.class, HitboxComponent.class));
        }
    }

    public Point2D getPosition() {
        return this.position.getWorldPosition();
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
        this.sendPlayerStateUpdate();
    }

    public DoubleProperty moneyProperty() {
        return this.money;
    }

    public void setClientSupplier(Supplier<GameClient> supplier) {
        this.clientSupplier = supplier;
    }

    public Consumer<PotionEntity> getThrownPotion() {
        return this.thrownPotion;
    }

    public void setThrownPotion(Consumer<PotionEntity> thrownPotion) {
        this.thrownPotion = thrownPotion;
    }

    /**
     * Sets world position of the Player.
     *
     * @param position {@link Point2D} position of the Player
     */
    public void setWorldPosition(Point2D position) {
        this.position.setPosition(position);
    }

    /**
     * Adds acceleration to the physics component of the Player.
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
        this.physics.setSpeedMultiplier(newState.getSpeedMultiplier());
        this.physics.acceleration = newState.getAcceleration();
        if (newState.getAcceleration().equals(Point2D.ZERO)) {
            this.position.setPosition(newState.getPosition());
        }
        this.money.set(newState.getMoney());
        this.playerState.updateStateFrom(newState);

        // Update remote players
        if (newState.getSpeedMultiplier() != 1) {
            if (newState.getSpeedMultiplier() < 1) {
                sabotageEffect.getParameters().setEmissionType(SLOW_PARTICLE);
            }
            else {
                sabotageEffect.getParameters().setEmissionType(FAST_PARTICLE);
            }

            if (!sabotageEffect.isPlaying()) {
                sabotageEffect.play();
            }
        }
        else if (sabotageEffect.isPlaying()) {
            sabotageEffect.stop();
        }
    }

    /**
     * When called with a market item, purchases an item for a player and returns true, otherwise if player has not enough money, returns false
     * @param market - from which item is bought
     * @param id - of item to buy
     * @param quantity - of items to buy
     * @return true if successful, false if unsuccessful
     */
    public boolean buyItem(Market market, int id, int quantity) {
        if (!this.hasEnoughMoney(market.calculateTotalCost(id, quantity, true))) {
            return false;
        }
        if (!this.acquireItem(id, quantity)) {
        	return false;
        };
        this.setMoney(this.money.getValue() - market.buyItem(id, quantity));
        this.playSound("coins.wav");
        return true;
    }

    /**
     * If you want to buy a task from the market.
     * @param task The task you want to buy.
     * @return TRUE if successful, otherwise FALSE.
     */
    public boolean buyTask(Task task) {
        if(currentAvailableTasks.containsKey(task.id)) {
            invUI.displayMessage(ERROR_TYPE.TASK_EXISTS);
            return false;
        } else if (currentAvailableTasks.size() >= MAX_TASK_SIZE) {
            invUI.displayMessage(ERROR_TYPE.TASKS_FULL);
            return false;
        } else if (!hasEnoughMoney(task.priceToBuy)) {
            invUI.displayMessage(ERROR_TYPE.MONEY);
            return false;
        }
        this.addNewTask(task);
        this.playSound("coins.wav");
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
        this.playSound("coins.wav");
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
            if (this.invUI != null) {
                invUI.displayMessage(ERROR_TYPE.INVENTORY_FULL);
            }
            return false;
        }
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
    private int countFreeItemSpaces(Integer itemID) {
    	int counter = 0;
    	int stackLimit = this.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
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
        int stackLimit = this.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
        for (InventoryItem pair : inventory) { //adding to item's existing slots
            if((pair != null) && (itemID == pair.itemID) && ((pair.quantity + quantity) <= stackLimit)) {
                pair.quantity += quantity;
                inventory.set(i, pair);
                if (this.invUI != null) {
                    this.invUI.updateUI(inventory, i);
                }
                return true; //returns index of change
            }else if((pair != null) && (itemID == pair.itemID)) {
            	quantity -= stackLimit - pair.quantity;
            	pair.quantity = stackLimit;
            	inventory.set(i, pair);
                if (this.invUI != null) {
                    this.invUI.updateUI(inventory, i);
                }
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
                    if (this.invUI != null) {
                        this.invUI.updateUI(inventory, i);
                    }
	                return true; //returns index of change
	            }else if(pair == null) {
	            	pair = new InventoryItem(itemID, stackLimit);
	            	quantity -= stackLimit;
	                inventory.set(i, pair);
	                occupiedSlots++;
                    if (this.invUI != null) {
                        this.invUI.updateUI(inventory, i);
                    }
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
    	for (InventoryItem pair : inventory) {
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
    	InventoryItem withSpacePair = inventory.get(slotWithSpace);
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
    	for (InventoryItem pair : inventory) {
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
    	int stackLimit = this.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
    	if(quantity > noOfThisItem) {
    	    if (this.invUI != null) {
                invUI.displayMessage(ERROR_TYPE.INVENTORY_EMPTY);
            }
    		return -1;
    	}
    	int endQuantity = noOfThisItem - quantity;
    	int slotWithSpace = -1;
    	InventoryItem pair;
        for (int i = MAX_SIZE - 1; i >= 0; i--) {
        	pair = inventory.get(i);
            if((pair != null) && (pair.itemID == itemID)) {
            	if(quantity >= pair.quantity) {
            		quantity -= pair.quantity;
            		inventory.set(i, null); //free inventory slot
                    if (this.invUI != null) {
                        invUI.updateUI(inventory, i);
                    }
                    occupiedSlots--; //inventory slot is freed
                    if(quantity == 0) {
                    	break;
                    }
            	}else {
            		pair.quantity -= quantity;
                    inventory.set(i, pair);
                    if (this.invUI != null) {
                        invUI.updateUI(inventory, i);
                    }
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
    	return itemID;
    }

    /**
     * removes item(s) from the inventory from the selected slot
     * @param quantity of items to be removed
     * @return true if successfully removed, false if unable to remove
     */
    public boolean removeItemFromSelectedSlot(int quantity) {
        InventoryItem pair = inventory.get(selectedSlot);
        if((pair != null) && (pair.quantity - quantity) >= 0) {
            if ((pair.quantity - quantity) == 0) {
                inventory.set(selectedSlot, null); //free inventory slot
                invUI.updateUI(inventory, selectedSlot);
                occupiedSlots--; //inventory slot is freed
                return true;
            } else {
                pair.quantity -= quantity;
                inventory.set(selectedSlot, pair);
                invUI.updateUI(inventory, selectedSlot);
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
    
    /**
     * Checks if the inventory contains item(s) with a specific id
     * @param itemID - of item to be checked
     * @return - index of the first occurence of this item
     */
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
    public Set<Pickables.Pickable> pickup() {
        List<GameEntity> entities = this.pickableCollector.getEntities();
        HashSet<Pickables.Pickable> picked = new HashSet<Pickables.Pickable>();

        for (GameEntity ge: entities){
            // Check if entity is pickable
            if (ge.hasComponent(PickableComponent.class)){
                if(HitboxComponent.checkCollides(this, ge)) {
                    Pickables.Pickable pickable = ge.getComponent(PickableComponent.class).pickable;
                    if (!this.acquireItem(pickable.getID())) {
                    	System.out.println("No space for item with id: " + pickable.getID());
                    } else {
                        picked.add(pickable);
                        this.playSound("pop.wav");
                    }
                }
            }
        }

        this.checkTasks();
        System.out.println("Inventory itemID to count:" + this.getInventory().toString());

        return picked;
    }

    // Tasks

    /**
     * Checks what tasks have been completed
     */
    public void checkTasks() {
        if (this.tasks == null) {
            return;
        }

        for (Task task : tasks) {
            if(task.completed) {
                continue;
            }
            if(task.condition.apply(this)) {
                task.completed = true;
                currentAvailableTasks.remove(task.id);
                this.money.set(this.money.getValue() + task.reward);
                this.playSound("coinPrize.wav");
                invUI.displayMessage(ERROR_TYPE.TASK_COMPLETED);
                System.out.println("Task is completed");
            }
        }
        taskUI.showTasks(tasks);
    }
    

    public int getNumberOfCompletedTasks() {
        int completedTasks = 0;
        for(Task task : this.tasks) {
            if(task.completed) {
                completedTasks += 1;
            }
        }
        return completedTasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
        for (Task task: this.tasks) {
            currentAvailableTasks.putIfAbsent(task.id, 1);
        }
    }

    /**
     * Adds a task to the player's list of tasks.
     * @param task The task to add.
     */
    public void addNewTask(Task task) {
        if(currentAvailableTasks.containsKey(task.id)) {
            return;
        } else if (currentAvailableTasks.size() >= MAX_TASK_SIZE) {
            return;
        }
        this.currentAvailableTasks.putIfAbsent(task.id, 1);
        this.tasks.add(task);
        this.checkTasks();
    }

    /**
     * Checks if a player has enough money to purchase something and displays an error if not.
     * @param price The item/task they wish to buy.
     * @return FALSE (and displays error message) if they do not have enough money, otherwise TRUE.
     */
    public boolean hasEnoughMoney(double price) {
        if (getMoney() < price) {
            if (this.invUI != null) {
                this.invUI.displayMessage(ERROR_TYPE.MONEY);
            }
            return false;
        }
        return true;
    }

    public void playSound(String fileName){
        audio.play(fileName);
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
        if (this.clientSupplier == null) {
            return;
        }
        GameClient client = this.clientSupplier.get();
        if (client != null) {
            try {
                client.send(this.playerState);

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}