package teamproject.wipeout.game.market;

import teamproject.wipeout.game.item.Item.ItemType;

public class MarketItem {
    
    private int id;
    private ItemType itemType;
    private double quantityDeviation;
    private double defaultBuyPrice;
    private double defaultSellPrice;
    private double currentBuyPrice;
    private double currentSellPrice;

    public static final int INITIAL_QUANTITY_DEVIATION = 0;

    public static final double GLOBALDAMPINGFACTOR = 2;

    public static final double LOCALDAMPINGFACTOR = 0.05;


    /**
     * Default constructor to create a market item - this contains only the relevant information for the market.
     * @param id The ID of the item to add to the market.
     * @param itemType The type of the item.
     * @param defaultBuyPrice The initial buy price of the item at the start of the game.
     * @param defaultSellPrice The initial sell price of the item at the start of the game.
     */
    public MarketItem(int id, ItemType itemType, double defaultBuyPrice, double defaultSellPrice) {
        this.id = id;
        this.itemType = itemType;
        this.quantityDeviation = INITIAL_QUANTITY_DEVIATION;
        this.defaultBuyPrice = defaultBuyPrice;
        this.defaultSellPrice = defaultSellPrice;
        this.currentBuyPrice = defaultBuyPrice;
        this.currentSellPrice = defaultSellPrice;
    }

    public int getID() {
        return this.id;
    }

    public ItemType getItemType() {
        return this.itemType;
    }

    public double getQuantityDeviation() {
        return this.quantityDeviation;
    }

    public void incrementQuantityDeviation(int i) {
        this.quantityDeviation += i;
        updatePrices();
    }

    public void decrementQuantityDeviation(int i) {
        this.quantityDeviation -= i;
        updatePrices();
    }

    public double getDefaultBuyPrice() {
        return this.defaultBuyPrice;
    }

    public double getDefaultSellPrice() {
        return this.defaultSellPrice;
    }

    public double getCurrentBuyPrice() {
        return this.currentBuyPrice;
    }

    public double getCurrentSellPrice() {
        return this.currentSellPrice;
    }

    /**
     * Called when an item is bought/sold from the market. Updates the prices of the item based on the hyperbolic sine function.
     */
    private void updatePrices() {
      
        double newCostDeviation = costFunction(this.quantityDeviation);

        this.currentBuyPrice = newCostDeviation + defaultBuyPrice;
        this.currentSellPrice = newCostDeviation + defaultSellPrice;

        if (currentBuyPrice <= 0.01) {
            this.currentBuyPrice = 0.01;
        }
        if (currentSellPrice <= 0.01) {
            this.currentSellPrice = 0.01;
        }
        
    }

    /**
     * Calculates the cost deviation based on the hyperbolic sine function.
     */
    public static double costFunction(double x) {

        double exponent = LOCALDAMPINGFACTOR * x;

        return (Math.exp(exponent) - Math.exp(-exponent))/GLOBALDAMPINGFACTOR;
    }
}
