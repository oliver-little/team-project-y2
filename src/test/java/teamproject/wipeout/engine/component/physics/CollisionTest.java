package teamproject.wipeout.engine.component.physics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.system.CollisionSystem;

class CollisionTest
{

	@Test
	void testNotIntersects()
	{
    	Point2D minP1 = new Point2D(0,0);
    	Point2D maxP1 = new Point2D(10,10);
    	Point2D minP2 = new Point2D(20,20);
    	Point2D maxP2 = new Point2D(40,40);
		assertFalse(CollisionSystem.intersects(minP1, maxP1, minP2, maxP2));
	}

}
