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
import teamproject.wipeout.networking.server.GameServerRunner;
import teamproject.wipeout.networking.server.ServerRunningException;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Networker {

    public final GameServerRunner serverRunner;

    public WorldEntity worldEntity;
    public ClockSystem clockSystem;

    protected ServerDiscovery serverDiscovery;
    protected GameClient client;

    public Networker() {
        this.serverRunner = new GameServerRunner();
    }

    public final Supplier<GameClient> clientSupplier = () -> {
        return this.client;
    };

    public GameClient getClient() {
        return this.client;
    }

    public InputKeyAction startServer(String serverName) {
        return () -> {
            if (this.serverRunner.isServerActive()) {
                return;
            }

            try {
                this.serverRunner.startServer(serverName);
                System.out.println("Started server");

            } catch (ServerRunningException | IOException exception) {
                exception.printStackTrace();
            }
        };
    }

    public void stopServer() throws IOException {
        if (this.serverRunner.isServerActive()) {
            this.serverRunner.stopServer();
        }
    }

    public InputKeyAction initiateClient(GameScene gameScene, SpriteManager spriteManager) {
        return () -> {
            try {
                this.serverDiscovery = new ServerDiscovery(this.onServerDiscovery(gameScene, spriteManager));
                this.serverDiscovery.startLookingForServers();

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        };
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

    protected NewServerDiscovery onServerDiscovery(GameScene gameScene, SpriteManager spriteManager) {
        return (name, address) -> {
            this.serverDiscovery.stopLookingForServers();

            Player myPlayer = this.worldEntity.getMyPlayer();
            Market myMarket = this.worldEntity.market.getMarket();

            Consumer<Pair<GameClient, Integer>> farmHandler = (farmPair) -> {
                GameClient currentClient = farmPair.getKey();
                Integer newFarmID = farmPair.getValue();
                myPlayer.client = currentClient;
                this.worldEntity.getMyAnimal().clientSupplier = () -> currentClient;
                currentClient.myAnimal = this.worldEntity.getMyAnimal();
                currentClient.market = this.worldEntity.market.getMarket();

                myMarket.client = currentClient;
                myMarket.setIsLocal(true);
                this.worldEntity.marketUpdater.stop();

                FarmEntity myFarm = this.worldEntity.farms.get(newFarmID);
                this.worldEntity.setMyFarm(myFarm);

                System.out.println("Connected client with ID: " + currentClient.id);

                try {
                    currentClient.send(new GameUpdate(myPlayer.getCurrentState()));
                    currentClient.send(new GameUpdate(GameUpdateType.CLOCK_CALIB, currentClient.id, this.clockSystem.gameStartTime));

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
            }
        };
    }
}
