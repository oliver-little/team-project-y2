package teamproject.wipeout.networking.state;

import java.io.ObjectStreamException;

/**
 * Custom {@code StateException} for cases when a state serialization goes wrong.
 *
 * @see ObjectStreamException
 */
public class GameEntityStateException extends ObjectStreamException {

    public GameEntityStateException(String message) {
        super(message);
    }

}
