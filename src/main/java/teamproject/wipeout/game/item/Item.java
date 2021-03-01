package teamproject.wipeout.game.item;

import teamproject.wipeout.game.item.components.ItemComponent;

import java.util.Map;

/**
 * Defines an obtainable item in the game.
 */
public class Item {


    public final int id;
    public final String name;

    protected final Map<Class<?>, ItemComponent> componentMap;

    /**
     * Protected initializer for an {@code Item}
     *
     * @param id ID of the item
     * @param name Name of the item
     * @param componentMap Components of the item
     */
    protected Item(int id, String name, Map<Class<?>, ItemComponent> componentMap) {
        this.id = id;
        this.name = name;
        this.componentMap = componentMap;
    }

    /**
     * Returns whether the item contains a component of a given type.
     *
     * @param component The component type to check for
     * @return Whether the given component type exists in the item
     */
    public <T extends ItemComponent> boolean hasComponent(Class<T> component) {
        return this.componentMap.containsKey(component);
    }

    /**
     * Gets a component in the item.
     *
     * @param component The component type to return
     * @return The instance of the component in the item
     */
    public <T extends ItemComponent> T getComponent(Class<T> component) {
        if (this.componentMap.containsKey(component)) {
            return (T) this.componentMap.get(component);
        }
        return null;
    }

}
