package teamproject.wipeout.engine.component.ai;

import java.util.List;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.GameComponent;

/**
 * Wrapper for a steering component, contains the current point, the traversable path, the method to run on arrival and the speed at which to traverse the path.
 */
public class SteeringComponent implements GameComponent {

    public int currentPoint;
    public Runnable onArrive;

    public boolean paused;

    public final List<Point2D> path;
    public final double accelerationMultiplier;

    public SteeringComponent(List<Point2D> path, Runnable onArrive, double accelerationMultiplier) {
        this.currentPoint = 0;
        this.onArrive = onArrive;

        this.paused = false;

        this.path = path;
        this.accelerationMultiplier = accelerationMultiplier;
    }

    public String getType() {
        return "ai-steering";
    }
}
