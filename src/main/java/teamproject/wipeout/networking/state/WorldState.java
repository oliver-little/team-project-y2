package teamproject.wipeout.networking.state;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import teamproject.wipeout.game.farm.Pickables;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@code WorldState} class represents objects which contain game-critical
 * information about the game world.
 * <br>
 * {@code WorldState} implements {@link Serializable}.
 */
public class WorldState implements Serializable {

    private Set<Pickables.Pickable> pickables;
    private HashMap<Integer, Point2D[]> potions;

    private long timestamp;

    /**
     * Default initializer for a {@link WorldState}.
     *
     * @param pickables Set of pickable items
     * @param potions Map of potions
     */
    public WorldState(HashSet<Pickables.Pickable> pickables, HashMap<Integer, Point2D[]> potions) {
        this.pickables = pickables;
        this.potions = potions;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Protected initializer for a {@link WorldState}.
     *
     * @param pickables Set of pickable items
     * @param potions Map of potions
     * @param timestamp Timestamp of the state
     */
    protected WorldState(Set<Pickables.Pickable> pickables, HashMap<Integer, Point2D[]> potions, long timestamp) {
        this.pickables = pickables;
        this.potions = potions;
        this.timestamp = timestamp;
    }

    /**
     * {@code pickables} getter
     *
     * @return Set of pickables
     */
    public Set<Pickables.Pickable> getPickables() {
        return this.pickables;
    }

    /**
     * {@code potions} getter
     *
     * @return Map of potions
     */
    public HashMap<Integer, Point2D[]> getPotions() {
        return this.potions;
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
     * Creates a copy of the WorldState
     *
     * @return {@link WorldState} copy
     */
    public WorldState carbonCopy() {
        return new WorldState(this.pickables, this.potions, this.timestamp);
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.pickables);

        HashMap<Integer, Double[]> deconstructedPotions = new HashMap<Integer, Double[]>();
        for (Map.Entry<Integer, Point2D[]> entry : this.potions.entrySet()) {
            Point2D[] value = entry.getValue();
            Double[] doubleValues = new Double[]{value[0].getX(), value[0].getY(), value[1].getX(), value[1].getY()};
            deconstructedPotions.put(entry.getKey(), doubleValues);
        }
        out.writeObject(deconstructedPotions);

        out.writeLong(this.timestamp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.pickables = (Set<Pickables.Pickable>) in.readObject();

        this.potions = new HashMap<Integer, Point2D[]>();
        HashMap<Integer, Double[]> deconstructedPotions = (HashMap<Integer, Double[]>) in.readObject();
        for (Map.Entry<Integer, Double[]> entry : deconstructedPotions.entrySet()) {
            Double[] values = entry.getValue();
            Point2D[] pointValue = new Point2D[]{new Point2D(values[0], values[1]), new Point2D(values[2], values[3])};
            this.potions.put(entry.getKey(), pointValue);
        }

        this.timestamp = in.readLong();
    }

    private void readObjectNoData() throws StateException {
        throw new StateException("WorldState is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        WorldState that = (WorldState) o;
        return this.pickables.equals(that.pickables);
    }

    @Override
    public int hashCode() {
        return this.pickables.hashCode();
    }

}
