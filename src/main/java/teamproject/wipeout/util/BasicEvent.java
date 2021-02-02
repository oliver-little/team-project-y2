package teamproject.wipeout.util;

import java.util.ArrayList;
import java.util.List;

public class BasicEvent<T> {
    private List<EventObserver<T>> observers = new ArrayList<EventObserver<T>>();

    public void addObserver(EventObserver<T> toAdd) {
        this.observers.add(toAdd);
    }

    public void removeObserver(EventObserver<T> toRemove) {
        this.observers.remove(toRemove);
    }

    public void emit(T data) {
        for (EventObserver<T> observer : this.observers) {
            observer.eventCallback(data);
        }
    }
}
