package teamproject.wipeout.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import teamproject.wipeout.engine.components.Component;

public class EntityManager {

    // This is a nested map: the first layer maps a component type string, and the second layer maps an entity UUID string to a component
    private Map<String, Map<String, Component>> componentMap;

    public EntityManager() {
        this.componentMap = new HashMap<String, Map<String, Component>>();
    }

    public String createEntity() {
        return UUID.randomUUID().toString();
    }

    public Set<String> getEntitiesWithComponent(String componentType) {
        Map<String, Component> map = this.componentMap.get(componentType);

        if (map != null) {
            return map.keySet();
        }
        return null;
    }

    public Component getComponent(String entityID, String componentType) {
        Map<String, Component> map = this.componentMap.get(componentType);

        if (map != null) {
            // This typecast is safe because if the map call didn't return null the type must be correct.
            return map.get(entityID);
        }
        return null;
    }

    /**
     *  Adds a new component to an entity
     * 
     *  @param {String} The UUID of the entity to add a component to
     *  @return true if the component was added successfully, false if not (because a component of that type already exists)
     */
    public <T extends Component> boolean addComponent(String entityID, T component) {

        Map<String, Component> map = this.componentMap.get(T.type);

        if (map == null) {
            map = new HashMap<String, Component>();
            this.componentMap.put(T.type, map);
        }
        if (map.get(entityID) == null) {
            map.put(entityID, component);
            return true;
        }
        return false;
    }


    public Component removeComponent(String entityID, String componentType) {
        Map<String, Component> map = this.componentMap.get(componentType);

        if (map != null) {
            // This typecast is safe because if the map call didn't return null the type must be correct. 
            return map.remove(entityID);
        }
        return null;
    }
}
