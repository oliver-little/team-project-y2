package teamproject.wipeout.game.market;

public class MarketPriceUpdater implements Runnable{

    public static final double timeFrequency = 1;

    public static final double quantityDeviationStep = 0.5;
    
    public Market market;
    
    private double negQuantityDeviationStep = -quantityDeviationStep;

    public MarketPriceUpdater(Market market) {
        this.market = market;
    }

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
