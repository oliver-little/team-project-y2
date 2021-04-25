package teamproject.wipeout.networking.state;

import java.io.Serializable;

/**
 * {@code GameEntityState} abstract class represents objects which contain game-critical
 * information about game entities that is needed for their synchronization.
 * <br>
 * {@code GameEntityState} implements {@link Serializable}.
 */
public abstract class GameEntityState implements Serializable {

    // Customized equals() and hashCode() methods to be implemented
    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

}
