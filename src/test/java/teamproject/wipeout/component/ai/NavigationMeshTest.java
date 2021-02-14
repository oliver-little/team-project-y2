package teamproject.wipeout.component.ai;

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

    @Test
    public void testTopDownSquareGeneration() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 10);
        a.bottomRight = new Point2D(10, 0);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(5, 20);
        b.bottomRight = new Point2D(15, 10);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, b.adjacentEdges.size());  
        
        NavigationEdge aEdge = a.adjacentEdges.get(0);
        assertEquals(b, aEdge.adjacentSquare);   
        assertEquals(new Point2D(5, 10), aEdge.start);
        assertEquals(new Point2D(10, 10), aEdge.end);

        NavigationEdge bEdge = b.adjacentEdges.get(0);
        assertEquals(a, bEdge.adjacentSquare);
        assertEquals(new Point2D(5, 10), bEdge.start);
        assertEquals(new Point2D(10, 10), bEdge.end);        
    }

    @Test
    public void testThreeSquareGeneration() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 5);
        a.bottomRight = new Point2D(10, 0);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(5, 10);
        b.bottomRight = new Point2D(15, 5);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 5);
        c.bottomRight = new Point2D(20, 0);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(2, c.adjacentEdges.size());   
        
        NavigationEdge aEdge0 = a.adjacentEdges.get(0);
        assertEquals(b, aEdge0.adjacentSquare);   
        assertEquals(new Point2D(5, 5), aEdge0.start);
        assertEquals(new Point2D(10, 5), aEdge0.end);

        NavigationEdge aEdge1 = a.adjacentEdges.get(1);
        assertEquals(c, aEdge1.adjacentSquare);   
        assertEquals(new Point2D(10, 5), aEdge1.start);
        assertEquals(new Point2D(10, 0), aEdge1.end);

        NavigationEdge bEdge0 = b.adjacentEdges.get(0);
        assertEquals(a, bEdge0.adjacentSquare);
        assertEquals(new Point2D(5, 5), bEdge0.start);
        assertEquals(new Point2D(10, 5), bEdge0.end);
        
        NavigationEdge bEdge1 = b.adjacentEdges.get(1);
        assertEquals(c, bEdge1.adjacentSquare);
        assertEquals(new Point2D(10, 5), bEdge1.start);
        assertEquals(new Point2D(15, 5), bEdge1.end);
        
        NavigationEdge cEdge0 = c.adjacentEdges.get(0);
        assertEquals(a, cEdge0.adjacentSquare);
        assertEquals(new Point2D(10, 5), cEdge0.start);
        assertEquals(new Point2D(10, 0), cEdge0.end);
        
        NavigationEdge cEdge1 = c.adjacentEdges.get(1);
        assertEquals(b, cEdge1.adjacentSquare);
        assertEquals(new Point2D(10, 5), cEdge1.start);
        assertEquals(new Point2D(15, 5), cEdge1.end); 
    }

    @Test
    public void testMultipleSquareGeneration() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(5, 5);
        a.bottomRight = new Point2D(15, 0);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 10);
        b.bottomRight = new Point2D(20, 5);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(15, 5);
        c.bottomRight = new Point2D(20, 0);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(0, 10);
        d.bottomRight = new Point2D(10, 5);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);
        navigationSquares.add(d);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(3, a.adjacentEdges.size());
        assertEquals(3, b.adjacentEdges.size());
        assertEquals(2, c.adjacentEdges.size());
        assertEquals(2, d.adjacentEdges.size());  
        
        NavigationEdge aEdge0 = a.adjacentEdges.get(0);
        assertEquals(b, aEdge0.adjacentSquare);   
        assertEquals(new Point2D(10, 5), aEdge0.start);
        assertEquals(new Point2D(15, 5), aEdge0.end);

        NavigationEdge aEdge1 = a.adjacentEdges.get(1);
        assertEquals(c, aEdge1.adjacentSquare);   
        assertEquals(new Point2D(15, 5), aEdge1.start);
        assertEquals(new Point2D(15, 0), aEdge1.end);

        NavigationEdge aEdge2 = a.adjacentEdges.get(2);
        assertEquals(d, aEdge2.adjacentSquare);   
        assertEquals(new Point2D(5, 5), aEdge2.start);
        assertEquals(new Point2D(10, 5), aEdge2.end);

        NavigationEdge bEdge0 = b.adjacentEdges.get(0);
        assertEquals(a, bEdge0.adjacentSquare);
        assertEquals(new Point2D(10, 5), bEdge0.start);
        assertEquals(new Point2D(15, 5), bEdge0.end);
        
        NavigationEdge bEdge1 = b.adjacentEdges.get(1);
        assertEquals(c, bEdge1.adjacentSquare);
        assertEquals(new Point2D(15, 5), bEdge1.start);
        assertEquals(new Point2D(20, 5), bEdge1.end);

        NavigationEdge bEdge2 = b.adjacentEdges.get(2);
        assertEquals(d, bEdge2.adjacentSquare);
        assertEquals(new Point2D(10, 10), bEdge2.start);
        assertEquals(new Point2D(10, 5), bEdge2.end);

        NavigationEdge cEdge0 = c.adjacentEdges.get(0);
        assertEquals(a, cEdge0.adjacentSquare);
        assertEquals(new Point2D(15, 5), cEdge0.start);
        assertEquals(new Point2D(15, 0), cEdge0.end);
        
        NavigationEdge cEdge1 = c.adjacentEdges.get(1);
        assertEquals(b, cEdge1.adjacentSquare);
        assertEquals(new Point2D(15, 5), cEdge1.start);
        assertEquals(new Point2D(20, 5), cEdge1.end);

        NavigationEdge dEdge0 = d.adjacentEdges.get(0);
        assertEquals(a, dEdge0.adjacentSquare);
        assertEquals(new Point2D(5, 5), dEdge0.start);
        assertEquals(new Point2D(10, 5), dEdge0.end);
        
        NavigationEdge dEdge1 = d.adjacentEdges.get(1);
        assertEquals(b, dEdge1.adjacentSquare);
        assertEquals(new Point2D(10, 10), dEdge1.start);
        assertEquals(new Point2D(10, 5), dEdge1.end);
    }
    
    @Test
    public void testOffSquareGeneration() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(5, 5);
        a.bottomRight = new Point2D(15, 0);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 10);
        b.bottomRight = new Point2D(20, 5);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(15, 5);
        c.bottomRight = new Point2D(20, 0);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(0, 10);
        d.bottomRight = new Point2D(10, 5);

        NavigationSquare e = new NavigationSquare();
        e.topLeft = new Point2D(5, 15);
        e.bottomRight = new Point2D(15, 10);

        NavigationSquare f = new NavigationSquare();
        f.topLeft = new Point2D(30, 15);
        f.bottomRight = new Point2D(40, 0);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);
        navigationSquares.add(d);
        navigationSquares.add(e);
        navigationSquares.add(f);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(3, a.adjacentEdges.size());
        assertEquals(4, b.adjacentEdges.size());
        assertEquals(2, c.adjacentEdges.size());
        assertEquals(3, d.adjacentEdges.size());
        assertEquals(2, e.adjacentEdges.size());
        assertEquals(0, f.adjacentEdges.size());  
        
        NavigationEdge aEdge0 = a.adjacentEdges.get(0);
        assertEquals(b, aEdge0.adjacentSquare);   
        assertEquals(new Point2D(10, 5), aEdge0.start);
        assertEquals(new Point2D(15, 5), aEdge0.end);

        NavigationEdge aEdge1 = a.adjacentEdges.get(1);
        assertEquals(c, aEdge1.adjacentSquare);   
        assertEquals(new Point2D(15, 5), aEdge1.start);
        assertEquals(new Point2D(15, 0), aEdge1.end);

        NavigationEdge aEdge2 = a.adjacentEdges.get(2);
        assertEquals(d, aEdge2.adjacentSquare);   
        assertEquals(new Point2D(5, 5), aEdge2.start);
        assertEquals(new Point2D(10, 5), aEdge2.end);

        NavigationEdge bEdge0 = b.adjacentEdges.get(0);
        assertEquals(a, bEdge0.adjacentSquare);
        assertEquals(new Point2D(10, 5), bEdge0.start);
        assertEquals(new Point2D(15, 5), bEdge0.end);
        
        NavigationEdge bEdge1 = b.adjacentEdges.get(1);
        assertEquals(c, bEdge1.adjacentSquare);
        assertEquals(new Point2D(15, 5), bEdge1.start);
        assertEquals(new Point2D(20, 5), bEdge1.end);

        NavigationEdge bEdge2 = b.adjacentEdges.get(2);
        assertEquals(d, bEdge2.adjacentSquare);
        assertEquals(new Point2D(10, 10), bEdge2.start);
        assertEquals(new Point2D(10, 5), bEdge2.end);

        NavigationEdge bEdge3 = b.adjacentEdges.get(3);
        assertEquals(e, bEdge3.adjacentSquare);
        assertEquals(new Point2D(10, 10), bEdge3.start);
        assertEquals(new Point2D(15, 10), bEdge3.end);

        NavigationEdge cEdge0 = c.adjacentEdges.get(0);
        assertEquals(a, cEdge0.adjacentSquare);
        assertEquals(new Point2D(15, 5), cEdge0.start);
        assertEquals(new Point2D(15, 0), cEdge0.end);
        
        NavigationEdge cEdge1 = c.adjacentEdges.get(1);
        assertEquals(b, cEdge1.adjacentSquare);
        assertEquals(new Point2D(15, 5), cEdge1.start);
        assertEquals(new Point2D(20, 5), cEdge1.end);

        NavigationEdge dEdge0 = d.adjacentEdges.get(0);
        assertEquals(a, dEdge0.adjacentSquare);
        assertEquals(new Point2D(5, 5), dEdge0.start);
        assertEquals(new Point2D(10, 5), dEdge0.end);
        
        NavigationEdge dEdge1 = d.adjacentEdges.get(1);
        assertEquals(b, dEdge1.adjacentSquare);
        assertEquals(new Point2D(10, 10), dEdge1.start);
        assertEquals(new Point2D(10, 5), dEdge1.end);

        NavigationEdge dEdge2 = d.adjacentEdges.get(2);
        assertEquals(e, dEdge2.adjacentSquare);
        assertEquals(new Point2D(5, 10), dEdge2.start);
        assertEquals(new Point2D(10, 10), dEdge2.end);

        assertEquals(0, f.adjacentEdges.size());
    }

    @Test
    public void testVerticalAxisGeneration() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 10);
        a.bottomRight = new Point2D(10, 0);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(0, 20);
        b.bottomRight = new Point2D(10, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 20);
        c.bottomRight = new Point2D(20, 15);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(1, c.adjacentEdges.size());   
        
        NavigationEdge aEdge0 = a.adjacentEdges.get(0);
        assertEquals(b, aEdge0.adjacentSquare);   
        assertEquals(new Point2D(0, 10), aEdge0.start);
        assertEquals(new Point2D(10, 10), aEdge0.end);

        NavigationEdge bEdge0 = b.adjacentEdges.get(1);
        assertEquals(c, bEdge0.adjacentSquare);
        assertEquals(new Point2D(10, 20), bEdge0.start);
        assertEquals(new Point2D(10, 15), bEdge0.end);
        
        NavigationEdge bEdge1 = b.adjacentEdges.get(0);
        assertEquals(a, bEdge1.adjacentSquare);
        assertEquals(new Point2D(0, 10), bEdge1.start);
        assertEquals(new Point2D(10, 10), bEdge1.end);
        
        NavigationEdge cEdge0 = c.adjacentEdges.get(0);
        assertEquals(b, cEdge0.adjacentSquare);
        assertEquals(new Point2D(10, 20), cEdge0.start);
        assertEquals(new Point2D(10, 15), cEdge0.end);
    }
}
