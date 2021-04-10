package teamproject.wipeout.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ObjectPool<T> {

    private Supplier<T> factory;
    private List<T> availableObjects;

    /**
     * Creates an instance of ObjectPool
     * @param factory A factory function to generate new instances of the pooled object if none are available
     */
    public ObjectPool(Supplier<T> factory) {
        this.factory = factory;
        this.availableObjects = new ArrayList<T>();
    }

    /**
     * Gets an instance of the pooled object, or null if no instances are available
     * @return A pooled object instance
     */
    public T getInstance() {
        if (this.availableObjects.size() > 0) {
            return availableObjects.remove(this.availableObjects.size() - 1);
        }
        else {
            return factory.get();
        }
    }

    /**
     * Returns an instance of the pooled object to the 
     * @param object
     */
    public void returnInstance(T object) {
        this.availableObjects.add(object);
    }
}
