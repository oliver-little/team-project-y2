package teamproject.wipeout.game.farm;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.networking.state.GameEntityStateException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Creates a {@link GameEntity} that can be picked by any {@code Player}.
 * Implements {@link Serializable} for networking purposes.
 */
public class Pickable implements Serializable {

    protected GameEntity entity;

    private int id;
    private Point2D startPosition;
    private Point2D velocity;

    /**
     * Creates a new instance of {@code Pickable}
     *
     * @param id            Pickable's item ID
     * @param startPosition {@link Point2D} start position of the {@code Pickable}
     * @param velocity      {@link Point2D} velocity of the {@code Pickable}
     */
    public Pickable(int id, Point2D startPosition, Point2D velocity) {
        this.id = id;
        this.startPosition = startPosition;
        this.velocity = velocity;
    }

    /**
     * @return {@link Pickable}'s item ID
     */
    public int getID() {
        return this.id;
    }

    /**
     * @return {@link Point2D} start position of the {@code Pickable}
     */
    public Point2D getStartPosition() {
        return this.startPosition;
    }

    /**
     * @return {@link Point2D} velocity of the {@code Pickable}
     */
    public Point2D getVelocity() {
        return this.velocity;
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.id);
        out.writeDouble(this.startPosition.getX());
        out.writeDouble(this.startPosition.getY());
        out.writeDouble(this.velocity.getX());
        out.writeDouble(this.velocity.getY());
    }

    private void readObject(ObjectInputStream in) throws IOException {
        this.id = in.readInt();
        this.startPosition = new Point2D(in.readDouble(), in.readDouble());
        this.velocity = new Point2D(in.readDouble(), in.readDouble());
    }

    private void readObjectNoData() throws GameEntityStateException {
        throw new GameEntityStateException("WorldState is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pickable that = (Pickable) o;
        return this.id == that.id && this.startPosition.equals(that.startPosition) && this.velocity.equals(that.velocity);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(id);
        result += startPosition.hashCode();
        result += velocity.hashCode();
        return result;
    }

}
