package teamproject.wipeout.engine.component.physics;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.shape.Shape;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.player.AIPlayer;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Component that is used resolve collisions between all entities with this component.
 * Has a boolean flag isMoveable that indicates whether an entity should be affected by collisions.
 */
public class CollisionResolutionComponent implements GameComponent {

	/**
	 * Boolean flag that indicates whether this entity should be affected by collisions
	 */
	private final boolean isMoveable;

	private boolean deactivateOnCollision;
	private int timeMultiplier;

	private final Function<Point2D, Consumer<Pair<Integer, Runnable>>> onCollision;
	private final ScheduledExecutorService avoidanceExecutorService;

	public CollisionResolutionComponent() {
		this.isMoveable = true;

		this.deactivateOnCollision = true;
		this.timeMultiplier = 0;

		this.onCollision = null;
		this.avoidanceExecutorService = null;
	}

	public CollisionResolutionComponent(boolean isMoveable, Function<Point2D, Consumer<Pair<Integer, Runnable>>> onCollision) {
		this.isMoveable = isMoveable;

		this.deactivateOnCollision = false;
		this.timeMultiplier = 1;

		this.onCollision = onCollision;
		this.avoidanceExecutorService = Executors.newSingleThreadScheduledExecutor();
	}

	public void cleanup() {
		if (this.avoidanceExecutorService != null) {
			this.avoidanceExecutorService.shutdown();
		}
	}

	public String getType() {
		return "collisionResolution";
	}

	public void resetControlVariables() {
		this.deactivateOnCollision = false;
		this.timeMultiplier = 1;
	}

	public static void resolveCollision(GameEntity g1, GameEntity g2, ArrayList<Pair<Shape, Shape>> p) {
		for (Pair<Shape, Shape> shapePair : p) {
			resolveCollision(g1, g2, shapePair);
		}
	}
    
    private static void resolveCollision(GameEntity g1, GameEntity g2, Pair<Shape, Shape> p) {
    	Point2D resolutionVector = GeometryUtil.getResolutionVector(p.getKey(), p.getValue());
    	if (resolutionVector == null) {
    		return;
    	}

    	Transform t1 = g1.getComponent(Transform.class);
    	CollisionResolutionComponent c1 = g1.getComponent(CollisionResolutionComponent.class);
    	
    	Transform t2 = g2.getComponent(Transform.class);
    	CollisionResolutionComponent c2 = g2.getComponent(CollisionResolutionComponent.class);
    	    	
    	
    	if (c1.isMoveable) {
        	if (c2.isMoveable) {
        		t1.setPosition(t1.getPosition().add(resolutionVector.multiply(0.5)));
        		t2.setPosition(t2.getPosition().add(resolutionVector.multiply(-0.5)));
				processOnCollision(c2, resolutionVector);

        	} else {
        		t1.setPosition(t1.getPosition().add(resolutionVector));
        	}
			processOnCollision(c1, resolutionVector);

    	} else if (c2.isMoveable) {
    		t2.setPosition(t2.getPosition().add(resolutionVector.multiply(-1)));
    		processOnCollision(c2, resolutionVector);
    	}
    }

    private static void processOnCollision(CollisionResolutionComponent c, Point2D resolutionVector) {
		if (c.onCollision != null && !c.deactivateOnCollision) {
			c.deactivateOnCollision = true;
			Consumer<Pair<Integer, Runnable>> task = c.onCollision.apply(editVector(resolutionVector));

			long delay = AIPlayer.COLLISION_RESOLUTION_TIME * c.timeMultiplier;
			c.timeMultiplier += 1;

			Runnable blockingTask = () -> {
				c.deactivateOnCollision = false;
				Runnable resetMultiplier = () -> c.timeMultiplier = 1;
				task.accept(new Pair<Integer, Runnable>(c.timeMultiplier, resetMultiplier));
			};

			c.avoidanceExecutorService.schedule(blockingTask, delay, TimeUnit.MILLISECONDS);
		}
	}

	private static Point2D editVector(Point2D vector) {
		int vectorX = 0;
		if (vector.getX() > 0.0) {
			vectorX = 1;
		} else if (vector.getX() < 0.0) {
			vectorX = -1;
		}

		int vectorY = 0;
		if (vector.getY() > 0.0) {
			vectorY = 1;
		} else if (vector.getY() < 0.0) {
			vectorY = -1;
		}

		return new Point2D(vectorX, vectorY);
	}

}

