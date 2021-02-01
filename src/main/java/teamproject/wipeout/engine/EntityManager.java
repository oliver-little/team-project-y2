package teamproject.wipeout.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import teamproject.wipeout.engine.components.Component;

public class EntityManager {

    // This is a nested map: the first layer maps an entity UUID, and the
    // second layer maps a component type string to a component.
    private Map<String, Map<String, Component>> _entityComponentMap;

    // This is a map that turns a set of component signatures (component type strings that must all be present in an entity)
    // into a list of entity UUIDs that match that signature
    private Map<Set<String>, List<String>> _signatures;

    public EntityManager() {
        this._entityComponentMap = new HashMap<String, Map<String, Component>>();
    }

    public String createEntity() {
        return UUID.randomUUID().toString();
    }

    public Component getComponent(String entityID, String componentType) {
        Map<String, Component> map = this._entityComponentMap.get(entityID);

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
     * @return true if the component was added successfully, false if not (because acomponent of that type already exists)
     */
    public <T extends Component> boolean addComponent(String entityID, T component) {
        Map<String, Component> map = this._entityComponentMap.get(entityID);

        if (map == null) {
            map = new HashMap<String, Component>();
            this._entityComponentMap.put(entityID, map);
        }
        if (!map.containsKey(component.type)) {
            map.put(component.type, component);
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
    public Component removeComponent(String entityID, String componentType) {
        Map<String, Component> map = this._entityComponentMap.get(entityID);

        if (map != null) {
            Component removed = map.remove(componentType);
            if (removed != null) {
                // Check all signatures to see if this component no longer meets the requirements for it
                for (Entry<Set<String>,List<String>> signatureEntry : this._signatures.entrySet()) {
                    // If the signature contains this component, remove the entity from the list of matching entities.
                    if (signatureEntry.getKey().contains(componentType)) {
                        signatureEntry.getValue().remove(entityID);
                    }
                }
            }
            return removed;
        }
        return null;
    }

    public void removeEntity(String entityID) {
        Set<String> componentStrings = this.getComponentStrings(entityID);

        if (componentStrings.size() > 0) {
            // Iterate over all signatures
            for (Entry<Set<String>,List<String>> signatureEntry : this._signatures.entrySet()) {
                Set<String> duplicate = new HashSet<>(signatureEntry.getKey());
                // Calculate the intersection of the signature and the components
                duplicate.retainAll(componentStrings);

                // If there are no entries left in the signature, this entity meets the signature requirements, so remove it.
                if (duplicate.size() == 0) {
                    signatureEntry.getValue().remove(entityID);
                }
            }
        }
    }

    /**
     * Instructs the EntityManager to maintain a list of entities that match a given
     * component signature. This is a costly operation, call during loading periods
     * if possible.
     * 
     * @param {Set<String>} The component signature to maintain
     */
    public void addSignature(Set<String> componentSignature) {
        if (!this._signatures.containsKey(componentSignature)) {
            List<String> matchingEntities = new ArrayList<String>();
          
            // Construct the list of entities that match the signature
            for (String entityID : this._entityComponentMap.keySet()) {
                // Get the list of components this entity has
                Set<String> componentStrings = this.getComponentStrings(entityID);
                Set<String> signatureDuplicate = new HashSet<>(componentSignature);

                // Calculate the intersection of the signature and the components
                signatureDuplicate.retainAll(componentStrings);

                // If there are no entries left in the signature, this entity meets the signature requirements, so add it.
                if (signatureDuplicate.size() == 0) {
                     matchingEntities.add(entityID);
                }
            }

            this._signatures.put(componentSignature, matchingEntities);  
        }
    }

    /** 
     * Stops a given component signature from being maintained if it exists
     */
    public void removeSignature(Set<String> componentSignature) {
        this._signatures.remove(componentSignature);
    }

    /**
     * Gets a list of entities with all components in a set This is an *expensive*
     * operation if addSignature has not been called previously as the list must be
     * constructed from scratch..
     * 
     * @param {Set<String>} The component signature (set of components) to provide
     * @return The entity UUIDs in the manager matching the signature.
     */
    public List<String> getEntitiesWithComponents(Set<String> componentSignature) {
        List<String> matchingEntities = this._signatures.get(componentSignature);
        if (matchingEntities == null) {
            this.addSignature(componentSignature);
            matchingEntities = this._signatures.get(componentSignature);
        }

        return matchingEntities;
    }

    /**
     * Gets the component strings for a given entityID
     * @param {String} The entity UUID to get
     * @return {Set<String>} A set containing all the components in this object.
     */
    private Set<String> getComponentStrings(String entityID) {
        Set<String> componentStrings = new HashSet<String>();
        this._entityComponentMap.get(entityID).values().stream().forEach(e -> componentStrings.add(e.type));
        return componentStrings;
    }

}
