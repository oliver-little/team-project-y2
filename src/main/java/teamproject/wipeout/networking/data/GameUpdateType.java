package teamproject.wipeout.networking.data;

import java.io.Serializable;

/**
 * Enum which describes the type of a {@link GameUpdate}.
 * Implements {@link Serializable} so that it can be
 * serialized inside a {@code GameUpdate} instance.
 */
public enum GameUpdateType implements Serializable {
    ACCEPT, DECLINE, CONNECTED,
    PLAYER_STATE, FARM_STATE, FARM_ID, MARKET_STATE, ANIMAL_STATE,
    CLOCK_CALIB, REQUEST, RESPONSE,
    DISCONNECT
}
