package teamproject.wipeout.networking.state;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import teamproject.wipeout.game.farm.Pickables;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
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

    private long timestamp;

    /**
     * Default initializer for a {@link WorldState}.
     *
     * @param pickables Set of pickable items
     */
    public WorldState(HashSet<Pickables.Pickable> pickables) {
        this.pickables = pickables;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Protected initializer for a {@link WorldState}.
     *
     * @param pickables Set of pickable items
     * @param timestamp Timestamp of the state
     */
    protected WorldState(Set<Pickables.Pickable> pickables, long timestamp) {
        this.pickables = pickables;
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
        return new WorldState(this.pickables, this.timestamp);
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.pickables);

        out.writeLong(this.timestamp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.pickables = (Set<Pickables.Pickable>) in.readObject();

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
