package teamproject.wipeout.networking.server;

import javafx.collections.MapChangeListener;
import javafx.geometry.Point2D;
import javafx.util.Pair;
import org.junit.jupiter.api.*;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.client.ServerDiscovery;
import teamproject.wipeout.networking.data.GameUpdate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Consumer;

import static teamproject.wipeout.networking.server.GameServer.MAX_CONNECTIONS;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameServerTest {
    private static final Integer[] CLIENT_IDs = {0, 1, 2, 3, 4, 5};
    private static final String SERVER_NAME = "TestServer#99";
    private static final int CATCHUP_TIME = 80;

    private final Consumer<Pair<GameClient, Integer>> farmIDReceived = (farmPair) -> {
        this.playerWaitingForFarmID.getCurrentState().setFarmID(farmPair.getValue());
        try {
            this.clientWaitingForFarmID.send(new GameUpdate(this.playerWaitingForFarmID.getCurrentState()));
            Thread.sleep(CATCHUP_TIME);

        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    };

    private GameClient[] gameClients;
    private Player[] clientPlayers;

    private GameServer gameServer;

    private InetSocketAddress serverAddress;

    private Player playerWaitingForFarmID;
    private GameClient clientWaitingForFarmID;

    @BeforeAll
    void initializeGameServer() {
        try {
            SpriteManager spriteManager = new SpriteManager();
            spriteManager.loadSpriteSheet("player/player-red-descriptor.json", "player/player-red.png");

            GameScene gameScene = new GameScene();

            ServerDiscovery serverDiscovery = new ServerDiscovery();
            serverDiscovery.getAvailableServers().addListener((MapChangeListener.Change<? extends String, ? extends InetSocketAddress> change) -> {
                this.serverAddress = change.getValueAdded();
            });

            this.gameServer = new GameServer(SERVER_NAME);

            this.gameServer.startClientSearch();
            serverDiscovery.startLookingForServers();

            Thread.sleep(ServerDiscovery.REFRESH_DELAY * 2);

            serverDiscovery.stopLookingForServers();

            this.clientPlayers = new Player[CLIENT_IDs.length];
            for (int i = 0; i < CLIENT_IDs.length; i++) {
                Pair<Integer, String> playerInfo = new Pair<Integer, String>(CLIENT_IDs[i], "id_"+i);
                Player player = new Player(gameScene, playerInfo, null, spriteManager, null);
                player.setWorldPosition(new Point2D(i, i));
                this.clientPlayers[i] = player;
            }

        } catch (IOException | InterruptedException | ReflectiveOperationException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @AfterAll
    void stopGameServer() {
        try {
            this.gameServer.stopServer();
        } catch (IOException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @BeforeEach
    void setUp() throws IOException, ClassNotFoundException, InterruptedException {
        this.gameClients = new GameClient[CLIENT_IDs.length];
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            GameClient client = GameClient.openConnection(this.serverAddress, this.clientPlayers[i].playerName);
            client.clockCalibration = (time) -> {};

            this.playerWaitingForFarmID = this.clientPlayers[i];
            this.clientWaitingForFarmID = client;
            this.gameClients[i] = client;

            Thread.sleep(CATCHUP_TIME); // time for the client to connect
        }
        this.gameServer.startGame();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        this.gameServer.serverStopping();
        Thread.sleep(CATCHUP_TIME); // time for the clients to disconnect
    }

    @RepeatedTest(5)
    void testClientSearchWithinLimit() {
        Assertions.assertFalse(this.gameServer.isSearching);
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());

        try {
            this.gameServer.startClientSearch();
            Assertions.assertTrue(this.gameServer.isSearching);

            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                Assertions.assertNotNull(this.gameClients[i]);
            }

            this.gameServer.stopClientSearch();
            Assertions.assertFalse(this.gameServer.isSearching);

        } catch (IOException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());
    }

    @RepeatedTest(5)
    void testClientSearchOutsideLimit() {
        Assertions.assertTrue(CLIENT_IDs.length > MAX_CONNECTIONS,
                "If this condition is not met, this test is pointless");

        Assertions.assertFalse(this.gameServer.isSearching);
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());

        try {
            for (int i = 5; i < CLIENT_IDs.length; ++i) {
                GameClient newClient = GameClient.openConnection(this.serverAddress, this.clientPlayers[i].playerName);
                Assertions.assertNull(newClient);
            }

        } catch (IOException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());
    }

    @RepeatedTest(5)
    void testStartingGame() {
        Assertions.assertTrue(this.gameServer.isActive);
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());
        Assertions.assertEquals(0, this.gameServer.playerStates.get().size());

        this.gameServer.startGame();

        Assertions.assertTrue(this.gameServer.isActive);
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());
    }

    @RepeatedTest(5)
    void testDisconnectingAllClients() {
        Assertions.assertFalse(this.gameServer.isSearching);
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());

        try {
            this.gameServer.serverStopping();

            Thread.sleep(CATCHUP_TIME); // so that the server has time to disconnect clients

        } catch (InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(0, this.gameServer.connectedClients.get().size());
    }

    @RepeatedTest(5)
    void testDisconnectingHalfClients() {
        Assertions.assertFalse(this.gameServer.isSearching);

        List<GameClientHandler> clients = this.gameServer.connectedClients.get();
        Assertions.assertEquals(MAX_CONNECTIONS, clients.size());

        try {
            this.gameServer.disconnectClient(clients.get(0).clientID);
            Assertions.assertEquals(MAX_CONNECTIONS - 1, this.gameServer.connectedClients.get().size());

            Thread.sleep(CATCHUP_TIME); // so that the server has time to disconnect clients

            this.gameServer.disconnectClient((clients.get(2).clientID));
            Thread.sleep(CATCHUP_TIME); // so that the server has time to disconnect clients

            Assertions.assertEquals(MAX_CONNECTIONS - 2, this.gameServer.connectedClients.get().size());

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(MAX_CONNECTIONS / 2, this.gameServer.connectedClients.get().size());
    }

    @Test
    void testStoppingServer_ZZZ() {
        try {
            this.gameServer.startClientSearch();
            Assertions.assertTrue(this.gameServer.isSearching);

            this.gameServer.startGame();
            Assertions.assertTrue(this.gameServer.isActive);

            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                Assertions.assertNotNull(this.gameClients[i]);
            }

            this.gameServer.stopServer();

            Thread.sleep(CATCHUP_TIME); // so that the server has time to stop

            Assertions.assertFalse(this.gameServer.isSearching);
            Assertions.assertFalse(this.gameServer.isActive);
            Assertions.assertEquals(0, this.gameServer.connectedClients.get().size());

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }
    }
}