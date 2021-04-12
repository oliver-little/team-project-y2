package teamproject.wipeout.util;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.PlayerAnimatorComponent;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.shape.Rectangle;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.gameclock.ClockSystem;
import teamproject.wipeout.engine.input.InputKeyAction;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.client.NewPlayerAction;
import teamproject.wipeout.networking.client.NewServerDiscovery;
import teamproject.wipeout.networking.client.ServerDiscovery;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.server.GameServer;
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

    protected ServerDiscovery serverDiscovery;
    protected GameClient client;

    private WorldEntity worldEntity;

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
            boolean started = this.serverRunner.startServer(serverName);
            if (started) {
                return new InetSocketAddress(InetAddress.getLocalHost(), GameServer.GAME_PORT);
            }

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

    public NewPlayerAction onPlayerConnection(GameScene gameScene, SpriteManager spriteManager) {
        return (newPlayerState) -> {
            if (newPlayerState.getPlayerID().equals(this.worldEntity.myPlayer.playerID)) {
                return null;
            }

            Player newPlayer = new Player(gameScene, newPlayerState.getPlayerID(), newPlayerState.getPlayerName(), newPlayerState.getPosition(), null, spriteManager);

            newPlayer.addComponent(new HitboxComponent(new Rectangle(5, 0, 24, 33)));
            newPlayer.addComponent(new CollisionResolutionComponent());

            try {
                newPlayer.addComponent(new RenderComponent(new Point2D(0, -3)));
                newPlayer.addComponent(new PlayerAnimatorComponent(
                        spriteManager.getSpriteSet("player-red", "walk-up"),
                        spriteManager.getSpriteSet("player-red", "walk-right"),
                        spriteManager.getSpriteSet("player-red", "walk-down"),
                        spriteManager.getSpriteSet("player-red", "walk-left"),
                        spriteManager.getSpriteSet("player-red", "idle")));

            } catch (Exception e) {
                e.printStackTrace();
            }

            FarmEntity myFarm = this.worldEntity.farms.get(newPlayerState.getFarmID());
            if (myFarm != null) {
                myFarm.assignPlayer(newPlayer.playerID, false, () -> this.client);
            }

            return newPlayer;
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
