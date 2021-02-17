package teamproject.wipeout.engine.system.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ai.NavigationEdge;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.ai.NavigationSquare;


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
    public double calculateEuclidianDistanceSquared(double xPos, double yPos, double xGoal, double yGoal) {

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
    public List<NavigationSquare> findPathThroughSquares(NavigationSquare startSquare, double xPos, double yPos, NavigationSquare goalSquare, double xGoal, double yGoal) {

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


    public List<Point2D> findStringPullPath(Point2D startPoint, Point2D endPoint, List<NavigationSquare> squarePath) {
        if (squarePath == null || squarePath.size() == 0) {
            return null;
        }
        else if (squarePath.size() == 1) {
            NavigationSquare square = squarePath.get(0);
            if(square.contains(startPoint) && square.contains(endPoint)) {
                return List.of(startPoint, endPoint); 
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

        // In order to simplify the code inside the for loop, add a fake navigation edge at the end point
        edges[squarePath.size() - 1] = new NavigationEdge(endPoint, endPoint, null);

        FunnelData funnel = new FunnelData(startPoint, edges[0]);

        Point2D newPoint = Point2D.ZERO;
        Point2D newVector = Point2D.ZERO;
        double newAngle = 0;
        // Use cross product to work out if two vectors overlap
        double newCrossProduct = 0;

        // Iterate over all edges until destination found
        for (int i = 1; i < edges.length; i++) {
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
                funnel.setData(funnel.right, edges[i]);
            }
            else if (newAngle < funnel.angle) {
                funnel.setLeft(newPoint);
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
                funnel.setData(funnel.left, edges[i]);
            }
            else if (newAngle < funnel.angle) {
                funnel.setRight(newPoint);
            }
        }

        // Once the algorithm completes, add the final apex and the endPoint to complete the path
        output.add(funnel.apex);
        output.add(endPoint);

        return output;
    }

    public List<Point2D> findPath(Point2D start, Point2D end, NavigationSquare startSquare, NavigationSquare endSquare) {
        return null;
    }

    public List<Point2D> findPath(Point2D start, Point2D end, NavigationMesh mesh) {
        return null;
    }
}
