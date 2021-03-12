package teamproject.wipeout.engine.component.ai;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.physics.Rectangle;

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
     * Generates a NavigationMesh over a given area, using a list of Rectangle colliders to determine impassable areas.
     * 
     * @param topLeft The top left point from which the NavigationMesh should begin
     * @param bottomRight The bottom right point at which the NavigationMesh should end
     * @param shapes The rectangle colliders to add to the NavigationMes
     */
    public static NavigationMesh generateMesh(Point2D topLeft, Point2D bottomRight, List<Rectangle> shapes) {
        List<NavigationSquare> meshSquares = new ArrayList<>();
        meshSquares.add(new NavigationSquare(topLeft, bottomRight));

        List<NavigationSquare> toAdd = new ArrayList<>();
        for (Rectangle shape : shapes) {
            int i = 0;
            while (i < meshSquares.size()) {
                NavigationSquare square = meshSquares.get(i);

                if (square.containsExclBoundaries(shape)) {
                    Point2D shapeTopLeft = new Point2D(shape.getX(), shape.getY());
                    Point2D shapeBottomRight = new Point2D(shape.getX() + shape.getWidth(), shape.getY() + shape.getHeight());

                    // Add square to left side only if shape starts after the left edge of the square
                    if (shapeTopLeft.getX() > square.topLeft.getX() && shapeTopLeft.getX() < square.bottomRight.getX()) {
                        toAdd.add(new NavigationSquare(square.topLeft, new Point2D(shapeTopLeft.getX(), square.bottomRight.getY())));
                    }
                    // Add square to right side only if shape ends before the right edge of the square
                    if (shapeBottomRight.getX() < square.bottomRight.getX() && shapeBottomRight.getX() > square.topLeft.getX()) {
                        toAdd.add(new NavigationSquare(new Point2D(shapeBottomRight.getX(), square.topLeft.getY()), square.bottomRight));
                    }
                    // Add square to top side only if shape starts after the top edge of the square
                    if (shapeTopLeft.getY() > square.topLeft.getY() && shapeTopLeft.getY() < square.bottomRight.getY()) {
                        toAdd.add(new NavigationSquare(new Point2D(Math.max(shapeTopLeft.getX(), square.topLeft.getX()), square.topLeft.getY()), new Point2D(Math.min(shapeBottomRight.getX(), square.bottomRight.getX()), shapeTopLeft.getY())));
                    }
                    // Add square to bottom side only if shape ends before the bottom edge of the square
                    if (shapeBottomRight.getY() < square.bottomRight.getY() && shapeBottomRight.getY() > square.topLeft.getY()) {
                        toAdd.add(new NavigationSquare(new Point2D(Math.max(shapeTopLeft.getX(), square.topLeft.getX()), shapeBottomRight.getY()), new Point2D(Math.min(shapeBottomRight.getX(), square.bottomRight.getX()), square.bottomRight.getY())));
                    }

                    meshSquares.remove(i); 
                
                }
                else {
                    i++;
                }
            }

            meshSquares.addAll(toAdd);
            toAdd.clear();
        }

        return generateMesh(meshSquares);
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
