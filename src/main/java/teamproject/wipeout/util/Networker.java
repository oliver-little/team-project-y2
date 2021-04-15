package teamproject.wipeout.util;

import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.shape.Rectangle;
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
import teamproject.wipeout.networking.server.ServerRunningException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Networker {

    public final GameServerRunner serverRunner;

    public WorldEntity worldEntity;

    protected ServerDiscovery serverDiscovery;
    protected GameClient client;

    public Networker() {
        this.serverRunner = new GameServerRunner();
        try {
            this.serverDiscovery = new ServerDiscovery();

        } catch (UnknownHostException exception) {
            exception.printStackTrace();
        }
    }

    public void setWorldEntity(WorldEntity worldEntity) {
        this.worldEntity = worldEntity;
        this.client.setWorldEntity(worldEntity);
    }

    public ServerDiscovery getServerDiscovery() {
        return this.serverDiscovery;
    }

    public final Supplier<GameClient> clientSupplier = () -> this.client;

    public GameClient getClient() {
        return this.client;
    }

    public InetSocketAddress startServer(String serverName) {
        try {
            short startedOnPort = this.serverRunner.startServer(serverName);
            return new InetSocketAddress(InetAddress.getLocalHost(), startedOnPort);

        } catch (ServerRunningException | IOException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public void stopServer() {
        if (this.serverRunner.isServerActive()) {
            try {
                this.serverRunner.stopServer();

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public NewPlayerAction onPlayerConnection(GameScene gameScene, ItemStore itemStore, SpriteManager spriteManager) {
        return (newPlayerState) -> {
            if (newPlayerState.getPlayerID().equals(this.worldEntity.myCurrentPlayer.playerID)) {
                return null;
            }

            Player newCurrentPlayer = new Player(gameScene, newPlayerState.getPlayerID(), newPlayerState.getPlayerName(), spriteManager, itemStore);
            newCurrentPlayer.setWorldPosition(newPlayerState.getPosition());

            FarmEntity myFarm = this.worldEntity.farms.get(newPlayerState.getFarmID());
            if (myFarm != null) {
                myFarm.assignPlayer(newCurrentPlayer.playerID, false, () -> this.client);
            }

            return newCurrentPlayer;
        };
    }

    public void connectClient(InetSocketAddress address, String clientName, Consumer<Long> gameStart) {
        try {
            this.client = GameClient.openConnection(address, clientName);
            // TODO client can be null
            this.client.clockCalibration = (originalGameStart) -> gameStart.accept(originalGameStart);

        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
    }

}
