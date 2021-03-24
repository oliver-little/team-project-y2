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
    private String playerName;

    private Integer farmID;

    private Point2D position;
    private Point2D acceleration;

    private Double money;

    private long timestamp;

    /**
     * Default initializer for a {@link PlayerState}.
     *
     * @param playerID Player's ID
     * @param playerName Player's name
     * @param money Player's money balance
     * @param position Player's position represented by {@link Point2D}.
     * @param acceleration Player's acceleration represented by {@link Point2D}
     */
    public PlayerState(Integer playerID, String playerName, Double money, Point2D position, Point2D acceleration) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.farmID = -1;
        this.position = position;
        this.acceleration = acceleration;
        this.money = money;
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
    protected PlayerState(Integer playerID, String playerName, Integer farmID, Point2D position, Point2D acceleration, Double money, long timestamp) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.farmID = farmID;
        this.position = position;
        this.acceleration = acceleration;
        this.money = money;
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
     * Player's name getter
     *
     * @return Player's name associated with the {@link PlayerState}.
     */
    public String getPlayerName() {
        return this.playerName;
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
     * {@code money} getter
     *
     * @return {@link Double} value of {@code money}
     */
    public Double getMoney() {
        return this.money;
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
        this.timestamp = System.currentTimeMillis();
        this.position = newPosition;
    }

    /**
     * {@code acceleration} setter
     *
     * @param newAcceleration New {@link Point2D} value of the {@code acceleration}
     */
    public void setAcceleration(Point2D newAcceleration) {
        this.timestamp = System.currentTimeMillis();
        this.acceleration = newAcceleration;
    }

    /**
     * {@code money} setter
     *
     * @param newMoney New {@link Double} value of {@code money}
     */
    public void setMoney(Double newMoney) {
        this.timestamp = System.currentTimeMillis();
        this.money = newMoney;
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
        this.money = state.money;
        this.timestamp = state.timestamp;
    }

    /**
     * Creates a copy of the PlayerState
     *
     * @return {@link PlayerState} copy
     */
    public PlayerState carbonCopy() {
        return new PlayerState(this.playerID, this.playerName, this.farmID, this.position, this.acceleration, this.money, this.timestamp);
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

        out.writeDouble(this.money);

        out.writeLong(this.timestamp);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        this.playerID = in.readInt();
        this.farmID = in.readInt();

        this.position = new Point2D(in.readDouble(), in.readDouble());
        this.acceleration = new Point2D(in.readDouble(), in.readDouble());

        this.money = in.readDouble();

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
