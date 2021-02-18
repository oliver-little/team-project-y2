package teamproject.wipeout.game.logic;

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
 * Game-critical information is: id, position.
 */
public class PlayerState implements Serializable {

    protected Point2D position;

    private String id;
    private long timestamp;

    /**
     * Protected initializer for a {@link PlayerState}.
     *
     * @param id        Player's ID
     * @param timestamp Timestamp of the state
     * @param position  Player's position represented by {@link Point2D}
     */
    protected PlayerState(String id, long timestamp, Point2D position) {
        this.id = id;
        this.position = position;
        this.timestamp = timestamp;
    }

    /**
     * Default initializer for a {@link PlayerState}.
     *
     * @param id        Player's ID
     * @param position  Player's position represented by {@link Point2D}.
     */
    public PlayerState(String id, Point2D position) {
        this.id = id;
        this.position = position;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Player's ID getter
     *
     * @return Player's ID associated with the {@link PlayerState}.
     */
    public String getID() {
        return this.id;
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
     * {@code position} setter
     *
     * @param newPosition New {@link Point2D} value of the {@code position}
     */
    public void setPosition(Point2D newPosition) {
        if (!this.position.equals(newPosition)) {
            this.timestamp = System.currentTimeMillis();
            this.position = newPosition;
        }
    }

    /**
     * Updates {@code position} based on another {@code PlayerState}.
     *
     * @param state {@link PlayerState} used for the update.
     */
    public void updatePositionFrom(PlayerState state) {
        this.position = state.position;
        this.timestamp = state.timestamp;
    }

    public PlayerState carbonCopy() {
        return new PlayerState(this.id, this.timestamp, this.position);
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(this.id);
        out.writeLong(this.timestamp);
        out.writeDouble(this.position.getX());
        out.writeDouble(this.position.getY());
    }

    private void readObject(ObjectInputStream in) throws IOException {
        this.id = in.readUTF();
        this.timestamp = in.readLong();
        this.position = new Point2D(in.readDouble(), in.readDouble());
    }

    private void readObjectNoData() throws PlayerStateException {
        throw new PlayerStateException("PlayerState is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PlayerState that = (PlayerState) o;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

}
