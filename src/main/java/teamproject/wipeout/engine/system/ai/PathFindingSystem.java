package teamproject.wipeout.engine.system.ai;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ai.NavigationEdge;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.ai.NavigationSquare;

import java.util.*;


/**
 * Calculates the optimal path through a mesh network of squares for an AI character to go from a start point to a goal location.
 */
public class PathFindingSystem {

    private static final float doubleCompare = 0.00001f;

    /**
     * Calculates the Euclidian distance between 2 points in a mesh or square.
     * @param xPos The current x co-ordinate.
     * @param yPos The current y co-ordinate.
     * @param xGoal The goal x co-ordinate.
     * @param yGoal The goal y co-ordinate.
     * @return
     */
    public static double calculateEuclidianDistanceSquared(double xPos, double yPos, double xGoal, double yGoal) {

        double euclideanDistanceSquared = Math.pow((xPos - xGoal),2) + Math.pow((yPos - yGoal),2);

        return euclideanDistanceSquared;

    }
    
    /**
     * Calculates the optimal route through a mesh of traversible squares. Uses the A-Star algorithm to perform shortest path analysis.
     * @param startSquare The square the AI is starting in.
     * @param xPos The starting x co-ordinate.
     * @param yPos The starting y co-ordiante.
     * @param goalSquare The goal square.
     * @param xGoal The goal x co-ordinate.
     * @param yGoal The goal y co-ordinate.
     * @return Returns the optimal traversal path of squares to get from the current location to the desired destination.
     */
    public static List<NavigationSquare> findPathThroughSquares(NavigationSquare startSquare, double xPos, double yPos, NavigationSquare goalSquare, double xGoal, double yGoal) {

        Point2D midPoint;

        PriorityQueue<NavigationPath> frontier = new PriorityQueue<>(new NavigationPathComparator());

        Set<NavigationSquare> visited = new HashSet<>();

        NavigationPath currentPath = new NavigationPath(List.of(startSquare), 0);
        NavigationSquare currentSquare = startSquare;

        //Add initial node into the frontier.
        frontier.add(currentPath);

        while (frontier.size() > 0 && !currentSquare.equals(goalSquare)) {

            //Select smallest child.
            currentPath = frontier.remove();
            currentSquare = currentPath.getPath().get(currentPath.getPath().size()-1);

            // Check if we have already visited this square, continue if we have.
            if (visited.contains(currentSquare)) {
                continue;
            }
            else {
                //Add the current square to the visited list.
                visited.add(currentSquare);
            }

            //Find adjacent squares
            for (int i = 0; i < currentSquare.adjacentEdges.size(); i++) {
                NavigationEdge edge = currentSquare.adjacentEdges.get(i);
                NavigationSquare adjacentSquare = edge.adjacentSquare;
                Point2D start = edge.start;
                Point2D end = edge.end;
                
                //Don't calculate mid-points for squares already visited.
                if (visited.contains(adjacentSquare)) {
                    continue;
                }

                //Calculate midpoint of intersection to a square.
                //Is the intersection along the horizontal axis.
                if (Math.abs(start.getX() - end.getX()) < doubleCompare) {
                    midPoint = new Point2D(start.getX(),(start.getY() + end.getY())/2);
                }
                //Is the intersection along the vertical axis.
                else if (Math.abs(start.getY() - end.getY()) < doubleCompare) {
                    midPoint = new Point2D((start.getX() + end.getX())/2,start.getY());
                }
                //In the event of an error.
                else {
                    throw new ArithmeticException("Arithmetic error in calculating mid-point of an intersection. Possible reason: X & Y co-ordinates for an axis are not the same OR comparing doubles failed.");
                }

                

                //Calculate cost g(node) to that midpoint using euclidian distance from current position.
                double cost = calculateEuclidianDistanceSquared(xPos, yPos, midPoint.getX(), midPoint.getY());

                //Calculate heuristic cost h(node) from current position to overall goal destination.
                double heuristic = calculateEuclidianDistanceSquared(xPos, yPos, xGoal, yGoal);

                //Calculate total cost.
                double totalCost = cost + heuristic;

                // Copy the list, add the next square, and create a new path object
                List<NavigationSquare> listCopy = new ArrayList<>(currentPath.getPath());
                listCopy.add(adjacentSquare);
                NavigationPath node = new NavigationPath(listCopy, totalCost);

                // Add to frontier (this node may already exist).
                frontier.add(node);     
            }
        }

        if (currentPath.getPath().get(currentPath.getPath().size()-1) != goalSquare) {
            return null;
        }

        return currentPath.getPath();
    }


    /**
     * Given a start and end position, and a path of squares through that start and end point, calculates the shortest route through that set of squares.
     * 
     * @param startPoint The start position
     * @param endPoint The end position
     * @param squarePath The set of squares to pass through
     * @return A list of points to traverse to follow the shortest path through the squares
     */
    public static List<Point2D> findStringPullPath(Point2D startPoint, Point2D endPoint, List<NavigationSquare> squarePath) {
        return findStringPullPath(startPoint, endPoint, squarePath, 0);
    }

    /**
     * Given a start and end position, and a path of squares through that start and end point, calculates the shortest route through that set of squares.
     * This includes a radius amount to attempt to avoid corners by.
     * 
     * @param startPoint The start position
     * @param endPoint The end position
     * @param squarePath The set of squares to pass through
     * @param radius The radius to avoid corners by
     * @return A list of points to traverse to follow the shortest path through the squares
     */
    public static List<Point2D> findStringPullPath(Point2D startPoint, Point2D endPoint, List<NavigationSquare> squarePath, double radius) {
        // Test for invalid cases
        if (squarePath == null || squarePath.size() == 0) {
            return null;
        }
        else if (squarePath.size() == 1) {
            NavigationSquare square = squarePath.get(0);
            if(square.contains(startPoint) && square.contains(endPoint)) {
                List<Point2D> output = new ArrayList<Point2D>();
                output.add(startPoint); 
                output.add(endPoint);
                return output;
            }
            else {
                return null;
            }
        }

        // Get a list of line segments that the path must cross
        NavigationEdge[] edges = new NavigationEdge[squarePath.size()];

        List<Point2D> output = new ArrayList<Point2D>();

        // Collect the correct edges along the path
        for (int i = 0; i < squarePath.size() - 1; i++) {
            NavigationSquare square = squarePath.get(i);
            boolean foundEdge = false;
            for (NavigationEdge edge : square.adjacentEdges) {
                if (edge.adjacentSquare == squarePath.get(i+1)) {
                    edges[i] = edge;
                    foundEdge = true;
                    break;
                }
            }
            if (!foundEdge) {
                return null;
            }
        }

        // Check for if start point is directly on top of one of the first edge's points, and skip the first edge if so
        // If the start point is on top of one of the first edge's points, this causes a path that is not the shortest path to be found.
        int startEdge = 0;
        if (startPoint.equals(edges[0].start) || startPoint.equals(edges[0].end)) {
            startEdge = 1;
        }

        // In order to simplify the code inside the for loop, add a fake navigation edge at the end point
        edges[squarePath.size() - 1] = new NavigationEdge(endPoint, endPoint, new NavigationSquare(endPoint, endPoint));

        FunnelData funnel = new FunnelData(startPoint, edges[startEdge], startEdge);

        Point2D newPoint = Point2D.ZERO;
        Point2D newVector = Point2D.ZERO;
        double newAngle = 0;
        // Use cross product to work out if two vectors overlap
        double newCrossProduct = 0;

        // Iterate over all edges until destination found
        for (int i = startEdge + 1; i < edges.length; i++) {
            // Parse left point
            if (funnel.startIsLeft) {
                newPoint = edges[i].start;
            }
            else {
                newPoint = edges[i].end;
            }

            newVector = newPoint.add(funnel.negApex);

            // Calculate angle between vectors
            newAngle = newVector.angle(funnel.rightVector);
            // Calculate cross product between vectors swapped (to get sign the right way round)
            newCrossProduct = newVector.crossProduct(funnel.rightVector).getZ();
            // Crossed over, move apex
            if (newCrossProduct < 0) {
                output.add(funnel.apex);
                funnel.setData(funnel.right, edges[funnel.rightIndex+1], funnel.rightIndex+1);
                continue;
            }
            else if (newAngle <= funnel.angle) {
                funnel.setLeft(newPoint, i);
            }

            // Parse right point
            if (funnel.startIsLeft) {
                newPoint = edges[i].end;
            }
            else {
                newPoint = edges[i].start;
            }

            newVector = newPoint.add(funnel.negApex);
            newAngle = funnel.leftVector.angle(newVector);
            newCrossProduct = funnel.leftVector.crossProduct(newVector).getZ();
            // Crossed over, move apex
            if (newCrossProduct < 0) {
                output.add(funnel.apex);
                funnel.setData(funnel.left, edges[funnel.leftIndex+1], funnel.leftIndex+1);
                continue;
            }
            else if (newAngle <= funnel.angle) {
                funnel.setRight(newPoint, i);
            }
        }

        // Once the algorithm completes, add the final apex and the endPoint to complete the path
        output.add(funnel.apex);
        output.add(endPoint);

        // If there is a radius > 0, increase the angle around the points
        if (output.size() > 2 && radius > 0) {
            // Clone the output list so we're always referencing the original points, not the modified points
            List<Point2D> originalOutput = new ArrayList<>(output);
            for (int i = 1; i < output.size() - 1; i++) {
                Point2D currentPoint = output.get(i);
                Point2D prevVector = currentPoint.subtract(originalOutput.get(i-1)).normalize();
                Point2D nextVector = currentPoint.subtract(output.get(i+1)).normalize();
                Point2D normal = prevVector.add(nextVector).normalize();
                Point2D after = currentPoint.add(normal.multiply(radius));
                output.set(i, after);
            }
        }

        return output;
    }

    /**
     * Finds a path from a start position and square to an end position and square.
     * @param start The start position
     * @param end The end position
     * @param startSquare The square to start in (start position must be inside this square)
     * @param endSquare The square to finish in (end position must be inside this square)
     * @return The list of points to traverse through to get from the start to end position
     */
    public static List<Point2D> findPath(Point2D start, Point2D end, NavigationSquare startSquare, NavigationSquare endSquare, double radius) {
        List<NavigationSquare> squarePath = findPathThroughSquares(startSquare, start.getX(), start.getY(), endSquare, end.getX(), end.getY());
        
        if (squarePath == null) {
            return null;
        }

        return findStringPullPath(start, end, squarePath, radius);
    }

    /**
     * Finds a path from a start position and end position through a NavigationMesh.
     * If either the start or end position is not on the mesh, the shortest path to get onto the mesh will be used
     * 
     * @param start The start position
     * @param end The end position
     * @param mesh The NavigationMesh to traverse through.
     * @return The list of points to traverse through to get from the start to end position.
     */
    public static List<Point2D> findPath(Point2D start, Point2D end, NavigationMesh mesh) {
        return findPath(start, end, mesh, 0);
    }

    /**
     * Finds a path from a start position and end position through a NavigationMesh.
     * If either the start or end position is not on the mesh, the shortest path to get onto the mesh will be used
     * 
     * @param start The start position
     * @param end The end position
     * @param mesh The NavigationMesh to traverse through.
     * @param radius The radius of the object finding a path
     * @return The list of points to traverse through to get from the start to end position.
     */
    public static List<Point2D> findPath(Point2D start, Point2D end, NavigationMesh mesh, double radius) {
        if (mesh.squares.size() == 0) {
            throw new IllegalArgumentException("NavigationMesh contains no squares");
        }

        NavigationSquare startSquare = null;
        NavigationSquare closestStartSquare = null;
        Point2D closestStartPoint = null;
        double closestStartDistance = Double.POSITIVE_INFINITY;

        NavigationSquare endSquare = null;
        NavigationSquare closestEndSquare = null;
        Point2D closestEndPoint = null;
        double closestEndDistance = Double.POSITIVE_INFINITY;

        for (NavigationSquare square : mesh.squares) {
            
            if (startSquare == null) {
                if (square.contains(start)) {
                    startSquare = square;
                    if (endSquare != null) {
                        break;
                    }
                }
                else {
                    Point2D closestPoint = rectClosestPoint(square, start);
                    double closestDistance = closestPoint.distance(start);
                    if (closestDistance < closestStartDistance) {
                            closestStartSquare = square;
                            closestStartPoint = closestPoint;
                            closestStartDistance = closestDistance;
                    }
                }
            }
            if (endSquare == null) {
                if (square.contains(end)) {
                    endSquare = square;
                    if (startSquare != null) {
                        break;
                    }
                }
                else {
                    Point2D closestPoint = rectClosestPoint(square, end);
                    double closestDistance = closestPoint.distance(end);
                    if (closestDistance < closestEndDistance) {
                            closestEndSquare = square;
                            closestEndPoint = closestPoint;
                            closestEndDistance = closestDistance;
                    }
                }
            }
        }

        List<Point2D> path = null;

        if (startSquare == null && endSquare == null) {
            path = findPath(closestStartPoint, closestEndPoint, closestStartSquare, closestEndSquare, radius);
            path.add(0, start);
            path.add(end);
        }
        else if (startSquare == null) {
            path = findPath(closestStartPoint, end, closestStartSquare, endSquare, radius);
            path.add(0, start);
        }
        else if (endSquare == null) {
            path = findPath(start, closestEndPoint, startSquare, closestEndSquare, radius);
            path.add(end);
        }
        else {
            path = findPath(start, end, startSquare, endSquare, radius);
        }
        
        return path;
    }

    private static Point2D rectClosestPoint(NavigationSquare square, Point2D point) {
        double x = point.getX();
        double y = point.getY();
        if (x < square.topLeft.getX()) {
            x = square.topLeft.getX();
        }
        else if (x > square.bottomRight.getX()) {
            x = square.bottomRight.getX();
        }
        if (y < square.topLeft.getY()) {
            y = square.topLeft.getY();
        }
        else if (y > square.bottomRight.getY()) {
            y = square.bottomRight.getY();
        }

        return new Point2D(x, y);
    }
}
