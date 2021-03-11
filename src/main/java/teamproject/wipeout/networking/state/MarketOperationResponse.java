package teamproject.wipeout.networking.state;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * {@code MarketOperationResponse} class represents a server response.
 * <br>
 * {@code MarketOperationResponse} implements {@link Serializable}.
 */
public class MarketOperationResponse  implements Serializable {

    public MarketOperationRequest request;
    public boolean allowed;

    /**
     * Default initializer for a {@link MarketOperationResponse}.
     *
     * @param request Original request
     * @param allowed Response == is request allowed?
     */
    public MarketOperationResponse(MarketOperationRequest request, boolean allowed) {
        this.request = request;
        this.allowed = allowed;
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.request);
        out.writeBoolean(this.allowed);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.request = (MarketOperationRequest) in.readObject();
        this.allowed = in.readBoolean();
    }

    private void readObjectNoData() throws StateException {
        throw new StateException("MarketOperationResponse is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MarketOperationResponse that = (MarketOperationResponse) o;
        return this.request == that.request && this.allowed == that.allowed;
    }

    @Override
    public int hashCode() {
        return this.request.hashCode() + (this.allowed ? 1 : -1);
    }

}
