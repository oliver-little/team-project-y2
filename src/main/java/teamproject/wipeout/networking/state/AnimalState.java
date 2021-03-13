package teamproject.wipeout.networking.state;

import javafx.geometry.Point2D;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * {@code AnimalState} class represents objects which contain game-critical
 * information about individual players (= their states).
 * <br>
 * {@code AnimalState} implements {@link Serializable}.
 * <br>
 */
public class AnimalState implements Serializable {

    private int[] traveseTo;
    private int eatAt;

    private long timestamp;

    /**
     * Default initializer for a {@link AnimalState}.
     *
     * @param position Player's position represented by {@link Point2D}.
     * @param acceleration Player's acceleration represented by {@link Point2D}
     */
    public AnimalState(int[] traveseTo, int eatAt) {
        this.traveseTo = traveseTo;
        this.eatAt = eatAt;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Protected initializer for a {@link PlayerState}.
     *
     * @param position Player's position represented by {@link Point2D}
     * @param acceleration Player's acceleration represented by {@link Point2D}
     * @param timestamp Timestamp of the state
     */
    protected AnimalState(int[] traveseTo, int eatAt, long timestamp) {
        this.traveseTo = traveseTo;
        this.eatAt = eatAt;
        this.timestamp = timestamp;
    }

    /**
     * {@code timestamp} variable getter
     *
     * @return Timestamp of the {@code PlayerState}
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * {@code position} getter
     *
     * @return {@link Point2D} position of the {@code PlayerState}
     */
    public int[] getTraveseTo() {
        return this.traveseTo;
    }

    /**
     * {@code acceleration} getter
     *
     * @return {@link Point2D} acceleration of the {@code PlayerState}
     */
    public int getEatAt() {
        return this.eatAt;
    }

    /**
     * {@code position} setter
     *
     * @param newPosition New {@link Point2D} value of the {@code position}
     */
    public void setTraveseTo(int[] newPosition) {
        this.timestamp = System.currentTimeMillis();
        this.traveseTo = newPosition;
    }

    /**
     * {@code acceleration} setter
     *
     * @param newAcceleration New {@link Point2D} value of the {@code acceleration}
     */
    public void setEatAt(int newEatAt) {
        this.timestamp = System.currentTimeMillis();
        this.eatAt = newEatAt;
    }

    /**
     * Updates this {@code AnimalState} based on another {@code AnimalState}.
     *
     * @param state {@link AnimalState} used for the update
     */
    public void updateStateFrom(AnimalState state) {
        this.traveseTo = state.traveseTo;
        this.eatAt = state.eatAt;
        this.timestamp = state.timestamp;
    }

    /**
     * Creates a copy of the PlayerState
     *
     * @return {@link PlayerState} copy
     */
    public AnimalState carbonCopy() {
        return new AnimalState(this.traveseTo, this.eatAt, this.timestamp);
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make AnimalState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (this.traveseTo == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeInt(this.traveseTo[0]);
            out.writeInt(this.traveseTo[1]);
        }

        out.writeInt(this.eatAt);

        out.writeLong(this.timestamp);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        if (in.readBoolean()) {
            this.traveseTo = new int[]{in.readInt(), in.readInt()};
        } else {
            this.traveseTo = null;
        }

        this.eatAt = in.readInt();

        this.timestamp = in.readLong();
    }

    private void readObjectNoData() throws StateException {
        throw new StateException("AnimalState is corrupted");
    }

    // Customized equals() and hashCode() methods implemented
    /*
    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PlayerState that = (PlayerState) o;
        return this.equals(that);
    }

    @Override
    public int hashCode() {
        return this.playerID.hashCode();
    }
    */
}