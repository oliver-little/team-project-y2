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

    public static final double QUANTITYDEVIATIONSTEP = 0.1;
    public static final double NEG_QUANTITYDEVIATIONSTEP = -0.1;
    
    public final Market market;

    private boolean isLocal;
    private final ScheduledExecutorService executor;

    /**
     * Platform.runLater wrapper for updatePrices to prevent JavaFX error
     */
    private final Runnable runUpdatePrices = () -> {
        if (this.isLocal) {
            Platform.runLater(this.priceUpdater());
        } else {
            this.priceUpdater().run();
        }
    };

    /**
     * Creates a new instance of MarketPriceUpdater
     * @param market The Market instance
     * @param local Whether this MarketPriceUpdater is running locally, or on the server.
     */
    public MarketPriceUpdater(Market market, boolean local) {
        this.market = market;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.start();

        this.isLocal = local;
    }

    /**
     * Starts the price updater
     */
    public void start() {
        executor.scheduleWithFixedDelay(this.runUpdatePrices, TIMEFREQUENCY, TIMEFREQUENCY, TimeUnit.SECONDS);
    }

    /**
     * Stops the price updater
     */
    public void stop() {
       executor.shutdown();
    }

    /**
     * This function runs once the program launches and is run every set time interval, this function updates the market quantity deviations, resulting in the prices eventually returning to equlibirum.
     */
    private Runnable priceUpdater() {
        return () -> {
            for (MarketItem item : this.market.stockDatabase.values()) {
                double quantityDeviation = item.getQuantityDeviation();

                if (Double.compare(quantityDeviation, 0) == 0) {
                    continue;

                } else if (quantityDeviation < 0) {
                    if (quantityDeviation > NEG_QUANTITYDEVIATIONSTEP) {
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

                market.sendMarketUpdate(item);
            }
        };
    }
}
