package teamproject.wipeout.util;

/**
 * Interface representing an observer to some BasicEvent
 */
public interface EventObserver<T> {
    /**
     * Callback function to be called when the BasicEvent occurs
     * @param eventData The associated data with this event emission.
     */
    public void eventCallback(T eventData);
}
