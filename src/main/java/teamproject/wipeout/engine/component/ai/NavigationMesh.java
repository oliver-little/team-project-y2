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
     * Removes a square from a pre-existing mesh and updates edges accordingly.
     * @param squares The mesh (or list of squares) to remove the square from.
     * @param square The square to be removed.
     * @return The new mesh/list of squares.
     */
    public static NavigationMesh removeSquare(List<NavigationSquare> squares, NavigationSquare square) {
        for (int i = 0; i < square.adjacentEdges.size(); i++) {
            NavigationEdge edge = square.adjacentEdges.get(i);
            ArrayList<NavigationEdge> nextSquareEdges = edge.adjacentSquare.adjacentEdges;

            for (int j = 0; j < nextSquareEdges.size(); j++) {
                if ((nextSquareEdges.get(j)).adjacentSquare == square) {
                    nextSquareEdges.remove(j);
                    break;
                }
            }

            square.adjacentEdges.remove(i);
            i--;
        }

        int index = squares.indexOf(square);
        if (index > -1) {
            squares.remove(index);
        }

        return new NavigationMesh(squares);
    }

    /**
     * Adds a square to a pre-existing mesh and updates edges accordingly.
     * @param squares The mesh (or list of squares) to add the square to.
     * @param square The square to be added to the msh.
     * @return The new mesh/list of squares.
     */
    public static NavigationMesh addSquare(List<NavigationSquare> squares, NavigationSquare square) {
        for (int i = 0; i < squares.size(); i++) {
            square.createEdgesBetweenSquares(squares.get(i));
        }

        squares.add(square);

        return new NavigationMesh(squares);
    }
}
