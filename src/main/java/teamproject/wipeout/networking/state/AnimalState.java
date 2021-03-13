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

    private Point2D position;

    private int[] traveseTo;
    private int eatAt;

    private long timestamp;

    /**
     * Default initializer for a {@link AnimalState}.
     *
     * @param position Animal's position represented by {@link Point2D}.
     * @param traveseTo Animal's traverse goal represented by {@code int[]}.
     * @param eatAt Animal's eat at farm represented by {@code int} farm ID.
     */
    public AnimalState(Point2D position, int[] traveseTo, int eatAt) {
        this.position = position;
        this.traveseTo = traveseTo;
        this.eatAt = eatAt;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Protected initializer for a {@link PlayerState}.
     *
     * @param position Animal's position represented by {@link Point2D}.
     * @param traveseTo Animal's traverse goal represented by {@code int[]}.
     * @param eatAt Animal's eat at farm represented by {@code int} farm ID.
     * @param timestamp Timestamp of the state
     */
    protected AnimalState(Point2D position, int[] traveseTo, int eatAt, long timestamp) {
        this.position = position;
        this.traveseTo = traveseTo;
        this.eatAt = eatAt;
        this.timestamp = timestamp;
    }

    /**
     * {@code timestamp} variable getter
     *
     * @return Timestamp of the {@code AnimalState}
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * {@code position} getter
     *
     * @return {@link Point2D} position of the {@code AnimalState}
     */
    public Point2D getPosition() {
        return this.position;
    }

    /**
     * {@code position} getter
     *
     * @return {@link Point2D} position of the {@code AnimalState}
     */
    public int[] getTraveseTo() {
        return this.traveseTo;
    }

    /**
     * {@code acceleration} getter
     *
     * @return {@link Point2D} acceleration of the {@code AnimalState}
     */
    public int getEatAt() {
        return this.eatAt;
    }

    /**
     * {@code position} setter
     *
     * @param newPosition New {@link Point2D} value of the {@code position}
     */
    public void setPosition(Point2D newPosition) {
        this.timestamp = System.currentTimeMillis();
        this.position = newPosition;
    }

    /**
     * {@code traveseTo} setter
     *
     * @param newPosition New {@code int[]} value of the {@code traveseTo}
     */
    public void setTraveseTo(int[] newPosition) {
        this.timestamp = System.currentTimeMillis();
        this.traveseTo = newPosition;
    }

    /**
     * {@code eatAt} setter
     *
     * @param newEatAt New {@code int} value of the {@code eatAt}
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
        this.position = state.position;
        this.traveseTo = state.traveseTo;
        this.eatAt = state.eatAt;
        this.timestamp = state.timestamp;
    }

    /**
     * Creates a copy of the AnimalState
     *
     * @return {@link AnimalState} copy
     */
    public AnimalState carbonCopy() {
        return new AnimalState(this.position, this.traveseTo, this.eatAt, this.timestamp);
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make AnimalState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeDouble(this.position.getX());
        out.writeDouble(this.position.getY());

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
        this.position = new Point2D(in.readDouble(), in.readDouble());

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

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        AnimalState that = (AnimalState) o;
        return this.position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return this.position.hashCode();
    }

}