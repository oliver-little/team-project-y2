package teamproject.wipeout.engine.component.ai;

import javafx.geometry.Point2D;

public class NavigationEdge {
    
    public Point2D start;
    public Point2D end;
    public NavigationSquare a;
    public NavigationSquare b;

    public NavigationEdge(Point2D start, Point2D end, NavigationSquare a, NavigationSquare b) {
        this.start = start;
        this.end = end;
        this.a = a;
        this.b = b;
    }
}
