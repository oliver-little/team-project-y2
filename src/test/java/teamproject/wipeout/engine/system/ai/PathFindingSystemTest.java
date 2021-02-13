package teamproject.wipeout.engine.system.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ai.NavigationEdge;
import teamproject.wipeout.engine.component.ai.NavigationSquare;
import teamproject.wipeout.engine.system.ai.PathFindingSystem;

public class PathFindingSystemTest {
    
    
    public void test_2SquarePathfinding() {
        NavigationSquare square0 = new NavigationSquare();
        
        PathFindingSystem pathFindingSystem = new PathFindingSystem();
        ArrayList<NavigationSquare> shortestPath = new ArrayList<>();

        //shortestPath = pathFindingSystem.findPath();
    }

    @Test
    public void simpleDestinationReachedSameNavigationSquare() {
        Point2D topLeft = new Point2D(0,10);
        Point2D bottomRight = new Point2D(10,0);

        NavigationSquare actual = new NavigationSquare();
        actual.topLeft = topLeft;
        actual.bottomRight = bottomRight;

        NavigationSquare expected = new NavigationSquare();
        expected.topLeft = topLeft;
        expected.bottomRight = bottomRight;

        PathFindingSystem system = new PathFindingSystem();

        assertTrue(system.destinationReached(expected, actual));
    }

    @Test
    public void simpleDestinationReachedDifferentNavigationSquare() {
        Point2D topLeft = new Point2D(0,10);
        Point2D bottomRight = new Point2D(10,0);

        NavigationSquare actual = new NavigationSquare();
        actual.topLeft = topLeft;
        actual.bottomRight = bottomRight;

        NavigationSquare expected = new NavigationSquare();
        expected.topLeft = new Point2D(1, 1);
        expected.bottomRight = bottomRight;

        PathFindingSystem system = new PathFindingSystem();

        assertFalse(system.destinationReached(expected, actual));
    }
}
