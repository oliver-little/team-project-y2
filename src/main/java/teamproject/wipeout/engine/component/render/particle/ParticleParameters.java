package teamproject.wipeout.engine.component.render.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.render.particle.property.ParticleBurst;
import teamproject.wipeout.engine.component.render.particle.property.ParticleType;
import teamproject.wipeout.engine.component.render.particle.type.ParticleUpdateFunction;

/**
 * Data container for parameters describing a particle system
 */
public class ParticleParameters {
    public static final int DEFAULT_MAX_PARTICLES = 100;

    private ParticleType emissionType;

    private boolean emissionEnabled = false;

    // Integer representing emissions per second
    private int emissionRate = 0;
    private double invEmissionRate;

    // Point2D offset representing the area over which particles should be generated
    private Point2D emissionArea;

    private boolean burstsEnabled = false;

    // Array containing an ordered list of burst values: Double = the time (s) to release the particles, Integer = the number of particles to release
    private List<ParticleBurst> bursts;

    // Represents how long each particle can last for
    private Supplier<Double> lifetime;

    // The width of each particle
    private Supplier<Double> width;

    // The height of each particle
    private Supplier<Double> height;

    // The starting opacity of each particle
    private Supplier<Double> opacity;

    // The starting velocity of each particle
    private Supplier<Point2D> velocity;

    // Integer representing the maximum number of particles that can exist in the system at any given time
    private int maxParticles = DEFAULT_MAX_PARTICLES;

    private List<ParticleUpdateFunction> updaters;

    public ParticleParameters(ParticleType emissionType, Point2D emissionArea, Supplier<Double> lifetime, Supplier<Double> width, Supplier<Double> height, Supplier<Double> opacity, Supplier<Point2D> velocity) {
        this.emissionType = emissionType;
        this.emissionArea = emissionArea;
        this.lifetime = lifetime;
        this.width = width;
        this.height = height;
        this.opacity = opacity;
        this.velocity = velocity;

        this.updaters = new ArrayList<>();
    }

    public ParticleType getEmissionType() {
        return this.emissionType;
    }

    public void setEmissionType(ParticleType type) {
        this.emissionType = type;
    }

    public Supplier<Double> getLifetime() {
        return this.lifetime;
    }

    public void setLifetime(Supplier<Double> lifetime) {
        this.lifetime = lifetime;
    }

    public Supplier<Double> getWidth() {
        return this.width;
    }

    public void setWidth(Supplier<Double> width) {
        this.width = width;
    }

    public Supplier<Double> getHeight() {
        return this.height;
    }

    public void setHeight(Supplier<Double> height) {
        this.height = height;
    }

    public Supplier<Double> getOpacity() {
        return this.opacity;
    }

    public void setOpacity(Supplier<Double> opacity) {
        this.opacity = opacity;
    }

    public Supplier<Point2D> getVelocity() {
        return this.velocity;
    }

    public void setVelocity(Supplier<Point2D> velocity) {
        this.velocity = velocity;
    }

    public Point2D getEmissionArea() {
        return this.emissionArea;
    }

    public void setEmissionArea(Point2D emissionArea) {
        this.emissionArea = emissionArea;
    }

    public boolean getEmissionEnabled() {
        return this.emissionEnabled;
    }

    public int getEmissionRate() {
        return this.emissionRate;
    }

    public double getSecsPerEmission() {
        return this.invEmissionRate;
    }

    public void setEmissionRate(int emissionRate) {
        this.emissionRate = emissionRate;

        if (emissionRate > 0) {
            this.invEmissionRate = 1.0 / emissionRate;
            this.emissionEnabled = true;
        }
        else {
            this.emissionEnabled = false;
        }
    }

    public boolean getBurstsEnabled() {
        return this.burstsEnabled;
    }

    public List<ParticleBurst> getBursts() {
        return this.bursts;
    }

    public void setBursts(List<ParticleBurst> bursts) {
        this.bursts = bursts;

        if (bursts == null || bursts.size() == 0) {
            this.burstsEnabled = false;
        }
        else {
            this.burstsEnabled = true;
        }
    }

    public int getMaxParticles() {
        return this.maxParticles;
    }

    public void setMaxParticles(int maxParticles) {
        this.maxParticles = maxParticles;
    }

    public List<ParticleUpdateFunction> getUpdateFunctions() {
        return this.updaters;
    }

    public void addUpdateFunction(ParticleUpdateFunction updateFunction) {
        this.updaters.add(updateFunction);
    }

    public boolean removeUpdateFunction(ParticleUpdateFunction updateFunction) {
        return this.updaters.remove(updateFunction);
    }
}
