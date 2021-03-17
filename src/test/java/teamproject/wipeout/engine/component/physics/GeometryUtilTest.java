package teamproject.wipeout.engine.component.physics;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.shape.Circle;
import teamproject.wipeout.engine.component.shape.Rectangle;
import teamproject.wipeout.engine.component.shape.Segment;
import teamproject.wipeout.engine.component.shape.Shape;

class GeometryUtilTest
{

	@Test
	void testCalculateGradient() {
		Segment l = new Segment(0,0,10,0);
		assertEquals(0, l.calculateGradient());
		
		l = new Segment(0,0,0,10);
		assertEquals(Double.MAX_VALUE, l.calculateGradient());
		
		l = new Segment(0,0,10,10);
		assertEquals(1, l.calculateGradient());
		
		l = new Segment(1,1,5,2);
		assertEquals(0.25, l.calculateGradient());
		
		l = new Segment(1,1,2,6);
		assertEquals(5, l.calculateGradient());
		
		//same lines but flipped start and end
		l = new Segment(5,5,8,9);
		Segment l2 = new Segment(8,9,5,5);
		assertEquals(l.calculateGradient(), l2.calculateGradient());
		
		l = new Segment(20,0,10,0);
		assertEquals(0, l.calculateGradient());
		
	}
	
	@Test
	void testCalculateYIntercept() {
		Segment l = new Segment(0,0,10,0);
		assertEquals(0, l.calculateYIntercept());
		
		//l = new Segment(0,0,0,10);
		//the above line does not intercept with the y axis
		
		
		l = new Segment(0,0,10,10);
		assertEquals(0, l.calculateYIntercept());
		
		l = new Segment(1,1,5,2);
		assertEquals(0.75, l.calculateYIntercept());
		
		l = new Segment(1,1,2,6);
		assertEquals(-4, l.calculateYIntercept());
		
	}
	
	@Test
	void testPointOfIntersection() {
		//horizontal lines that dont meet
		Segment l1 = new Segment(0,0,10,0);
		Segment l2 = new Segment(0,10,10,10);
		assertNull(l1.pointOfIntersection(l2));
		
		//horizontal lines that dont meet
		l1 = new Segment(0,0,10,0);
		l2 = new Segment(20,0,10,0);
		assertNull(l1.pointOfIntersection(l2));
		
		//overlapping lines
		l1 = new Segment(0,0,10,10);
		l2 = new Segment(0,0,5,5);
		//not null test because there are infinite points of intersection on overlapping lines
		assertNotNull(l1.pointOfIntersection(l2));
		
		//overlapping lines - l2 inside l1
		l1 = new Segment(0,0,10,10);
		l2 = new Segment(1,1,5,5);
		assertNotNull(l1.pointOfIntersection(l2));
		
		//overlapping lines - l1 inside l2
		l1 = new Segment(5,5,10,10);
		l2 = new Segment(1,1,20,20);
		assertNotNull(l1.pointOfIntersection(l2));
		
		//overlapping lines - segments not meet
		l1 = new Segment(0,0,10,10);
		l2 = new Segment(11,11,20,20);
		assertNull(l1.pointOfIntersection(l2));
		
		
		l1 = new Segment(0,0,1,1);
		l2 = new Segment(0,1,1,0);
		assertEquals(new Point2D(0.5,0.5), l1.pointOfIntersection(l2));
		
	}

	
	@Test
	void testCirclesIntersect() {
		Shape c1 = new Circle(0,0,10);
		Shape c2 = new Circle(1,1,10);
		assertTrue(GeometryUtil.intersects(c1, c2));
		
		c1 = new Circle(0,0,10);
		c2 = new Circle(20,20,5);
		assertFalse(GeometryUtil.intersects(c1, c2));
	}
	
	@Test
	void getDistanceBetweenTwoPoints() {
		Point2D p1 = new Point2D(0,0);
		Point2D p2 = new Point2D(0,0);
		assertEquals(0, GeometryUtil.getDistanceBetweenTwoPoints(p1, p2));
		
		p1 = new Point2D(0,0);
		p2 = new Point2D(1,0);
		assertEquals(1, GeometryUtil.getDistanceBetweenTwoPoints(p1, p2));
		
		p1 = new Point2D(0,0);
		p2 = new Point2D(3,4);
		assertEquals(5, GeometryUtil.getDistanceBetweenTwoPoints(p1, p2));
	}
	
	@Test
	void testCircleAndRectangleIntersect() {
		Rectangle r1 = new Rectangle(0,0,10,10);
		Circle c1 = new Circle(0,0,10);
		assertTrue(GeometryUtil.intersects(c1,r1));
		
		r1 = new Rectangle(0,0,10,10);
		c1 = new Circle(4,4,2);
		//c1 inside r1
		assertTrue(GeometryUtil.intersects(c1,r1));
		
		r1 = new Rectangle(0,0,10,10);
		c1 = new Circle(11,5,2);
		//c1 colliding at r1's right side
		assertTrue(GeometryUtil.intersects(c1,r1));
		
		r1 = new Rectangle(0,0,10,10);
		c1 = new Circle(11,11,2);
		//c1 colliding at r1's bottom right corner
		assertTrue(GeometryUtil.intersects(c1,r1));
	}

	@Test
	void testRectanglesIntersects()
	{
		Shape r1 = new Rectangle(10,10,10,10);
		Shape r2 = new Rectangle(0,0,10,10);
		//collides at top left corner of r1
		assertTrue(GeometryUtil.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(0,0,9,9);
		//close but not colliding
		assertFalse(GeometryUtil.intersects(r1,r2));
		
		r1 = new Rectangle(12,12,4,4);
		r2 = new Rectangle(10,10,10,10);
		//r1 inside r2
		assertTrue(GeometryUtil.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(12,12,4,4);
		//r2 inside r1
		assertTrue(GeometryUtil.intersects(r1,r2));
				
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(10,10,4,4);
		//collides at bottom right corner of r1
		assertTrue(GeometryUtil.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(0,10,10,5);
		//collides at bottom left corner of r1
		assertTrue(GeometryUtil.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(10,0,4,10);
		//collides at top right corner of r1
		assertTrue(GeometryUtil.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(12,12,5,5);
		//collides in middle of left side
		assertTrue(GeometryUtil.intersects(r1,r2));
		assertTrue(GeometryUtil.intersects(r2,r1));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(18,12,5,5);
		//collides in middle of right side
		assertTrue(GeometryUtil.intersects(r1,r2));
		assertTrue(GeometryUtil.intersects(r2,r1));
		
		r1 = new Rectangle(212,100,50,50);
		r2 = new Rectangle(200,110,30,30);
		//collides in middle of right side
		assertTrue(GeometryUtil.intersects(r1,r2));
		assertTrue(GeometryUtil.intersects(r2,r1));
		
		//not intersecting
		r1 = new Rectangle(0,0,10,10);
		r2 = new Rectangle(20,20,10,10);
		assertFalse(GeometryUtil.intersects(r1,r2));
	}
	
	
	@Test
	void testSegmentsIntersect() {
		Segment l1 = new Segment(0,0,10,10);
		Segment l2 = new Segment(1,1,3,3);
		assertTrue(GeometryUtil.intersects(l1, l2));
		
    	l1= new Segment(3, 2, 6, 2);
    	l2 = new Segment(5, 3, 5, 0);
    	assertTrue(GeometryUtil.intersects(l1, l2));
    	assertEquals(new Point2D(5,2),l1.pointOfIntersection(l2));
		
    	System.out.println("\nthe test");
		l1 = new Segment(1,2,5,3);
		l2 = new Segment(2,1,7,5);
		assertTrue(GeometryUtil.intersects(l1, l2));
		
		l1 = new Segment(1,2,8,6);
		l2 = new Segment(4,1,8,5);
		assertFalse(GeometryUtil.intersects(l1, l2));
		
		
		l1 = new Segment(1,7,3,1);
		l2 = new Segment(1,2,3,6);
		assertTrue(GeometryUtil.intersects(l1, l2));
		assertEquals(new Point2D(2,4),l1.pointOfIntersection(l2));
		
	}
	
	@Test
	void testCalculateUnitNormal() {
		Segment l = new Segment(0,0,0,10);
		Point2D n1 = new Point2D(1,0);
		Point2D n2 = new Point2D(-1,0);
		Point2D actual = GeometryUtil.calculateUnitNormal(l);
		System.out.println(actual.toString());
		assertTrue(actual.equals(n1) || actual.equals(n2));
		
		l = new Segment(0,0,10,10);
		n1 = new Point2D(-1,1);
		n1 = n1.multiply(1/Math.sqrt(2));
		n2 = new Point2D(1,-1);
		n2 = n2.multiply(1/Math.sqrt(2));
		actual = GeometryUtil.calculateUnitNormal(l);
		System.out.println("actual: "+actual.toString());
		assertTrue(actual.equals(n1) || actual.equals(n2));
	}
	
	@Test
	void testCalculateDistanceBetweenPointAndSegment() {
		//point on line
		Segment l = new Segment(0,0,10,10);
		Point2D p = new Point2D(1,1);
		assertEquals(0, GeometryUtil.calculateDistanceBetweenPointAndLine(p, l));
		
		l = new Segment(0,0,10,10);
		p = new Point2D(1,0);
		assertTrue(GeometryUtil.approxEquals(Math.sqrt(Math.pow(0.5, 2)*2), GeometryUtil.calculateDistanceBetweenPointAndLine(p, l)));
		
		l = new Segment(5, 5, 5, 10);
		p = new Point2D(3,6);
		assertTrue(GeometryUtil.approxEquals(2, GeometryUtil.calculateDistanceBetweenPointAndLine(p, l)));
		
		l = new Segment(0, 0, 0, 10);
		p = new Point2D(2,6);
		assertTrue(GeometryUtil.approxEquals(2, GeometryUtil.calculateDistanceBetweenPointAndLine(p, l)));
		
		l = new Segment(1, 3, 1, 1);
		p = new Point2D(5,4);
		double actual = GeometryUtil.calculateDistanceBetweenPointAndLine(p, l);
		assertTrue(GeometryUtil.approxEquals(Math.sqrt(17), actual));
		
		

	}
	
	@Test
	void circleResolutionVectorTest() {
		Shape c1 = new Circle(0,0,10);
		Shape c2 = new Circle(18,0,10);
		assertTrue(GeometryUtil.intersects(c1, c2));
		Point2D rv = GeometryUtil.getResolutionVector(c1, c2);
		//System.out.println(rv.toString());
		Point2D expected = new Point2D(2,0);
		assertTrue(rv.equals(expected) || rv.equals(expected.multiply(-1)));

		
		c1 = new Circle(0,0,5);
		c2 = new Circle(3,4,5);
		assertTrue(GeometryUtil.intersects(c1, c2));
		rv = GeometryUtil.getResolutionVector(c1, c2);
		System.out.println(rv.toString());
		expected = new Point2D(3,4);
		assertTrue(rv.equals(expected) || rv.equals(expected.multiply(-1)));
	}
	
	@Test
	void rectangleResolutionVectorTest() {
		Shape r1 = new Rectangle(0,0,10,10);
		Shape r2 = new Rectangle(9,0,10,10);
		assertTrue(GeometryUtil.intersects(r1, r2));
		Point2D rv = GeometryUtil.getResolutionVector(r1, r2);
		//System.out.println(rv.toString());
		Point2D expected = new Point2D(1,0);
		assertTrue(rv.equals(expected) || rv.equals(expected.multiply(-1)));
		
		r1 = new Rectangle(0,0,10,10);
		r2 = new Rectangle(8,9,10,10);
		assertTrue(GeometryUtil.intersects(r1, r2));
		rv = GeometryUtil.getResolutionVector(r1, r2);
		//System.out.println(rv.toString());
		expected = new Point2D(0,1);
		assertTrue(rv.equals(expected) || rv.equals(expected.multiply(-1)));
		
		r1 = new Rectangle(0,0,10,10);
		r2 = new Rectangle(-2,-1,4,5);
		assertTrue(GeometryUtil.intersects(r1, r2));
		rv = GeometryUtil.getResolutionVector(r1, r2);
		//System.out.println(rv.toString());
		expected = new Point2D(2,0);
		assertTrue(rv.equals(expected) || rv.equals(expected.multiply(-1)));
	}
	
	@Test
	void rectangleCircleResolutionVectorTest() {
		Shape r1 = new Rectangle(0,0,10,10);
		Shape c2 = new Circle(14,10,10);
		assertTrue(GeometryUtil.intersects(r1, c2));
		Point2D rv = GeometryUtil.getResolutionVector(r1, c2);
		//System.out.println(rv.toString());
		Point2D expected = new Point2D(6,0);
		assertTrue(rv.equals(expected) || rv.equals(expected.multiply(-1)));

	}

}
