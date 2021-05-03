package teamproject.wipeout.networking.client;

import javafx.collections.MapChangeListener;
import javafx.util.Pair;
import org.junit.jupiter.api.*;
import teamproject.wipeout.game.UI.GameMode;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.server.GameServer;
import teamproject.wipeout.util.resources.ResourceLoader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameClientTest {
    private static final Integer CLIENT_ID = 123;
    private static final Integer DUMMY_CLIENT_ID = 999;
    private static final String SERVER_NAME = "TestServer#1";
    private static final int CATCHUP_TIME = 100;

    private SpriteManager spriteManager;
    private GameScene gameScene;

    private GameClient gameClient;
    private Player clientPlayer;

    private GameServer gameServer;
    private InetSocketAddress serverAddress;

    private Player playerWaitingForFarmID;
    private GameClient clientWaitingForFarmID;

    private final Consumer<Pair<GameClient, Integer>> farmIDReceived = (farmPair) -> {
        this.playerWaitingForFarmID.getCurrentState().setFarmID(farmPair.getValue());
        if (this.clientWaitingForFarmID == null) {
            this.gameClient.send(new GameUpdate(this.playerWaitingForFarmID.getCurrentState()));
        } else {
            this.clientWaitingForFarmID.send(new GameUpdate(this.playerWaitingForFarmID.getCurrentState()));
        }
    };

    @BeforeAll
    void initializeGameClient() throws IOException, InterruptedException, ReflectiveOperationException {
        ResourceLoader.setTargetClass(ResourceLoader.class);

        this.spriteManager = new SpriteManager();
        this.spriteManager.loadSpriteSheet("player/player-one-female-descriptor.json", "player/player-one-female.png");

        this.gameScene = new GameScene();
        Pair<Integer, String> playerInfo = new Pair<Integer, String>(CLIENT_ID, "Test");
        this.clientPlayer = new Player(this.gameScene, playerInfo, null, this.spriteManager, null);

        this.gameServer = new GameServer(SERVER_NAME, GameMode.TIME_MODE, 1_000);
        this.gameServer.startClientSearch();

        ServerDiscovery serverDiscovery = new ServerDiscovery();
        serverDiscovery.getAvailableServers().addListener((MapChangeListener.Change<? extends String, ? extends InetSocketAddress> change) -> {
            this.serverAddress = change.getValueAdded();
        });

        serverDiscovery.startLookingForServers();
        Thread.sleep(ServerDiscovery.REFRESH_DELAY);
        serverDiscovery.stopLookingForServers();
    }

    @AfterAll
    void stopGameServer() throws IOException {
        this.gameServer.stopServer();
    }

    @BeforeEach
    void setUp() {
        this.gameClient = null;
        this.playerWaitingForFarmID = this.clientPlayer;
    }

    @AfterEach
    void tearDown() {
        if (this.gameClient.getIsActive()) {
            this.gameClient.closeConnection(true);
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testOpeningConnection() {
        try {
            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer.playerName, null);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            Assertions.assertTrue(this.gameClient.getIsActive(),
                    "The client is not active despite opening a connection.");

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            Set<Integer> states = this.gameClient.connectedClients.keySet();
            Assertions.assertEquals(1, states.size());

        } catch (IOException | InterruptedException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testClosingConnection() {
        try {
            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer.playerName, null);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            Assertions.assertTrue(this.gameClient.getIsActive(),
                    "The client is not active despite opening a connection.");

            this.gameClient.closeConnection(true);
            // The repeated calls must do nothing...
            this.gameClient.closeConnection(true);
            this.gameClient.closeConnection(true);

            Assertions.assertFalse(this.gameClient.getIsActive(),
                    "The client is active despite closing the connection.");
            Assertions.assertTrue(this.gameClient.clientSocket.isClosed(),
                    "The client socket is not closed despite closing the connection.");

        } catch (IOException | ClassNotFoundException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testSendingUpdates() {
        try {
            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer.playerName, null);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            this.gameClient.send(new GameUpdate(this.clientPlayer.getCurrentState()));

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            Set<Integer> states = this.gameClient.connectedClients.keySet();
            Assertions.assertEquals(1, states.size(),
                    "Incorrect number of received player states");

            this.gameClient.closeConnection(true);

            Thread.sleep(CATCHUP_TIME); // time for the client to disconnect
            Assertions.assertTrue(this.gameClient.connectedClients.isEmpty());

            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer.playerName, null);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            states = this.gameClient.connectedClients.keySet();
            Assertions.assertEquals(1, states.size(),
                    "Incorrect number of received player states");

            //this.gameServer.();

        } catch (IOException | InterruptedException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testReceivingOthersDisconnect() {
        try {
            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer.playerName, null);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            Pair<Integer, String> playerInfo = new Pair<Integer, String>(DUMMY_CLIENT_ID, "TestLast");
            Player secondPlayer = new Player(this.gameScene, playerInfo, null, this.spriteManager, null);
            this.playerWaitingForFarmID = secondPlayer;
            GameClient secondClient = GameClient.openConnection(this.serverAddress, secondPlayer.playerName, null);
            this.clientWaitingForFarmID = secondClient;
            Assertions.assertNotNull(secondClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect
            this.playerWaitingForFarmID = this.clientPlayer;
            this.clientWaitingForFarmID = null;

            Set<Integer> players = this.gameClient.connectedClients.keySet();

            Assertions.assertEquals(2, players.size());

            secondClient.closeConnection(true);

            Thread.sleep(CATCHUP_TIME); // time for the client to disconnect

            Set<Integer> disconnectedPlayers = this.gameClient.connectedClients.keySet();

            Assertions.assertEquals(1, disconnectedPlayers.size());

        } catch (IOException | InterruptedException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

}