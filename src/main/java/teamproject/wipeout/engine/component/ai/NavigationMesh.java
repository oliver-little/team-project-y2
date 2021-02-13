package teamproject.wipeout.engine.component.ai;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;

public class NavigationMesh {
    
    public List<NavigationSquare> listOfSquares = new ArrayList<>();

    private static final float doubleCompare = 0.00001f;

    public NavigationMesh (List<NavigationSquare> squares) {
        listOfSquares = squares;

        for (int i = 0; i < squares.size() - 1; i++) {
            for (int j = i + 1; j < squares.size(); j++) {
                adjacentSquare(squares.get(i),squares.get(j));
            }
        }
    }

    public void adjacentSquare(NavigationSquare a, NavigationSquare b) {

        Point2D start;
        Point2D end;

        if (Math.abs(a.topLeft.getY() - b.bottomRight.getY()) < doubleCompare) {
            //Calculate start and end for two mesh squares that are adjacent with the top of a and the bottom of b.
            start = new Point2D(Math.max(a.topLeft.getX(), b.topLeft.getX()), a.topLeft.getY());
            end = new Point2D(Math.min(a.bottomRight.getX(), b.bottomRight.getX()), a.topLeft.getY());

            b.adjacentEdges.add(new NavigationEdge(start, end, a));
            a.adjacentEdges.add(new NavigationEdge(start, end, b));
        }
        else if (Math.abs(a.bottomRight.getY() - b.topLeft.getY()) < doubleCompare) {
            //Calculate start and end for two mesh squares that are adjacent with the bottom of a and top of b.
            start = new Point2D(Math.max(a.topLeft.getX(), b.topLeft.getX()), b.topLeft.getY());
            end = new Point2D(Math.min(a.bottomRight.getX(), b.bottomRight.getX()), b.topLeft.getY());

            b.adjacentEdges.add(new NavigationEdge(start, end, a));
            a.adjacentEdges.add(new NavigationEdge(start, end, b));
        }
        else if (Math.abs(a.topLeft.getX() - b.bottomRight.getX()) < doubleCompare) {
            //Calculate start and end for two mesh squares that are adjacent with the left of a and the right of b.
            start = new Point2D(Math.max(a.topLeft.getY(), b.topLeft.getY()), a.topLeft.getX());
            end = new Point2D(Math.min(a.bottomRight.getY(), b.bottomRight.getY()), a.topLeft.getX());

            b.adjacentEdges.add(new NavigationEdge(start, end, a));
            a.adjacentEdges.add(new NavigationEdge(start, end, b));
        }
        else if (Math.abs(a.bottomRight.getX() - b.topLeft.getX()) < doubleCompare) {
            //Calculate start and end for two mesh squares that are adjacent with the left of b and the right of a.
            start = new Point2D(Math.max(a.topLeft.getY(), b.topLeft.getY()), b.topLeft.getX());
            end = new Point2D(Math.min(a.bottomRight.getY(), b.bottomRight.getY()), b.topLeft.getX());

            b.adjacentEdges.add(new NavigationEdge(start, end, a));
            a.adjacentEdges.add(new NavigationEdge(start, end, b));
        }
    }

}
