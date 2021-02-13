package teamproject.wipeout.engine.system.ai;

import teamproject.wipeout.engine.component.ai.NavigationSquare;

public class Node {
    
    private NavigationSquare adjacentSquare;
    private double cost;

    public Node (NavigationSquare adjacentSquare) {
        this.adjacentSquare = adjacentSquare;
        this.cost = 0;
    }

    public Node (NavigationSquare adjacentSquare, double cost) {
        this.adjacentSquare = adjacentSquare;
        this.cost = cost;
    }

    public NavigationSquare getAdjacentSquare() {
        return this.adjacentSquare;
    }

    public double getCost() {
        return this.cost;
    }

}
