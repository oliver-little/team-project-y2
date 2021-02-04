package teamproject.wipeout.engine.component.physics;

import javafx.geometry.Point2D;

import teamproject.wipeout.engine.component.GameComponent;

public class PhysicsComponent implements GameComponent {
    
    public Point2D velocity;
    public Point2D acceleration;

    public PhysicsComponent() {
        this.velocity = Point2D.ZERO;
        this.acceleration = Point2D.ZERO;
    }

    public PhysicsComponent(Point2D velocity, Point2D acceleration) {
        this.velocity = velocity;
        this.acceleration = acceleration;
    }

    public PhysicsComponent(float xVelocity, float yVelocity, float xAcceleration, float yAcceleration) {
        this.velocity = new Point2D(xVelocity, yVelocity);
        this.acceleration = new Point2D(xAcceleration, yAcceleration);
    }

    public String getType() {
        return "physics";
    }
}
