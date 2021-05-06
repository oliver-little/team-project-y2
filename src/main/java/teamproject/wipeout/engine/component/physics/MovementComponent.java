package teamproject.wipeout.engine.component.physics;

import javafx.geometry.Point2D;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.util.BasicEvent;

import java.util.function.Consumer;

/**
 * Component to add velocity and acceleration properties to an entity
 */
public class MovementComponent implements GameComponent {

    public static final double ZERO_VELOCITY_THRESHOLD = 25.0;

    /**
     * Point to represent the horizontal and vertical velocity respectively
     */
    public Point2D velocity;
    public double velocityDecayRate = 0.95;
    /**
     * Point to represent the horizontal and vertical acceleration respectively
     */
    public Point2D acceleration;
    public FacingDirection facingDirection;
    public BasicEvent<FacingDirection> facingDirectionChanged;

    public Consumer<Point2D> stopCallback;
    public Consumer<Double> speedMultiplierChanged;

    private double speedMultiplier = 1.0;

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

    public MovementComponent(Point2D velocity, Point2D acceleration, double velocityDecayRate) {
        this.facingDirectionChanged = new BasicEvent<>();
        this.velocity = velocity;
        this.velocityDecayRate = velocityDecayRate;
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

    public MovementComponent(float xVelocity, float yVelocity, float xAcceleration, float yAcceleration, double velocityDecayRate) {
        this.facingDirectionChanged = new BasicEvent<>();
        this.velocity = new Point2D(xVelocity, yVelocity);
        this.velocityDecayRate = velocityDecayRate;
        this.acceleration = new Point2D(xAcceleration, yAcceleration);
        this.facingDirection = FacingDirection.NONE;
        this.updateFacingDirection();
    }

    public void multiplySpeedMultiplierBy(double multiplier) {
        this.speedMultiplier *= multiplier;
        if (this.speedMultiplierChanged != null) {
            this.speedMultiplierChanged.accept(speedMultiplier);
        }
    }

    public void divideSpeedMultiplierBy(double multiplier) {
        this.speedMultiplier /= multiplier;
        if (this.speedMultiplierChanged != null) {
            this.speedMultiplierChanged.accept(speedMultiplier);
        }
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public double getSpeedMultiplier() {
        return this.speedMultiplier;
    }

    /** Update the facing diection every timestep, based on velocity */
    public void updateFacingDirection() {
        double x = this.acceleration.getX();
        double y = this.acceleration.getY();
        FacingDirection old = this.facingDirection;
        if (x != 0 && Math.abs(x) > Math.abs(y)) {
            if (x < 0) {
                this.facingDirection = FacingDirection.LEFT;
            }
            else {
                this.facingDirection = FacingDirection.RIGHT;
            }
        }
        else if (y != 0) {
            if (y < 0) {
                this.facingDirection = FacingDirection.UP;
            }
            else {
                this.facingDirection = FacingDirection.DOWN;
            }
        }
        else if (this.velocity.getX() == 0 && this.velocity.getY() == 0) {
            this.facingDirection = FacingDirection.NONE;
        }

        if (old != this.facingDirection) {
            facingDirectionChanged.emit(this.facingDirection);
        }
    }

    /**
     * Update the velocity based on accelaration
     * @param timestep each timestep when the velocity should get updated
     */
    public void updateVelocity(Double timestep){
        this.velocity = this.velocity.add(this.acceleration.multiply(timestep * this.speedMultiplier));
        this.decayVelocity(timestep);
        this.capVelocity();
    }

    /**
     *  Decay is helping us make the movement smoother
     * @param timestep each timestep when the velocity should get updated
     */
    public void decayVelocity(Double timestep){
        double decay_rate = 1f - Math.min(timestep, 1f);
        this.velocity = this.velocity.multiply(decay_rate * velocityDecayRate);
    }

    /** Cap is making sure that the speed of the object won't become too long with time */
    public void capVelocity(){
        double xVelocity = this.velocity.getX();
        double yVelocity = this.velocity.getY();
        if (this.acceleration.getX() == 0 && Math.abs(xVelocity) < ZERO_VELOCITY_THRESHOLD){
            xVelocity = 0.0;
        }
        if (this.acceleration.getY() == 0 && Math.abs(yVelocity) < ZERO_VELOCITY_THRESHOLD){
            yVelocity = 0.0;
        }
        this.velocity = new Point2D(xVelocity, yVelocity);
    }

    public String getType() {
        return "movement";
    }
}
