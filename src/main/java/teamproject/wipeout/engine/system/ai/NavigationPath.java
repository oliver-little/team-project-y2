package teamproject.wipeout.engine.system.ai;

import java.util.List;

import teamproject.wipeout.engine.component.ai.NavigationSquare;

/**
 * A wrapper containing the cost to a destination from a particular point, including the path of how to arrive at the current location.
 */
public class NavigationPath {
    
    private List<NavigationSquare> path;
    private double cost;

    /**
     * Sets the default path and cost.
     * @param path The path to arrive at the current location.
     */
    public NavigationPath (List<NavigationSquare> path) {
        this.path = path;
        this.cost = 0;
    }

    /**
     * Sets the current path and cost.
     * @param path The path to arrive at the current location.
     * @param cost The cost to get to the current location.
     */
    public NavigationPath (List<NavigationSquare> path, double cost) {
        this.path = path;
        this.cost = cost;
    }

    /**
     * Obtains the path to the current location.
     * @return The path.
     */
    public List<NavigationSquare> getPath() {
        return this.path;
    }

    /**
     * Obtains the cost to the current location.
     * @return The cost.
     */
    public double getCost() {
        return this.cost;
    }
}
