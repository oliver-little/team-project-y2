package teamproject.wipeout.engine.component.physics;

import javafx.geometry.Point2D;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.util.BasicEvent;

public class MovementComponent implements GameComponent {

    public Point2D velocity;
    public Point2D acceleration;
    public FacingDirection facingDirection;
    public BasicEvent<FacingDirection> facingDirectionChanged;

    public MovementComponent() {
        this.facingDirectionChanged = new BasicEvent<>();
        this.velocity = Point2D.ZERO;
        this.acceleration = Point2D.ZERO;
        this.facingDirection = FacingDirection.NONE;
    }

    public MovementComponent(Point2D velocity, Point2D acceleration) {
        this.facingDirectionChanged = new BasicEvent<>();
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.facingDirection = FacingDirection.NONE;
        this.updateFacingDirection();
    }

    public MovementComponent(float xVelocity, float yVelocity, float xAcceleration, float yAcceleration) {
        this.facingDirectionChanged = new BasicEvent<>();
        this.velocity = new Point2D(xVelocity, yVelocity);
        this.acceleration = new Point2D(xAcceleration, yAcceleration);
        this.facingDirection = FacingDirection.NONE;
        this.updateFacingDirection();
    }

    /** Update the facing diection every timestep, based on velocity */
    public void updateFacingDirection() {
        double x = this.acceleration.getX();
        double y = this.acceleration.getY();
        FacingDirection old = this.facingDirection;
        if(x > 0f) {
            this.facingDirection = FacingDirection.RIGHT;
        }
        else if(x < 0f) {
            this.facingDirection = FacingDirection.LEFT;
        }
        else if(y > 0f) {
            this.facingDirection = FacingDirection.DOWN;
        }
        else if(y < 0f) {
            this.facingDirection = FacingDirection.UP;
        }
        else if (this.velocity.getX() == 0 && this.velocity.getY() == 0) {
            this.facingDirection = FacingDirection.NONE;
        }

        if (old != this.facingDirection) {
            facingDirectionChanged.emit(this.facingDirection);
        }
    }

    /**
     * Update the velociy based on accelaration
     * @param timestep each timestep when the velocity should get updated
     */
    public void updateVelocity(Double timestep){
        this.velocity = this.velocity.add(this.acceleration.multiply(timestep));
        this.decayVelocity(timestep);
        this.capVelocity();
    }

    /**
     *  Decay is helping us make the movement smoother
     * @param timestep each timestep when the velocity should get updated
     */
    public void decayVelocity(Double timestep){
        Double decay_rate = 1f -Math.min(timestep, 1f);
        this.velocity = this.velocity.multiply(decay_rate*0.95);
    }

    /** Cap is making sure that the speed of the object won't become too long with time */
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
