package teamproject.wipeout.networking.engine.extension.system;

import teamproject.wipeout.game.logic.PlayerState;

/**
 * {@code NewPlayerAction} is a functional interface representing an action that will be triggered
 * when a new {@link PlayerState} is detected by {@link PlayerStateSystem}.
 */
@FunctionalInterface
public interface NewPlayerAction {
    /**
     * Method representing the action that will be triggered
     * when a new player state is detected by {@link PlayerStateSystem}.
     *
     * @param playerState {@link PlayerState} of the newly detected player
     */
    void createWith(PlayerState playerState);
}
