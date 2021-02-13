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
}
