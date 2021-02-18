package teamproject.wipeout.engine.component.ai;

import javafx.geometry.Point2D;

/**
 * Stores an intersecting edge between 2 squares. Also acts as a two-way pointer to show which square each square is adjacent to.
 */
public class NavigationEdge {
    
    public NavigationSquare adjacentSquare;
    public Point2D start;
    public Point2D end;


    /**
     * 
     * @param start The start of the intersecting edge
     * @param end The end of the intersecting edge
     * @param adjacentSquare The square the current square is adjacent to.
     */
    public NavigationEdge(Point2D start, Point2D end, NavigationSquare adjacentSquare) {
        this.adjacentSquare = adjacentSquare;
        this.start = start;
        this.end = end;
    }
}
