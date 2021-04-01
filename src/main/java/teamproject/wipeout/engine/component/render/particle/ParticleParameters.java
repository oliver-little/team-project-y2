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
    public static final int DEFAULT_MAX_PARTICLES = 1000;

    public enum ParticleSimulationSpace {
        LOCAL,
        WORLD
    }

    private ParticleType emissionType;

    private boolean emissionEnabled = false;

    private double runtime;
    private boolean loop;

    private int emissionRate = 0;
    private double invEmissionRate;

    private Point2D emissionArea;

    private boolean burstsEnabled = false;

    private ParticleSimulationSpace simulationSpace;

    private List<ParticleBurst> bursts;

    private Supplier<Double> lifetime;

    private Supplier<Double> width;

    private Supplier<Double> height;

    private Supplier<Double> opacity;

    private Supplier<Point2D> velocity;

    private int maxParticles = DEFAULT_MAX_PARTICLES;

    private List<ParticleUpdateFunction> updaters;

    public ParticleParameters(double runtime, boolean loop, ParticleType emissionType, ParticleSimulationSpace simulationSpace, Supplier<Double> lifetime, Supplier<Double> width, Supplier<Double> height, Supplier<Double> opacity, Supplier<Point2D> velocity) {
        this.runtime = runtime;
        this.loop = loop;
        
        this.simulationSpace = simulationSpace;
        this.emissionType = emissionType;
        this.lifetime = lifetime;
        this.width = width;
        this.height = height;
        this.opacity = opacity;
        this.velocity = velocity;

        this.updaters = new ArrayList<>();
    }

    /**
     * Gets the runtime of this particle effect
     * @return The runtime (s)
     */
    public double getRuntime() {
        return this.runtime;
    }

    /**
     * Sets the runtime of this particle effect
     * @param runtime The new runtime (s)
     */
    public void setRuntime(double runtime) {
        this.runtime = runtime;
    }

    /**
     * Whether this particle effect should loop when its runtime ends
     * @return A boolean value representing if the particle effect should loop
     */
    public boolean doesLoop() {
        return this.loop;
    }

    /**
     * Sets whether this particle effect should loop when its runtime ends
     * @param loop The new value for looping
     */
    public void setLooping(boolean loop) {
        this.loop = loop;
    }

    /**
     * Gets the ParticleType object representing what this particle effect should emit
     * @return A particle type object
     */
    public ParticleType getEmissionType() {
        return this.emissionType;
    }

    /** 
     * Changes the ParticleType object this particle effect should emit
     * @param type The new particle type object
     */
    public void setEmissionType(ParticleType type) {
        this.emissionType = type;
    }

    /**
     * Gets the simulation space of this particle effect.
     * @return The simulation space of the particle (local or world)
     */
    public ParticleSimulationSpace getSimulationSpace() {
        return this.simulationSpace;
    }

    /**
     * Sets the simulation space for this particle effect
     * @param simulationSpace The new simulation space
     */
    public void setSimulationSpace(ParticleSimulationSpace simulationSpace) {
        this.simulationSpace = simulationSpace;
    }

    /**
     * Gets the generator for the starting lifetime of individual particles in the effect
     * @return The generator function for lifetime values
     */
    public Supplier<Double> getLifetime() {
        return this.lifetime;
    }

    /**
     * Sets the generator for the starting lifetime of individual particles
     * @param lifetime The new generator to use
     */
    public void setLifetime(Supplier<Double> lifetime) {
        this.lifetime = lifetime;
    }

    /**
     * Gets the generator for the starting width of particles in the effect
     * @return The generator for width values
     */
    public Supplier<Double> getWidth() {
        return this.width;
    }

    /**
     * Sets the generator for the starting width of particles in the effect
     * @param width The new generator to use
     */
    public void setWidth(Supplier<Double> width) {
        this.width = width;
    }

    /**
     * Gets the generator for the starting height of particles in the effect
     * @return The generator for height values
     */
    public Supplier<Double> getHeight() {
        return this.height;
    }

    /**
     * Sets the generator for the starting height of particles in the effect
     * @param width The new generator to use
     */
    public void setHeight(Supplier<Double> height) {
        this.height = height;
    }

    /**
     * Gets the generator for the starting opacity of particles in the effect
     * @return The generator for opacity values
     */
    public Supplier<Double> getOpacity() {
        return this.opacity;
    }

    /**
     * Sets the generator for the opacity of particles in the effect
     * @param width The new generator to use
     */
    public void setOpacity(Supplier<Double> opacity) {
        this.opacity = opacity;
    }

    /**
     * Sets the generator for the starting velocity of particles in the effect
     * @param width The new generator to use
     */
    public Supplier<Point2D> getVelocity() {
        return this.velocity;
    }

    /**
     * Sets the generator for the starting velocity of particles in the effect
     * @param width The new generator to use
     */
    public void setVelocity(Supplier<Point2D> velocity) {
        this.velocity = velocity;
    }

    /**
     * Gets the area over which particles should be emitted (local to the transform of this object)
     * @return A Point2D object representing the width and height of the box to generate particles in
     */
    public Point2D getEmissionArea() {
        return this.emissionArea;
    }

    /**
     * Sets the area over which particles should be emitted
     * @param emissionArea A Point2D object representing the width and height of the box to generate particles in
     */
    public void setEmissionArea(Point2D emissionArea) {
        this.emissionArea = emissionArea;
    }

    /**
     * Whether this particle system should emit particles regularly (number per second)
     * @return A boolean object representing if emission is enabled
     */
    public boolean getEmissionEnabled() {
        return this.emissionEnabled;
    }

    /**
     * Gets the emission rate of this object
     * @return The emission rate (particles per second)
     */
    public int getEmissionRate() {
        return this.emissionRate;
    }

    /**
     * Gets the number of seconds between each emission 
     * This is derived from the emission rate
     * @return The number of seconds between each emission
     */
    public double getSecsPerEmission() {
        return this.invEmissionRate;
    }

    /**
     * Sets the emission rate of this object
     * If the emission rate is null or < 0, emission will be disabled
     * @param emissionRate The new emission rate
     */
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

    /**
     * Whether this particle effect should emit bursts during its runtime
     * @return A boolean representing whether bursts should occur
     */
    public boolean getBurstsEnabled() {
        return this.burstsEnabled;
    }

    /**
     * Gets bursts during the effect's duration
     * @return The list of ParticleBurst objects representing bursts to occurs
     */
    public List<ParticleBurst> getBursts() {
        return this.bursts;
    }

    /**
     * Sets the bursts that should occur during this particle effect's duration
     * If the list is null or has no elements, bursts will be disabled
     * @param bursts The new list of bursts to occur
     */
    public void setBursts(List<ParticleBurst> bursts) {
        this.bursts = bursts;

        if (bursts == null || bursts.size() == 0) {
            this.burstsEnabled = false;
        }
        else {
            this.burstsEnabled = true;
        }
    }

    /**
     * Gets the max number of particles that can exist in the effect at once.
     * If the limit is reached, no particles will be generated until old ones disappear
     * @return The maximum number of particles (by default 1000)
     */
    public int getMaxParticles() {
        return this.maxParticles;
    }

    /**
     * Sets the maximum number of particles that can exist in the effect.
     * Lower or increase this value to improve performance or improve effect quality.
     * @param maxParticles The new maximum number of particles
     */
    public void setMaxParticles(int maxParticles) {
        this.maxParticles = maxParticles;
    }

    /**
     * Gets the list of update functions to occur on the particles
     * These functions should be used in conjunction with Bezier curves to alter particle attributes based on the original value
     * @return The list of ParticleUpdateFunctions
     */
    public List<ParticleUpdateFunction> getUpdateFunctions() {
        return this.updaters;
    }

    /**
     * Adds a new update function 
     * @param updateFunction The new update function to be called
     */
    public void addUpdateFunction(ParticleUpdateFunction updateFunction) {
        this.updaters.add(updateFunction);
    }

    /**
     * Removes an existing update function from this effect
     * @param updateFunction The update function to be removed
     * @return Whether the update function was removed successfully
     */
    public boolean removeUpdateFunction(ParticleUpdateFunction updateFunction) {
        return this.updaters.remove(updateFunction);
    }
}
