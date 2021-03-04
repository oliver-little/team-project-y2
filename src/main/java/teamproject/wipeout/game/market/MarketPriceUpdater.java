package teamproject.wipeout.game.market;


/**
 * Class which automatically returns market items to their equilibrium price after a set amount of time.
 */
public class MarketPriceUpdater implements Runnable{

    /**
     * Time frequency is the interval in which the prices update - in seconds. The quantity deviation step walks along the x-axis of the price function, resulting in the prices (on the y axis) being increased/decreased accordingly.
     */
    public static final double timeFrequency = 1;

    public static final double quantityDeviationStep = 0.5;
    
    public Market market;
    
    private double negQuantityDeviationStep = -quantityDeviationStep;

    public MarketPriceUpdater(Market market) {
        this.market = market;
    }

    /**
     * This function runs once the program launches and is run every set time interval, this function updates the market quantity deviations, resulting in the prices eventually returning to equlibirum.
     */
    public void run(){
        for (MarketItem item : market.stockDatabase.values()) {
            double quantityDeviation = item.getQuantityDeviation();

            if (Double.compare(quantityDeviation, 0) == 0) {
                continue;
            }
            else if (quantityDeviation < 0) {
                if (quantityDeviation > negQuantityDeviationStep) {
                    item.setQuantityDeviation(0);
                }
                else {
                    item.incrementQuantityDeviation(quantityDeviationStep);
                }
            }
            else {
                if (quantityDeviation < quantityDeviationStep) {
                    item.setQuantityDeviation(0);
                }
                else {
                    item.decrementQuantityDeviation(quantityDeviationStep);
                }
            }
        }
    }
}
