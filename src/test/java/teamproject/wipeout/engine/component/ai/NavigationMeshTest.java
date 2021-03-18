package teamproject.wipeout.engine.component.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.shape.Rectangle;

public class NavigationMeshTest {

    @Test
    public void test2CornerAdjacentSquares() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 10);
        b.bottomRight = new Point2D(20, 20);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(0, a.adjacentEdges.size());
        assertEquals(0, b.adjacentEdges.size());
        
        assertEquals(2, navigationMesh.squares.size());
    }

    @Test
    public void test2SidePerfectlyAdjacentSquares() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 10);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, b.adjacentEdges.size());

        NavigationEdge aEdge = a.adjacentEdges.get(0);
        assertEquals(b, aEdge.adjacentSquare);   
        assertEquals(new Point2D(10, 0), aEdge.start);
        assertEquals(new Point2D(10, 10), aEdge.end);

        NavigationEdge bEdge = b.adjacentEdges.get(0);
        assertEquals(a, bEdge.adjacentSquare);
        assertEquals(new Point2D(10, 0), bEdge.start);
        assertEquals(new Point2D(10, 10), bEdge.end);
        
        assertEquals(2, navigationMesh.squares.size());
    }

    @Test
    public void test2TopPerfectlyAdjacentSquares() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(0, 10);
        b.bottomRight = new Point2D(10, 20);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, b.adjacentEdges.size());

        NavigationEdge aEdge = a.adjacentEdges.get(0);
        assertEquals(b, aEdge.adjacentSquare);   
        assertEquals(new Point2D(0, 10), aEdge.start);
        assertEquals(new Point2D(10, 10), aEdge.end);

        NavigationEdge bEdge = b.adjacentEdges.get(0);
        assertEquals(a, bEdge.adjacentSquare);
        assertEquals(new Point2D(0, 10), bEdge.start);
        assertEquals(new Point2D(10, 10), bEdge.end);
        
        assertEquals(2, navigationMesh.squares.size());
    }

    @Test
    public void testLeftRightSquareGeneration() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 5);
        b.bottomRight = new Point2D(20, 15);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, b.adjacentEdges.size());  
        
        NavigationEdge aEdge = a.adjacentEdges.get(0);
        assertEquals(b, aEdge.adjacentSquare);   
        assertEquals(new Point2D(10, 5), aEdge.start);
        assertEquals(new Point2D(10, 10), aEdge.end);

        NavigationEdge bEdge = b.adjacentEdges.get(0);
        assertEquals(a, bEdge.adjacentSquare);
        assertEquals(new Point2D(10, 5), bEdge.start);
        assertEquals(new Point2D(10, 10), bEdge.end);
        
        assertEquals(2, navigationMesh.squares.size());
    }

    @Test
    public void testTopDownSquareGeneration() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(5, 10);
        b.bottomRight = new Point2D(15, 20);

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
        
        assertEquals(2, navigationMesh.squares.size());
    }

    @Test
    public void testThreeSquareGeneration() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 5);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(5, 5);
        b.bottomRight = new Point2D(15, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 0);
        c.bottomRight = new Point2D(20, 5);

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
        assertEquals(new Point2D(10, 0), aEdge1.start);
        assertEquals(new Point2D(10, 5), aEdge1.end);

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
        assertEquals(new Point2D(10, 0), cEdge0.start);
        assertEquals(new Point2D(10, 5), cEdge0.end);
        
        NavigationEdge cEdge1 = c.adjacentEdges.get(1);
        assertEquals(b, cEdge1.adjacentSquare);
        assertEquals(new Point2D(10, 5), cEdge1.start);
        assertEquals(new Point2D(15, 5), cEdge1.end);
        
        assertEquals(3, navigationMesh.squares.size());
    }

    @Test
    public void testMultipleSquareGeneration() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(5, 0);
        a.bottomRight = new Point2D(15, 5);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 5);
        b.bottomRight = new Point2D(20, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(15, 0);
        c.bottomRight = new Point2D(20, 5);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(0, 5);
        d.bottomRight = new Point2D(10, 10);

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
        assertEquals(new Point2D(15, 0), aEdge1.start);
        assertEquals(new Point2D(15, 5), aEdge1.end);

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
        assertEquals(new Point2D(10, 5), bEdge2.start);
        assertEquals(new Point2D(10, 10), bEdge2.end);

        NavigationEdge cEdge0 = c.adjacentEdges.get(0);
        assertEquals(a, cEdge0.adjacentSquare);
        assertEquals(new Point2D(15, 0), cEdge0.start);
        assertEquals(new Point2D(15, 5), cEdge0.end);
        
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
        assertEquals(new Point2D(10, 5), dEdge1.start);
        assertEquals(new Point2D(10, 10), dEdge1.end);

        assertEquals(4, navigationMesh.squares.size());
    }
    
    @Test
    public void testOffSquareGeneration() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(5, 0);
        a.bottomRight = new Point2D(15, 5);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 5);
        b.bottomRight = new Point2D(20, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(15, 0);
        c.bottomRight = new Point2D(20, 5);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(0, 5);
        d.bottomRight = new Point2D(10, 10);

        NavigationSquare e = new NavigationSquare();
        e.topLeft = new Point2D(5, 10);
        e.bottomRight = new Point2D(15, 15);

        NavigationSquare f = new NavigationSquare();
        f.topLeft = new Point2D(30, 0);
        f.bottomRight = new Point2D(40, 15);

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
        assertEquals(new Point2D(15, 0), aEdge1.start);
        assertEquals(new Point2D(15, 5), aEdge1.end);

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
        assertEquals(new Point2D(10, 5), bEdge2.start);
        assertEquals(new Point2D(10, 10), bEdge2.end);

        NavigationEdge bEdge3 = b.adjacentEdges.get(3);
        assertEquals(e, bEdge3.adjacentSquare);
        assertEquals(new Point2D(10, 10), bEdge3.start);
        assertEquals(new Point2D(15, 10), bEdge3.end);

        NavigationEdge cEdge0 = c.adjacentEdges.get(0);
        assertEquals(a, cEdge0.adjacentSquare);
        assertEquals(new Point2D(15, 0), cEdge0.start);
        assertEquals(new Point2D(15, 5), cEdge0.end);
        
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
        assertEquals(new Point2D(10, 5), dEdge1.start);
        assertEquals(new Point2D(10, 10), dEdge1.end);

        NavigationEdge dEdge2 = d.adjacentEdges.get(2);
        assertEquals(e, dEdge2.adjacentSquare);
        assertEquals(new Point2D(5, 10), dEdge2.start);
        assertEquals(new Point2D(10, 10), dEdge2.end);

        assertEquals(0, f.adjacentEdges.size());

        assertEquals(6, navigationMesh.squares.size());
    }

    @Test
    public void testVerticalAxisGeneration() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(0, 10);
        b.bottomRight = new Point2D(10, 20);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 15);
        c.bottomRight = new Point2D(20, 20);

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
        assertEquals(new Point2D(10, 15), bEdge0.start);
        assertEquals(new Point2D(10, 20), bEdge0.end);
        
        NavigationEdge bEdge1 = b.adjacentEdges.get(0);
        assertEquals(a, bEdge1.adjacentSquare);
        assertEquals(new Point2D(0, 10), bEdge1.start);
        assertEquals(new Point2D(10, 10), bEdge1.end);
        
        NavigationEdge cEdge0 = c.adjacentEdges.get(0);
        assertEquals(b, cEdge0.adjacentSquare);
        assertEquals(new Point2D(10, 15), cEdge0.start);
        assertEquals(new Point2D(10, 20), cEdge0.end);

        assertEquals(3, navigationMesh.squares.size());
    }

    @Test
    public void testAddingSquareToMesh() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 5);
        b.bottomRight = new Point2D(20, 15);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, b.adjacentEdges.size());
        
        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(0, 10);
        c.bottomRight = new Point2D(10, 15);
        
        assertEquals(true, navigationMesh.addSquare(c));

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(2, c.adjacentEdges.size());


        NavigationEdge aEdge0 = a.adjacentEdges.get(0);
        assertEquals(b, aEdge0.adjacentSquare);   
        assertEquals(new Point2D(10, 5), aEdge0.start);
        assertEquals(new Point2D(10, 10), aEdge0.end);

        NavigationEdge aEdge1 = a.adjacentEdges.get(1);
        assertEquals(c, aEdge1.adjacentSquare);   
        assertEquals(new Point2D(0, 10), aEdge1.start);
        assertEquals(new Point2D(10, 10), aEdge1.end);

        NavigationEdge bEdge0 = b.adjacentEdges.get(0);
        assertEquals(a, bEdge0.adjacentSquare);
        assertEquals(new Point2D(10, 5), bEdge0.start);
        assertEquals(new Point2D(10, 10), bEdge0.end);

        NavigationEdge bEdge1 = b.adjacentEdges.get(1);
        assertEquals(c, bEdge1.adjacentSquare);
        assertEquals(new Point2D(10, 10), bEdge1.start);
        assertEquals(new Point2D(10, 15), bEdge1.end);

        NavigationEdge cEdge0 = c.adjacentEdges.get(0);
        assertEquals(a, cEdge0.adjacentSquare);
        assertEquals(new Point2D(0, 10), cEdge0.start);
        assertEquals(new Point2D(10, 10), cEdge0.end);

        NavigationEdge cEdge1 = c.adjacentEdges.get(1);
        assertEquals(b, cEdge1.adjacentSquare);
        assertEquals(new Point2D(10, 10), cEdge1.start);
        assertEquals(new Point2D(10, 15), cEdge1.end);

        assertEquals(3, navigationMesh.squares.size());
    }

    @Test
    public void testRemovingSquareFromMesh() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 5);
        b.bottomRight = new Point2D(20, 15);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(0, 10);
        c.bottomRight = new Point2D(10, 15);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(2, c.adjacentEdges.size());
      
        assertEquals(true, navigationMesh.removeSquare(b));

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, c.adjacentEdges.size());

        NavigationEdge aEdge = a.adjacentEdges.get(0);
        assertEquals(c, aEdge.adjacentSquare);   
        assertEquals(new Point2D(0, 10), aEdge.start);
        assertEquals(new Point2D(10, 10), aEdge.end);

        NavigationEdge cEdge = c.adjacentEdges.get(0);
        assertEquals(a, cEdge.adjacentSquare);
        assertEquals(new Point2D(0, 10), cEdge.start);
        assertEquals(new Point2D(10, 10), cEdge.end);
        
        assertEquals(2, navigationMesh.squares.size());
    }

    @Test
    public void testComplexAddingSquareToMesh() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(10, 20);
        a.bottomRight = new Point2D(20, 30);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(0, 10);
        b.bottomRight = new Point2D(10, 20);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 0);
        c.bottomRight = new Point2D(20, 10);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(20, 10);
        d.bottomRight = new Point2D(30, 20);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);
        navigationSquares.add(d);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(0, a.adjacentEdges.size());
        assertEquals(0, b.adjacentEdges.size());
        assertEquals(0, c.adjacentEdges.size());
        assertEquals(0, d.adjacentEdges.size());
        
        NavigationSquare e = new NavigationSquare();
        e.topLeft = new Point2D(10, 10);
        e.bottomRight = new Point2D(20, 20);
        
        navigationMesh.addSquare(e);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, b.adjacentEdges.size());
        assertEquals(1, c.adjacentEdges.size());
        assertEquals(1, d.adjacentEdges.size());
        assertEquals(4, e.adjacentEdges.size());


        NavigationEdge aEdge0 = a.adjacentEdges.get(0);
        assertEquals(e, aEdge0.adjacentSquare);   
        assertEquals(new Point2D(10, 20), aEdge0.start);
        assertEquals(new Point2D(20, 20), aEdge0.end);

        NavigationEdge bEdge0 = b.adjacentEdges.get(0);
        assertEquals(e, bEdge0.adjacentSquare);
        assertEquals(new Point2D(10, 10), bEdge0.start);
        assertEquals(new Point2D(10, 20), bEdge0.end);

        NavigationEdge cEdge0 = c.adjacentEdges.get(0);
        assertEquals(e, cEdge0.adjacentSquare);
        assertEquals(new Point2D(10, 10), cEdge0.start);
        assertEquals(new Point2D(20, 10), cEdge0.end);

        NavigationEdge dEdge0 = d.adjacentEdges.get(0);
        assertEquals(e, dEdge0.adjacentSquare);
        assertEquals(new Point2D(20, 10), dEdge0.start);
        assertEquals(new Point2D(20, 20), dEdge0.end);

        NavigationEdge eEdge0 = e.adjacentEdges.get(0);
        assertEquals(a, eEdge0.adjacentSquare);   
        assertEquals(new Point2D(10, 20), eEdge0.start);
        assertEquals(new Point2D(20, 20), eEdge0.end);

        NavigationEdge eEdge1 = e.adjacentEdges.get(1);
        assertEquals(b, eEdge1.adjacentSquare);
        assertEquals(new Point2D(10, 10), eEdge1.start);
        assertEquals(new Point2D(10, 20), eEdge1.end);

        NavigationEdge eEdge2 = e.adjacentEdges.get(2);
        assertEquals(c, eEdge2.adjacentSquare);
        assertEquals(new Point2D(10, 10), eEdge2.start);
        assertEquals(new Point2D(20, 10), eEdge2.end);

        NavigationEdge eEdge3 = e.adjacentEdges.get(3);
        assertEquals(d, eEdge3.adjacentSquare);
        assertEquals(new Point2D(20, 10), eEdge3.start);
        assertEquals(new Point2D(20, 20), eEdge3.end);

        assertEquals(5, navigationMesh.squares.size());
    }

    @Test
    public void testComplexRemoveSquareToMesh() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(10, 20);
        a.bottomRight = new Point2D(20, 30);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(0, 10);
        b.bottomRight = new Point2D(10, 20);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 0);
        c.bottomRight = new Point2D(20, 10);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(20, 10);
        d.bottomRight = new Point2D(30, 20);

        NavigationSquare e = new NavigationSquare();
        e.topLeft = new Point2D(10, 10);
        e.bottomRight = new Point2D(20, 20);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);
        navigationSquares.add(d);
        navigationSquares.add(e);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, b.adjacentEdges.size());
        assertEquals(1, c.adjacentEdges.size());
        assertEquals(1, d.adjacentEdges.size());
        assertEquals(4, e.adjacentEdges.size());
        
        assertEquals(true, navigationMesh.removeSquare(e));

        assertEquals(0, a.adjacentEdges.size());
        assertEquals(0, b.adjacentEdges.size());
        assertEquals(0, c.adjacentEdges.size());
        assertEquals(0, d.adjacentEdges.size());

        assertEquals(4, navigationMesh.squares.size());
    }

    @Test
    public void testRemoveSquareThatDoesNotExist() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(10, 30);
        a.bottomRight = new Point2D(20, 20);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(0, 10);
        b.bottomRight = new Point2D(10, 20);

        assertEquals(0, a.adjacentEdges.size());
        
        navigationMesh.removeSquare(b);

        assertEquals(0, a.adjacentEdges.size());

        assertEquals(1, navigationMesh.squares.size());
    }

    @Test
    public void testAddSquareThatAlreadyExists() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(10, 20);
        a.bottomRight = new Point2D(20, 30);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 20);
        b.bottomRight = new Point2D(20, 30);

        assertEquals(0, a.adjacentEdges.size());
        
        assertEquals(true, navigationMesh.addSquare(b));

        assertEquals(0, a.adjacentEdges.size());
        assertEquals(0, b.adjacentEdges.size());

        assertEquals(2, navigationMesh.squares.size());
    }

    @Test
    public void testOverlappingSquares() {
        
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(5, 5);
        b.bottomRight = new Point2D(15, 15);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(0, a.adjacentEdges.size());
        assertEquals(0, b.adjacentEdges.size());

        assertEquals(2, navigationMesh.squares.size());
        
    }

    @Test
    public void testGenerateNoColliders() {
        Point2D topLeft = new Point2D(0, 0);
        Point2D bottomRight = new Point2D(100, 100);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, new ArrayList<>());

        assertEquals(1, navigationMesh.squares.size());
        
        NavigationSquare square = navigationMesh.squares.get(0);
        assertEquals(topLeft, square.topLeft);
        assertEquals(bottomRight, square.bottomRight);
    }

    @Test
    public void testGenerateSingleCollider() {
        Point2D topLeft = new Point2D(0, 0);
        Point2D bottomRight = new Point2D(100, 100);

        List<Rectangle> colliders = new ArrayList<>();
        colliders.add(new Rectangle(new Point2D(10, 10), 10, 10));

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(4, navigationMesh.squares.size());
        
        NavigationSquare square = navigationMesh.squares.get(0);
        assertEquals(topLeft, square.topLeft);
        assertEquals(new Point2D(10, 100), square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(20, 0), square.topLeft);
        assertEquals(bottomRight, square.bottomRight);
        square = navigationMesh.squares.get(2);
        assertEquals(new Point2D(10, 0), square.topLeft);
        assertEquals(new Point2D(20, 10), square.bottomRight);
        square = navigationMesh.squares.get(3);
        assertEquals(new Point2D(10, 20), square.topLeft);
        assertEquals(new Point2D(20, 100), square.bottomRight);

        colliders.clear();
        colliders.add(new Rectangle(new Point2D(0, 10), 10, 10));
        navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(3, navigationMesh.squares.size());
        square = navigationMesh.squares.get(0);
        assertEquals(new Point2D(10, 0), square.topLeft);
        assertEquals(bottomRight, square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(0, 0), square.topLeft);
        assertEquals(new Point2D(10, 10), square.bottomRight);
        square = navigationMesh.squares.get(2);
        assertEquals(new Point2D(0, 20), square.topLeft);
        assertEquals(new Point2D(10, 100), square.bottomRight);

        colliders.clear();
        colliders.add(new Rectangle(new Point2D(90, 10), 10, 10));
        navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(3, navigationMesh.squares.size());
        
        square = navigationMesh.squares.get(0);
        assertEquals(topLeft, square.topLeft);
        assertEquals(new Point2D(90, 100), square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(90, 0), square.topLeft);
        assertEquals(new Point2D(100, 10), square.bottomRight);
        square = navigationMesh.squares.get(2);
        assertEquals(new Point2D(90, 20), square.topLeft);
        assertEquals(new Point2D(100, 100), square.bottomRight);

        colliders.clear();
        colliders.add(new Rectangle(new Point2D(10, 0), 10, 10));
        navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(3, navigationMesh.squares.size());
        
        square = navigationMesh.squares.get(0);
        assertEquals(topLeft, square.topLeft);
        assertEquals(new Point2D(10, 100), square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(20, 0), square.topLeft);
        assertEquals(bottomRight, square.bottomRight);
        square = navigationMesh.squares.get(2);
        assertEquals(new Point2D(10, 10), square.topLeft);
        assertEquals(new Point2D(20, 100), square.bottomRight);

        colliders.clear();
        colliders.add(new Rectangle(new Point2D(10, 90), 10, 10));
        navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(3, navigationMesh.squares.size());
        
        square = navigationMesh.squares.get(0);
        assertEquals(topLeft, square.topLeft);
        assertEquals(new Point2D(10, 100), square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(20, 0), square.topLeft);
        assertEquals(bottomRight, square.bottomRight);
        square = navigationMesh.squares.get(2);
        assertEquals(new Point2D(10, 0), square.topLeft);
        assertEquals(new Point2D(20, 90), square.bottomRight);

        colliders.clear();
        colliders.add(new Rectangle(new Point2D(0, 0), 10, 10));
        navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(2, navigationMesh.squares.size());

        square = navigationMesh.squares.get(0);
        assertEquals(new Point2D(10, 0), square.topLeft);
        assertEquals(new Point2D(100, 100), square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(0, 10), square.topLeft);
        assertEquals(new Point2D(10, 100), square.bottomRight);

        colliders.clear();
        colliders.add(new Rectangle(new Point2D(0, 90), 10, 10));
        navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(2, navigationMesh.squares.size());
        
        square = navigationMesh.squares.get(0);
        assertEquals(new Point2D(10, 0), square.topLeft);
        assertEquals(new Point2D(100, 100), square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(topLeft, square.topLeft);
        assertEquals(new Point2D(10, 90), square.bottomRight);

        colliders.clear();
        colliders.add(new Rectangle(new Point2D(90, 0), 10, 10));
        navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(2, navigationMesh.squares.size());
        
        square = navigationMesh.squares.get(0);
        assertEquals(topLeft, square.topLeft);
        assertEquals(new Point2D(90, 100), square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(90, 10), square.topLeft);
        assertEquals(new Point2D(100, 100), square.bottomRight);

        colliders.clear();
        colliders.add(new Rectangle(new Point2D(90, 90), 10, 10));
        navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(2, navigationMesh.squares.size());
        
        square = navigationMesh.squares.get(0);
        assertEquals(topLeft, square.topLeft);
        assertEquals(new Point2D(90, 100), square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(90, 0), square.topLeft);
        assertEquals(new Point2D(100, 90), square.bottomRight);
    }

    @Test
    public void testGenerateTwoColliders() {
        Point2D topLeft = new Point2D(0, 0);
        Point2D bottomRight = new Point2D(100, 100);

        List<Rectangle> colliders = new ArrayList<>();
        colliders.add(new Rectangle(new Point2D(10, 10), 10, 10));
        colliders.add(new Rectangle(new Point2D(80, 80), 10, 10));

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(7, navigationMesh.squares.size());

        NavigationSquare square = navigationMesh.squares.get(0);
        assertEquals(topLeft, square.topLeft);
        assertEquals(new Point2D(10, 100), square.bottomRight);;
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(10, 0), square.topLeft);
        assertEquals(new Point2D(20, 10), square.bottomRight);
        square = navigationMesh.squares.get(2);
        assertEquals(new Point2D(10, 20), square.topLeft);
        assertEquals(new Point2D(20, 100), square.bottomRight);
        square = navigationMesh.squares.get(3);
        assertEquals(new Point2D(20, 0), square.topLeft);
        assertEquals(new Point2D(80, 100), square.bottomRight);
        square = navigationMesh.squares.get(4);
        assertEquals(new Point2D(90, 0), square.topLeft);
        assertEquals(new Point2D(100, 100), square.bottomRight);
        square = navigationMesh.squares.get(5);
        assertEquals(new Point2D(80, 0), square.topLeft);
        assertEquals(new Point2D(90, 80), square.bottomRight);
        square = navigationMesh.squares.get(6);
        assertEquals(new Point2D(80, 90), square.topLeft);
        assertEquals(new Point2D(90, 100), square.bottomRight);

        colliders.clear();
        colliders.add(new Rectangle(new Point2D(10, 10), 10, 10));
        colliders.add(new Rectangle(new Point2D(10, 80), 10, 10));

        navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(5, navigationMesh.squares.size());

        square = navigationMesh.squares.get(0);
        assertEquals(topLeft, square.topLeft);
        assertEquals(new Point2D(10, 100), square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(20, 0), square.topLeft);
        assertEquals(bottomRight, square.bottomRight);
        square = navigationMesh.squares.get(2);
        assertEquals(new Point2D(10, 0), square.topLeft);
        assertEquals(new Point2D(20, 10), square.bottomRight);
        square = navigationMesh.squares.get(3);
        assertEquals(new Point2D(10, 20), square.topLeft);
        assertEquals(new Point2D(20, 80), square.bottomRight);
        square = navigationMesh.squares.get(4);
        assertEquals(new Point2D(10, 90), square.topLeft);
        assertEquals(new Point2D(20, 100), square.bottomRight);

        colliders.clear();
        colliders.add(new Rectangle(new Point2D(10, 10), 10, 10));
        colliders.add(new Rectangle(new Point2D(5, 80), 20, 10));

        navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(9, navigationMesh.squares.size());

        square = navigationMesh.squares.get(0);
        assertEquals(new Point2D(10, 0), square.topLeft);
        assertEquals(new Point2D(20, 10), square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(0, 0), square.topLeft);
        assertEquals(new Point2D(5, 100), square.bottomRight);
        square = navigationMesh.squares.get(2);
        assertEquals(new Point2D(5, 0), square.topLeft);
        assertEquals(new Point2D(10, 80), square.bottomRight);
        square = navigationMesh.squares.get(3);
        assertEquals(new Point2D(5, 90), square.topLeft);
        assertEquals(new Point2D(10, 100), square.bottomRight);
        square = navigationMesh.squares.get(4);
        assertEquals(new Point2D(25, 0), square.topLeft);
        assertEquals(new Point2D(100, 100), square.bottomRight);
        square = navigationMesh.squares.get(5);
        assertEquals(new Point2D(20, 0), square.topLeft);
        assertEquals(new Point2D(25, 80), square.bottomRight);
        square = navigationMesh.squares.get(6);
        assertEquals(new Point2D(20, 90), square.topLeft);
        assertEquals(new Point2D(25, 100), square.bottomRight);
        square = navigationMesh.squares.get(7);
        assertEquals(new Point2D(10, 20), square.topLeft);
        assertEquals(new Point2D(20, 80), square.bottomRight);
        square = navigationMesh.squares.get(8);
        assertEquals(new Point2D(10, 90), square.topLeft);
        assertEquals(new Point2D(20, 100), square.bottomRight);


        colliders.clear();
        colliders.add(new Rectangle(new Point2D(10, 10), 80, 10));
        colliders.add(new Rectangle(new Point2D(5, 80), 90, 10));

        navigationMesh = NavigationMesh.generateMesh(topLeft, bottomRight, colliders);

        assertEquals(9, navigationMesh.squares.size());

        square = navigationMesh.squares.get(0);
        assertEquals(new Point2D(10, 0), square.topLeft);
        assertEquals(new Point2D(90, 10), square.bottomRight);
        square = navigationMesh.squares.get(1);
        assertEquals(new Point2D(0, 0), square.topLeft);
        assertEquals(new Point2D(5, 100), square.bottomRight);
        square = navigationMesh.squares.get(2);
        assertEquals(new Point2D(5, 0), square.topLeft);
        assertEquals(new Point2D(10, 80), square.bottomRight);
        square = navigationMesh.squares.get(3);
        assertEquals(new Point2D(5, 90), square.topLeft);
        assertEquals(new Point2D(10, 100), square.bottomRight);
        square = navigationMesh.squares.get(4);
        assertEquals(new Point2D(95, 0), square.topLeft);
        assertEquals(new Point2D(100, 100), square.bottomRight);
        square = navigationMesh.squares.get(5);
        assertEquals(new Point2D(90, 0), square.topLeft);
        assertEquals(new Point2D(95, 80), square.bottomRight);
        square = navigationMesh.squares.get(6);
        assertEquals(new Point2D(90, 90), square.topLeft);
        assertEquals(new Point2D(95, 100), square.bottomRight);
        square = navigationMesh.squares.get(7);
        assertEquals(new Point2D(10, 20), square.topLeft);
        assertEquals(new Point2D(90, 80), square.bottomRight);
        square = navigationMesh.squares.get(8);
        assertEquals(new Point2D(10, 90), square.topLeft);
        assertEquals(new Point2D(90, 100), square.bottomRight);
    }
}
