package teamproject.wipeout.networking.state;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * {@code MarketOperationRequest} class represents a client request.
 * <br>
 * {@code MarketOperationRequest} implements {@link Serializable}.
 */
public class MarketOperationRequest implements Serializable {

    public int itemID;
    public double price;
    public int quantity;
    public boolean buy;

    /**
     * Default initializer for a {@link MarketOperationRequest}.
     *
     * @param itemID Item ID
     * @param price Item price
     * @param quantity Item quantity
     * @param buy Buying or selling the item?
     */
    public MarketOperationRequest(int itemID, double price, int quantity, boolean buy) {
        this.itemID = itemID;
        this.price = price;
        this.quantity = quantity;
        this.buy = buy;
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.itemID);
        out.writeDouble(this.price);
        out.writeInt(this.quantity);
        out.writeBoolean(this.buy);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        this.itemID = in.readInt();
        this.price = in.readDouble();
        this.quantity = in.readInt();
        this.buy = in.readBoolean();
    }

    private void readObjectNoData() throws GameEntityStateException {
        throw new GameEntityStateException("MarketOperationRequest is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MarketOperationRequest that = (MarketOperationRequest) o;
        return this.itemID == that.itemID &&
                this.buy == that.buy &&
                this.quantity == that.quantity &&
                this.price == that.price;
    }

    @Override
    public int hashCode() {
        return Double.valueOf((this.itemID + this.price) / this.quantity + (this.buy ? 1 : -1)).hashCode();
    }

}
