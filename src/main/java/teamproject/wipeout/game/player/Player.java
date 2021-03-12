package teamproject.wipeout.game.player;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.audio.GameAudio;
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
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.networking.state.StateUpdatable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class Player extends GameEntity implements StateUpdatable<PlayerState> {

    public static int MAX_SIZE = 10; //no. of inventory slots

    public final Integer playerID;
    public String playerName;
    public String spriteSheetName;
    public Double money;
    public Integer occupiedSlots;

    public int selectedSlot;

    private ArrayList<invPair> inventory = new ArrayList<>(); //ArrayList used to store inventory
    //private LinkedHashMap<Integer, Integer> inventory = new LinkedHashMap<>();

    public InventoryUI invUI;
    public ItemStore itemStore;

    public Integer size;

    public ArrayList<Task> tasks;

    private LinkedHashMap<Integer, Integer> soldItems = new LinkedHashMap<>();

    public GameClient client;

    private SignatureEntityCollector pickableCollector;

    private final PlayerState playerState;
    

    /**
     * Creates a new instance of GameEntity
     *
     * @param scene The GameScene this entity is part of
     */
    public Player(GameScene scene, Integer playerID, String playerName, Point2D position, InventoryUI invUI) {
        super(scene);
        this.playerID = playerID;
        this.playerName = playerName;
        this.money = 100.0;
        this.occupiedSlots = 0;

        this.addComponent(new Transform(position, 0.0, 1));
        this.addComponent(new MovementComponent(0f, 0f, 0f, 0f));
        this.addComponent(new MovementAudioComponent(this.getComponent(MovementComponent.class), "steps.wav"));

        this.addComponent(new HitboxComponent(new Rectangle(14, 7, 36, 26)));
        this.addComponent(new CollisionResolutionComponent());

        this.playerState = new PlayerState(this.playerID, position, Point2D.ZERO);

        this.invUI = invUI;
        if (invUI != null) {
            for (int i = 0; i < MAX_SIZE; i++) {
                inventory.add(null);
            }
            selectSlot(0);

            this.pickableCollector = new SignatureEntityCollector(scene, Set.of(PickableComponent.class, HitboxComponent.class));
        }
    }

    /**
     * Adds acceleration to the physics component of the Player
     *
     * @param x X axis acceleration
     * @param y Y axis acceleration
     */
    public void addAcceleration(float x, float y) {
        MovementComponent physics = this.getComponent(MovementComponent.class);

        physics.acceleration = physics.acceleration.add(x, y);
        this.playerState.setPosition(this.getComponent(Transform.class).getWorldPosition());
        this.playerState.setAcceleration(physics.acceleration);

        if (this.client != null) {
            try {
                client.send(this.playerState);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public PlayerState getCurrentState() {
        return this.playerState;
    }

    public void updateFromState(PlayerState newState) {
        this.getComponent(MovementComponent.class).acceleration = newState.getAcceleration();
        //if (newState.getAcceleration().equals(Point2D.ZERO)) {
            this.getComponent(Transform.class).setPosition(newState.getPosition());
        //}
        this.playerState.updateStateFrom(newState);
    }

    // When called with a market item, purchases an item for a player and returns true,
    // otherwise if player has not enough money, returns false
    public boolean buyItem(Market market, int id, int quantity) {
        if (market.calculateTotalCost(id, quantity, true) > this.money) {
            return false;
        }
        if (!this.acquireItem(id, quantity)) {
        	return false;
        };
        this.money -= market.buyItem(id, quantity);
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
        if (removeItem(id, quantity) < 0) {
            return false;
        }
        this.money += market.sellItem(id, quantity);
        this.soldItems.putIfAbsent(id, 0);
        this.soldItems.put(id, this.soldItems.get(id) + 1);
        checkTasks();
        return true;
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
    private int addToInventory(int itemID, Integer quantity) {
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
    private int removeItem(int itemID, int quantity) {
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