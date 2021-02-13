package teamproject.wipeout.engine.system.ai;

import java.util.ArrayList;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ai.NavigationSquare;

public class PathFindingSystem {

    private static final float doubleCompare = 0.00001f;

    public boolean destinationReached(NavigationSquare currentSquare, NavigationSquare goalSquare) {

        if (currentSquare == goalSquare) {
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
    
    public ArrayList<NavigationSquare> findPath(NavigationSquare currentSquare, double xPos, double yPos, NavigationSquare goalSquare, double xGoal, double yGoal) {

        Point2D midPoint;

        ArrayList<Node> frontier = new ArrayList<>();

        ArrayList<NavigationSquare> visited = new ArrayList<>();

        //Add initial node into the frontier.
        Node initialNode = new Node(currentSquare, 0);

        frontier.add(initialNode);


        while (!destinationReached(currentSquare, goalSquare)) {

            //Add the current square to the visited list.
            visited.add(currentSquare);

            //Find adjacent squares
            for (int i = 0; i < currentSquare.adjacentEdges.size(); i++) {
                NavigationSquare adjacentSquare = (currentSquare.adjacentEdges.get(i)).adjacentSquare;
                Point2D start = (currentSquare.adjacentEdges.get(i)).start;
                Point2D end = (currentSquare.adjacentEdges.get(i)).end;

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

                //Create a node in our mesh to store total cost with location.
                Node node = new Node(adjacentSquare, totalCost);

                Boolean childExists = false;

                //Check to see if child already exists in the frontier.
                for (int j = 0; j < frontier.size(); j++) {
                    if (adjacentSquare == (frontier.get(j)).getAdjacentSquare()) {
                        if (totalCost <= (frontier.get(j)).getCost()) {
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

            //Remove current node from frontier.
            frontier.remove(0);

            //Select smallest child.
            currentSquare = (frontier.get(0)).getAdjacentSquare();
        }

        visited.add(currentSquare);

        return visited;
    }

    
}
