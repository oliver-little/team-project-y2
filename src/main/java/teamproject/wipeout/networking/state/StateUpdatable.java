package teamproject.wipeout.networking.state;

/**
 * {@code StateUpdatable} is an interface that represents actions which are needed
 * for interactive updates of the game entities.
 */
public interface StateUpdatable<T> {

    /**
     * @return Current state of the entity
     */
    public T getCurrentState();

    /**
     * Method representing the action that will be triggered
     * when an entity needs to be updated based on a given state.
     *
     * @param newState State to be used for the entity update
     */
    public void updateFromState(T newState);

}