package teamproject.wipeout.game.item;

import teamproject.wipeout.game.item.components.ItemComponent;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines an raw item in the items.json file.
 */
public class RawItem {

    public Integer id;
    public String name;
    public Map<String, Map<String, Object>> components;

    /**
     * Turns the {@code RawItem} to more usable {@code Item}.
     *
     * @return {@link Item} with all components initialized
     * @throws ReflectiveOperationException If components from the JSON file are invalid.
     */
    protected Item initializeRealItem() throws ReflectiveOperationException {
        HashMap<Class<?>, ItemComponent> componentMap = new HashMap<>();

        for (String key : this.components.keySet()) {
            String packageName = ItemComponent.class.getPackageName();
            Class<?> componentClass = Class.forName(packageName + '.' + key);

            Map<String, Object> componentData = components.get(key);
            Constructor<?> componentConstructor = componentClass.getConstructor(Map.class);

            ItemComponent newComponent = (ItemComponent) componentConstructor.newInstance(componentData);
            componentMap.put(componentClass, newComponent);
        }
        return new Item(this.id, this.name, componentMap);
    }

}