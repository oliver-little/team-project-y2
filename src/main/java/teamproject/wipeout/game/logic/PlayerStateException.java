package teamproject.wipeout.game.logic;

import java.io.ObjectStreamException;

/**
 * Custom {@code PlayerStateException} for cases when a {@link PlayerState} serialization goes wrong.
 *
 * @see ObjectStreamException
 */
public class PlayerStateException extends ObjectStreamException {

    public PlayerStateException(String message) {
        super(message);
    }

}
