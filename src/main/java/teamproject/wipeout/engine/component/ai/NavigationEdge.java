package teamproject.wipeout.engine.component.ai;

import javafx.geometry.Point2D;

public class NavigationEdge {
    
    public NavigationSquare adjacentSquare;
    public Point2D start;
    public Point2D end;

    public NavigationEdge(Point2D start, Point2D end, NavigationSquare adjacentSquare) {
        this.adjacentSquare = adjacentSquare;
        this.start = start;
        this.end = end;
    }
}
