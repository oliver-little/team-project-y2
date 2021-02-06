package teamproject.wipeout.engine.component.physics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import teamproject.wipeout.engine.system.CollisionSystem;

class CollisionTest
{

	@Test
	void testRectanglesNotIntersects()
	{
		Rectangle r1 = new Rectangle(0,0,10,10);
		Rectangle r2 = new Rectangle(20,20,10,10);
		assertFalse(CollisionSystem.intersects(r1,r2));
	}

	
	@Test
	void testRectanglesIntersects()
	{
		Rectangle r1 = new Rectangle(10,10,10,10);
		Rectangle r2 = new Rectangle(0,0,10,10);
		//collides at top left corner of r1
		assertTrue(CollisionSystem.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(0,0,9,9);
		//close but not colliding
		assertFalse(CollisionSystem.intersects(r1,r2));
		
		r1 = new Rectangle(12,12,4,4);
		r2 = new Rectangle(10,10,10,10);
		//r1 inside r2
		assertTrue(CollisionSystem.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(12,12,4,4);
		//r2 inside r1
		assertTrue(CollisionSystem.intersects(r1,r2));
				
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(10,10,4,4);
		//collides at bottom right corner of r1
		assertTrue(CollisionSystem.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(0,10,10,5);
		//collides at bottom left corner of r1
		assertTrue(CollisionSystem.intersects(r1,r2));
		
		r1 = new Rectangle(10,10,10,10);
		r2 = new Rectangle(10,0,4,10);
		//collides at top right corner of r1
		assertTrue(CollisionSystem.intersects(r1,r2));
	}
	
	
	@Test
	void testCirclesIntersect() {
		Circle c1 = new Circle(0,0,10);
		Circle c2 = new Circle(1,1,10);
		assertTrue(CollisionSystem.intersects(c1, c2));
	}
}
