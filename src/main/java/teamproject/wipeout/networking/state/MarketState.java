package teamproject.wipeout.networking.state;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * {@code MarketState} class represents objects which contain game-critical
 * information about the market.
 * <br>
 * {@code MarketState} extends {@link GameEntityState}.
 */
public class MarketState extends GameEntityState {

    private Integer itemID;
    private Double itemDeviation;

    /**
     * Default initializer for a {@link MarketState}.
     *
     * @param itemID Item ID of a {@code MarketItem}
     * @param itemDeviation Quantity deviation of the {@code MarketItem}
     */
    public MarketState(Integer itemID, Double itemDeviation) {
        this.itemID = itemID;
        this.itemDeviation = itemDeviation;
    }

    /**
     * {@code itemID} getter
     *
     * @return {@code Integer} value of market item ID
     */
    public Integer getItemID() {
        return this.itemID;
    }

    /**
     * {@code itemDeviation} getter
     *
     * @return {@code Double} value of market item's quantity deviation
     */
    public Double getItemDeviation() {
        return this.itemDeviation;
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.itemID);
        out.writeDouble(this.itemDeviation);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.itemID = in.readInt();
        this.itemDeviation = in.readDouble();
    }

    private void readObjectNoData() throws GameEntityStateException {
        throw new GameEntityStateException("MarketState is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MarketState that = (MarketState) o;
        return this.itemID.equals(that.itemID) && this.itemDeviation.equals(that.itemDeviation);
    }

    @Override
    public int hashCode() {
        return this.itemID.hashCode() / this.itemDeviation.hashCode();
    }
}