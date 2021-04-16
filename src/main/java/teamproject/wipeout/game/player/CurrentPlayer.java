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
import teamproject.wipeout.game.farm.Pickables;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.inventory.*;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.ui.ErrorUI.ERROR_TYPE;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.game.task.ui.TaskUI;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.networking.state.StateUpdatable;

import java.util.*;

public class CurrentPlayer extends Player implements StateUpdatable<PlayerState> {

    //no. of task slots
    public static final int MAX_TASK_SIZE = 10;

    private FarmEntity myFarm;

    private int selectedSlot;
    private InventoryUI inventoryUI;

    private TaskUI taskUI;
    private ArrayList<Task> tasks;
    private final LinkedHashMap<Integer, Integer> currentAvailableTasks = new LinkedHashMap<>();

    private final LinkedHashMap<Integer, Integer> soldItems = new LinkedHashMap<>();
    private final SignatureEntityCollector pickableCollector;
    private final AudioComponent audio;

    /**
     * Creates a new instance of GameEntity
     *
     * @param scene The GameScene this entity is part of
     */
    public CurrentPlayer(GameScene scene, Pair<Integer, String> playerInfo, SpriteManager spriteManager, ItemStore itemStore) {
        super(scene, playerInfo, spriteManager, itemStore);

        this.addComponent(new MovementAudioComponent(this.physics, "steps.wav"));

        this.audio = new AudioComponent();
        this.addComponent(this.audio);

        this.pickableCollector = new SignatureEntityCollector(scene, Set.of(PickableComponent.class, HitboxComponent.class));

        this.myFarm = null;
        this.taskUI = null;
    }

    public void assignFarm(FarmEntity farm) {
        super.assignFarm(farm);
        this.myFarm = farm;
    }

    public FarmEntity getMyFarm() {
        return this.myFarm;
    }

    public void setInventoryUI(InventoryUI inventoryUI) {
        this.inventoryUI = inventoryUI;
        this.selectSlot(0);
    }

    public ArrayList<Task> getTasks() {
        return this.tasks;
    }

    public LinkedHashMap<Integer, Integer> getCurrentAvailableTasks() {
        return this.currentAvailableTasks;
    }

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
            inventoryUI.displayMessage(ERROR_TYPE.TASK_EXISTS);
            return false;
        } else if (currentAvailableTasks.size() >= MAX_TASK_SIZE) {
            inventoryUI.displayMessage(ERROR_TYPE.TASKS_FULL);
            return false;
        } else if (!hasEnoughMoney(task.priceToBuy)) {
            inventoryUI.displayMessage(ERROR_TYPE.MONEY);
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
            this.inventoryUI.displayMessage(ERROR_TYPE.INVENTORY_FULL);
            return false;
        }
        return true;
    }

    /**
     * Adds new pair to inventory if item not present, otherwise increments quantity
     * @param itemID ID of item to be added
     * @param quantity quantity to be added
     * @return index of where item was changed
     */
    protected int addToInventory(int itemID, Integer quantity) {
        int addedToInventoryIndex = super.addToInventory(itemID, quantity);
        if (addedToInventoryIndex >= 0) {
            this.inventoryUI.updateUI(this.inventory, addedToInventoryIndex);
        }
    	return addedToInventoryIndex;
    }
    
    /**
     * Rearranges items with a specific itemID to ensure they use the minimum number of slots possible
     * @param itemID - of item to be rearranged
     * @param quantity - quantity of item to be rearranged
     */
    protected int[] rearrangeItems(Integer itemID, int quantity, int slotWithSpace, int stackLimit) {
    	int[] rearrangedItems = super.rearrangeItems(itemID, quantity, slotWithSpace, stackLimit);
    	if (rearrangedItems.length == 2) {
            this.inventoryUI.updateUI(this.inventory, rearrangedItems[0]);
            this.inventoryUI.updateUI(this.inventory, rearrangedItems[1]);
        }
    	return rearrangedItems;
    }

    /**
     * removes item(s) from the inventory, even if they span multiple slots, starting from the right-most slot
     * @param itemID for item to be removed
     * @param quantity of items to be removed
     * @return itemID if successfully removed, negative int if unable to remove
     */
    public int[] removeItem(int itemID, int quantity) {
        int[] removedItem = super.removeItem(itemID, quantity);
        if (removedItem.length == 2) {
            this.inventoryUI.updateUI(this.inventory, removedItem[1]);
        } else {
            this.inventoryUI.displayMessage(ERROR_TYPE.INVENTORY_EMPTY);
        }
        return removedItem;
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
                inventoryUI.updateUI(inventory, selectedSlot);
                return true;

            } else {
                pair.quantity -= quantity;
                inventory.set(selectedSlot, pair);
                inventoryUI.updateUI(inventory, selectedSlot);
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
        if (inventory.get(selectedSlot) == null) {
            return -1;
        }
        int id = inventory.get(selectedSlot).itemID;
        this.removeItemFromSelectedSlot(1); //drops one item from selected slot in inventory
        return id;
    }

    /**
     * Called to select which slot/index in the inventory, ready for dropItem() method to use
     * @param slot or index selected
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
                this.currentAvailableTasks.remove(task.id);
                this.setMoney(this.getMoney() + task.reward);
                this.playSound("coinPrize.wav");
                this.inventoryUI.displayMessage(ERROR_TYPE.TASK_COMPLETED);
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
        boolean enoughMoney = super.hasEnoughMoney(price);
        if (!enoughMoney) {
            this.inventoryUI.displayMessage(ERROR_TYPE.MONEY);
        }
        return enoughMoney;
    }

    public void playSound(String fileName){
        this.audio.play(fileName);
    }

    /**
     * method to clear/empty the inventory
     */
    public void clearInventory() {
        super.clearInventory();
    	for (int i = 0; i < Player.MAX_SIZE; i++) {
            this.inventoryUI.updateUI(inventory, i);
        }
    }

}