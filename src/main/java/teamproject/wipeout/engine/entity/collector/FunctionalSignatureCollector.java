package teamproject.wipeout.engine.entity.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;

/**
 * EntityCollector that checks entities against a signature, then calls a function if they meet the signature
 */
public class FunctionalSignatureCollector extends FunctionalEntityCollector {
    
    protected Set<Class<? extends GameComponent>> signature;
    protected List<Class<? extends GameComponent>> signatureList;
    
    /**
     * Creates a new instance of FunctionalSignatureCollector
     * @param scene The GameScene this object should check against
     * @param signature The signature to match against
     * @param addComponent A function to call when a component is added
     * @param removeComponent A function to call when a component is removed
     * @param removeEntity A function to call when an entity is removed
     */
    public FunctionalSignatureCollector(GameScene scene, Set<Class<? extends GameComponent>> signature, Consumer<GameEntity> addComponent, Consumer<GameEntity> removeComponent, Consumer<GameEntity> removeEntity) {
        super(scene, addComponent, removeComponent, removeEntity);

        this.signature = signature;
        this.signatureList = new ArrayList<>(signature);
    }

    /** 
     * Called when an entity adds a component
     * 
     * @param entity The entity that added a component
     */
    protected void addComponent(GameEntity entity) {
        if (this.testComponent(entity)) {
            super.addComponent(entity);
        }
    }

    /**
     * Called when an entity removes a component
     * 
     * @param entity The entity that removed a component
     */
    protected void removeComponent(GameEntity entity) {
        if (!this.testComponent(entity)) {
            super.removeComponent(entity);
        }
    }

    /**
     * Called when an entity is removed from the scene
     * 
     * @param entity The entity that was removed
     */
    private boolean testComponent(GameEntity entity) {
        for (Class<? extends GameComponent> componentClass : this.signatureList) {
            if (!entity.hasComponent(componentClass)) {
                return false;
            }
        }
        return true;
    }
}
