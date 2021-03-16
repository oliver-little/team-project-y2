package teamproject.wipeout.engine.component.shape;

import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShapeTest {

    @Test
    void testSegmentContains() {
        Point2D p = new Point2D(0,0);
        Segment l = new Segment(0,0,10,10);
        assertTrue(l.contains(p));

        p = new Point2D(1,1);
        l = new Segment(0,0,10,10);
        assertTrue(l.contains(p));

        p = new Point2D(10,10);
        l = new Segment(0,0,10,10);
        assertTrue(l.contains(p));

        p = new Point2D(11,11);
        l = new Segment(0,0,10,10);
        assertFalse(l.contains(p));

        p = new Point2D(1,0);
        l = new Segment(0,0,10,10);
        assertFalse(l.contains(p));

        l = new Segment(1, 3, 1, 1);
        p = new Point2D(1,4);
        assertFalse(l.contains(p));
    }

    @Test
    void testRectangleContains() {
        Point2D p = new Point2D(0,0);
        Rectangle r = new Rectangle(0,0,10,10);
        assertTrue(r.contains(p));

        p = new Point2D(5,4);
        assertTrue(r.contains(p));

        p = new Point2D(10,10);
        assertTrue(r.contains(p));

        p = new Point2D(10,0);
        assertTrue(r.contains(p));

        p = new Point2D(0,10);
        assertTrue(r.contains(p));

        p = new Point2D(10,4);
        assertTrue(r.contains(p));

        p = new Point2D(3,10);
        assertTrue(r.contains(p));

        p = new Point2D(0,0);
        r = new Rectangle(1,1,10,10);
        assertFalse(r.contains(p));

        p = new Point2D(15,5);
        r = new Rectangle(1,1,10,10);
        assertFalse(r.contains(p));

    }

    @Test
    void testCircleContains(){
        Point2D p = new Point2D(0,0);
        Circle c = new Circle(0,0,5);
        assertTrue(c.contains(p));

        p = new Point2D(5,0);
        assertTrue(c.contains(p));

        p = new Point2D(0,5);
        assertTrue(c.contains(p));

        p = new Point2D(6,0);
        assertFalse(c.contains(p));

        p = new Point2D(4,4);
        assertFalse(c.contains(p));

        p = new Point2D(5,10);
        c = new Circle(5,10,3);
        assertTrue(c.contains(p));

        p = new Point2D(6,11);
        assertTrue(c.contains(p));

        p = new Point2D(3,7);
        assertFalse(c.contains(p));
    }


}
