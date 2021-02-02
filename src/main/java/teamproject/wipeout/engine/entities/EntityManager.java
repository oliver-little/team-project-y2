package teamproject.wipeout.engine.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import teamproject.wipeout.engine.components.GameComponent;
import teamproject.wipeout.engine.entities.events.EntityChangeData;
import teamproject.wipeout.engine.entities.events.EntityChangeEvent;
import teamproject.wipeout.util.BasicEvent;

/** 
 * Central storage location for all entities in a scene.
 * This class also notifies observer 
 */
public class EntityManager {
    public BasicEvent<EntityChangeData> entityEvent;

    // This is a nested map: the first layer maps an entity UUID, and the
    // second layer maps a component type string to a component.
    private Map<String, Map<String, GameComponent>> _entityComponentMap;


    public EntityManager() {
        this.entityEvent = new BasicEvent<EntityChangeData>();
        this._entityComponentMap = new HashMap<String, Map<String, GameComponent>>();
    }

    public String createEntity() {
        return UUID.randomUUID().toString();
    }

    public GameComponent getComponent(String entityID, String componentType) {
        Map<String, GameComponent> map = this._entityComponentMap.get(entityID);

        if (map != null) {
            return map.get(componentType);
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
    public <T extends GameComponent> boolean addComponent(String entityID, T component) {
        Map<String, GameComponent> map = this._entityComponentMap.get(entityID);

        if (map == null) {
            map = new HashMap<String, GameComponent>();
            this._entityComponentMap.put(entityID, map);
        }
        if (!map.containsKey(component.type)) {
            map.put(component.type, component);

            this.entityEvent.emit(new EntityChangeEvent("COMPONENT_ADDED", entityID, this._entityComponentMap.get(entityID).keySet()));

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
    public GameComponent removeComponent(String entityID, String componentType) {
        Map<String, GameComponent> map = this._entityComponentMap.get(entityID);

        if (map != null) {
            GameComponent removed = map.remove(componentType);

            if (removed != null) {
                this.entityEvent.emit(new EntityChangeEvent("COMPONENT_REMOVED", entityID, this._entityComponentMap.get(entityID).keySet()));
            }
            return removed;
        }
        return null;
    }

    public void removeEntity(String entityID) {
        this._entityComponentMap.remove(entityID);
        this.entityEvent.emit(new EntityChangeEvent("ENTITY_REMOVED", entityID, null));
    }
}
