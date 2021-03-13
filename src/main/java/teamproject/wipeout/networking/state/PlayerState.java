package teamproject.wipeout.networking.state;

import javafx.geometry.Point2D;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * {@code PlayerState} class represents objects which contain game-critical
 * information about individual players (= their states).
 * <br>
 * {@code PlayerState} implements {@link Serializable}.
 * <br>
 */
public class PlayerState implements Serializable {

    private Integer playerID;
    private Integer farmID;

    private Point2D position;
    private Point2D acceleration;

    private long timestamp;

    /**
     * Default initializer for a {@link PlayerState}.
     *
     * @param playerID Player's ID
     * @param position Player's position represented by {@link Point2D}.
     * @param acceleration Player's acceleration represented by {@link Point2D}
     */
    public PlayerState(Integer playerID, Point2D position, Point2D acceleration) {
        this.playerID = playerID;
        this.farmID = -1;
        this.position = position;
        this.acceleration = acceleration;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Protected initializer for a {@link PlayerState}.
     *
     * @param playerID Player's ID
     * @param timestamp Timestamp of the state
     * @param position Player's position represented by {@link Point2D}
     * @param acceleration Player's acceleration represented by {@link Point2D}
     */
    protected PlayerState(Integer playerID, Integer farmID, Point2D position, Point2D acceleration, long timestamp) {
        this.playerID = playerID;
        this.farmID = farmID;
        this.position = position;
        this.acceleration = acceleration;
        this.timestamp = timestamp;
    }

    /**
     * Player's ID getter
     *
     * @return Player's ID associated with the {@link PlayerState}.
     */
    public Integer getPlayerID() {
        return this.playerID;
    }

    /**
     * Gets ID of the farm assigned to the player.
     *
     * @return Farm ID associated with the player.
     */
    public Integer getFarmID() {
        return this.farmID;
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
    public Point2D getPosition() {
        return this.position;
    }

    /**
     * {@code acceleration} getter
     *
     * @return {@link Point2D} acceleration of the {@code PlayerState}
     */
    public Point2D getAcceleration() {
        return this.acceleration;
    }

    /**
     * Sets ID of the farm assigned to the player.
     *
     * @param farmID ID of the farm assigned to the player.
     */
    public void assignFarm(Integer farmID) {
        this.farmID = farmID;
    }

    /**
     * {@code position} setter
     *
     * @param newPosition New {@link Point2D} value of the {@code position}
     */
    public void setPosition(Point2D newPosition) {
        this.position = newPosition;
    }

    /**
     * {@code acceleration} setter
     *
     * @param newAcceleration New {@link Point2D} value of the {@code acceleration}
     */
    public void setAcceleration(Point2D newAcceleration) {
        if (!this.acceleration.equals(newAcceleration)) {
            this.timestamp = System.currentTimeMillis();
            this.acceleration = newAcceleration;
        }
    }

    /**
     * Updates this {@code PlayerState} based on another {@code PlayerState}.
     *
     * @param state {@link PlayerState} used for the update
     */
    public void updateStateFrom(PlayerState state) {
        this.farmID = state.farmID;
        this.position = state.position;
        this.acceleration = state.acceleration;
        this.timestamp = state.timestamp;
    }

    /**
     * Creates a copy of the PlayerState
     *
     * @return {@link PlayerState} copy
     */
    public PlayerState carbonCopy() {
        return new PlayerState(this.playerID, this.farmID, this.position, this.acceleration, this.timestamp);
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.playerID);
        out.writeInt(this.farmID);

        out.writeDouble(this.position.getX());
        out.writeDouble(this.position.getY());

        out.writeDouble(this.acceleration.getX());
        out.writeDouble(this.acceleration.getY());

        out.writeLong(this.timestamp);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        this.playerID = in.readInt();
        this.farmID = in.readInt();

        this.position = new Point2D(in.readDouble(), in.readDouble());
        this.acceleration = new Point2D(in.readDouble(), in.readDouble());

        this.timestamp = in.readLong();
    }

    private void readObjectNoData() throws StateException {
        throw new StateException("PlayerState is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PlayerState that = (PlayerState) o;
        return this.playerID.equals(that.playerID);
    }

    @Override
    public int hashCode() {
        return this.playerID.hashCode();
    }

}
