package teamproject.wipeout.networking.data;

import java.io.IOException;

/**
 * {@code GameUpdatable} is a functional interface representing an action that
 * will be called when a {@link GameUpdate} is received.
 * Utilised by {@link teamproject.wipeout.networking.server.GameClientHandler}.
 */
@FunctionalInterface
public interface GameUpdatable {
    /**
     * Method representing the action that will be called when a {@link GameUpdate} is received.
     *
     * @param update The received {@link GameUpdate}
     * @throws IOException Network problem
     */
    void updateWith(GameUpdate update) throws IOException;
}

