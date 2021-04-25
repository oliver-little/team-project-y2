package teamproject.wipeout.networking.state;

import javafx.util.Pair;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.item.Item;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code FarmState} class represents objects which contain game-critical
 * information about a certain farm.
 * <br>
 * {@code FarmState} extends {@link GameEntityState}.
 */
public class FarmState extends GameEntityState {

    private Integer farmID;
    private Integer expansions;
    private List<List<Pair<Integer, Double>>> items;

    private double growthMultiplier;
    private double aiMultiplier;

    /**
     * Default initializer for a {@link FarmState}.
     *
     * @param farmID           Farm ID
     * @param expansions       Number of farm expansions(= expansion level)
     * @param items            Items planted on the farm
     * @param growthMultiplier Farm's current growth multiplier
     * @param aiMultiplier     Farm's current AI multiplier
     */
    public FarmState(Integer farmID, Integer expansions, ArrayList<ArrayList<FarmItem>> items, double growthMultiplier, double aiMultiplier) {
        this.farmID = farmID;
        this.expansions = expansions;
        this.items = items.stream().map((row) -> {
            return row.stream().map((item) -> {
                if (item != null) {
                    Item currentItem = item.get();
                    return currentItem == null ? null : new Pair<Integer, Double>(currentItem.id, item.growth.getValue());
                }
                return null;
            }).collect(Collectors.toList());
        }).collect(Collectors.toList());

        this.growthMultiplier = growthMultiplier;
        this.aiMultiplier = aiMultiplier;
    }

    /**
     * {@code farmID} getter
     *
     * @return Farm ID
     */
    public Integer getFarmID() {
        return this.farmID;
    }

    /**
     * {@code expansions} getter
     *
     * @return Number of farm expansions(= expansion level)
     */
    public Integer getExpansions() {
        return this.expansions;
    }

    /**
     * {@code items} getter
     *
     * @return {@code List<List<Pair<Integer, Double>>>} of items currently planted on the farm
     */
    public List<List<Pair<Integer, Double>>> getItems() {
        return this.items;
    }

    /**
     * {@code growthMultiplier} getter
     *
     * @return Farm's current growth multiplier
     */
    public double getGrowthMultiplier() {
        return this.growthMultiplier;
    }

    /**
     * {@code aiMultiplier} getter
     *
     * @return Farm's current AI multiplier
     */
    public double getAiMultiplier() {
        return this.aiMultiplier;
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.farmID);

        out.writeInt(this.expansions);

        out.writeObject(this.items);

        out.writeDouble(this.growthMultiplier);
        out.writeDouble(this.aiMultiplier);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.farmID = in.readInt();

        this.expansions = in.readInt();

        this.items = (List<List<Pair<Integer, Double>>>) in.readObject();

        this.growthMultiplier = in.readDouble();
        this.aiMultiplier = in.readDouble();
    }

    private void readObjectNoData() throws GameEntityStateException {
        throw new GameEntityStateException("FarmState is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        FarmState that = (FarmState) o;
        boolean equalMultipliers = this.growthMultiplier == that.growthMultiplier && this.aiMultiplier == that.aiMultiplier;
        return this.farmID.equals(that.farmID) && this.expansions.equals(that.expansions) &&
                this.items.equals(that.items) && equalMultipliers;
    }

    @Override
    public int hashCode() {
        return this.farmID.hashCode();
    }

}
