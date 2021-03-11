package teamproject.wipeout.networking.data;

import teamproject.wipeout.networking.state.PlayerState;

import java.io.Serializable;

/**
 * {@code GameUpdate} is a wrapper class for messages(= objects)
 * that are exchanged between a client and a server (implements {@link Serializable} to allow the exchange).
 * <br>
 * It encapsulates information about the type of the message (see {@link GameUpdateType}),
 * ID of the machine it was sent from, the message(= object),
 * and the timestamp of when it was sent(/of when the object was created if relevant).
 */
public class GameUpdate implements Serializable {

    public final GameUpdateType type;
    public final Integer originClientID;
    public final Serializable content;
    public final long timestamp;

    /**
     * Default initializer for {@code GameUpdate}
     *
     * @param type     {@link GameUpdateType} of the message(= object)
     * @param clientID ID of the client or server
     * @param content  Message(= object) to be sent which needs to implement {@link Serializable}
     */
    public GameUpdate(GameUpdateType type, Integer clientID, Serializable content) {
        this.type = type;
        this.originClientID = clientID;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Short message initializer for {@code GameUpdate} <br>
     * {@code this.content} will be set to null
     *
     * @param type     {@link GameUpdateType} of the message(= object)
     * @param clientID ID of the client or server
     */
    public GameUpdate(GameUpdateType type, Integer clientID) {
        this.type = type;
        this.originClientID = clientID;
        this.content = null;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Initializer which instantiates a {@code GameUpdate} from a given {@link PlayerState}.
     *
     * @param playerState {@link PlayerState} to be sent
     */
    public GameUpdate(PlayerState playerState) {
        this.type = GameUpdateType.PLAYER_STATE;
        this.originClientID = playerState.getPlayerID();
        this.content = playerState;
        this.timestamp = playerState.getTimestamp();
    }

}
