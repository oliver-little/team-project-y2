package teamproject.wipeout.engine.system.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ai.NavigationEdge;
import teamproject.wipeout.engine.component.ai.NavigationSquare;

public class PathFindingSystem {

    private static final float doubleCompare = 0.00001f;

    public boolean destinationReached(NavigationSquare currentSquare, NavigationSquare goalSquare) {

        if (currentSquare.equals(goalSquare)) {
            return true;
        }
        else {
            return false;
        }
    }

    public double calculateEuclidianDistanceSquared(double xPos, double yPos, double xGoal, double yGoal) {

        double euclideanDistanceSquared = Math.pow((xPos - xGoal),2) + Math.pow((yPos - yGoal),2);

        return euclideanDistanceSquared;

    }
    
    public List<NavigationSquare> findPath(NavigationSquare startSquare, double xPos, double yPos, NavigationSquare goalSquare, double xGoal, double yGoal) {

        Point2D midPoint;

        ArrayList<NavigationPath> frontier = new ArrayList<>();

        Set<NavigationSquare> visited = new HashSet<>();

        //Add initial node into the frontier.
        NavigationPath currentPath = new NavigationPath(List.of(startSquare), 0);

        NavigationSquare currentSquare = currentPath.getPath().get(currentPath.getPath().size() - 1);

        frontier.add(currentPath);

        while (frontier.size() > 0 && !destinationReached(currentSquare, goalSquare)) {
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
                    
                    if (adjacentSquare == frontierElement.getPath().get(frontierElement.getPath().size())) {
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
                    for (int k = 0; k < frontier.size(); k++) {
                        if (totalCost <= (frontier.get(k).getCost())) {
                            frontier.add(k, node);
                            break;
                        }
                        else if (k == frontier.size() - 1) {
                            frontier.add(node);
                        }
                    }
                }            
            }

            //Select smallest child.
            currentPath = (frontier.get(0));
            currentSquare = currentPath.getPath().get(currentPath.getPath().size());
        }

        if (currentPath.getPath().get(currentPath.getPath().size()) != goalSquare) {
            return null;
        }

        return currentPath.getPath();
    }

    
}
