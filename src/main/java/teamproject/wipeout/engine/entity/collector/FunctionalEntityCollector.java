package teamproject.wipeout.engine.entity.collector;

import java.util.function.Consumer;

import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;

/**
 * Extends BaseEntityCollector to call functions when a change occurs in the scene.
 */
public class FunctionalEntityCollector extends BaseEntityCollector {

    public Consumer<GameEntity> addComponentFunction;
    public Consumer<GameEntity> removeComponentFunction;
    public Consumer<GameEntity> removeEntityFunction;

    /**
     * Creates a new instance of FunctionalEntityCollector
     * @param scene The GameScene this EntityCollector is listening to
     * @param addComponent Function to call when an entity adds a component
     * @param removeComponent Function to call when an entity removes a component
     * @param removeEntity Function to call when an entity is removed from the scene
     */
    public FunctionalEntityCollector(GameScene scene, Consumer<GameEntity> addComponent, Consumer<GameEntity> removeComponent, Consumer<GameEntity> removeEntity) {
        super(scene);
        this.addComponentFunction = addComponent;
        this.removeComponentFunction = removeComponent;
        this.removeEntityFunction = removeEntity;

        // Go through all existing entities once created
        for (GameEntity entity: this.scene.entities) {
            this.addComponent(entity);
        }
    }

    protected void addComponent(GameEntity entity) {
        if (addComponentFunction != null) {
            this.addComponentFunction.accept(entity);
        }
    }

    protected void removeComponent(GameEntity entity) {
        if (removeComponentFunction != null) {
            this.removeComponentFunction.accept(entity);
        }
    }

    protected void removeEntity(GameEntity entity) {
        if (removeEntityFunction != null) {
            this.removeEntityFunction.accept(entity);
        }
    }
}
