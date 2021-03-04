package teamproject.wipeout.game.market;

import teamproject.wipeout.game.item.components.TradableComponent;

public class MarketItem {
    
    private int id;
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
     * @param tradableComponent The tradable component of the item.
     */
    public MarketItem(int id, TradableComponent tradableComponent) {
        this.id = id;
        this.quantityDeviation = INITIAL_QUANTITY_DEVIATION;
        this.defaultBuyPrice = tradableComponent.defaultBuyPrice;
        this.defaultSellPrice = tradableComponent.defaultSellPrice;
        this.currentBuyPrice = defaultBuyPrice;
        this.currentSellPrice = defaultSellPrice;
    }

    /**
     * Get function to return the item's ID.
     * @return Item ID.
     */
    public int getID() {
        return this.id;
    }

    /**
     * Get function to return the item's quantity deviation.
     * @return Item quantity deviation.
     */
    public double getQuantityDeviation() {
        return this.quantityDeviation;
    }

    /**
     * Set function to set an item's quantity deviation to a specified value.
     * @param i The value to set the quantity deviation to.
     */
    public void setQuantityDeviation(double i) {
        this.quantityDeviation = i;
        updatePrices();
    }

    /**
     * Set function to increment an item's quantity deviation by a specified value. Also updates the price of that item according to its new quantity deviation.
     * @param i The amount to increment the quantity deviation by.
     */
    public void incrementQuantityDeviation(double i) {
        this.quantityDeviation += i;
        updatePrices();
    }

    /**
     * Set function to decrement an item's quantity deviation by a specified value. Also updates the price of that item according to its new quantity deviation.
     * @param i The amount to decrement the quantity deviation by.
     */
    public void decrementQuantityDeviation(double i) {
        this.quantityDeviation -= i;
        updatePrices();
    }

    /**
     * Get function to return the item's default buy price.
     * @return The item's default buy price.
     */
    public double getDefaultBuyPrice() {
        return this.defaultBuyPrice;
    }

    /**
     * Get function to return the item's default sell price.
     * @return The item's default sell price.
     */
    public double getDefaultSellPrice() {
        return this.defaultSellPrice;
    }

    /**
     * Get function to return the item's current buy price.
     * @return The item's current buy price.
     */
    public double getCurrentBuyPrice() {
        return this.currentBuyPrice;
    }

    /**
     * Get function to return the item's current sell price.
     * @return The item's current sell price.
     */
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
