package teamproject.wipeout.engine.component.ai;

import java.util.List;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.GameComponent;

/**
 * Wrapper for a steering component, contains the current point, the traversable path, the method to run on arrival and the speed at which to traverse the path.
 */
public class SteeringComponent implements GameComponent {
    
    public List<Point2D> path;
    public int currentPoint;
    public Runnable onArrive;
    public double accelerationMultiplier;

    public SteeringComponent subsequentSteering;

    public SteeringComponent(List<Point2D> path) {
        this.path = path;
        this.currentPoint = 0;
        this.accelerationMultiplier = 10;
        this.subsequentSteering = null;
    }

    public SteeringComponent(List<Point2D> path, Runnable onArrive) {
        this.path = path;
        this.currentPoint = 0;
        this.onArrive = onArrive;
        this.accelerationMultiplier = 10;
        this.subsequentSteering = null;
    }

    public SteeringComponent(List<Point2D> path, Runnable onArrive, double accelerationMultiplier) {
        this.path = path;
        this.currentPoint = 0;
        this.onArrive = onArrive;
        this.accelerationMultiplier = accelerationMultiplier;
        this.subsequentSteering = null;
    }

    public String getType() {
        return "steering";
    }
}
