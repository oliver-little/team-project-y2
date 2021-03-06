package teamproject.wipeout.engine.component.physics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;

class MovementTest
{

	@Test
	void testMovementComponentFacings()
	{
		MovementComponent testComponent = new MovementComponent(0f, 0f, 0f, 0f);
		assertSame(testComponent.facingDirection, FacingDirection.NONE);

		testComponent.acceleration = new Point2D(10.0, 0.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, FacingDirection.RIGHT);

		testComponent.acceleration = new Point2D(-10.0, 0.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, FacingDirection.LEFT);

		testComponent.acceleration = new Point2D(0.0, 10.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, FacingDirection.DOWN);

		testComponent.acceleration = new Point2D(0.0, -10.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, FacingDirection.UP);
	}

	@Test
	void testMovementComponentKeepsFacing()
	{
		MovementComponent testComponent = new MovementComponent(0f, 0f, 10f, 0f);
		assertSame(testComponent.facingDirection, FacingDirection.RIGHT);

		testComponent.acceleration = new Point2D(0.0, 0.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, FacingDirection.NONE);
	}

	@Test
	void testMovementComponentMultipleVelocities()
	{
		MovementComponent testComponent = new MovementComponent(0f, 0f, 0f, -10f);
		assertSame(testComponent.facingDirection, FacingDirection.UP);

		testComponent.acceleration = new Point2D(10.0, -10.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, FacingDirection.RIGHT);

		testComponent.acceleration = new Point2D(0.0, -10.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, FacingDirection.UP);

		testComponent.acceleration = new Point2D(10.0, -10.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, FacingDirection.RIGHT);

		testComponent.acceleration = new Point2D(10.0, 0.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, FacingDirection.RIGHT);

		testComponent.acceleration = new Point2D(0.0, 0.0);
		testComponent.updateFacingDirection();
		assertSame(testComponent.facingDirection, FacingDirection.NONE);
	}

}
