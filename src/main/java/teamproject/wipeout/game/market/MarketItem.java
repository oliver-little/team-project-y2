package teamproject.wipeout.game.market;

import teamproject.wipeout.game.item.Item.ItemType;

public class MarketItem {
    
    private Integer id;
    private ItemType itemType;
    private double quantity;
    private double defaultBuyPrice;
    private double defaultSellPrice;
    private double currentBuyPrice;
    private double currentSellPrice;

    public static final Integer INITIAL_STOCK = 0;

    public static final Integer INFINITY = Integer.MAX_VALUE;

    public static final Integer DAMPINGFACTOR = 1;

    public static final Integer MAXMARKETCAPACITY = 89;

    public static final Integer MINMARKETCAPACITY = -89;

    /**
     * Default constructor to create a market item - this contains only the relevant information for the market.
     * @param id The ID of the item to add to the market.
     * @param defaultBuyPrice The initial buy price of the item at the start of the game.
     * @param defaultSellPrice The initial sell price of the item at the start of the game.
     */
    public MarketItem(Integer id, ItemType itemType, double defaultBuyPrice, double defaultSellPrice) {
        this.id = id;
        this.itemType = itemType;
        this.defaultBuyPrice = defaultBuyPrice;
        this.defaultSellPrice = defaultSellPrice;
        this.currentBuyPrice = defaultBuyPrice;
        this.currentSellPrice = defaultSellPrice;

        if (itemType == ItemType.PLANTABLE || itemType == ItemType.NONE) {
            this.quantity = INITIAL_STOCK;
        }
        else if (itemType == ItemType.CONSTRUCTABLE || itemType == ItemType.USABLE) {
            this.quantity = INFINITY;
        }
        else {
            throw new IllegalArgumentException("An item had an invalid type when constructing the market database.");
        }
    }

    public Integer getID() {
        return this.id;
    }

    public ItemType getItemType() {
        return this.itemType;
    }

    public double getQuantity() {
        return this.quantity;
    }

    public void incrementQuantity(Integer i) {
        this.quantity += i;
    }

    public void decrementQuantity(Integer i) {
        this.quantity -= i;
    }

    public double getCurrentBuyPrice() {
        return this.currentBuyPrice;
    }

    public double getCurrentSellPrice() {
        return this.currentSellPrice;
    }

    /**
     * Called when an item is bought/sold from the market. Updates the prices of the item based on the tangent function.
     */
    public void updatePrices() {

        double costQuantity = this.quantity;

        if (hasMaxBreached()) {
            costQuantity = MAXMARKETCAPACITY;
        }
        else if (hasMinBreached()) {
            costQuantity = MINMARKETCAPACITY;
        }
        
        double newBuyPrice =  DAMPINGFACTOR * (Math.tan(Math.toRadians(-costQuantity))) + this.defaultBuyPrice;
        double newSellPrice = DAMPINGFACTOR * (Math.tan(Math.toRadians(-costQuantity))) + this.defaultSellPrice;
        
        if (newBuyPrice < this.defaultSellPrice) {
            this.currentBuyPrice = defaultSellPrice;
        }
        else if (newBuyPrice < 0) {
            this.currentBuyPrice = 0.01;
        }
        else {
            this.currentBuyPrice = newBuyPrice;
        }


        if (newSellPrice > this.defaultBuyPrice) {
            this.currentSellPrice = defaultBuyPrice;
        }
        else if (newSellPrice < 0) {
            this.currentSellPrice = 0.01;
        }
        else {
            this.currentSellPrice = newSellPrice;
        }
    }

    private boolean hasMaxBreached() {
        if (this.quantity > MAXMARKETCAPACITY) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean hasMinBreached() {
        if (this.quantity < MINMARKETCAPACITY) {
            return true;
        }
        else {
            return false;
        }
    }
}
