package teamproject.wipeout.networking.state;

import javafx.geometry.Point2D;
import teamproject.wipeout.game.farm.Pickable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@code WorldState} class represents objects which contain game-critical
 * information about the game world.
 * <br>
 * {@code WorldState} extends {@link GameEntityState}.
 */
public class WorldState extends GameEntityState {

    private Set<Pickable> pickables;
    private Map<Integer, Point2D[]> potions;

    /**
     * Default initializer for a {@link WorldState}.
     *
     * @param pickables Set of pickable items
     * @param potions   Map of potions
     */
    public WorldState(Set<Pickable> pickables, Map<Integer, Point2D[]> potions) {
        this.pickables = pickables;
        this.potions = potions;
    }

    /**
     * {@code pickables} getter
     *
     * @return {@code Set} of pickables
     */
    public Set<Pickable> getPickables() {
        return this.pickables;
    }

    /**
     * {@code potions} getter
     *
     * @return {@code Map} with potions
     */
    public Map<Integer, Point2D[]> getPotions() {
        return this.potions;
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
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.pickables = (Set<Pickable>) in.readObject();

        this.potions = new HashMap<Integer, Point2D[]>();
        HashMap<Integer, Double[]> deconstructedPotions = (HashMap<Integer, Double[]>) in.readObject();
        for (Map.Entry<Integer, Double[]> entry : deconstructedPotions.entrySet()) {
            Double[] values = entry.getValue();
            Point2D[] pointValue = new Point2D[]{new Point2D(values[0], values[1]), new Point2D(values[2], values[3])};
            this.potions.put(entry.getKey(), pointValue);
        }
    }

    private void readObjectNoData() throws GameEntityStateException {
        throw new GameEntityStateException("WorldState is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        WorldState that = (WorldState) o;
        return this.pickables.equals(that.pickables) && this.potions.equals(that.potions);
    }

    @Override
    public int hashCode() {
        return this.pickables.hashCode() / this.potions.hashCode();
    }

}
