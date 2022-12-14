package teamproject.wipeout.engine.system.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.render.particle.*;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters.ParticleSimulationSpace;
import teamproject.wipeout.engine.component.render.particle.type.Particle;
import teamproject.wipeout.engine.component.render.particle.type.ParticleUpdateFunction;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.FunctionalSignatureCollector;
import teamproject.wipeout.engine.system.GameSystem;
import teamproject.wipeout.util.ObjectPool;

/**
 * Creates and manages particle emitters for entities with particle components.
 */
public class ParticleSystem implements GameSystem {
    
    public FunctionalSignatureCollector entityCollector;
    public Map<GameEntity, ParticleRenderable> trackedEntities;

    private ObjectPool<Particle> particlePool;

    /**
     * Creates a new instance of ParticleSystem
     * @param scene The scene this particle system is part of
     */
    public ParticleSystem(GameScene scene) {
        entityCollector = new FunctionalSignatureCollector(scene, Set.of(Transform.class, ParticleComponent.class), add, remove, remove);
        trackedEntities = new HashMap<>();

        particlePool = new ObjectPool<Particle>(() -> {return new Particle();});
    }

    /**
     * Cleans up the entity collector when the ParticleSystem is destroyed
     */
    public void cleanup() {
        entityCollector.cleanup();
    }

    public void accept(Double timeStep) {
        ArrayList<ParticleComponent> toStop = new ArrayList<>();
        for (Map.Entry<GameEntity, ParticleRenderable> entry : trackedEntities.entrySet()) {
            ParticleComponent pc = entry.getKey().getComponent(ParticleComponent.class);
            ParticleRenderable renderable = entry.getValue();

            // Check if we should skip this ParticleComponent - only skip if stopped and no particles left simulating
            if (!pc.isPlaying() && renderable.particles.size() == 0) {
                continue;
            }

            ParticleParameters parameters = pc.parameters;
            
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;

            // Increment particles by timeStep
            int index = 0;
            while (index < renderable.particles.size()) {
                Particle particle = renderable.particles.get(index);
                particle.aliveTime += timeStep;
                double percentage = particle.aliveTime / particle.getLifetime();

                if (percentage > 1) {
                    renderable.particles.remove(index);
                    particlePool.returnInstance(particle);
                }
                else {
                    for (ParticleUpdateFunction function : parameters.getUpdateFunctions()) {
                        function.update(particle, percentage, timeStep);
                    }
                    particle.position = particle.position.add(particle.velocity.multiply(timeStep));
                    if (particle.position.getX() > maxX) {
                        maxX = particle.position.getX();
                    }
                    if (particle.position.getY() > maxY) {
                        maxY = particle.position.getY();
                    }
                    index++;
                }
            }

            renderable.setWidth(maxX);
            renderable.setHeight(maxY);

            // Check if stopped, if so we should only be simulating existing particles, so move on
            if (!pc.isPlaying()) {
                continue;
            }
            
            // Check if this ParticleComponent's runtime has elapsed
            pc.time += timeStep;
            if (pc.time > parameters.getRuntime()) {
                if (parameters.doesLoop()) {
                    pc.time = 0;
                    pc.lastEmissionTime = 0;
                    
                    if (parameters.getBurstsEnabled()) {
                        pc.nextBurst = 0;
                    }
                }
                else {
                    for (Particle p : renderable.particles) {
                        particlePool.returnInstance(p);
                    }
                    renderable.particles.clear();
                    
                    toStop.add(pc);
                    continue;
                }
            }
            
            // Finally, generate new particles as the emission describes
            if (parameters.getEmissionEnabled()) {
                double timeDiff = pc.time - pc.lastEmissionTime;
                if (timeDiff > parameters.getSecsPerEmission()) {
                    int numberOfEmissions = (int) (timeDiff / parameters.getSecsPerEmission());

                    if (numberOfEmissions + renderable.particles.size() > parameters.getMaxParticles()) {
                        numberOfEmissions = parameters.getMaxParticles() - renderable.particles.size();
                    }

                    Transform particleTransform = entry.getKey().getComponent(Transform.class);

                    for (int i = 0; i < numberOfEmissions; i++) {
                        Particle p = initialiseParticle(parameters, particleTransform.getWorldPosition());
                        renderable.particles.add(p);
                    }

                    pc.lastEmissionTime = pc.time;
                }
            }
            // Generate bursts
            if (parameters.getBurstsEnabled() && pc.nextBurst != -1 && parameters.getBursts().get(pc.nextBurst).burstTime < pc.time) {
                int numberOfEmissions = (int) parameters.getBursts().get(pc.nextBurst).burstAmount.get();

                if (numberOfEmissions + renderable.particles.size() > parameters.getMaxParticles()) {
                    numberOfEmissions = parameters.getMaxParticles() - renderable.particles.size();
                }

                Transform particleTransform = entry.getKey().getComponent(Transform.class);

                for (int i = 0; i < numberOfEmissions; i++) {
                    Particle p = initialiseParticle(parameters, particleTransform.getWorldPosition());
                    renderable.particles.add(p);
                }

                // Prepare for next burst
                if (pc.nextBurst == parameters.getBursts().size() - 1) {
                    pc.nextBurst = -1;
                }
                else {
                    pc.nextBurst++;
                }
            }
        }

        // Call stop function after main loop to prevent concurrent modification errors
        while (toStop.size() > 0) {
            toStop.remove(0).stop();
        }
    }

    /**
     * Consumer function called when an entity with a Transform and ParticleComponent is added to the scene
     * @param entity The entity that was added
     */
    public Consumer<GameEntity> add = (entity) -> {
        if (!trackedEntities.containsKey(entity)) {
            if (!entity.hasComponent(RenderComponent.class)) {
                entity.addComponent(new RenderComponent());
            }

            RenderComponent render = entity.getComponent(RenderComponent.class);

            ParticleRenderable particle = new ParticleRenderable();
            render.addRenderable(particle);

            trackedEntities.put(entity, particle);
        }
    };

    /**
     * Consumer function called when an entity that does not meet the requirements has a component removed
     * @param entity The entity that was removed
     */
    public Consumer<GameEntity> remove = (entity) -> {
        if (trackedEntities.containsKey(entity)) {
            if (entity.hasComponent(RenderComponent.class)) {
                RenderComponent render = entity.getComponent(RenderComponent.class);
                ParticleRenderable renderable = trackedEntities.get(entity);
                render.removeRenderable(renderable);

                for (Particle p : renderable.particles) {
                    particlePool.returnInstance(p);
                }

                renderable.particles.clear();

            }
            trackedEntities.remove(entity);
        }
    };

    /**
     * Initialises a new particle from the pool
     * @param parameters The particle parameter object to reference for initialisation
     * @return The newly initialised particle
     */
    private Particle initialiseParticle(ParticleParameters parameters, Point2D particleEntityPos) {
        Point2D startPos = Point2D.ZERO;
        if (parameters.getSimulationSpace() == ParticleSimulationSpace.WORLD) {
            startPos = particleEntityPos;
        }

        Supplier<Point2D> emissionArea = parameters.getEmissionPositionGenerator();
        if (emissionArea != null) {
            startPos = startPos.add(emissionArea.get());
        }

        double width = parameters.getWidth().get();
        double height = width;
        if (parameters.getHeight() != null) {
            height = parameters.getHeight().get();
        }

        Particle p = particlePool.getInstance();
        p.initialise(startPos, parameters.getSimulationSpace(), parameters.getVelocity().get(), parameters.getLifetime().get(), width, height, parameters.getOpacity().get(), parameters.getEmissionType().renderFactory());
        return p;
    }
}
