package teamproject.wipeout.networking;

import javafx.util.Pair;
import teamproject.wipeout.GameMode;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.client.NewPlayerAction;
import teamproject.wipeout.networking.client.ServerDiscovery;
import teamproject.wipeout.networking.server.GameServerRunner;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * {@code Networker} object binds together the game with the client-server networking architecture.
 */
public class Networker {

    public final GameServerRunner serverRunner;
    public final Supplier<GameClient> clientSupplier;

    public WorldEntity worldEntity;

    private ServerDiscovery serverDiscovery;
    private GameClient client;

    /**
     * Default initializer for {@code Networker}.
     */
    public Networker() {
        this.serverRunner = new GameServerRunner();
        this.clientSupplier = () -> this.client;

        try {
            this.serverDiscovery = new ServerDiscovery();

        } catch (UnknownHostException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * {@code serverDiscovery} getter
     *
     * @return {@link ServerDiscovery} instance
     */
    public ServerDiscovery getServerDiscovery() {
        return this.serverDiscovery;
    }

    /**
     * {@code client} getter
     *
     * @return Current {@link GameClient} instance
     */
    public GameClient getClient() {
        return this.client;
    }

    /**
     * {@code worldEntity} setter
     *
     * @param worldEntity New {@link WorldEntity} instance
     */
    public void setWorldEntity(WorldEntity worldEntity) {
        this.worldEntity = worldEntity;
        this.client.setWorldEntity(worldEntity);
    }

    /**
     * Starts a server. (= creates a child process and runs the server in it)
     *
     * @param serverName    Your server name
     * @param gameMode      Chosen {@link GameMode}
     * @param gameModeValue {@code long} value for chosen game mode
     * @return {@link InetSocketAddress} of the new server
     */
    public InetSocketAddress startServer(String serverName, GameMode gameMode, long gameModeValue) {
        try {
            short startedOnPort = this.serverRunner.startServer(serverName, gameMode, gameModeValue);
            return new InetSocketAddress(InetAddress.getLocalHost(), startedOnPort);

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    /**
     * Stops the server. Does nothing if no server has been running.
     *
     * @return {@code true} if server was stopped (and child process killed), otherwise {@code false}
     */
    public boolean stopServer() {
        if (this.serverRunner.isServerActive()) {
            try {
                this.serverRunner.stopServer();
                return true;

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Connects you as a client to the given server.
     *
     * @param address    {@link InetSocketAddress} of the game server you want to connect to
     * @param clientName Your name
     * @param startGame  Action that is executed when the gameplay is started by the player hosting the game server
     * @return {@link GameClient} that was created or {@code null} if connection has failed
     */
    public GameClient connectClient(InetSocketAddress address, String clientName, Consumer<GameClient> startGame) {
        try {
            Consumer<GameClient> runOnGameStart = (currentClient) -> startGame.accept(currentClient);

            this.client = GameClient.openConnection(address, clientName, runOnGameStart);

            if (this.client != null) {
                return this.client;
            }

        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    /**
     * Creates an action that handles a new player connection.
     *
     * @param gameScene     Current {@link GameScene}
     * @param itemStore     Current {@link ItemStore}
     * @param spriteManager Current {@link SpriteManager}
     * @return {@link NewPlayerAction} handles a new player connection
     */
    public NewPlayerAction onPlayerConnection(GameScene gameScene, ItemStore itemStore, SpriteManager spriteManager) {
        return (newPlayerState) -> {
            if (newPlayerState.getPlayerID().equals(this.worldEntity.myCurrentPlayer.playerID)) {
                return null;
            }

            Pair<Integer, String> playerInfo = new Pair<Integer, String>(newPlayerState.getPlayerID(), newPlayerState.getPlayerName());
            Player newCurrentPlayer = new Player(gameScene, playerInfo, newPlayerState.getSpriteSheet(), spriteManager, itemStore);
            newCurrentPlayer.setWorldPosition(newPlayerState.getPosition());

            FarmEntity myFarm = this.worldEntity.farms.get(newPlayerState.getFarmID());
            if (myFarm != null) {
                myFarm.assignPlayer(newCurrentPlayer.playerID, false, () -> this.client);
            }

            return newCurrentPlayer;
        };
    }

}
