package teamproject.wipeout.engine.component.physics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import teamproject.wipeout.engine.system.CollisionSystem;

class CollisionTest
{

	@Test
	void testRectanglesNotIntersects()
	{
		Rectangle r1 = new Rectangle(0,0,10,10);
		Rectangle r2 = new Rectangle(20,20,10,10);
		assertFalse(CollisionComponent.intersects(r1,r2));
	}

	@Test
	void testCircleAndRectangleIntersect() {
		Rectangle r1 = new Rectangle(0,0,10,10);
		Circle c1 = new Circle(0,0,10);
		assertTrue(CollisionComponent.intersects(c1,r1));
		
		r1 = new Rectangle(0,0,10,10);
		c1 = new Circle(4,4,2);
		//c1 inside r1
		assertTrue(CollisionComponent.intersects(c1,r1));
	}
	
	
	@Test
	void testRectanglesIntersects()
	{
		Rectangle r1 = new Rectangle(10,10,10,10);
		Rectangle r2 = new Rectangle(0,0,10,10);
		//collides at top left corner of r1
		assertTrue(CollisionComponent.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(0,0,9,9);
		//close but not colliding
		assertFalse(CollisionComponent.intersects(r1,r2));
		
		r1 = new Rectangle(12,12,4,4);
		r2 = new Rectangle(10,10,10,10);
		//r1 inside r2
		assertTrue(CollisionComponent.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(12,12,4,4);
		//r2 inside r1
		assertTrue(CollisionComponent.intersects(r1,r2));
				
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(10,10,4,4);
		//collides at bottom right corner of r1
		assertTrue(CollisionComponent.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(0,10,10,5);
		//collides at bottom left corner of r1
		assertTrue(CollisionComponent.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(10,0,4,10);
		//collides at top right corner of r1
		assertTrue(CollisionComponent.intersects(r1,r2));
	}
	
	
	@Test
	void testCirclesIntersect() {
		Circle c1 = new Circle(0,0,10);
		Circle c2 = new Circle(1,1,10);
		assertTrue(CollisionComponent.intersects(c1, c2));
	}
	
	@Test
	void testLinesIntersect() {
		Line l1 = new Line(0,0,10,10);
		Line l2 = new Line(1,1,3,3);
		assertTrue(CollisionComponent.intersects(l1, l2));
		
		
    	l1= new Line(3, 2, 6, 2);
    	l2 = new Line(5, 3, 5, 0);
    	assertTrue(CollisionComponent.intersects(l1, l2));
    	assertEquals(new Point2D(5,2),CollisionComponent.pointOfIntersection(l1, l2));
		
		
		l1 = new Line(1,2,5,3);
		l2 = new Line(2,1,7,5);
		assertTrue(CollisionComponent.intersects(l1, l2));
		
		l1 = new Line(1,2,8,6);
		l2 = new Line(4,1,8,5);
		assertFalse(CollisionComponent.intersects(l1, l2));
		
		
		System.out.println("The test");
		l1 = new Line(1,7,3,1);
		l2 = new Line(1,2,3,6);
		assertTrue(CollisionComponent.intersects(l1, l2));
		assertEquals(new Point2D(2,4),CollisionComponent.pointOfIntersection(l1, l2));
		

		
	}
}
