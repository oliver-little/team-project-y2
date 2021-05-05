package teamproject.wipeout.game.player;

import javafx.util.Pair;
import teamproject.wipeout.engine.component.PickableComponent;
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.audio.MovementAudioComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.Pickable;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.inventory.InventoryItem;
import teamproject.wipeout.game.inventory.InventoryUI;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.ui.ErrorUI.ERROR_TYPE;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.game.task.ui.TaskUI;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.networking.state.StateUpdatable;

import java.util.*;

/**
 * Class which represents the player of this instance of the game
 */
public class CurrentPlayer extends Player implements StateUpdatable<PlayerState> {

    public static final int MAX_TASK_SIZE = 10;

    public static final String DEFAULT_NAME = "Me";

    private FarmEntity myFarm;

    private int selectedSlot;
    private InventoryUI inventoryUI;

    private TaskUI taskUI;
    private ArrayList<Task> tasks;
    private final LinkedHashMap<Integer, Integer> currentAvailableTasks = new LinkedHashMap<>();

    private final LinkedHashMap<Integer, Integer> soldItems = new LinkedHashMap<>();
    private final SignatureEntityCollector pickableCollector;
    private final AudioComponent audio;

    public boolean debug = false;

    /**
     * constructor for CurrentPlayer (player represented by current instance of game)
     * @param scene - GameScene player is to be added to
     * @param playerInfo - represents player's id and name
     * @param spriteSheet - sprites for the player
     * @param spriteManager - used to get player's sprited
     * @param itemStore - store of items, their ids and other details
     */
    public CurrentPlayer(GameScene scene, Pair<Integer, String> playerInfo, String spriteSheet, SpriteManager spriteManager, ItemStore itemStore) {
        super(scene, playerInfo, spriteSheet, spriteManager, itemStore);

        this.addComponent(new MovementAudioComponent(this.physics, "steps.wav"));

        this.audio = new AudioComponent();
        this.addComponent(this.audio);

        this.pickableCollector = new SignatureEntityCollector(scene, Set.of(PickableComponent.class, HitboxComponent.class));

        this.myFarm = null;
        this.taskUI = null;
    }

    /**
     * assigns a farm to the player
     * @param farm - the farm entity to be assigned to the player
     */
    public void assignFarm(FarmEntity farm) {
        super.assignFarm(farm);
        this.myFarm = farm;
    }

    /**
     * gets player's farm
     * @return the player's FarmEntity
     */
    public FarmEntity getMyFarm() {
        return this.myFarm;
    }

    /**
     * sets the UI for the player's inventory
     * @param inventoryUI - UI which represents the inventory to be assigned to player
     */
    public void setInventoryUI(InventoryUI inventoryUI) {
        this.inventoryUI = inventoryUI;
        this.selectSlot(0);
    }

    /**
     * gets the player's tasks
     * @return an ArrayList of the player's tasks
     */
    public ArrayList<Task> getTasks() {
        return this.tasks;
    }

    /**
     * gets the tasks which the player hasn't yet completed 
     * @return ArrayList of player's tasks
     */
    public LinkedHashMap<Integer, Integer> getCurrentAvailableTasks() {
        return this.currentAvailableTasks;
    }

    /**
     * assigns the player a UI to represent the tasks
     * @param taskUI - the UI which shows the player's tasks
     */
    public void setTaskUI(TaskUI taskUI) {
        this.taskUI = taskUI;
        this.checkTasks();
    }

    /**
     * When called with a market item, purchases an item for a player and returns true, otherwise if player has not enough money, returns false
     * @param market - from which item is bought
     * @param id - of item to buy
     * @param quantity - of items to buy
     * @return true if successful, false if unsuccessful
     */
    public boolean buyItem(Market market, int id, int quantity) {
        boolean boughtItem = super.buyItem(market, id, quantity);
        if (boughtItem) {
            this.playSound("coins.wav");
        }
        return boughtItem;
    }

    /**
     * If you want to buy a task from the market.
     * @param task The task you want to buy.
     * @return TRUE if successful, otherwise FALSE.
     */
    public boolean buyTask(Task task) {
        if(currentAvailableTasks.containsKey(task.id)) {
            if (!debug) inventoryUI.displayMessage(ERROR_TYPE.TASK_EXISTS);
            return false;
        } else if (currentAvailableTasks.size() >= MAX_TASK_SIZE) {
            if (!debug) inventoryUI.displayMessage(ERROR_TYPE.TASKS_FULL);
            return false;
        } else if (!hasEnoughMoney(task.priceToBuy)) {
            if (!debug) inventoryUI.displayMessage(ERROR_TYPE.MONEY);
            return false;
        }
        this.addNewTask(task);
        this.playSound("coins.wav");
        this.setMoney(this.getMoney() - task.priceToBuy);
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
        boolean soldItem = super.sellItem(market, id, quantity);
        if (soldItem) {
            this.playSound("coins.wav");
            this.soldItems.merge(id, quantity, (a, b) -> Integer.sum(a, b));
            this.checkTasks();
        }
        return soldItem;
    }

    /**
     * Adds a single item to the inventory
     * @param itemID
     * @return true if successful, false if unsuccessful (e.g. no space)
     */
    public boolean acquireItem(Integer itemID) {
        return this.acquireItem(itemID, 1);
    }

    /**
     * Adds item(s) of same id to the inventory
     * @param itemID
     * @param quantity
     * @return true if successful, false if unsuccessful (e.g. no space)
     */
    public boolean acquireItem(Integer itemID, int quantity) {
        if (this.addToInventory(itemID, quantity) < 0) {
            if (!debug) this.inventoryUI.displayMessage(ERROR_TYPE.INVENTORY_FULL);
            return false;
        }
        return true;
    }

    /**
     * Adds new pair to inventory if item not present, otherwise increments quantity and updates UI
     * @param itemID ID of item to be added
     * @param quantity quantity to be added
     * @return index of where item was added to
    */
    @Override
    protected int addToInventory(int itemID, Integer quantity) {
        if(quantity > countFreeItemSpaces(itemID)) {
            return -1; //not enough space for this many items
        }

        int i = 0;
        int stackLimit = this.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;

        //adding to item's existing slots
        for (InventoryItem pair : inventory) {
            if ((pair != null) && (itemID == pair.itemID) && ((pair.quantity + quantity) <= stackLimit)) {
                pair.quantity += quantity;
                this.inventory.set(i, pair);
                if (!debug) this.inventoryUI.updateUI(this.inventory, i);
                return i;
            } else if ((pair != null) && (itemID == pair.itemID)) {
                quantity -= stackLimit - pair.quantity;
                pair.quantity = stackLimit;
                this.inventory.set(i, pair);
                if (!debug) this.inventoryUI.updateUI(this.inventory, i);
            }
            i++;
        }

        //adding to separate slot(s) to hold remaining items
        if (quantity != 0) {
            i = 0;
            for (InventoryItem pair : inventory) {
                if ((pair == null) && (quantity <= stackLimit)) {
                    pair = new InventoryItem(itemID, quantity);
                    this.inventory.set(i, pair);
                    if (!debug) this.inventoryUI.updateUI(this.inventory, i);
                    return i;

                } else if (pair == null) {
                    pair = new InventoryItem(itemID, stackLimit);
                    quantity -= stackLimit;
                    this.inventory.set(i, pair);
                    if (!debug) this.inventoryUI.updateUI(this.inventory, i);
                }
                i++;
            }
        }
        return -1;
    }
    
    /**
     * Rearranges items with a specific itemID to ensure they use the minimum number of slots possible
     * @param itemID - of item to be rearranged
     * @param quantity - quantity of item to be rearranged
     * @param slotWithSpace - slot which contains space for more items.
     * @param stackLimit - the stack limit of the particular item
     * @return int[] with slot used - 0, and index of freed slot - 1 (if successfully rearranged, empty int[] if unable to rearrange)
     */
    protected int[] rearrangeItems(Integer itemID, int quantity, int slotWithSpace, int stackLimit) {
    	int[] rearrangedItems = super.rearrangeItems(itemID, quantity, slotWithSpace, stackLimit);
    	if (rearrangedItems.length == 2 && !(debug)) {
            if (!debug) this.inventoryUI.updateUI(this.inventory, rearrangedItems[0]);
            if (!debug) this.inventoryUI.updateUI(this.inventory, rearrangedItems[1]);
        }
    	return rearrangedItems;
    }

    /**
     * removes item(s) from the inventory, even if they span multiple slots, starting from the right-most slot and updates UI
     * @param itemID for item to be removed
     * @param quantity of items to be removed
     * @return int[] with itemID - 0, and quantity removed - 1 (if successfully removed, empty int[] if unable to remove)
     */
    @Override
    public int[] removeItem(int itemID, int quantity) {
        int noOfThisItem = countItems(itemID);
        int stackLimit = this.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
        if(quantity > noOfThisItem) {
            if (!debug) this.inventoryUI.displayMessage(ERROR_TYPE.INVENTORY_EMPTY);
            return new int[0];
        }
        int endQuantity = noOfThisItem - quantity;
        int slotWithSpace = -1;
        InventoryItem pair;

        int i = MAX_SIZE - 1;
        for (; i >= 0; i--) {
            pair = inventory.get(i);
            if((pair != null) && (pair.itemID == itemID)) {
                if(quantity >= pair.quantity) {
                    quantity -= pair.quantity;
                    inventory.set(i, null); //free inventory slot
                    if (!debug) this.inventoryUI.updateUI(this.inventory, i);
                    if(quantity == 0) {
                        break;
                    }

                } else {
                    pair.quantity -= quantity;
                    inventory.set(i, pair);
                    if (!debug) this.inventoryUI.updateUI(this.inventory, i);
                    slotWithSpace = i;
                    break;
                }
            }
        }
        int itemOccupiedSlots = countSlotsOccupiedBy(itemID);
        int minRequiredSlots = (int) (((double) (endQuantity + stackLimit -1))/((double) stackLimit));
        if(itemOccupiedSlots > minRequiredSlots) {
            this.rearrangeItems(itemID, endQuantity, slotWithSpace, stackLimit); //rearranges items if slots are being wasted
        }

        return new int[]{itemID, i};
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
                if (!debug) inventoryUI.updateUI(inventory, selectedSlot);
                return true;

            } else {
                pair.quantity -= quantity;
                inventory.set(selectedSlot, pair);
                if (!debug) inventoryUI.updateUI(inventory, selectedSlot);
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
        if (inventory.get(selectedSlot) == null) {
            return -1;
        }
        int id = inventory.get(selectedSlot).itemID;
        this.removeItemFromSelectedSlot(1); //drops one item from selected slot in inventory
        return id;
    }

    /**
     * Called to select which slot/index in the inventory, ready for dropItem() method to use
     * @param slot - slot or index selected
     * @return ID of item in the selected slot, or -1 if it is empty
     */
    public int selectSlot(int slot) {
        this.selectedSlot = slot;
        this.inventoryUI.selectSlot(slot);

        InventoryItem selectedItem = this.inventory.get(slot);
        if (selectedItem != null) {
            return selectedItem.itemID;
        }
        return -1;
    }

    public LinkedHashMap<Integer, Integer> getSoldItems() {
        return this.soldItems;
    }

    /**
     * Scan all entities for items the player is standing over, and pick them up, and delete them from the map
     */
    public Set<Pickable> pickup() {
        List<GameEntity> entities = this.pickableCollector.getEntities();
        HashSet<Pickable> picked = new HashSet<Pickable>();

        for (GameEntity ge: entities){
            // Check if entity is pickable
            if (ge.hasComponent(PickableComponent.class)){
                if(HitboxComponent.checkCollides(this, ge)) {
                    Pickable pickable = ge.getComponent(PickableComponent.class).pickable;
                    if (!this.acquireItem(pickable.getID())) {
                    } else {
                        picked.add(pickable);
                        this.playSound("pop.wav");
                    }
                }
            }
        }

        this.checkTasks();

        return picked;
    }

    /**
     * Method to check what tasks have been completed
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
                this.currentAvailableTasks.remove(task.id);
                this.setMoney(this.getMoney() + task.reward);
                this.playSound("coinPrize.wav");
                if (!debug) this.inventoryUI.displayMessage(ERROR_TYPE.TASK_COMPLETED);
            }
        }
        taskUI.showTasks(tasks);
    }
    
    /**
     * Counts number of completed tasks
     * @return number of completed tasks
     */
    public int getNumberOfCompletedTasks() {
        int completedTasks = 0;
        for(Task task : this.tasks) {
            if(task.completed) {
                completedTasks += 1;
            }
        }
        return completedTasks;
    }

    /**
     * sets the player's tasks
     * @param tasks - ArrayList of task
     */
    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
        for (Task task: this.tasks) {
            currentAvailableTasks.putIfAbsent(task.id, 1);
        }
    }

    /**
     * method which clears the player's tasks
     */
    public void clearTasks() {
        if(this.tasks != null) {
            this.tasks.clear();
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
        if(!debug) {
            this.checkTasks();
        }
    }

    /**
     * Checks if a player has enough money to purchase something and displays an error if not.
     * @param price The item/task they wish to buy.
     * @return FALSE (and displays error message) if they do not have enough money, otherwise TRUE.
     */
    public boolean hasEnoughMoney(double price) {
        boolean enoughMoney = super.hasEnoughMoney(price);
        if (!enoughMoney) {
            if (!debug) this.inventoryUI.displayMessage(ERROR_TYPE.MONEY);
        }
        return enoughMoney;
    }

    /**
     * method to queue sounds using player's AudioComponent
     */
    public void playSound(String fileName){
        this.audio.play(fileName);
    }

    /**
     * method to clear/empty the inventory
     */
    public void clearInventory() {
        super.clearInventory();
        if (!debug) return;
    	for (int i = 0; i < Player.MAX_SIZE; i++) {
            this.inventoryUI.updateUI(inventory, i);
        }
    }

}