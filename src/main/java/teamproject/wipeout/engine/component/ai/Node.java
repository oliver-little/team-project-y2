package teamproject.wipeout.engine.component.ai;

public class Node {
    double x, y, cost;

    public Node() {
        this.x = 0;
        this.y = 0;
        cost = 0;
    }

    public Node(double x, double y, double cost) {
        this.x = x;
        this.y = y;
        this.cost = cost;
    }
}
