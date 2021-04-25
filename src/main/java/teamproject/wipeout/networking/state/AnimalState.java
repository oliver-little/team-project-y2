package teamproject.wipeout.networking.state;

import javafx.geometry.Point2D;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * {@code AnimalState} class represents objects which contain game-critical
 * information about an animal entity (= its state).
 * <br>
 * {@code AnimalState} extends {@link GameEntityState}.
 */
public class AnimalState extends GameEntityState {

    private Point2D position;
    private Point2D[] path;
    private Double speedMultiplier;

    /**
     * Default initializer for a {@link AnimalState}
     *
     * @param position Animal's position represented by {@link Point2D}
     * @param path     Animal's traverse path
     */
    public AnimalState(Point2D position, List<Point2D> path) {
        this.position = position;

        Point2D[] pointArray = new Point2D[0];
        this.path = path == null ? pointArray : path.toArray(pointArray);

        this.speedMultiplier = 1.0;
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
     * {@code position} setter
     *
     * @param newPosition New {@link Point2D} value of the animal's {@code position}
     */
    public void setPosition(Point2D newPosition) {
        this.position = newPosition;
    }

    /**
     * {@code path} getter
     *
     * @return {@code List<Point2D>} path for the animal
     */
    public List<Point2D> getPath() {
        return Arrays.asList(this.path);
    }

    /**
     * {@code path} setter
     *
     * @param newPath New {@code List<Point2D>} path for the animal
     */
    public void setPath(List<Point2D> newPath) {
        Point2D[] pointArray = new Point2D[0];
        this.path = newPath == null ? pointArray : newPath.toArray(pointArray);
    }

    /**
     * {@code speedMultiplier} getter
     *
     * @return Speed multiplier of the animal
     */
    public Double getSpeedMultiplier() {
        return this.speedMultiplier;
    }

    /**
     * {@code speedMultiplier} setter
     *
     * @param speedMultiplier New speed multiplier of the animal
     */
    public void setSpeedMultiplier(Double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    /**
     * Updates the {@code AnimalState} instance based on the given {@code AnimalState} instance.
     *
     * @param updatedState {@link AnimalState} used for the state update
     */
    public void updateStateFrom(AnimalState updatedState) {
        this.position = updatedState.position;
        this.path = updatedState.path;
        this.speedMultiplier = updatedState.speedMultiplier;
    }

    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make AnimalState serializable despite it containing non-serializable properties (e.g., Point2D)

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
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.position = new Point2D(in.readDouble(), in.readDouble());

        Double[] deconstructedPath = (Double[]) in.readObject();
        this.path = new Point2D[deconstructedPath.length / 2];
        int pathIndex = 0;
        for (int i = 0; i < deconstructedPath.length; i += 2) {
            this.path[pathIndex++] = new Point2D(deconstructedPath[i], deconstructedPath[i + 1]);
        }

        this.speedMultiplier = in.readDouble();
    }

    private void readObjectNoData() throws GameEntityStateException {
        throw new GameEntityStateException("AnimalState is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        AnimalState that = (AnimalState) o;
        return this.position.equals(that.position) && Arrays.equals(this.path, that.path);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.path);
    }

}