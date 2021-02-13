package teamproject.wipeout.engine.system.ai;

import java.util.List;

import teamproject.wipeout.engine.component.ai.NavigationSquare;

public class NavigationPath {
    
    private List<NavigationSquare> path;
    private double cost;

    public NavigationPath (List<NavigationSquare> path) {
        this.path = path;
        this.cost = 0;
    }

    public NavigationPath (List<NavigationSquare> path, double cost) {
        this.path = path;
        this.cost = cost;
    }

    public List<NavigationSquare> getPath() {
        return this.path;
    }

    public double getCost() {
        return this.cost;
    }

}
