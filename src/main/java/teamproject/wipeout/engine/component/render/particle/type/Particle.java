package teamproject.wipeout.engine.component.render.particle.type;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters.ParticleSimulationSpace;

/**
 * Represents an individual particle in a particle system
 */
public class Particle {
    // Position of the particle, relative to the particle system
    public Point2D position;

    public double aliveTime;

    // Velocity of this particle
    public Point2D velocity;

    // The width of this particle
    public double width;

    // The height of this particle
    public double height;

    // The opacity of this particle
    public double opacity;

    private double lifetime;

    private ParticleSimulationSpace simulationSpace;
    private Point2D startPosition;
    private Point2D startVelocity;
    private double startWidth;
    private double startHeight;
    private double startOpacity;

    private ParticleRender renderFunction;

    /**
     * Initialises this particle with a new set of values (required every time this particle is reused)
     * @param position The starting position
     * @param velocity The starting velocity
     * @param width The starting width
     * @param height The starting height
     * @param opacity The starting opacity
     * @param render The render function to use, determines how this particle will appear on the screen
     */
    public void initialise(Point2D position, ParticleSimulationSpace simulationSpace, Point2D velocity, double lifetime, double width, double height, double opacity, ParticleRender render) {
        this.position = position;
        this.startPosition = position;
        this.simulationSpace = simulationSpace;

        this.startVelocity = velocity;
        this.velocity = velocity;

        this.lifetime = lifetime;

        this.startWidth = width;
        this.width = width;
        this.startHeight = height;
        this.height = height;

        this.startOpacity = opacity;
        this.opacity = opacity;

        this.renderFunction = render;
        this.aliveTime = 0;
    }

    /**
     * Gets the simulation space for this particle
     */
    public ParticleSimulationSpace getSimulationSpace() {
        return this.simulationSpace;
    }

    /**
     * Gets the initial value of the lifetime for this particle
     * @return The lifetime value (s)
     */
    public double getLifetime() {
        return this.lifetime;
    }

    /**
     * Gets the initial value of the position for this particle
     * @return A Point2D object representing the initial position
     */
    public Point2D getStartPosition() {
        return this.startPosition;
    }

    /**
     * Gets the initial value of the velocity for this particle
     * @return A Point2D object representing the initial velocity of this particle
     */
    public Point2D getStartVelocity() {
        return this.startVelocity;
    }

    /**
     * Gets the initial width of this particle
     * @return The width (game units)
     */
    public double getStartWidth() {
        return this.startWidth;
    }

    /**
     * Gets the initial height of this particle
     * @return The height (game units)
     */
    public double getStartHeight() {
        return this.startHeight;
    }

    /**
     * Gets the initial opacity of this particle
     * @return The initial opacity
     */
    public double getStartOpacity() {
        return this.startOpacity;
    }

    public void render(GraphicsContext gc, double x, double y, double scale) {
        if (opacity != 1) {
            double globalOpacity = gc.getGlobalAlpha();
            gc.setGlobalAlpha(this.opacity);
            if (this.simulationSpace == ParticleSimulationSpace.LOCAL) {
                this.renderFunction.render(gc, (x + this.position.getX()) * scale, (y + this.position.getY()) * scale, width * scale, height * scale);
            }
            else {
                this.renderFunction.render(gc, this.position.getX() * scale, this.position.getY() * scale, width * scale, height * scale);
            }
            gc.setGlobalAlpha(globalOpacity);
        }
        else {
            if (this.simulationSpace == ParticleSimulationSpace.LOCAL) {
                this.renderFunction.render(gc, (x + this.position.getX()) * scale, (y + this.position.getY()) * scale, width * scale, height * scale);
            }
            else {
                this.renderFunction.render(gc, this.position.getX() * scale, this.position.getY() * scale, width * scale, height * scale);
            }
        }
    }
}
