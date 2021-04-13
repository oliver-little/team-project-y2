package teamproject.wipeout.networking.client;

import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.state.PlayerState;

/**
 * {@code NewPlayerAction} is a functional interface representing an action that will be triggered
 * when a new {@link PlayerState} is detected.
 */
@FunctionalInterface
public interface NewPlayerAction {
    /**
     * Method representing the action that will be triggered
     * when a new player state is detected.
     *
     * @param playerState {@link PlayerState} of the newly detected player
     */
    public Player createWith(PlayerState playerState);
}
