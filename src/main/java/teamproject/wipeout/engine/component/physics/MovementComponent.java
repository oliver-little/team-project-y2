package teamproject.wipeout.engine.component.physics;

import javafx.geometry.Point2D;

import teamproject.wipeout.engine.component.GameComponent;

public class MovementComponent implements GameComponent {
    public Point2D velocity;
    public Point2D acceleration;
    public FacingDirection facingDirection;

    public MovementComponent() {
        this.velocity = Point2D.ZERO;
        this.acceleration = Point2D.ZERO;
        this.facingDirection = FacingDirection.UP;
    }

    public MovementComponent(Point2D velocity, Point2D acceleration) {
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.facingDirection = FacingDirection.UP;
    }

    public MovementComponent(float xVelocity, float yVelocity, float xAcceleration, float yAcceleration) {
        this.velocity = new Point2D(xVelocity, yVelocity);
        this.acceleration = new Point2D(xAcceleration, yAcceleration);
        this.facingDirection = FacingDirection.UP;
    }

    public void updateFacingDirection() {
        double xVelocity = this.velocity.getX();
        double yVelocity = this.velocity.getY();
        if(xVelocity > 0f) {
            this.facingDirection = FacingDirection.RIGHT;
        }
        else if(xVelocity < 0f) {
            this.facingDirection = FacingDirection.LEFT;
        }
        else if(yVelocity > 0f) {
            this.facingDirection = FacingDirection.DOWN;
        }
        else if(yVelocity < 0f) {
            this.facingDirection = FacingDirection.UP;
        }
    }

    public void updateVelocity(Double timestep){
        this.velocity = this.velocity.add(this.acceleration.multiply(timestep));
        this.decayVelocity(timestep);
        this.capVelocity();
    }

    public void decayVelocity(Double timestep){
        Double decay_rate = 1f -Math.min(timestep, 1f);
        this.velocity = this.velocity.multiply(decay_rate*0.95);
    }

    public void capVelocity(){
        Double threshold = 25.0;
        Double xVelocity = this.velocity.getX();
        Double yVelocity = this.velocity.getY();
        Double xAcceleration = this.acceleration.getX();
        Double yAcceleration = this.acceleration.getY();
        if (Math.abs(xAcceleration) == 0.0 && Math.abs(xVelocity) < threshold){
            xVelocity = 0.0;
        }
        if (Math.abs(yAcceleration) == 0.0 &&Math.abs(yVelocity) < threshold){
            yVelocity = 0.0;
        }
        this.velocity = new Point2D(xVelocity, yVelocity);
    }

    public String getType() {
        return "movement";
    }
}
