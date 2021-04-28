package teamproject.wipeout.networking.state;

import teamproject.wipeout.game.market.MarketItem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code MarketState} class represents objects which contain game-critical
 * information about the market.
 * <br>
 * {@code MarketState} extends {@link GameEntityState}.
 */
public class MarketState extends GameEntityState {

    private Map<Integer, Double> itemDeviations;

    /**
     * Default initializer for a {@link MarketState}.
     *
     * @param stocks Items on the market
     */
    public MarketState(Map<Integer, MarketItem> stocks) {
        this.itemDeviations = new HashMap<Integer, Double>();
        stocks.forEach((itemID, marketItem) -> {
            this.itemDeviations.put(itemID, marketItem.getQuantityDeviation());
        });
    }

    /**
     * {@code items} getter
     *
     * @return {@code Map<Integer, Double>} of market item quantity deviations. <br>
     * (Integer = market item ID, Double = market item quantity deviation)
     */
    public Map<Integer, Double> getItemDeviations() {
        return this.itemDeviations;
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.itemDeviations);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.itemDeviations = (Map<Integer, Double>) in.readObject();
    }

    private void readObjectNoData() throws GameEntityStateException {
        throw new GameEntityStateException("MarketState is corrupted");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MarketState that = (MarketState) o;
        return this.itemDeviations.equals(that.itemDeviations);
    }

    @Override
    public int hashCode() {
        return this.itemDeviations.hashCode();
    }
}