package teamproject.wipeout.networking.data;

import teamproject.wipeout.networking.state.PlayerState;

import java.io.Serializable;

/**
 * {@code GameUpdate} is a wrapper class for messages(= objects)
 * that are exchanged between a client and a server (implements {@link Serializable} to allow the exchange).
 * <br><br>
 * It encapsulates information about the type of the content (see {@link GameUpdateType}),
 * ID of the client or server that sent the {@code GameUpdate},
 * and the content(= object).
 */
public class GameUpdate implements Serializable {

    public final GameUpdateType type;
    public final Integer originID;
    public final Serializable content;

    /**
     * Default initializer for {@code GameUpdate}
     *
     * @param type     {@link GameUpdateType} of the content(= object)
     * @param originID ID of the client or server
     * @param content  Content(= object, which implements {@link Serializable}) to be sent
     */
    public GameUpdate(GameUpdateType type, Integer originID, Serializable content) {
        this.type = type;
        this.originID = originID;
        this.content = content;
    }

    /**
     * Short message initializer for {@code GameUpdate}. <br>
     * Content will be set to null.
     *
     * @param type     {@link GameUpdateType} of the content(= object)
     * @param originID ID of the client or server
     */
    public GameUpdate(GameUpdateType type, Integer originID) {
        this.type = type;
        this.originID = originID;
        this.content = null;
    }

    /**
     * Initializer which instantiates a {@code GameUpdate} from a given {@link PlayerState}.
     *
     * @param playerState {@link PlayerState} to be sent inside the {@code GameUpdate}
     */
    public GameUpdate(PlayerState playerState) {
        this.type = GameUpdateType.PLAYER_STATE;
        this.originID = playerState.getPlayerID();
        this.content = playerState;
    }

}
