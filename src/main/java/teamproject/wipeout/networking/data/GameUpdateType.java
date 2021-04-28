package teamproject.wipeout.networking.data;

import java.io.Serializable;

/**
 * Enum which describes the type of a {@link GameUpdate}.
 * Implements {@link Serializable} so that it can be
 * serialized inside a {@code GameUpdate} instance.
 */
public enum GameUpdateType implements Serializable {
    ACCEPT, DECLINE, CONNECTED, // Types used for establishing a connection
    CLOCK_CALIB, REQUEST,       // Types used for during the game
    DISCONNECT, SERVER_STOP,    // Types used for closing a connection
    WORLD_STATE, PLAYER_STATE, FARM_STATE, FARM_ID, MARKET_STATE, ANIMAL_STATE // Types describing specific state updates
}
