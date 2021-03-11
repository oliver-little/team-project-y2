package teamproject.wipeout.networking.state;

import teamproject.wipeout.game.market.MarketItem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code MarketState} class represents objects which contain game-critical
 * information about the market.
 * <br>
 * {@code MarketState} implements {@link Serializable}.
 */
public class MarketState implements Serializable {

    public Map<Integer, Double> items;

    /**
     * Default initializer for a {@link MarketState}.
     *
     * @param stocks Items on the market
     */
    public MarketState(Map<Integer, MarketItem> stocks) {
        this.items = new HashMap<Integer, Double>();
        stocks.forEach((itemID, marketItem) -> {
            this.items.put(itemID, marketItem.getQuantityDeviation());
        });
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.items);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.items = (Map<Integer, Double>) in.readObject();
    }

    private void readObjectNoData() throws StateException {
        throw new StateException("MarketState is corrupted");
    }

}