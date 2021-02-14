package teamproject.wipeout.engine.system.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ai.NavigationEdge;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.ai.NavigationSquare;

public class PathFindingSystemTest {

    @Test
    public void test2SquarePathFinding() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 10);
        a.bottomRight = new Point2D(10, 0);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 10);
        b.bottomRight = new Point2D(20, 0);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, b.adjacentEdges.size());  

        NavigationEdge aEdge = a.adjacentEdges.get(0);
        assertEquals(b, aEdge.adjacentSquare);   
        assertEquals(new Point2D(10, 10), aEdge.start);
        assertEquals(new Point2D(10, 0), aEdge.end);

        NavigationEdge bEdge = b.adjacentEdges.get(0);
        assertEquals(a, bEdge.adjacentSquare);
        assertEquals(new Point2D(10, 10), bEdge.start);
        assertEquals(new Point2D(10, 0), bEdge.end);

        //Find path through network
        PathFindingSystem system = new PathFindingSystem();
        
        List<NavigationSquare> path = system.findPath(a, 1, 9, b, 19, 9);

        assertEquals(2, path.size());

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),b);
    }
}
