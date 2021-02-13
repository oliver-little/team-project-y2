package teamproject.wipeout.component.ai;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ai.NavigationSquare;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.ai.NavigationEdge;

public class NavigationMeshTest {

    // TODO: top bottom square generation
    // big one with lots of complicated connections

    @Test
    public void testLeftRightSquareGeneration() {
        //Test
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 10);
        a.bottomRight = new Point2D(10, 0);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 15);
        b.bottomRight = new Point2D(20, 5);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, b.adjacentEdges.size());  
        
        NavigationEdge aEdge = a.adjacentEdges.get(0);
        assertEquals(b, aEdge.adjacentSquare);   
        assertEquals(new Point2D(10, 10), aEdge.start);
        assertEquals(new Point2D(10, 5), aEdge.end);

        NavigationEdge bEdge = b.adjacentEdges.get(0);
        assertEquals(a, b.adjacentEdges.get(0).adjacentSquare);
        assertEquals(new Point2D(10, 10), bEdge.start);
        assertEquals(new Point2D(10, 5), bEdge.end);        
    }   
}
