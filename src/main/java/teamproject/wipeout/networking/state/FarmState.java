package teamproject.wipeout.networking.state;

import javafx.util.Pair;
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

    private List<List<Pair<Integer, Double>>> items;
    private double growthMultiplier;
    private double AIMultiplier;

    private long timestamp;

    /**
     * Default initializer for a {@link FarmState}.
     *
     * @param farmID Farm ID
     * @param items  Items at the farm
     */
    public FarmState(Integer farmID, ArrayList<ArrayList<FarmItem>> items, double growthMultiplier, double AIMultiplier) {
        this.farmID = farmID;
        this.items = items.stream().map((row) -> {
            return row.stream().map((item) -> {
                if (item != null) {
                    Item currentItem = item.get();
                    return currentItem == null ? null : new Pair<Integer, Double>(currentItem.id, item.growth);
                }
                return null;
            }).collect(Collectors.toList());
        }).collect(Collectors.toList());
        System.out.println("State: "+this.items.toString());

        this.growthMultiplier = growthMultiplier;
        this.AIMultiplier = AIMultiplier;

        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Farm ID getter
     *
     * @return Farm ID
     */
    public Integer getFarmID() {
        return this.farmID;
    }

    public List<List<Pair<Integer, Double>>> getItems() {
        return this.items;
    }

    public double getGrowthMultiplier() {
        return this.growthMultiplier;
    }

    public double getAIMultiplier() {
        return this.AIMultiplier;
    }

    /**
     * {@code timestamp} variable getter
     *
     * @return Timestamp of the {@code FarmState}
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.farmID);

        out.writeObject(this.items);

        out.writeDouble(this.growthMultiplier);
        out.writeDouble(this.AIMultiplier);

        out.writeLong(this.timestamp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.farmID = in.readInt();

        this.items = (List<List<Pair<Integer, Double>>>) in.readObject();

        this.growthMultiplier = in.readDouble();
        this.AIMultiplier = in.readDouble();

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
