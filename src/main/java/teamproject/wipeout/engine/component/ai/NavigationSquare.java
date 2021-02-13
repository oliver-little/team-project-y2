package teamproject.wipeout.engine.component.ai;

import java.util.ArrayList;

import javafx.geometry.Point2D;

public class NavigationSquare {
    
    public Point2D topLeft;
    public Point2D bottomRight;
    
    public ArrayList<NavigationEdge> adjacentEdges;
}
