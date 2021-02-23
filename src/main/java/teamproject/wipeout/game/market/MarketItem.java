package teamproject.wipeout.game.market;

import teamproject.wipeout.game.item.Item.ItemType;

public class MarketItem {
    
    private Integer id;
    private ItemType itemType;
    private Integer quantity;
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

    public float getcurrentBuyPrice() {
        return this.currentBuyPrice;
    }

    public void incrementCurrentBuyPrice(float i) {
        this.currentBuyPrice += i;
    }

    public void decrementCurrentBuyPrice(float i) {
        this.currentBuyPrice -= i;
    }

    public float getcurrentSellPrice() {
        return this.currentSellPrice;
    }

    public void incrementCurrentSellPrice(float i) {
        this.currentSellPrice += i;
    }

    public void decrementCurrentSellPrice(float i) {
        this.currentSellPrice -= i;
    }

}
