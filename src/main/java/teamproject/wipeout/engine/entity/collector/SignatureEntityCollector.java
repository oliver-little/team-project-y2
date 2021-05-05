package teamproject.wipeout.engine.entity.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.core.GameScene;

/** 
 * Uses a component signature to determine what entities to collect
 */
public class SignatureEntityCollector extends BaseEntityCollector {

    // Called when an entity is added to the tracking list
    public Consumer<GameEntity> onAdd;

    // Called when an entity is removed from the tracking list
    public Consumer<GameEntity> onRemove;

    protected Set<Class<? extends GameComponent>> signature;
    protected List<Class<? extends GameComponent>> signatureList;
    protected List<GameEntity> entityList;

    /**
     * Creates a new instance of SignatureEntityCollector
     * @param scene The GameScene this EntityCollector is listening to
     * @param signature The component signature to listen for (represented as a set of unique GameComponents)
     */
    public SignatureEntityCollector(GameScene scene, Set<Class<? extends GameComponent>> signature) {
        super(scene);

        // Convert signature to a list internally for faster iteration
        this.signature = signature;
        this.signatureList = new ArrayList<>(signature);

        this.entityList = new ArrayList<GameEntity>();

        // Go through all existing entities once created
        for (GameEntity entity: this.scene.entities) {
            this.addComponent(entity);
        }
    }

    /**
     * Gets the list of entities in the scene that meet the provided signature
     * @return A list of GameEntities
     */
    public List<GameEntity> getEntities() {
        return this.entityList;
    }

    protected void addComponent(GameEntity entity) {
        if (this.testComponent(entity) && !this.entityList.contains(entity)) {
            this.entityList.add(entity);

            if (this.onAdd != null) {
                this.onAdd.accept(entity);
            }
        }
    }

    protected void removeComponent(GameEntity entity) {
        if (!this.testComponent(entity)) {
            this.entityList.remove(entity);

            if (this.onRemove != null) {
                this.onRemove.accept(entity);
            }
        }
    }

    protected void removeEntity(GameEntity entity) {
        this.entityList.remove(entity);

        if (this.onRemove != null) {
            this.onRemove.accept(entity);
        }
    }
    
    /**
     * Tests a component against the provided signature
     * @param entity The GameEntity to test
     * @return Whether the component contains all components in the signature
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
