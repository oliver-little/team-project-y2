package teamproject.wipeout.engine.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.entity.event.EntityChangeEvent;
import teamproject.wipeout.engine.core.GameScene;

public class GameEntity {

    private String _uuid;

    protected GameScene _scene;

    // This maps componentIDs to components
    protected Map<Class<?>, GameComponent> _componentMap;

    public GameEntity(GameScene scene) {
        this._uuid = UUID.randomUUID().toString();
        this._scene = scene;
        this._componentMap = new HashMap<Class<?>, GameComponent>();
    }

    public GameScene getScene() {
        return this._scene;
    }

    public void setScene(GameScene scene) {
        this._scene = scene;
    }

    public String getUUID() {
        return this._uuid;
    }

    public void destroy() {
        this._componentMap = null;
        this._scene.entityChangeEvent.emit(new EntityChangeEvent("ENTITY_REMOVED", this));
    }

    public <T extends GameComponent> boolean hasComponent(Class<T> c) {
        return this._componentMap.containsKey(c);
    }

    public <T extends GameComponent> T getComponent(Class<T> c) {
        if (this._componentMap.containsKey(c)) {
            return (T) this._componentMap.get(c);
        }
        return null;
    }

    /**
     * Adds a new component to an entity
     * 
     * @param {String} The UUID of the entity to add a component to
     * @param {Component} The component to add
     * @return true if the component was added successfully, false if not (because a component of that type already exists)
     */
    public <T extends GameComponent> boolean addComponent(T component) {
        if (!this._componentMap.containsKey(component.getClass())) {
            this._componentMap.put(component.getClass(), component);

            this._scene.entityChangeEvent.emit(new EntityChangeEvent("COMPONENT_ADDED", this));

            return true;
        }
        return false;
    }

    /**
     * Removes a component from an entity
     * 
     * @param {String} The UUID of the entity to remove a component from
     * @param {String} The component type string of the component to remove
     * @return The component that was removed
     */
    public <T extends GameComponent> T removeComponent(Class<T> c) {
        if (this._componentMap.containsKey(c)) {
            T removed = (T) this._componentMap.remove(c);
            this._scene.entityChangeEvent.emit(new EntityChangeEvent("COMPONENT_REMOVED", this));
            return (T) removed;
        }
        return null;
    }
}
