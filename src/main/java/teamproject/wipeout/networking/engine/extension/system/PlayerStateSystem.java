package teamproject.wipeout.networking.engine.extension.system;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;
import teamproject.wipeout.game.logic.PlayerState;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.engine.extension.component.PlayerStateComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Subclass of a {@link GameSystem} which both updates player states
 * based on information received from a server, and sends updated
 * {@link PlayerState} of the current(= local) player to the server.
 *
 * @see GameScene
 * @see GameClient
 * @see NewPlayerAction
 */
public class PlayerStateSystem implements GameSystem {

    public static final Set<Class<? extends GameComponent>> playerSignaturePattern = Set.of(
            PlayerStateComponent.class, Transform.class
    );

    protected GameClient client;
    protected final NewPlayerAction newPlayerAction;

    protected final SignatureEntityCollector playerEntityCollector;
    protected final HashSet<String> existingIDs;

    /**
     * Default initializer for {@code PlayerStateSystem} <br>
     * {@code this.client} must be set via {@code .setClient(GameClient)}.
     * Otherwise, {@code PlayerStateSystem} will not work.
     *
     * @param scene           {@link GameScene} running the system
     * @param newPlayerAction Action triggered when a new player(= new {@code PlayerState}) is detected
     */
    public PlayerStateSystem(GameScene scene, NewPlayerAction newPlayerAction) {
        this.newPlayerAction = newPlayerAction;

        this.playerEntityCollector = new SignatureEntityCollector(scene, playerSignaturePattern);
        this.existingIDs = new HashSet<String>();
    }

    /**
     * Setter method for the {@code this.client} variable
     *
     * @param newClient {@link GameClient} which will be connected with this {@code PlayerStateSystem}
     */
    public void setClient(GameClient newClient) {
        this.client = newClient;
    }

    // Called by the GameSystem
    public void cleanup() {
        this.existingIDs.clear();
        this.playerEntityCollector.cleanup();
    }

    // Called by the GameSystem
    public void accept(Double timeStep) {
        if (this.client == null) {
            return;
        }

        List<GameEntity> entities = this.playerEntityCollector.getEntities();
        ArrayList<PlayerState> serverPlayerStates = this.client.getPlayerStates();

        ArrayList<GameEntity> deleteEntities = new ArrayList<GameEntity>();

        // Update existing entities
        for (GameEntity entity : entities) {
            Transform transform = entity.getComponent(Transform.class);
            PlayerState playerState = entity.getComponent(PlayerStateComponent.class).playerState;

            int pIndex = serverPlayerStates.indexOf(playerState);

            if (pIndex < 0 && this.existingIDs.contains(playerState.getID())) {
                deleteEntities.add(entity);
                this.existingIDs.remove(playerState.getID());

            } else if (pIndex >= 0) {
                PlayerState serverPlayerState = serverPlayerStates.get(pIndex);
                this.existingIDs.add(serverPlayerState.getID());

                if (playerState.getPosition().equals(serverPlayerState.getPosition())) {
                    continue;
                }

                if (playerState.getTimestamp() > serverPlayerState.getTimestamp()) {
                    try {
                        this.client.send(playerState);
                    } catch (IOException ignore) {
                    }
                } else {
                    playerState.updatePositionFrom(serverPlayerState);
                    transform.setPosition(serverPlayerState.getPosition());
                }
            }
        }

        // updatedPlayerStates which weren't in any of the entities
        for (PlayerState newPlayerState : serverPlayerStates) {
            if (!this.existingIDs.contains(newPlayerState.getID())) {
                newPlayerAction.createWith(newPlayerState);
            }
        }

        // Destroy old/not-anymore-existing entities (e.g. disconnected players)
        for (GameEntity deletedEntity : deleteEntities) {
            deletedEntity.destroy();
        }
    }

}
