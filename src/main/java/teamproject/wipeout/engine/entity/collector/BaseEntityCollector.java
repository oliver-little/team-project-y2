package teamproject.wipeout.engine.entity.collector;

import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.event.EntityChangeData;

public abstract class BaseEntityCollector implements EntityCollector {

    protected GameScene _scene;

    public BaseEntityCollector(GameScene scene) {
        this._scene = scene;

        scene.entityChangeEvent.addObserver(this);
    }

    public void cleanup() {
        this._scene.entityChangeEvent.removeObserver(this);
    }
    
    public void eventCallback(EntityChangeData e) {
        String change = e.getChange();
        GameEntity entity = e.getEntity();
        
        switch (change) {
            case "COMPONENT_ADDED":
                this._addComponent(entity);
                break;
            case "COMPONENT_REMOVED":
                this._removeComponent(entity);
                break;
            case "ENTITY_REMOVED":
                this._removeEntity(entity);
                break;
            default:
                System.out.println("Invalid entity change message:" + change);
                break;
        }
    }

    protected abstract void _addComponent(GameEntity entity);
    protected abstract void _removeComponent(GameEntity entity);
    protected abstract void _removeEntity(GameEntity entity);
}
