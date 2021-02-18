package teamproject.wipeout.networking.data;

import java.io.Serializable;

/**
 * Enum which describes the type of a {@link GameUpdate}.
 * Implements {@link Serializable} so that it can be
 * serialized inside a {@code GameUpdate} instance.
 */
public enum GameUpdateType implements Serializable {
    ACCEPT, DECLINE, PLAYER_STATE, REQUEST, RESPONSE, DISCONNECT
}