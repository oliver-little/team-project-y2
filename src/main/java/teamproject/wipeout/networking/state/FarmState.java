package teamproject.wipeout.networking.state;

import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.item.Item;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code FarmState} class represents objects which contain game-critical
 * information about a certain farm.
 * <br>
 * {@code FarmState} implements {@link Serializable}.
 */
public class FarmState implements Serializable {

    private Integer farmID;

    public List<List<Integer>> items;

    private long timestamp;

    /**
     * Default initializer for a {@link FarmState}.
     *
     * @param farmID Farm ID
     * @param items  Items at the farm
     */
    public FarmState(Integer farmID, ArrayList<ArrayList<FarmItem>> items) {
        this.farmID = farmID;
        this.items = items.stream().map((row) -> {
            return row.stream().map((item) -> {
                if (item != null) {
                    Item currentItem = item.get();
                    return currentItem == null ? null : currentItem.id;
                }
                return null;
            }).collect(Collectors.toList());
        }).collect(Collectors.toList());
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Protected initializer for a {@link FarmState}.
     *
     * @param farmID Farm ID
     * @param items  Items at the farm
     * @param timestamp Timestamp of the state
     */
    protected FarmState(Integer farmID, List<List<Integer>> items, long timestamp) {
        this.farmID = farmID;
        this.items = items;
        this.timestamp = timestamp;
    }

    /**
     * Farm ID getter
     *
     * @return Farm ID
     */
    public Integer getFarmID() {
        return this.farmID;
    }

    /**
     * {@code timestamp} variable getter
     *
     * @return Timestamp of the {@code FarmState}
     */
    public long getTimestamp() {
        return this.timestamp;
    }


    /**
     * Creates a copy of the FarmState
     *
     * @return {@link FarmState} copy
     */
    public FarmState carbonCopy() {
        return new FarmState(this.farmID, this.items, this.timestamp);
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.farmID);

        out.writeObject(this.items);

        out.writeLong(this.timestamp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.farmID = in.readInt();

        this.items = (List<List<Integer>>) in.readObject();

        this.timestamp = in.readLong();
    }

    private void readObjectNoData() throws StateException {
        throw new StateException("FarmState is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        FarmState that = (FarmState) o;
        return this.farmID.equals(that.farmID);
    }

    @Override
    public int hashCode() {
        return this.farmID.hashCode();
    }

}
