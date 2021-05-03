package teamproject.wipeout.networking.server;

import javafx.collections.MapChangeListener;
import javafx.geometry.Point2D;
import javafx.util.Pair;
import org.junit.jupiter.api.*;
import teamproject.wipeout.game.UI.GameMode;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.client.ServerDiscovery;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import static teamproject.wipeout.networking.server.GameServer.MAX_CONNECTIONS;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameServerTest {
    private static final Integer[] CLIENT_IDs = {0, 1, 2, 3, 4, 5};
    private static final String SERVER_NAME = "TestServer#99";
    private static final int CATCHUP_TIME = 100;

    private GameClient[] gameClients;
    private Player[] clientPlayers;

    private GameServer gameServer;

    private InetSocketAddress serverAddress;

    @BeforeAll
    void initializeGameServer() {
        try {
            SpriteManager spriteManager = new SpriteManager();
            spriteManager.loadSpriteSheet("player/player-one-female-descriptor.json", "player/player-one-female.png");

            GameScene gameScene = new GameScene();

            ServerDiscovery serverDiscovery = new ServerDiscovery();
            serverDiscovery.getAvailableServers().addListener((MapChangeListener.Change<? extends String, ? extends InetSocketAddress> change) -> {
                this.serverAddress = change.getValueAdded();
            });

            this.gameServer = new GameServer(SERVER_NAME, GameMode.TIME_MODE, 1_000);

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
            GameClient client = GameClient.openConnection(this.serverAddress, this.clientPlayers[i].playerName, (c) -> {});

            this.gameClients[i] = client;

            Thread.sleep(CATCHUP_TIME); // time for the client to connect
        }
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        this.gameServer.serverStopping();
        Thread.sleep(CATCHUP_TIME * 4); // time for the clients to disconnect
    }

    @RepeatedTest(5)
    void testClientSearchWithinLimit() {
        try {
            this.gameServer.startClientSearch();
            Assertions.assertTrue(this.gameServer.isSearching.get());

            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                Assertions.assertNotNull(this.gameClients[i]);
            }

            this.gameServer.stopClientSearch();
            Assertions.assertFalse(this.gameServer.isSearching.get());

        } catch (IOException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.getConnectedClients().size());
    }

    @RepeatedTest(5)
    void testClientSearchOutsideLimit() {
        Assertions.assertTrue(CLIENT_IDs.length > MAX_CONNECTIONS,
                "If this condition is not met, this test is pointless");

        Assertions.assertTrue(this.gameServer.isSearching.get());
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.getConnectedClients().size());

        try {
            for (int i = 5; i < CLIENT_IDs.length; ++i) {
                GameClient newClient = GameClient.openConnection(this.serverAddress, this.clientPlayers[i].playerName, (c) -> {});
                Assertions.assertNull(newClient);
            }

        } catch (IOException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.getConnectedClients().size());
    }

    @RepeatedTest(5)
    void testStartingGame() {
        Assertions.assertFalse(this.gameServer.isActive.get());
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.getConnectedClients().size());

        this.gameServer.startGame();

        Assertions.assertTrue(this.gameServer.isActive.get());
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.getConnectedClients().size());
    }

    @RepeatedTest(5)
    void testDisconnectingAllClients() {

        try {
            this.gameServer.serverStopping();

            Thread.sleep(CATCHUP_TIME); // so that the server has time to disconnect clients

        } catch (InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(0, this.gameServer.getConnectedClients().size());
    }

    @RepeatedTest(5)
    void testDisconnectingHalfClients() {
        this.gameServer.stopClientSearch();
        Assertions.assertFalse(this.gameServer.isSearching.get());

        List<GameClientHandler> clients = this.gameServer.getConnectedClients();
        Assertions.assertEquals(MAX_CONNECTIONS, clients.size());

        try {
            this.gameServer.disconnectClient(clients.get(0).clientID);

            Thread.sleep(CATCHUP_TIME * 2); // so that the server has time to disconnect clients
            Assertions.assertEquals(MAX_CONNECTIONS - 1, this.gameServer.getConnectedClients().size());

            this.gameServer.disconnectClient((clients.get(2).clientID));

            Thread.sleep(CATCHUP_TIME * 2); // so that the server has time to disconnect clients
            Assertions.assertEquals(MAX_CONNECTIONS - 2, this.gameServer.getConnectedClients().size());

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(MAX_CONNECTIONS / 2, this.gameServer.getConnectedClients().size());
    }

    @Test
    void testStoppingServer_ZZZ() {
        try {
            this.gameServer.startClientSearch();
            Assertions.assertTrue(this.gameServer.isSearching.get());

            this.gameServer.startGame();
            Assertions.assertTrue(this.gameServer.isActive.get());
            Assertions.assertFalse(this.gameServer.isSearching.get());

            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                Assertions.assertNotNull(this.gameClients[i]);
            }

            this.gameServer.stopServer();

            Thread.sleep(CATCHUP_TIME); // so that the server has time to stop

            Assertions.assertFalse(this.gameServer.isSearching.get());
            Assertions.assertFalse(this.gameServer.isActive.get());
            Assertions.assertEquals(0, this.gameServer.getConnectedClients().size());

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

}