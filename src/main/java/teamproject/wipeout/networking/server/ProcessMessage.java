package teamproject.wipeout.networking.server;

import java.io.Serializable;

/**
 * {@link Serializable} enum representing a {@code String} messages
 * that can be sent to a process running an instance of {@link GameServer}.
 *
 * @see GameServerRunner
 */
public enum ProcessMessage implements Serializable {
    START_GAME("start_game"),
    STOP_SERVER("stop_server"),
    CONFIRMATION("_confirmed");

    public final String rawValue;

    ProcessMessage(String message) {
        this.rawValue = message;
    }

    /**
     * Creates a {@code ProcessMessage} from the given {@code String} raw value.
     *
     * @param rawValue {@code String} value of a {@code ProcessMessage}
     * @return {@code ProcessMessage} or, {@code null} if the raw value does not match the default ones
     */
    public static ProcessMessage fromRawValue(String rawValue) {
        for (ProcessMessage message : values()) {
            if (message.rawValue.equals(rawValue)) {
                return message;
            }
        }
        return null;
    }
}
