package teamproject.wipeout.engine.component.ai;

import java.util.ArrayList;

import javafx.geometry.Point2D;

public class NavigationSquare {
    
    public Point2D topLeft;
    public Point2D bottomRight;
    
    public ArrayList<NavigationEdge> adjacentEdges;

    private static final float doubleCompare = 0.00001f;

    public NavigationSquare() {
        topLeft = new Point2D(0,0);
        bottomRight = new Point2D(0,0);
        adjacentEdges = new ArrayList<>();
    }

    /**
     * Creates edges between this square, and another NavigationSquare, if they are adjacent.
     * @param other The other NavigationSquare
     * @return Whether edges were created between the two NavigationSquares
     */
    public boolean createEdgesBetweenSquares(NavigationSquare other) {

        Point2D start;
        Point2D end;

        if ((Math.abs(this.topLeft.getY() - other.bottomRight.getY()) < doubleCompare) && (this.topLeft.getX() < other.bottomRight.getX()) && (this.bottomRight.getX() > other.topLeft.getX())) {
            //Calculate start and end for two mesh squares that are adjacent with the top of a and the bottom of b.
            start = new Point2D(Math.max(this.topLeft.getX(), other.topLeft.getX()), this.topLeft.getY());
            end = new Point2D(Math.min(this.bottomRight.getX(), other.bottomRight.getX()), this.topLeft.getY());

            other.adjacentEdges.add(new NavigationEdge(start, end, this));
            this.adjacentEdges.add(new NavigationEdge(start, end, other));
        }
        else if ((Math.abs(this.bottomRight.getY() - other.topLeft.getY()) < doubleCompare) && (this.topLeft.getX() < other.bottomRight.getX()) && (this.bottomRight.getX() > other.topLeft.getX())) {
            //Calculate start and end for two mesh squares that are adjacent with the bottom of a and top of b.
            start = new Point2D(Math.max(this.topLeft.getX(), other.topLeft.getX()), other.topLeft.getY());
            end = new Point2D(Math.min(this.bottomRight.getX(), other.bottomRight.getX()), other.topLeft.getY());

            other.adjacentEdges.add(new NavigationEdge(start, end, this));
            this.adjacentEdges.add(new NavigationEdge(start, end, other));
        }
        else if ((Math.abs(this.topLeft.getX() - other.bottomRight.getX()) < doubleCompare) && (this.topLeft.getY() > other.bottomRight.getY()) && (this.bottomRight.getY() < other.topLeft.getY())){
            //Calculate start and end for two mesh squares that are adjacent with the left of a and the right of b.
            start = new Point2D(this.topLeft.getX(), Math.min(this.topLeft.getY(), other.topLeft.getY()));
            end = new Point2D(this.topLeft.getX(), Math.max(this.bottomRight.getY(), other.bottomRight.getY()));

            other.adjacentEdges.add(new NavigationEdge(start, end, this));
            this.adjacentEdges.add(new NavigationEdge(start, end, other));
        }
        else if ((Math.abs(this.bottomRight.getX() - other.topLeft.getX()) < doubleCompare) && (this.topLeft.getY() > other.bottomRight.getY()) && (this.bottomRight.getY() < other.topLeft.getY())) {
            //Calculate start and end for two mesh squares that are adjacent with the left of b and the right of a.
            start = new Point2D(other.topLeft.getX(), Math.min(this.topLeft.getY(), other.topLeft.getY()));
            end = new Point2D(other.topLeft.getX(), Math.max(this.bottomRight.getY(), other.bottomRight.getY()));

            other.adjacentEdges.add(new NavigationEdge(start, end, this));
            this.adjacentEdges.add(new NavigationEdge(start, end, other));
        }
        else {
            return false;
        }

        return true;
    }
}
