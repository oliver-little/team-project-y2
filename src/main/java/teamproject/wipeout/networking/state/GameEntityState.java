package teamproject.wipeout.networking.state;

import java.io.Serializable;

public abstract class GameEntityState implements Serializable {

    // Customized equals() and hashCode() methods to be implemented
    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

}
