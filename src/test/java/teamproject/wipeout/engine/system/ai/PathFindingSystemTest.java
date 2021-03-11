package teamproject.wipeout.engine.system.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ai.NavigationEdge;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.ai.NavigationSquare;

public class PathFindingSystemTest {

    @Test
    public void testSameSquarePathFinding() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);

        NavigationMesh.generateMesh(navigationSquares);

        assertEquals(0, a.adjacentEdges.size()); 

        
        
        
        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 1, 9, a, 2, 8);

        assertEquals(1, path.size());

        assertEquals(path.get(0),a);
    }

    @Test
    public void testNegativeCoordinatePathFinding() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(0, -10);
        b.bottomRight = new Point2D(10, 0);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, b.adjacentEdges.size()); 

        
        
        
        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 2, 3, b, 2, -8);

        assertEquals(2, path.size());

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),b);
    }

    @Test
    public void test2SquarePathFinding() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 10);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh.generateMesh(navigationSquares);

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

        
        
        
        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 1, 9, b, 19, 9);

        assertEquals(2, path.size());

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),b);
    }

    @Test
    public void test4EqualSquarePathFinding() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(20, 5);
        a.bottomRight = new Point2D(30, 15);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 10);
        c.bottomRight = new Point2D(20, 20);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(0, 5);
        d.bottomRight = new Point2D(10, 15);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);
        navigationSquares.add(d);

        NavigationMesh.generateMesh(navigationSquares);

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(3, b.adjacentEdges.size());
        assertEquals(3, c.adjacentEdges.size());
        assertEquals(2, d.adjacentEdges.size());   

        
        
        
        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 25, 10, d, 5, 10);

        assertEquals(3, path.size());

        System.out.println(path);

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),c);
        assertEquals(path.get(2),d);
    }

    @Test
    public void test4UnEqualSquarePathFinding() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(20, 5);
        a.bottomRight = new Point2D(30, 15);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 10);
        c.bottomRight = new Point2D(20, 20);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(0, 5);
        d.bottomRight = new Point2D(10, 15);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);
        navigationSquares.add(d);

        NavigationMesh.generateMesh(navigationSquares);

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(3, b.adjacentEdges.size());
        assertEquals(3, c.adjacentEdges.size());
        assertEquals(2, d.adjacentEdges.size());   

        
        
        
        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 25, 15, d, 5, 10);

        assertEquals(3, path.size());

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),c);
        assertEquals(path.get(2),d);
    }

    @Test
    public void testComplexPathFinding() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(20, 5);
        a.bottomRight = new Point2D(30, 15);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 10);
        c.bottomRight = new Point2D(20, 20);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(0, 5);
        d.bottomRight = new Point2D(10, 15);

        NavigationSquare e = new NavigationSquare();
        e.topLeft = new Point2D(0, -10);
        e.bottomRight = new Point2D(10, 5);

        NavigationSquare f = new NavigationSquare();
        f.topLeft = new Point2D(10, -10);
        f.bottomRight = new Point2D(20, -5);

        NavigationSquare g = new NavigationSquare();
        g.topLeft = new Point2D(20, -10);
        g.bottomRight = new Point2D(30, 0);

        NavigationSquare h = new NavigationSquare();
        h.topLeft = new Point2D(10, -5);
        h.bottomRight = new Point2D(20, 0);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);
        navigationSquares.add(d);
        navigationSquares.add(e);
        navigationSquares.add(f);
        navigationSquares.add(g);
        navigationSquares.add(h);

        NavigationMesh.generateMesh(navigationSquares);

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(5, b.adjacentEdges.size());
        assertEquals(3, c.adjacentEdges.size());
        assertEquals(3, d.adjacentEdges.size());
        assertEquals(4, e.adjacentEdges.size());
        assertEquals(3, f.adjacentEdges.size());
        assertEquals(2, g.adjacentEdges.size());
        assertEquals(4, h.adjacentEdges.size());   

        
        

        
        
        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 25, 10, g, 25, -5);

        assertEquals(4, path.size());

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),b);
        assertEquals(path.get(2),h);
        assertEquals(path.get(3),g);
    }

    @Test
    public void testComplexPathFinding2() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(20, 5);
        a.bottomRight = new Point2D(30, 15);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 10);
        c.bottomRight = new Point2D(20, 20);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(0, 5);
        d.bottomRight = new Point2D(10, 15);

        NavigationSquare e = new NavigationSquare();
        e.topLeft = new Point2D(0, -10);
        e.bottomRight = new Point2D(10, 5);

        NavigationSquare f = new NavigationSquare();
        f.topLeft = new Point2D(10, -10);
        f.bottomRight = new Point2D(20, -5);

        NavigationSquare g = new NavigationSquare();
        g.topLeft = new Point2D(20, -10);
        g.bottomRight = new Point2D(30, 0);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);
        navigationSquares.add(d);
        navigationSquares.add(e);
        navigationSquares.add(f);
        navigationSquares.add(g);

        NavigationMesh.generateMesh(navigationSquares);

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(4, b.adjacentEdges.size());
        assertEquals(3, c.adjacentEdges.size());
        assertEquals(3, d.adjacentEdges.size());
        assertEquals(3, e.adjacentEdges.size());
        assertEquals(2, f.adjacentEdges.size());
        assertEquals(1, g.adjacentEdges.size());    

        
        
        
        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 21, 14, d, 9, 8);

        assertEquals(3, path.size());

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),c);
        assertEquals(path.get(2),d);
    }

    @Test
    public void testComplexPathFinding3() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(20, 5);
        a.bottomRight = new Point2D(30, 15);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 10);
        c.bottomRight = new Point2D(20, 20);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(0, 5);
        d.bottomRight = new Point2D(10, 15);

        NavigationSquare e = new NavigationSquare();
        e.topLeft = new Point2D(0, -10);
        e.bottomRight = new Point2D(10, 5);

        NavigationSquare f = new NavigationSquare();
        f.topLeft = new Point2D(10, -10);
        f.bottomRight = new Point2D(20, -5);

        NavigationSquare g = new NavigationSquare();
        g.topLeft = new Point2D(20, -10);
        g.bottomRight = new Point2D(30, 0);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);
        navigationSquares.add(d);
        navigationSquares.add(e);
        navigationSquares.add(f);
        navigationSquares.add(g);

        NavigationMesh.generateMesh(navigationSquares);

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(4, b.adjacentEdges.size());
        assertEquals(3, c.adjacentEdges.size());
        assertEquals(3, d.adjacentEdges.size());
        assertEquals(3, e.adjacentEdges.size());
        assertEquals(2, f.adjacentEdges.size());
        assertEquals(1, g.adjacentEdges.size());    

        
        
        
        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 21, 14, e, 8, 4);

        assertEquals(3, path.size());

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),b);
        assertEquals(path.get(2),e);
    }

    @Test
    public void testReversePathFinding() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(20, 5);
        a.bottomRight = new Point2D(30, 15);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(10, 10);
        c.bottomRight = new Point2D(20, 20);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(0, 5);
        d.bottomRight = new Point2D(10, 15);

        NavigationSquare e = new NavigationSquare();
        e.topLeft = new Point2D(0, -10);
        e.bottomRight = new Point2D(10, 5);

        NavigationSquare f = new NavigationSquare();
        f.topLeft = new Point2D(10, -10);
        f.bottomRight = new Point2D(20, -5);

        NavigationSquare g = new NavigationSquare();
        g.topLeft = new Point2D(20, -10);
        g.bottomRight = new Point2D(30, 0);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);
        navigationSquares.add(d);
        navigationSquares.add(e);
        navigationSquares.add(f);
        navigationSquares.add(g);

        NavigationMesh.generateMesh(navigationSquares);

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(4, b.adjacentEdges.size());
        assertEquals(3, c.adjacentEdges.size());
        assertEquals(3, d.adjacentEdges.size());
        assertEquals(3, e.adjacentEdges.size());
        assertEquals(2, f.adjacentEdges.size());
        assertEquals(1, g.adjacentEdges.size());    

        
        
        
        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(g, 25, -5, a, 25, 10);

        assertEquals(5, path.size());

        assertEquals(path.get(0),g);
        assertEquals(path.get(1),f);
        assertEquals(path.get(2),e);
        assertEquals(path.get(3),b);
        assertEquals(path.get(4),a);
    }

    @Test
    public void testNoPathFinding() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(20, 0);
        b.bottomRight = new Point2D(30, 10);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);

        NavigationMesh.generateMesh(navigationSquares);

        assertEquals(0, a.adjacentEdges.size());
        assertEquals(0, b.adjacentEdges.size());  

        
        

        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 5, 5, b, 25, 5);

        assertEquals(path, null);
    }

    @Test
    public void testNoPathFinding2() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(30, 0);
        c.bottomRight = new Point2D(40, 10);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);

        NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(1, b.adjacentEdges.size());
        assertEquals(0, c.adjacentEdges.size());   

        
        

        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 5, 5, c, 35, 5);

        assertEquals(path, null);
    }

    @Test
    public void testAddSquarePathFinding() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 10);
        a.bottomRight = new Point2D(10, 20);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(0, 0);
        b.bottomRight = new Point2D(30, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(20, 10);
        c.bottomRight = new Point2D(30, 20);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(1, c.adjacentEdges.size());   

        
        
        
        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 5, 15, c, 25, 15);

        assertEquals(3, path.size());

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),b);
        assertEquals(path.get(2),c);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(10, 10);
        d.bottomRight = new Point2D(20, 20);

        assertEquals(true, navigationMesh.addSquare(d));

        assertEquals(4, navigationMesh.squares.size());

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(3, b.adjacentEdges.size());
        assertEquals(2, c.adjacentEdges.size());
        assertEquals(3, d.adjacentEdges.size());

        path = PathFindingSystem.findPathThroughSquares(a, 5, 15, c, 25, 15);
        
        assertEquals(3, path.size());

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),d);
        assertEquals(path.get(2),c);
    }

    @Test
    public void testRemoveSquarePathFinding() {
        
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 10);
        a.bottomRight = new Point2D(10, 20);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(0, 0);
        b.bottomRight = new Point2D(30, 10);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(20, 10);
        c.bottomRight = new Point2D(30, 20);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(10, 10);
        d.bottomRight = new Point2D(20, 20);

        List<NavigationSquare> navigationSquares = new ArrayList<>();

        navigationSquares.add(a);
        navigationSquares.add(b);
        navigationSquares.add(c);
        navigationSquares.add(d);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(navigationSquares);

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(3, b.adjacentEdges.size());
        assertEquals(2, c.adjacentEdges.size());
        assertEquals(3, d.adjacentEdges.size()); 

        
        
        
        List<NavigationSquare> path = PathFindingSystem.findPathThroughSquares(a, 5, 15, c, 25, 15);

        assertEquals(3, path.size());

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),d);
        assertEquals(path.get(2),c);

        assertEquals(true, navigationMesh.removeSquare(d));

        assertEquals(3, navigationMesh.squares.size());

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(1, c.adjacentEdges.size());

        path = PathFindingSystem.findPathThroughSquares(a, 5, 15, c, 25, 15);
        
        assertEquals(3, path.size());

        assertEquals(path.get(0),a);
        assertEquals(path.get(1),b);
        assertEquals(path.get(2),c);
    }

    @Test 
    public void testSameSquareShortestFunnelPath() {
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        // Shortest path is a-b-c, node list should just be start, then end.
        

        Point2D start = new Point2D(9, 9);
        Point2D end = new Point2D(1, 1);

        List<Point2D> nodes = PathFindingSystem.findStringPullPath(start, end, List.of(a));

        assertNotEquals(null, nodes);

        assertEquals(2, nodes.size());
        assertEquals(start, nodes.get(0));
        assertEquals(end, nodes.get(1));
    }

    @Test
    public void testStraightLineShortestFunnelPath() {
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(0, 10);
        b.bottomRight = new Point2D(10, 20);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(0, 20);
        c.bottomRight = new Point2D(10, 30);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(List.of(a, b, c));

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(1, c.adjacentEdges.size());

        // Shortest path is a-b-c, node list should just be start, then end.
        

        Point2D start = new Point2D(5, 5);
        Point2D end = new Point2D(5, 25);

        List<Point2D> nodes = PathFindingSystem.findStringPullPath(start, end, navigationMesh.squares);

        assertNotEquals(null, nodes);

        assertEquals(2, nodes.size());
        assertEquals(start, nodes.get(0));
        assertEquals(end, nodes.get(1));
    }

    @Test
    public void testSingleApexShortestFunnelPath() {
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 3);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(20, 0);
        c.bottomRight = new Point2D(30, 10);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(List.of(a, b, c));

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(1, c.adjacentEdges.size());

        // Find shortest path
        

        Point2D start = new Point2D(9, 0);
        Point2D end = new Point2D(21, 9);

        List<Point2D> nodes = PathFindingSystem.findStringPullPath(start, end, navigationMesh.squares);

        assertNotEquals(null, nodes);

        assertEquals(3, nodes.size());
        assertEquals(start, nodes.get(0));
        assertEquals(new Point2D(20, 3), nodes.get(1));
        assertEquals(end, nodes.get(2));
    }

    @Test
    public void testAroundCornerShortestFunnelPath() {
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 3);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(20, 0);
        c.bottomRight = new Point2D(30, 10);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(List.of(a, b, c));

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(1, c.adjacentEdges.size());

        // Find shortest path
        

        Point2D start = new Point2D(9, 9);
        Point2D end = new Point2D(21, 9);

        List<Point2D> nodes = PathFindingSystem.findStringPullPath(start, end, List.of(a, b, c));

        assertNotEquals(null, nodes);

        assertEquals(4, nodes.size());
        assertEquals(start, nodes.get(0));
        assertEquals(new Point2D(10, 3), nodes.get(1));
        assertEquals(new Point2D(20, 3), nodes.get(2));
        assertEquals(end, nodes.get(3));

        start = new Point2D(21, 9);
        end = new Point2D(9, 9);

        nodes = PathFindingSystem.findStringPullPath(start, end, List.of(c, b, a));

        assertNotEquals(null, nodes);

        assertEquals(4, nodes.size());
        assertEquals(start, nodes.get(0));
        assertEquals(new Point2D(20, 3), nodes.get(1));
        assertEquals(new Point2D(10, 3), nodes.get(2));
        assertEquals(end, nodes.get(3));
    }

    @Test
    public void testFullCircleShortestPath() {
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(-20, 10);
        b.bottomRight = new Point2D(5, 20);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(-25, 0);
        c.bottomRight = new Point2D(-15, 10);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(-20, -10);
        d.bottomRight = new Point2D(5, 0);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(List.of(a, b, c, d));

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(2, c.adjacentEdges.size());
        assertEquals(2, d.adjacentEdges.size());

        // Find shortest path
        

        Point2D start = new Point2D(5, 5);

        List<Point2D> nodes = PathFindingSystem.findStringPullPath(start, start, List.of(a, b, c, d, a));

        assertNotEquals(null, nodes);

        assertEquals(6, nodes.size());
        assertEquals(start, nodes.get(0));
        assertEquals(new Point2D(0, 10), nodes.get(1));
        assertEquals(new Point2D(-15, 10), nodes.get(2));
        assertEquals(new Point2D(-15, 0), nodes.get(3));
        assertEquals(new Point2D(0, 0), nodes.get(4));
        assertEquals(start, nodes.get(5));
    }

    @Test
    public void testWideSquareShortestPath() {
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 0);
        b.bottomRight = new Point2D(20, 100);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(20, 0);
        c.bottomRight = new Point2D(30, 10);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(List.of(a, b, c));

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(1, c.adjacentEdges.size());

        // Find shortest path
        

        Point2D start = new Point2D(0, 0);
        Point2D end = new Point2D(30, 0);

        List<Point2D> nodes = PathFindingSystem.findStringPullPath(start, end, List.of(a, b, c));

        assertNotEquals(null, nodes);

        assertEquals(2, nodes.size());
        assertEquals(start, nodes.get(0));
        assertEquals(end, nodes.get(1));
    }

    @Test
    public void testStraightPathAcrossManySquares() {
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 20);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(10, 10);
        b.bottomRight = new Point2D(20, 30);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(20, 20);
        c.bottomRight = new Point2D(30, 40);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(30, 30);
        d.bottomRight = new Point2D(40, 50);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(List.of(a, b, c, d));

        assertEquals(1, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(2, c.adjacentEdges.size());
        assertEquals(1, d.adjacentEdges.size());

        // Find shortest path
        

        Point2D start = new Point2D(9, 18);
        Point2D end = new Point2D(31, 32);

        List<Point2D> nodes = PathFindingSystem.findStringPullPath(start, end, List.of(a, b, c, d));

        assertNotEquals(null, nodes);

        assertEquals(2, nodes.size());
        assertEquals(start, nodes.get(0));
        assertEquals(end, nodes.get(1));
    }

    @Test
    public void testInvalidSquarePath() {
        Point2D start = new Point2D(0, 0);
        Point2D end = new Point2D(0, 1);

        // Find shortest path
        

        List<Point2D> nodes = PathFindingSystem.findStringPullPath(start, end, new ArrayList<>());
        assertEquals(null, nodes);

        nodes = PathFindingSystem.findStringPullPath(start, end, null);
        assertEquals(null, nodes);

        nodes = PathFindingSystem.findStringPullPath(start, end, List.of(new NavigationSquare(new Point2D(5, 5), new Point2D(10, 10))));
        assertEquals(null, nodes);
    }

    @Test
    public void testFindFullPath() {
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(-20, 10);
        b.bottomRight = new Point2D(5, 20);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(-25, 0);
        c.bottomRight = new Point2D(-15, 10);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(-20, -10);
        d.bottomRight = new Point2D(5, 0);

        NavigationSquare e = new NavigationSquare();
        e.topLeft = new Point2D(-35, -9);
        e.bottomRight = new Point2D(-25, 1);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(List.of(a, b, c, d, e));

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(3, c.adjacentEdges.size());
        assertEquals(2, d.adjacentEdges.size());
        assertEquals(1, e.adjacentEdges.size());

        // Find shortest path
        

        Point2D start = new Point2D(0, 0);
        Point2D end = new Point2D(0, 1);

        List<Point2D> nodes = PathFindingSystem.findPath(start, end, navigationMesh);

        assertNotEquals(null, nodes);
        assertEquals(2, nodes.size());
        assertEquals(start, nodes.get(0));
        assertEquals(end, nodes.get(1));

        end = new Point2D(-20, 5);

        nodes = PathFindingSystem.findPath(start, end, navigationMesh);

        assertNotEquals(null, nodes);
        assertEquals(3, nodes.size());
        assertEquals(start, nodes.get(0));
        assertEquals(new Point2D(-15, 0), nodes.get(1));
        assertEquals(end, nodes.get(2));

        end = new Point2D(-30, -5);

        nodes = PathFindingSystem.findPath(start, end, navigationMesh);

        assertNotEquals(null, nodes);
        assertEquals(3, nodes.size());
        assertEquals(start, nodes.get(0));
        assertEquals(new Point2D(-25, 0), nodes.get(1));
        assertEquals(end, nodes.get(2));
    }

    @Test
    public void findInvalidFullPath() {
        //Mesh generation
        NavigationSquare a = new NavigationSquare();
        a.topLeft = new Point2D(0, 0);
        a.bottomRight = new Point2D(10, 10);

        NavigationSquare b = new NavigationSquare();
        b.topLeft = new Point2D(-20, 10);
        b.bottomRight = new Point2D(5, 20);

        NavigationSquare c = new NavigationSquare();
        c.topLeft = new Point2D(-25, 0);
        c.bottomRight = new Point2D(-15, 10);

        NavigationSquare d = new NavigationSquare();
        d.topLeft = new Point2D(-20, -10);
        d.bottomRight = new Point2D(5, 0);

        NavigationSquare e = new NavigationSquare();
        e.topLeft = new Point2D(-35, -9);
        e.bottomRight = new Point2D(-26, 1);

        NavigationMesh navigationMesh = NavigationMesh.generateMesh(List.of(a, b, c, d, e));

        assertEquals(2, a.adjacentEdges.size());
        assertEquals(2, b.adjacentEdges.size());
        assertEquals(2, c.adjacentEdges.size());
        assertEquals(2, d.adjacentEdges.size());
        assertEquals(0, e.adjacentEdges.size());

        // Find shortest path
        

        Point2D start = new Point2D(0, 0);
        Point2D end = new Point2D(-30, -5);

        List<Point2D> nodes = PathFindingSystem.findPath(start, end, navigationMesh);

        assertEquals(null, nodes);
    }
}
