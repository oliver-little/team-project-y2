package teamproject.wipeout.networking.server;

import teamproject.wipeout.networking.data.GameUpdate;

import java.io.IOException;

/**
 * {@code GameUpdateHandler} is a functional interface representing an action that
 * will be executed when a {@link GameUpdate} object is received.
 * <p>
 * Utilised by {@link teamproject.wipeout.networking.server.GameClientHandler}.
 */
@FunctionalInterface
public interface GameUpdateHandler {

    /**
     * Method representing the action that will be executed when a {@link GameUpdate} object is received.
     *
     * @param update The received {@link GameUpdate}
     * @throws IOException Network problem
     */
    public void updateWith(GameUpdate update) throws IOException;

}

