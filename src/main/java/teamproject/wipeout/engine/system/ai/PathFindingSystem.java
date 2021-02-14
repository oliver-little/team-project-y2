package teamproject.wipeout.engine.system.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ai.NavigationEdge;
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
    public List<NavigationSquare> findPath(NavigationSquare startSquare, double xPos, double yPos, NavigationSquare goalSquare, double xGoal, double yGoal) {

        Point2D midPoint;

        ArrayList<NavigationPath> frontier = new ArrayList<>();

        Set<NavigationSquare> visited = new HashSet<>();

        //Add initial node into the frontier.
        NavigationPath currentPath = new NavigationPath(List.of(startSquare), 0);
        
        NavigationSquare currentSquare = currentPath.getPath().get(currentPath.getPath().size() - 1);

        frontier.add(currentPath);

        while (frontier.size() > 0 && !currentSquare.equals(goalSquare)) {
            frontier.remove(0);

            //Add the current square to the visited list.
            visited.add(currentSquare);

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

                Boolean childExists = false;
                
                //Check to see if child already exists in the frontier.
                for (int j = 0; j < frontier.size(); j++) {
                    NavigationPath frontierElement = frontier.get(j);
                    if (adjacentSquare == frontierElement.getPath().get(frontierElement.getPath().size()-1)) {
                        if (totalCost <= frontierElement.getCost()) {
                            frontier.remove(j);
                            childExists = false;
                            break;
                        }
                        else {
                            childExists = true;
                        }
                    }
                }

                //Add to frontier if not already in correct position sorted.
                if (childExists == false) {
                    int k;
                    for (k = 0; k < frontier.size(); k++) {
                        if (totalCost <= (frontier.get(k).getCost())) {
                            frontier.add(k, node);
                            break;
                        }
                    }
                    if (k == frontier.size()) {
                        frontier.add(node);
                    }
                }            
            }

            if (frontier.size() == 0) {
                throw new ArrayIndexOutOfBoundsException("NO THROUGH ROUTE: Cannot calculate shorest path to destination due to current location and destination being disconnected.");
            }

            //Select smallest child.
            currentPath = (frontier.get(0));
            currentSquare = currentPath.getPath().get(currentPath.getPath().size()-1);
        }

        if (currentPath.getPath().get(currentPath.getPath().size()-1) != goalSquare) {
            return null;
        }

        return currentPath.getPath();
    }

    
}
