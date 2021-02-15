package teamproject.wipeout.engine.component.physics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import teamproject.wipeout.engine.system.CollisionSystem;

class MovementTest
{

	@Test
	void testMovementComponentFacings()
	{
		MovementComponent testComponent = new MovementComponent(10f, 0f, 0f, 0f);
		assertSame(testComponent.facingDirection, MovementComponent.FacingDirection.RIGHT);

		testComponent.velocity = new Point2D(-10.0, 0.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, MovementComponent.FacingDirection.LEFT);

		testComponent.velocity = new Point2D(0.0, 10.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, MovementComponent.FacingDirection.DOWN);

		testComponent.velocity = new Point2D(0.0, -10.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, MovementComponent.FacingDirection.UP);
	}

	@Test
	void testMovementComponentKeepsFacing()
	{
		MovementComponent testComponent = new MovementComponent(10f, 0f, 0f, 0f);
		assertSame(testComponent.facingDirection, MovementComponent.FacingDirection.RIGHT);

		testComponent.velocity = new Point2D(0.0, 0.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, MovementComponent.FacingDirection.RIGHT);
	}

	@Test
	void testMovementComponentMultipleVelocities()
	{
		MovementComponent testComponent = new MovementComponent(0f, -10f, 0f, 0f);
		assertSame(testComponent.facingDirection, MovementComponent.FacingDirection.UP);

		testComponent.velocity = new Point2D(10.0, -10.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, MovementComponent.FacingDirection.RIGHT);

		testComponent.velocity = new Point2D(0.0, -10.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, MovementComponent.FacingDirection.UP);

		testComponent.velocity = new Point2D(10.0, -10.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, MovementComponent.FacingDirection.RIGHT);

		testComponent.velocity = new Point2D(10.0, 0.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, MovementComponent.FacingDirection.RIGHT);
	}

}
