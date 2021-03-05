package teamproject.wipeout.game.market;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import teamproject.wipeout.game.item.components.TradableComponent;

public class MarketItem {
    
    public static final int INITIAL_QUANTITY_DEVIATION = 0;

    public static final double GLOBALDAMPINGFACTOR = 2;

    public static final double LOCALDAMPINGFACTOR = 0.05;

    private int id;
    private DoubleProperty quantityDeviation;
    private DoubleProperty defaultBuyPrice;
    private DoubleProperty defaultSellPrice;
    private DoubleProperty currentBuyPrice;
    private DoubleProperty currentSellPrice;

    /**
     * Default constructor to create a market item - this contains only the relevant information for the market.
     * @param id The ID of the item to add to the market.
     * @param tradableComponent The tradable component of the item.
     */
    public MarketItem(int id, TradableComponent tradableComponent) {
        this.id = id;
        this.quantityDeviation = new SimpleDoubleProperty(INITIAL_QUANTITY_DEVIATION);
        this.defaultBuyPrice = new SimpleDoubleProperty(tradableComponent.defaultBuyPrice);
        this.defaultSellPrice = new SimpleDoubleProperty(tradableComponent.defaultSellPrice);
        this.currentBuyPrice = new SimpleDoubleProperty(defaultBuyPrice.get());
        this.currentSellPrice = new SimpleDoubleProperty(defaultSellPrice.get());
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
    public final double getQuantityDeviation() {
        return this.quantityDeviation.get();
    }

    /**
     * Set function to set an item's quantity deviation to a specified value.
     * @param i The value to set the quantity deviation to.
     */
    public final void setQuantityDeviation(double i) {
        this.quantityDeviation.set(i);
        updatePrices();
    }

    /**
     * Get function for the DoubleProperty instance of QuantityDeviation
     * @return The DoubleProperty
     */
    public DoubleProperty quantityDeviationProperty() {
        return this.quantityDeviation;
    }

    /**
     * Set function to increment an item's quantity deviation by a specified value. Also updates the price of that item according to its new quantity deviation.
     * @param i The amount to increment the quantity deviation by.
     */
    public void incrementQuantityDeviation(double i) {
        this.setQuantityDeviation(quantityDeviation.get() + i);
        updatePrices();
    }

    /**
     * Set function to decrement an item's quantity deviation by a specified value. Also updates the price of that item according to its new quantity deviation.
     * @param i The amount to decrement the quantity deviation by.
     */
    public void decrementQuantityDeviation(double i) {
        this.setQuantityDeviation(quantityDeviation.get() - i);
        updatePrices();
    }

    /**
     * Get function to return the item's default buy price.
     * @return The item's default buy price.
     */
    public final double getDefaultBuyPrice() {
        return this.defaultBuyPrice.get();
    }


    /**
     * Get function for the DoubleProperty instance of defaultBuyPrice
     * @return The DoubleProperty
     */
    public DoubleProperty defaultBuyPrice() {
        return this.defaultBuyPrice;
    }

    /**
     * Get function to return the item's default sell price.
     * @return The item's default sell price.
     */
    public final double getDefaultSellPrice() {
        return this.defaultSellPrice.get();
    }

    /**
     * Get function for the DoubleProperty instance of defaultSellPrice
     * @return The DoubleProperty
     */
    public DoubleProperty defaultSellPrice() {
        return this.defaultSellPrice;
    }

    /**
     * Get function to return the item's current buy price.
     * @return The item's current buy price.
     */
    public final double getCurrentBuyPrice() {
        return this.currentBuyPrice.get();
    }

    /**
     * Get function for the DoubleProperty instance of currentBuyPrice
     * @return The DoubleProperty
     */
    public DoubleProperty currentBuyPrice() {
        return this.currentBuyPrice;
    }

    /**
     * Get function to return the item's current sell price.
     * @return The item's current sell price.
     */
    public final double getCurrentSellPrice() {
        return this.currentSellPrice.get();
    }

    /**
     * Get function for the DoubleProperty instance of currentSellPrice
     * @return The DoubleProperty
     */
    public DoubleProperty currentSellPrice() {
        return this.currentSellPrice;
    }

    /**
     * Called when an item is bought/sold from the market. Updates the prices of the item based on the hyperbolic sine function.
     */
    private void updatePrices() {
      
        double newCostDeviation = costFunction(this.quantityDeviation.get());

        this.currentBuyPrice.set(Math.max(0.01, newCostDeviation + defaultBuyPrice.get()));
        this.currentSellPrice.set(Math.max(0.01, newCostDeviation + defaultSellPrice.get()));        
    }

    /**
     * Calculates the cost deviation based on the hyperbolic sine function.
     */
    public static double costFunction(double x) {

        double exponent = LOCALDAMPINGFACTOR * x;

        return (Math.exp(exponent) - Math.exp(-exponent))/GLOBALDAMPINGFACTOR;
    }
}
