package teamproject.wipeout.networking.state;

import java.io.ObjectStreamException;

/**
 * Custom {@code StateException} for cases when a state serialization goes wrong.
 *
 * @see ObjectStreamException
 */
public class StateException extends ObjectStreamException {

    public StateException(String message) {
        super(message);
    }

}
