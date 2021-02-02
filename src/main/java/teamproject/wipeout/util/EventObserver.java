package teamproject.wipeout.util;

public interface EventObserver<T> {
    public void eventCallback(T eventData);
}
