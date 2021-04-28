package teamproject.wipeout.networking.state;

import java.io.ObjectStreamException;

/**
 * Custom {@code StateException} for cases when a state serialization goes wrong.
 *
 * @see ObjectStreamException
 */
public class GameEntityStateException extends ObjectStreamException {

    /**
     * Custom {@code StateException} for cases when a state serialization goes wrong.
     *
     * @param message Malformed state description
     */
    public GameEntityStateException(String message) {
        super(message);
    }

}
