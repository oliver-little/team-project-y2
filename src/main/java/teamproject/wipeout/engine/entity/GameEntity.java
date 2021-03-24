package teamproject.wipeout.engine.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import teamproject.wipeout.engine.component.EntityAwareGameComponent;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.entity.event.EntityChangeEvent;
import teamproject.wipeout.engine.core.GameScene;

/**
 * Represents any entity in the GameScene
 */
public class GameEntity {

    private GameEntity parent;
    private List<GameEntity> children;

    private String uuid;

    protected GameScene scene;

    // This maps componentIDs to components
    protected Map<Class<?>, GameComponent> componentMap;

    /**
     * Creates a new instance of GameEntity
     * 
     * @param scene The GameScene this entity is part of
     */
    public GameEntity(GameScene scene) {
        this.uuid = UUID.randomUUID().toString();
        this.componentMap = new HashMap<Class<?>, GameComponent>();
        this.parent = null;
        this.children = new ArrayList<>();
        this.setScene(scene);
    }

    /**
     * Gets the scene this entity is part of
     * 
     * @return The GameScene this entity is in 
     */
    public GameScene getScene() {
        return this.scene;
    }

    /**
     * Changes the scene this entity is part of
     * 
     * @param scene The new scene
     */
    public void setScene(GameScene scene) {
        if (this.scene != null) {
            this.scene.removeEntity(this);
            this.scene.entityChangeEvent.emit(new EntityChangeEvent("ENTITY_REMOVED", this));
        }
        this.scene = scene;
        this.scene.addEntity(this);
        this.scene.entityChangeEvent.emit(new EntityChangeEvent("COMPONENT_ADDED", this));
    }

    /**
     * Gets this entity's UUID
     * 
     * @return The UUID of this entity
     */
    public String getUUID() {
        return this.uuid;
    }

    /**
     * Gets this entity's parent GameEntity
     * 
     * @return The parent GameEntity
     */
    public GameEntity getParent() {
        return this.parent;
    }

    /**
     * Sets the parent entity of this GameEntity
     * 
     * @param newParent The new parent entity 
     */
    public void setParent(GameEntity newParent) {
        if (newParent == this) {
            return;
        }

        if (this.parent != null) {
            this.parent.removeChild(this);
        }

        this.parent = newParent;

        if (this.parent != null) {
            this.parent.addChild(this);
        }
    }

    /**
     * Gets the list of children of this GameEntity
     * 
     * @return The list of GameEntity children
     */
    public List<GameEntity> getChildren() {
        return this.children;
    }

    /**
     * Adds a new child entity to this GameEntity
     * 
     * @param child The child entity to add
     */
    public void addChild(GameEntity child) {
        if (!this.children.contains(child)) {
            this.children.add(child);
        }
    }

    /**
     * Removes a child entity from this GameEntity
     * 
     * @param child The child entity to remove
     * @return Whether the child entity existed as a child of this GameEntity
     */
    public boolean removeChild(GameEntity child) {
        return this.children.remove(child);
    }

    /**
     * Destroys this Entity
     */
    public void destroy() {
        this.componentMap = null;

        for (GameEntity entity : children) {
            entity.destroy();
        }

        this.children = null;
        this.parent = null;

        this.scene.removeEntity(this);
        this.scene.entityChangeEvent.emit(new EntityChangeEvent("ENTITY_REMOVED", this));
    }

    /**
     * Returns whether this entity contains a component of a given type
     * 
     * @param c The component type to check for
     * @return Whether the given component type exists on this entity
     */
    public <T extends GameComponent> boolean hasComponent(Class<T> c) {
        return this.componentMap.containsKey(c);
    }

    /**
     * Gets a component on this entity
     * 
     * @param c The component type to return
     * @return The instance of the component on this entity
     */
    public <T extends GameComponent> T getComponent(Class<T> c) {
        if (this.componentMap.containsKey(c)) {
            return (T) this.componentMap.get(c);
        }
        return null;
    }

    /**
     * Adds a new component to an entity
     * 
     * @param component The component to add
     * @return true if the component was added successfully, false if not (because a component of that type already exists)
     */
    public <T extends GameComponent> boolean addComponent(T component) {
        if (!this.componentMap.containsKey(component.getClass())) {
            this.componentMap.put(component.getClass(), component);

            if (component instanceof EntityAwareGameComponent) {
                EntityAwareGameComponent c = (EntityAwareGameComponent) component;
                c.setEntity(this);
            }

            this.scene.entityChangeEvent.emit(new EntityChangeEvent("COMPONENT_ADDED", this));

            return true;
        }
        return false;
    }

    /**
     * Removes a component from an entity
     * 
     * @param c The component type to remove
     * @return The component that was removed
     */
    public <T extends GameComponent> T removeComponent(Class<T> c) {
        if (this.componentMap.containsKey(c)) {
            T removed = (T) this.componentMap.remove(c);
            this.scene.entityChangeEvent.emit(new EntityChangeEvent("COMPONENT_REMOVED", this));
            return (T) removed;
        }
        return null;
    }
}
