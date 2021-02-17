package teamproject.wipeout.engine.component.ai;

import java.util.ArrayList;

import javafx.geometry.Point2D;

/**
 * Stores a square that can be navigated by an AI. Stores the top left and bottom right co-ordinates, including edges which intersect adjacent squares.
 */
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
     * Adds a given navigation edge to the list of edges, if it does not already exist.
     * @param edge The edge to add
     */
    public void addEdge(NavigationEdge edge) {
        if (!this.adjacentEdges.contains(edge)) {
            this.adjacentEdges.add(edge);
        }
    }

    /**
     * Removes a navigation edge from the list of edges, using a connecting square, if it exists.
     * @param connectingSquare The connecting square for the edge that should be removed.
     * @return Whether the remove operation completed successfully
     */
    public boolean removeEdge(NavigationSquare connectingSquare) {
        for (int i = 0; i < adjacentEdges.size(); i++) {
            NavigationEdge edge = adjacentEdges.get(i);
            if (edge.adjacentSquare == connectingSquare) {
                adjacentEdges.remove(i);
                return true;
            }
        }

        return false;
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
        }
        else if ((Math.abs(this.bottomRight.getY() - other.topLeft.getY()) < doubleCompare) && (this.topLeft.getX() < other.bottomRight.getX()) && (this.bottomRight.getX() > other.topLeft.getX())) {
            //Calculate start and end for two mesh squares that are adjacent with the bottom of a and top of b.
            start = new Point2D(Math.max(this.topLeft.getX(), other.topLeft.getX()), other.topLeft.getY());
            end = new Point2D(Math.min(this.bottomRight.getX(), other.bottomRight.getX()), other.topLeft.getY());
        }
        else if ((Math.abs(this.bottomRight.getX() - other.topLeft.getX()) < doubleCompare) && (this.topLeft.getY() < other.bottomRight.getY()) && (this.bottomRight.getY() > other.topLeft.getY())){
            //Calculate start and end for two mesh squares that are adjacent with the right of a and the left of b.
            start = new Point2D(this.bottomRight.getX(), Math.max(this.topLeft.getY(), other.topLeft.getY()));
            end = new Point2D(this.bottomRight.getX(), Math.min(this.bottomRight.getY(), other.bottomRight.getY()));
        }
        else if ((Math.abs(this.topLeft.getX() - other.bottomRight.getX()) < doubleCompare) && (this.topLeft.getY() < other.bottomRight.getY()) && (this.bottomRight.getY() > other.topLeft.getY())) {
            //Calculate start and end for two mesh squares that are adjacent with the right of b and the left of a.
            start = new Point2D(other.bottomRight.getX(), Math.min(this.topLeft.getY(), other.topLeft.getY()));
            end = new Point2D(other.bottomRight.getX(), Math.max(this.bottomRight.getY(), other.bottomRight.getY()));
        }
        else {
            return false;
        }

        other.addEdge(new NavigationEdge(start, end, this));
        this.addEdge(new NavigationEdge(start, end, other));

        return true;
    }

    public boolean contains(Point2D position) {
        return this.topLeft.getX() <= position.getX() && position.getX() <= this.bottomRight.getX() && this.topLeft.getY() <= position.getY() && position.getY() <= this.bottomRight.getY();
    }

    @Override
    public String toString() {
        return "NavigationSquare - TL:" + topLeft.toString() + ", BR:" + bottomRight.toString();
    }
}
