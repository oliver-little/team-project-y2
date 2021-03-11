package teamproject.wipeout.game.market;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

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

    private boolean isLocal;

    public MarketPriceUpdater(Market market, boolean local) {
        this.market = market;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.start();

        this.isLocal = local;
    }

    public void start() {
        executor.scheduleWithFixedDelay(this.runUpdatePrices, TIMEFREQUENCY, TIMEFREQUENCY, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdown();
    }

    /**
     * Platform.runLater wrapper for updatePrices to prevent JavaFX error
     */
    private Runnable runUpdatePrices = () -> {
        if (this.isLocal) {
            Platform.runLater(this.updatePrices);
        } else {
            this.updatePrices.run();
        }
    };

    /**
     * This function runs once the program launches and is run every set time interval, this function updates the market quantity deviations, resulting in the prices eventually returning to equlibirum.
     */
    private Runnable updatePrices = () -> {
        boolean stateChanged = false;

        for (MarketItem item : this.market.stockDatabase.values()) {
            double quantityDeviation = item.getQuantityDeviation();

            if (Double.compare(quantityDeviation, 0) == 0) {
                continue;

            } else if (quantityDeviation < 0) {
                if (quantityDeviation > negQuantityDeviationStep) {
                    item.setQuantityDeviation(0);

                } else {
                    item.incrementQuantityDeviation(QUANTITYDEVIATIONSTEP);
                }

            } else {
                if (quantityDeviation < QUANTITYDEVIATIONSTEP) {
                    item.setQuantityDeviation(0);

                } else {
                    item.decrementQuantityDeviation(QUANTITYDEVIATIONSTEP);
                }
            }
            stateChanged = true;
        }

        if (stateChanged) {
            this.market.sendMarketUpdate();
        }
    };

}
