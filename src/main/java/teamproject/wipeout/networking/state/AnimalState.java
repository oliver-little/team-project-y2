package teamproject.wipeout.networking.state;

import javafx.geometry.Point2D;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * {@code AnimalState} class represents objects which contain game-critical
 * information about individual players (= their states).
 * <br>
 * {@code AnimalState} implements {@link Serializable}.
 * <br>
 */
public class AnimalState implements Serializable {

    private Point2D position;

    private Point2D[] path;

    private Double speedMultiplier;

    private long timestamp;

    /**
     * Default initializer for a {@link AnimalState}
     *
     * @param position Animal's position represented by {@link Point2D}
     * @param path Animal's traverse path
     */
    public AnimalState(Point2D position, List<Point2D> path) {
        this.position = position;

        Point2D[] pointArray = new Point2D[0];
        this.path = path == null ? pointArray : path.toArray(pointArray);

        this.speedMultiplier = 1.0;
        this.timestamp = System.currentTimeMillis();
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
     * {@code path} getter
     *
     * @return {@code List<Point2D>} path
     */
    public List<Point2D> getPath() {
        return Arrays.asList(this.path);
    }

    public Double getSpeedMultiplier() {
        return this.speedMultiplier;
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
     * {@code position} setter
     *
     * @param newPosition New {@link Point2D} value of the {@code position}
     */
    public void setPosition(Point2D newPosition) {
        this.timestamp = System.currentTimeMillis();
        this.position = newPosition;
    }

    /**
     * {@code path} setter
     *
     * @param newPath New {@code List<Point2D>} path value
     */
    public void setPath(List<Point2D> newPath) {
        this.timestamp = System.currentTimeMillis();

        Point2D[] pointArray = new Point2D[0];
        this.path = newPath == null ? pointArray : newPath.toArray(pointArray);
    }

    /**
     * {@code speedMultiplier} setter
     *
     * @param speedMultiplier New {@code Double} value
     */
    public void setSpeedMultiplier(Double speedMultiplier) {
        this.timestamp = System.currentTimeMillis();
        this.speedMultiplier = speedMultiplier;
    }

    /**
     * Updates this {@code AnimalState} based on another {@code AnimalState}.
     *
     * @param state {@link AnimalState} used for the update
     */
    public void updateStateFrom(AnimalState state) {
        this.position = state.position;
        this.path = state.path;
        this.speedMultiplier = state.speedMultiplier;
        this.timestamp = state.timestamp;
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make AnimalState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeDouble(this.position.getX());
        out.writeDouble(this.position.getY());

        Double[] deconstructedPath = new Double[this.path.length * 2];
        int i = 0;
        for (Point2D point : this.path) {
            deconstructedPath[i++] = point.getX();
            deconstructedPath[i++] = point.getY();
        }
        out.writeObject(deconstructedPath);

        out.writeDouble(this.speedMultiplier);

        out.writeLong(this.timestamp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.position = new Point2D(in.readDouble(), in.readDouble());

        Double[] deconstructedPath = (Double[]) in.readObject();
        this.path = new Point2D[deconstructedPath.length / 2];
        int pathIndex = 0;
        for (int i = 0; i < deconstructedPath.length; i += 2) {
            this.path[pathIndex++] = new Point2D(deconstructedPath[i], deconstructedPath[i+1]);
        }

        this.speedMultiplier = in.readDouble();

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