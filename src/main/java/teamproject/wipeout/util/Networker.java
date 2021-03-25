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

    public WorldEntity worldEntity;

    protected ServerDiscovery serverDiscovery;
    protected GameClient client;

    public Networker() {
        this.serverRunner = new GameServerRunner();
        try {
            this.serverDiscovery = new ServerDiscovery((name, address) -> {
                System.out.println(name);
            });

        } catch (UnknownHostException exception) {
            exception.printStackTrace();
        }
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
            if (newPlayerState.getPlayerID().equals(this.worldEntity.getMyPlayer().playerID)) {
                return null;
            }

            Player newPlayer = gameScene.createPlayer(newPlayerState.getPlayerID(), "NAME", newPlayerState.getPosition(), null);

            newPlayer.addComponent(new HitboxComponent(new Rectangle(5, 0, 24, 33)));
            newPlayer.addComponent(new CollisionResolutionComponent());

            try {
                newPlayer.addComponent(new RenderComponent(new Point2D(0, -32)));
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
            myFarm.assignPlayer(newPlayer.playerID, false, () -> this.client);

            return newPlayer;
        };
    }

    public void connectClient(InetSocketAddress address, String clientName, Consumer<Long> gameStart) {
        try {
            this.client = GameClient.openConnection(address, clientName);
            // TODO client can be null
            this.client.clockCalibration = (originalGameStart) -> {
                //double timeDifference = this.clockSystem.gameStartTime - originalGameStart;
                //this.clockSystem.setTimeDifference(timeDifference);
                gameStart.accept(originalGameStart);
            };

        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
    }

    protected NewServerDiscovery onServerDiscovery(GameScene gameScene, SpriteManager spriteManager) {
        return (name, address) -> {
            /*this.serverDiscovery.stopLookingForServers();

            Player myPlayer = this.worldEntity.getMyPlayer();
            Market myMarket = this.worldEntity.market.getMarket();

            Consumer<Pair<GameClient, Integer>> farmHandler = (farmPair) -> {
                GameClient currentClient = farmPair.getKey();
                Integer newFarmID = farmPair.getValue();

                currentClient.myAnimal = this.worldEntity.getMyAnimal();
                currentClient.market = this.worldEntity.market.getMarket();

                myMarket.setIsLocal(true);
                this.worldEntity.marketUpdater.stop();

                FarmEntity myFarm = this.worldEntity.farms.get(newFarmID);
                this.worldEntity.setMyFarm(myFarm);

                System.out.println("Connected client with ID: " + currentClient.getID());

                try {
                    currentClient.send(new GameUpdate(myPlayer.getCurrentState()));

                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            };

            try {
                this.client = GameClient.openConnection(address, myPlayer, this.worldEntity.farms, farmHandler, this.onPlayerConnection(gameScene, spriteManager));
                this.client.clockCalibration = (originalGameStart) -> {
                    double timeDifference = this.clockSystem.gameStartTime - originalGameStart;
                    this.clockSystem.setTimeDifference(timeDifference);
                };

            } catch (IOException | ClassNotFoundException exception) {
                exception.printStackTrace();
            }*/
        };
    }
}
