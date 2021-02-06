package teamproject.wipeout.engine.entity.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.core.GameScene;

/** 
 * Uses a component signature to determine what entities to collect
 */
public class SignatureEntityCollector extends BaseEntityCollector {

    protected Set<Class<? extends GameComponent>> _signature;
    protected List<GameEntity> _entityList;

    public SignatureEntityCollector(GameScene scene, Set<Class<? extends GameComponent>> signature) {
        super(scene);

        // Convert signature to a list internally for faster iteration
        this._signature = signature;

        this._entityList = new ArrayList<GameEntity>();
    }

    public List<GameEntity> getEntities() {
        return this._entityList;
    }

    protected void _addComponent(GameEntity entity) {
        if (this._testComponent(entity) && !this._entityList.contains(entity)) {
            this._entityList.add(entity);
        }
    }

    protected void _removeComponent(GameEntity entity) {
        if (!this._testComponent(entity)) {
            this._entityList.remove(entity);
        }
    }

    protected void _removeEntity(GameEntity entity) {
        this._entityList.remove(entity);
    }
    

    private boolean _testComponent(GameEntity entity) {
        for (Class<? extends GameComponent> componentClass : this._signature) {
            if (!entity.hasComponent(componentClass)) {
                return false;
            }
        }
        return true;
    }
}
