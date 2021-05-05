package teamproject.wipeout.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of the observer pattern
 */
public class BasicEvent<T> {
    private List<EventObserver<T>> observers = new ArrayList<EventObserver<T>>();

    /**
     * Adds an observer to the list of observers of this event
     * @param toAdd The observer to add
     */
    public void addObserver(EventObserver<T> toAdd) {
        this.observers.add(toAdd);
    }

    /**
     * Removes an observer from the list of observers of this event
     * @param toRemove The observer to remove
     */
    public void removeObserver(EventObserver<T> toRemove) {
        this.observers.remove(toRemove);
    }

    /**
     * Notifies all observers that this event has occurred
     * @param data Any relevant data to include with this event emission
     */
    public void emit(T data) {
        for (EventObserver<T> observer : this.observers) {
            observer.eventCallback(data);
        }
    }
}
