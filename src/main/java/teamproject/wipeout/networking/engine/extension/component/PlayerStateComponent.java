package teamproject.wipeout.networking.engine.extension.component;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.game.logic.PlayerState;

/**
 * Wrapper component which contains a {@link PlayerState}
 * of a game entity with this component.
 *
 * @see GameComponent
 * @see teamproject.wipeout.engine.entity.GameEntity
 */
public class PlayerStateComponent implements GameComponent {

    public final PlayerState playerState;

    /**
     * Default initializer for {@code PlayerStateComponent}
     *
     * @param state {@link PlayerState} of the {@code GameEntity} it is being added to.
     */
    public PlayerStateComponent(PlayerState state) {
        this.playerState = state;
    }

    public String getType() {
        return "player-state";
    }

}
