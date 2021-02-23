package teamproject.wipeout.game.market;

import teamproject.wipeout.game.item.Item.ItemType;

public class MarketItem {
    
    private Integer id;
    private ItemType itemType;
    private Integer quantity;
    private float defaultBuyPrice;
    private float defaultSellPrice;
    private float currentBuyPrice;
    private float currentSellPrice;

    public static final Integer INITIAL_STOCK = 50;

    public static final Integer INFINITY = Integer.MAX_VALUE;

    /**
     * Default constructor to create a market item - this contains only the relevant information for the market.
     * @param id The ID of the item to add to the market.
     * @param defaultBuyPrice The initial buy price of the item at the start of the game.
     * @param defaultSellPrice The initial sell price of the item at the start of the game.
     */
    public MarketItem(Integer id, ItemType itemType, float defaultBuyPrice, float defaultSellPrice) {
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

    public Integer getQuantity() {
        return this.quantity;
    }

    public void incrementQuantity(Integer i) {
        this.quantity += i;
    }

    public void decrementQuantity(Integer i) {
        this.quantity -= i;
    }

    public float getCurrentBuyPrice() {
        return this.currentBuyPrice;
    }

    public float getCurrentSellPrice() {
        return this.currentSellPrice;
    }

    /**
     * Called when an item is bought/sold from the market. Updates the prices of the item based on a depreciation factor.
     */
    public void updatePrices() {

        float depreciationFactor = (INITIAL_STOCK / this.quantity);

        float newBuyPrice =  depreciationFactor * this.defaultBuyPrice;
        float newSellPrice = depreciationFactor * this.defaultSellPrice;

        this.currentBuyPrice = newBuyPrice;
        this.currentSellPrice = newSellPrice;

    }
}
