package teamproject.wipeout.engine.system.ai;

import java.util.ArrayList;

import teamproject.wipeout.engine.component.ai.Node;

public class PathFindingSystem {
    

    //Need to initialise grid co-ordinates across the map.

    ArrayList<Node> explored = new ArrayList<>();

    public boolean destinationReached(double xPos, double yPos, double xGoal, double yGoal) {

        if ((xPos == xGoal) && (yPos == yGoal)) {
            return true;
        }
        else {
            return false;
        }
    }

    public double calculateHeuristic(double xPos, double yPos, double xGoal, double yGoal) {

        if (destinationReached(xPos, yPos, xGoal, yGoal)) {
            return 0;
        }

        double euclideanDistanceSquared = Math.pow((xPos - xGoal),2) + Math.pow((yPos - yGoal),2);

        return euclideanDistanceSquared;

    }
    
    public ArrayList<Node> findPath(double xPos, double yPos, double xGoal, double yGoal) {

        final double costOfMovement = 1;

        final double initialHeuristic = calculateHeuristic(xPos, yPos, xGoal, yGoal);

        while (!destinationReached(xPos, yPos, xGoal, yGoal)) {

            double up = calculateHeuristic(xPos, yPos + 1, xGoal, yGoal) + costOfMovement;
            double down = calculateHeuristic(xPos, yPos - 1, xGoal, yGoal) + costOfMovement;
            double left = calculateHeuristic(xPos - 1, yPos, xGoal, yGoal) + costOfMovement;
            double right = calculateHeuristic(xPos + 1, yPos, xGoal, yGoal) + costOfMovement;

            if (up <= down && up <= left && up <= right) {
                explored.add(new Node(xPos, yPos + 1, up));
            }
            else if (down <= up && down <= left && down <= right) {
                explored.add(new Node(xPos, yPos - 1, down));
            }
            else if (left <= up && left <= down && left <= right) {
                explored.add(new Node(xPos - 1, yPos, left));
            }
            else if (right <= up && right <= left && right<= down) {
                explored.add(new Node(xPos + 1, yPos, right));
            }
            else {
                throw new ArithmeticException("Heuristic calculation failed.");
            }
            //change xpos and ypos
        }

        return explored;

    }

}
