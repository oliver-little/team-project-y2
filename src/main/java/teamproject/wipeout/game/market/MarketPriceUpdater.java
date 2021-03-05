package teamproject.wipeout.game.market;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class which automatically returns market items to their equilibrium price after a set amount of time.
 */
public class MarketPriceUpdater {

    /**
     * Time frequency is the interval in which the prices update - in seconds. The quantity deviation step walks along the x-axis of the price function, resulting in the prices (on the y axis) being increased/decreased accordingly.
     */
    public static final long TIMEFREQUENCY = 1;

    public static final double QUANTITYDEVIATIONSTEP = 0.5;
    
    public Market market;
    
    private double negQuantityDeviationStep = -QUANTITYDEVIATIONSTEP;

    private ScheduledExecutorService executor;

    public MarketPriceUpdater(Market market) {
        this.market = market;
        executor = Executors.newSingleThreadScheduledExecutor();
        start();
    }

    public void start() {
        executor.scheduleWithFixedDelay(this.updatePrices, TIMEFREQUENCY, TIMEFREQUENCY, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdown();
    }

    /**
     * This function runs once the program launches and is run every set time interval, this function updates the market quantity deviations, resulting in the prices eventually returning to equlibirum.
     */
    public Runnable updatePrices = () -> {
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
                    item.incrementQuantityDeviation(QUANTITYDEVIATIONSTEP);
                }
            }
            else {
                if (quantityDeviation < QUANTITYDEVIATIONSTEP) {
                    item.setQuantityDeviation(0);
                }
                else {
                    item.decrementQuantityDeviation(QUANTITYDEVIATIONSTEP);
                }
            }
        }
    };
}
