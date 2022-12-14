package teamproject.wipeout.game.player;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.PlayerAnimatorComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters.ParticleSimulationSpace;
import teamproject.wipeout.engine.component.render.particle.property.EaseCurve;
import teamproject.wipeout.engine.component.render.particle.property.OvalParticle;
import teamproject.wipeout.engine.component.shape.Rectangle;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputKeyAction;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.ParticleEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.inventory.InventoryItem;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.potion.PotionEntity;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.networking.state.StateUpdatable;
import teamproject.wipeout.util.SupplierGenerator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Class which represents the Player entity. 
 */
public class Player extends GameEntity implements StateUpdatable<PlayerState> {

    public static final String[] PLAYER_SPRITESHEETS = {
            "player-one-female", "player-two-male", "player-three-female",
            "player-one-male", "player-two-female", "player-three-male",
            "skeleton"
    };

    private static final OvalParticle FAST_PARTICLE = new OvalParticle(new Color(1, 0.824, 0.004, 1));
    private static final OvalParticle SLOW_PARTICLE = new OvalParticle(new Color(0.001, 1, 0.733, 1));

    //no. of inventory slots
    public static final int MAX_SIZE = 10;
    //initial amount of money
    public static final int INITIAL_MONEY = 25;

    private static final double NOMINAL_SPEED = 500.0;

    public final Integer playerID;
    public final String playerName;
    public final Point2D size;

    protected final MovementComponent physics;
    protected final ItemStore itemStore;

    //ArrayList used to store inventory
    protected ArrayList<InventoryItem> inventory = new ArrayList<InventoryItem>();

    private final PlayerState playerState;
    private final DoubleProperty money;
    private final ParticleEntity sabotageEffect;

    private Transform position;
    private Consumer<PotionEntity> thrownPotion;
    private Supplier<GameClient> clientSupplier;

    /**
     * default constructor for the PlayerEntity
     * @param scene - GameScene player is to be added to
     * @param playerInfo - represents player's id and name
     * @param spriteSheet - sprites for the player
     * @param spriteManager - used to get player's sprited
     * @param itemStore - store of items, their ids and other details
     */
    public Player(GameScene scene, Pair<Integer, String> playerInfo, String spriteSheet, SpriteManager spriteManager, ItemStore itemStore) {
        super(scene);
        this.playerID = playerInfo.getKey();
        this.playerName = playerInfo.getValue();
        String spriteSheetName = spriteSheet == null ? PLAYER_SPRITESHEETS[0] : spriteSheet;

        this.money = new SimpleDoubleProperty(INITIAL_MONEY);

        this.itemStore = itemStore;

        this.playerState = new PlayerState(playerID, playerName, spriteSheetName, money.getValue(), Point2D.ZERO, Point2D.ZERO);

        // Particle simulation
        ParticleParameters parameters = new ParticleParameters(100, true,
            FAST_PARTICLE,
            ParticleSimulationSpace.WORLD,
            SupplierGenerator.rangeSupplier(0.5, 1.5),
            SupplierGenerator.rangeSupplier(1.0, 4.0),
            null,
            SupplierGenerator.staticSupplier(0.0),
            SupplierGenerator.rangeSupplier(new Point2D(-25, -30), new Point2D(25, -10))
        );

        parameters.setEmissionRate(20);
        parameters.setEmissionPositionGenerator(SupplierGenerator.rangeSupplier(new Point2D(12, 0), new Point2D(52, 40)));
        parameters.addUpdateFunction((particle, percentage, timeStep) -> particle.opacity = EaseCurve.FADE_IN_OUT.apply(percentage));

        // Sabotage
        this.sabotageEffect = new ParticleEntity(scene, 0, parameters);
        this.sabotageEffect.setParent(this);

        this.position = null;

        // Physics
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
        this.addComponent(this.physics);

        this.addComponent(new HitboxComponent(new Rectangle(20, 45, 24, 16)));
        this.addComponent(new CollisionResolutionComponent());

        for (int i = 0; i < MAX_SIZE; i++) {
            this.inventory.add(null);
        }

        Point2D tempSize = null;
        try {
            Image[] idleSprites = spriteManager.getSpriteSet(spriteSheetName, "idle");

            this.addComponent(new RenderComponent(new Point2D(0, -3)));
            this.addComponent(new PlayerAnimatorComponent(
                    spriteManager.getSpriteSet(spriteSheetName, "walk-up"),
                    spriteManager.getSpriteSet(spriteSheetName, "walk-right"),
                    spriteManager.getSpriteSet(spriteSheetName, "walk-down"),
                    spriteManager.getSpriteSet(spriteSheetName, "walk-left"),
                    idleSprites
            ));

            Image idleSprite = idleSprites[0];
            tempSize = new Point2D(idleSprite.getWidth(), idleSprite.getHeight());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.size = tempSize;

        TextRenderable tag= new TextRenderable(playerName, 20);
        GameEntity nameTag = new GameEntity(scene);
        nameTag.addComponent(new RenderComponent(tag));
        RenderComponent playerRender = this.getComponent(RenderComponent.class);
        nameTag.addComponent(new Transform(playerRender.getWidth()/2f -tag.getWidth()/2f, -tag.getHeight()*0.5f, 10));
        nameTag.setParent(this);
    }

    /**
     * gets position of the player in the world
     * @return Point2D of the player
     */
    public Point2D getWorldPosition() {
        return this.position.getWorldPosition();
    }

    /**
     * gets amount of money the player currently has
     * @return - player's money value
     */
    public double getMoney() {
        return this.money.getValue();
    }

    /**
     * sets the player's money, and updates the player's state for neworking
     * @param value - new value of player's money
     */
    public void setMoney(double value) {
        this.money.set(value);
        this.playerState.setMoney(value);
        this.sendPlayerStateUpdate();
    }

    /**
     * gets money property
     * @return money property of player
     */
    public DoubleProperty moneyProperty() {
        return this.money;
    }
    
    /**
     * method to set supplier from client for networking
     * @param supplier - client supplier
     */
    public void setClientSupplier(Supplier<GameClient> supplier) {
        this.clientSupplier = supplier;
    }

    /**
     * gets the potion that has been thrown
     * @return potion that has been thrown
     */
    public Consumer<PotionEntity> getThrownPotion() {
        return this.thrownPotion;
    }

    /**
     * sets a potion which has been thrown
     * @param thrownPotion
     */
    public void setThrownPotion(Consumer<PotionEntity> thrownPotion) {
        this.thrownPotion = thrownPotion;
    }

    /**
     * Sets world position of the Player.
     *
     * @param position {@link Point2D} position of the Player
     */
    public void setWorldPosition(Point2D position) {
        this.playerState.setPosition(position);
        if (this.position == null) {
            this.position = new Transform(position, 0.0, 1);
            this.addComponent(this.position);
        } else {
            this.position.setPosition(position);
        }
        this.sendPlayerStateUpdate();
    }

    /**
     * Adds acceleration to the physics component of the Player.
     *
     * @param xAxisMultiplier Multiplier of X axis acceleration
     * @param yAxisMultiplier Multiplier of Y axis acceleration
     */
    public InputKeyAction addAcceleration(int xAxisMultiplier, int yAxisMultiplier) {
        return () -> {
            this.physics.acceleration = this.physics.acceleration.add(
                    xAxisMultiplier * Player.NOMINAL_SPEED,
                    yAxisMultiplier * Player.NOMINAL_SPEED
            );
            this.playerState.setPosition(this.position.getWorldPosition());
            this.playerState.setAcceleration(this.physics.acceleration);
            this.sendPlayerStateUpdate();
        };
    }

    public PlayerState getCurrentState() {
        return this.playerState;
    }

    /**
     * Method which updates the player's state for the networker
     * @param newState - state to replace the old one
     */
    public void updateFromState(PlayerState newState) {
        this.physics.setSpeedMultiplier(newState.getSpeedMultiplier());
        this.physics.acceleration = newState.getAcceleration();
        if (newState.getAcceleration().equals(Point2D.ZERO) && this.position != null) {
            this.position.setPosition(newState.getPosition());
        }
        this.money.set(newState.getMoney());
        this.playerState.updateStateFrom(newState);

        // Update remote players
        if (newState.getSpeedMultiplier() != 1.0) {
            if (newState.getSpeedMultiplier() < 1.0) {
                sabotageEffect.getParameters().setEmissionType(SLOW_PARTICLE);

            } else {
                sabotageEffect.getParameters().setEmissionType(FAST_PARTICLE);
            }

            if (!sabotageEffect.isPlaying()) {
                sabotageEffect.play();
            }

        } else if (sabotageEffect.isPlaying()) {
            sabotageEffect.stop();
        }
    }

    public void assignFarm(FarmEntity farm) {
        this.playerState.setFarmID(farm.farmID);
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
        if (removeItem(id, quantity).length == 0) {
            return false;
        }
        this.setMoney(this.money.getValue() + market.sellItem(id, quantity));
        return true;
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
        return this.addToInventory(itemID, quantity) >= 0;
    }

    /**
     * Method to count how many slots are occupied.
     * @return - number of occupied slots
     */
    protected int countOccupiedSlots() {
        int counter = 0;
        for (InventoryItem pair : inventory) {
            if (pair != null) {
                counter += 1;
            }
        }
        return counter;
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
    protected int countFreeItemSpaces(Integer itemID) {
        int counter = 0;
        int stackLimit = this.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
        for (InventoryItem pair : inventory) {
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
     * @return index of where item was updated
     */
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
                inventory.set(i, pair);
                return i;
            } else if ((pair != null) && (itemID == pair.itemID)) {
                quantity -= stackLimit - pair.quantity;
                pair.quantity = stackLimit;
                inventory.set(i, pair);
            }
            i++;
        }

        //adding to separate slot(s) to hold remaining items
        if (quantity != 0) {
            i = 0;
            for (InventoryItem pair : inventory) {
                if ((pair == null) && (quantity <= stackLimit)) {
                    pair = new InventoryItem(itemID, quantity);
                    inventory.set(i, pair);
                    return i;

                } else if (pair == null) {
                    pair = new InventoryItem(itemID, stackLimit);
                    quantity -= stackLimit;
                    inventory.set(i, pair);
                }
                i++;
            }
        }
        return -1;
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
     * @param slotWithSpace - slot which contains space for more items.
     * @param stackLimit - the stack limit of the particular item
     * @return int[] with slot used - 0, and index of freed slot - 1 (if successfully rearranged, empty int[] if unable to rearrange)
     */
    protected int[] rearrangeItems(Integer itemID, int quantity, int slotWithSpace, int stackLimit) {
        if (quantity <= 1) {
            return new int[0];

        } else if (slotWithSpace < 0) {
            return new int[0];
        }

        int indexOfExtraSlot = findIndexOfLeast(itemID, stackLimit);
        InventoryItem withSpacePair = inventory.get(slotWithSpace);
        withSpacePair.quantity += inventory.get(indexOfExtraSlot).quantity;

        inventory.set(slotWithSpace, withSpacePair);
        inventory.set(indexOfExtraSlot, null);

        return new int[]{slotWithSpace, indexOfExtraSlot};
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
     * @return int[] with itemID - 0, and quantity removed - 1 (if successfully removed, empty int[] if unable to remove)
     */
    public int[] removeItem(int itemID, int quantity) {
        int noOfThisItem = countItems(itemID);
        int stackLimit = this.itemStore.getItem(itemID).getComponent(InventoryComponent.class).stackSizeLimit;
        if(quantity > noOfThisItem) {
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
                    if(quantity == 0) {
                        break;
                    }

                } else {
                    pair.quantity -= quantity;
                    inventory.set(i, pair);
                    slotWithSpace = i;
                    break;
                }
            }
        }
        int itemOccupiedSlots = countSlotsOccupiedBy(itemID);
        int minRequiredSlots = (int) (((double) (endQuantity + stackLimit -1))/((double) stackLimit));
        if(itemOccupiedSlots > minRequiredSlots) {
            rearrangeItems(itemID, endQuantity, slotWithSpace, stackLimit); //rearranges items if slots are being wasted
        }
        return new int[]{itemID, i};
    }

    public ArrayList<InventoryItem> getInventory() {
        return this.inventory;
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

    /**
     * Checks if a player has enough money to purchase something and displays an error if not.
     * @param price The item/task they wish to buy.
     * @return FALSE (and displays error message) if they do not have enough money, otherwise TRUE.
     */
    public boolean hasEnoughMoney(double price) {
        return this.money.get() >= price;
    }

    /**
     * method to clear/empty the inventory
     */
    public void clearInventory() {
        for (int i = 0; i < MAX_SIZE; i++) {
            this.inventory.set(i, null);
        }
    }

    /**
     * method to update player's state for multiplayer games
     */
    protected void sendPlayerStateUpdate() {
        if (this.clientSupplier == null) {
            return;
        }
        GameClient client = this.clientSupplier.get();
        if (client != null) {
            client.send(this.playerState);
        }
    }

}