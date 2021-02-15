package teamproject.wipeout.engine.component.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a navigable mesh which AI characters can traverse.
 */
public class NavigationMesh {
    
    public List<NavigationSquare> squares;


    /**
     * Creates an empty mesh with a blank arraylist of squares.
     */
    private NavigationMesh() {
        squares = new ArrayList<NavigationSquare>();
    }

    /**
     * Creates a list of traversable squares.
     * @param squares The list of traversable squares.
     */
    private NavigationMesh(List<NavigationSquare> squares) {
        this.squares = squares;
    }

    /**
     * Generates a mesh by adding edges between a list of NavigationSquares
     * 
     * @param squares The list of squares to add to the mesh
     * @return The new NavigationMesh
     */
    public static NavigationMesh generateMesh(List<NavigationSquare> squares) {
        for (int i = 0; i < squares.size() - 1; i++) {
            NavigationSquare s = squares.get(i);
             for (int j = i + 1; j < squares.size(); j++) {
                s.createEdgesBetweenSquares(squares.get(j));
            }
        }

        return new NavigationMesh(squares);
    }

    /**
     * Adds a square to a pre-existing mesh and updates edges accordingly.
     * @param square The square to be added to the msh.
     * @return Whether the square was added successfully.
     */
    public boolean addSquare( NavigationSquare square) {
        if (squares.contains(square)) {
            return false;
        }

        for (int i = 0; i < squares.size(); i++) {
            square.createEdgesBetweenSquares(squares.get(i));
        }

        squares.add(square);

        return true;
    }

    /**
     * Removes a square from a pre-existing mesh and updates edges accordingly.
     * @param square The square to be removed.
     * @return Whether the square was removed successfully
     */
    public boolean removeSquare(NavigationSquare square) {
        // Attempt to remove the square
        int index = squares.indexOf(square);
        if (index != -1) {
            squares.remove(index);
        }
        else {
            return false;
        }

        // For each edge, remove the edge in the opposite direction
        for (int i = 0; i < square.adjacentEdges.size(); i++) {
            NavigationEdge edge = square.adjacentEdges.get(i);
            edge.adjacentSquare.removeEdge(square);
        }

        square.adjacentEdges.clear();

        return true;
    }
}
