package teamproject.wipeout.networking.state;

import javafx.geometry.Point2D;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * {@code PlayerState} class represents objects which contain game-critical
 * information about individual players (= their states).
 * <br>
 * {@code PlayerState} extends {@link GameEntityState}.
 */
public class PlayerState extends GameEntityState {

    private Integer playerID;
    private String playerName;
    private double money;

    private int farmID;

    private Point2D position;
    private Point2D acceleration;
    private double speedMultiplier;

    /**
     * Default initializer for a {@link PlayerState}.
     *
     * @param playerID     Player's ID
     * @param playerName   Player's name
     * @param money        Player's money balance
     * @param position     Player's position represented by {@link Point2D}.
     * @param acceleration Player's acceleration represented by {@link Point2D}
     */
    public PlayerState(Integer playerID, String playerName, double money, Point2D position, Point2D acceleration) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.money = money;

        this.farmID = -1;

        this.position = position;
        this.acceleration = acceleration;
        this.speedMultiplier = 1.0;
    }

    /**
     * {@code playerID} getter
     *
     * @return Player's ID
     */
    public Integer getPlayerID() {
        return this.playerID;
    }

    /**
     * {@code playerName} getter
     *
     * @return Player's name
     */
    public String getPlayerName() {
        return this.playerName;
    }

    /**
     * {@code money} getter
     *
     * @return {@link Double} value of player's money balance
     */
    public Double getMoney() {
        return this.money;
    }

    /**
     * {@code money} setter
     *
     * @param newMoney New {@link Double} value of player's money balance
     */
    public void setMoney(Double newMoney) {
        this.money = newMoney;
    }

    /**
     * {@code farmID} getter
     *
     * @return Farm ID associated with the player
     */
    public int getFarmID() {
        return this.farmID;
    }

    /**
     * {@code farmID} setter
     *
     * @param farmID Farm ID assigned to the player
     */
    public void setFarmID(Integer farmID) {
        this.farmID = farmID == null ? -1 : farmID;
    }

    /**
     * {@code position} getter
     *
     * @return {@link Point2D} position of the player
     */
    public Point2D getPosition() {
        return this.position;
    }

    /**
     * {@code position} setter
     *
     * @param newPosition New {@link Point2D} value of the player's {@code position}
     */
    public void setPosition(Point2D newPosition) {
        this.position = newPosition;
    }

    /**
     * {@code acceleration} getter
     *
     * @return {@link Point2D} acceleration of the player
     */
    public Point2D getAcceleration() {
        return this.acceleration;
    }

    /**
     * {@code acceleration} setter
     *
     * @param newAcceleration New {@link Point2D} value of the player's {@code acceleration}
     */
    public void setAcceleration(Point2D newAcceleration) {
        this.acceleration = newAcceleration;
    }

    /**
     * {@code speedMultiplier} getter
     *
     * @return {@link Double} value of the player's {@code speedMultiplier}
     */
    public Double getSpeedMultiplier() {
        return this.speedMultiplier;
    }

    /**
     * {@code speedMultiplier} setter
     *
     * @param speedMultiplier New {@link Double} value of the player's {@code speedMultiplier}
     */
    public void setSpeedMultiplier(Double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    /**
     * Updates this {@code PlayerState} based on a given {@code PlayerState}.
     *
     * @param state {@link PlayerState} used for the state update
     */
    public void updateStateFrom(PlayerState state) {
        this.money = state.money;
        this.farmID = state.farmID;
        this.position = state.position;
        this.acceleration = state.acceleration;
        this.speedMultiplier = state.speedMultiplier;
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.playerID);
        out.writeUTF(this.playerName);
        out.writeDouble(this.money);

        out.writeInt(this.farmID);

        out.writeDouble(this.position.getX());
        out.writeDouble(this.position.getY());

        out.writeDouble(this.acceleration.getX());
        out.writeDouble(this.acceleration.getY());

        out.writeDouble(this.speedMultiplier);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        this.playerID = in.readInt();
        this.playerName = in.readUTF();
        this.money = in.readDouble();

        this.farmID = in.readInt();

        this.position = new Point2D(in.readDouble(), in.readDouble());
        this.acceleration = new Point2D(in.readDouble(), in.readDouble());

        this.speedMultiplier = in.readDouble();
    }

    private void readObjectNoData() throws GameEntityStateException {
        throw new GameEntityStateException("PlayerState is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PlayerState that = (PlayerState) o;
        return this.playerID.equals(that.playerID) && this.money == that.money &&
                this.position == that.position && this.acceleration.equals(that.acceleration);
    }

    @Override
    public int hashCode() {
        return this.playerID.hashCode();
    }

}
