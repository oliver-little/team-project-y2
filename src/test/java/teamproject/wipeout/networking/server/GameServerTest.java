package teamproject.wipeout.networking.server;

import javafx.geometry.Point2D;
import org.junit.jupiter.api.*;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.client.ServerDiscovery;
import teamproject.wipeout.networking.data.GameUpdate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/*@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameServerTest {

    private static final Integer[] CLIENT_IDs = {0, 1, 2, 3, 4, 5, 6};
    private static final String SERVER_NAME = "TestServer#99";
    private static final int CATCHUP_TIME = 50;
    private static final int MAX_CONNECTIONS = 6;

    private GameClient[] gameClients;
    private PlayerState[] clientPlayerStates;

    private GameServer gameServer;

    private InetSocketAddress serverAddress;

    @BeforeAll
    void initializeGameServer() {
        try {
            this.gameServer = new GameServer(SERVER_NAME);
            this.gameServer.startClientSearch();

            ServerDiscovery serverDiscovery = new ServerDiscovery((name, address) -> {
                this.serverAddress = address;
            });

            serverDiscovery.startLookingForServers();
            Thread.sleep(505);
            serverDiscovery.stopLookingForServers();

            this.gameServer.stopClientSearch();

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
        this.clientPlayerStates = new PlayerState[CLIENT_IDs.length];
        for (int i = 0; i < CLIENT_IDs.length; i++) {
            this.clientPlayerStates[i] = new PlayerState(CLIENT_IDs[i], new Point2D(i, i), Point2D.ZERO);
        }
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            this.gameClients[i] = GameClient.openConnection(this.clientPlayerStates[i], this.serverAddress);
            Thread.sleep(CATCHUP_TIME); // time for the client to connect
        }
        this.gameServer.startNewGame();
    }

    @AfterEach
    void tearDown() throws IOException, InterruptedException {
        this.gameServer.stopGame();
        this.gameServer.disconnectClients();
        Thread.sleep(CATCHUP_TIME); // time for the clients to disconnect
    }

    @RepeatedTest(5)
    void testClientSearchWithinLimit() {
        Assertions.assertFalse(this.gameServer.isSearching.get());
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());

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

        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());
    }

    @RepeatedTest(5)
    void testClientSearchOutsideLimit() {
        Assertions.assertTrue(CLIENT_IDs.length > MAX_CONNECTIONS,
                "If this condition is not met, this test is pointless");

        Assertions.assertFalse(this.gameServer.isSearching.get());
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());

        try {
            this.gameServer.stopGame();
            Assertions.assertFalse(this.gameServer.isActive.get());

            for (int i = 5; i < CLIENT_IDs.length; ++i) {
                GameClient newClient = GameClient.openConnection(this.clientPlayerStates[i], this.serverAddress);
                Assertions.assertNull(newClient);
            }

        } catch (IOException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());
    }

    @RepeatedTest(5)
    void testStartingAndStoppingGame() {
        Assertions.assertFalse(this.gameServer.isSearching.get());
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());

        Assertions.assertTrue(this.gameServer.isActive.get());
        this.gameServer.stopGame();
        Assertions.assertFalse(this.gameServer.isActive.get());
        Assertions.assertEquals(0, this.gameServer.playerStates.get().size());

        this.gameServer.startNewGame();
        Assertions.assertTrue(this.gameServer.isActive.get());

        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());
    }

    @RepeatedTest(5)
    void testFirstStates() {
        Assertions.assertFalse(this.gameServer.isSearching.get());
        Assertions.assertTrue(this.gameServer.isActive.get());
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());

        Collection<PlayerState> playerStates = this.gameServer.playerStates.get().values();
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            PlayerState expectedState = this.clientPlayerStates[i];
            Assertions.assertTrue(playerStates.contains(expectedState));
        }

        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());
    }

    @RepeatedTest(5)
    void testUpdatedStates() {
        Assertions.assertFalse(this.gameServer.isSearching.get());
        Assertions.assertTrue(this.gameServer.isActive.get());
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());

        try {
            HashMap<Integer, Point2D> updatedPositions = new HashMap<Integer, Point2D>();

            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                GameClient chosenClient = this.gameClients[i];
                PlayerState updatedState = this.clientPlayerStates[i];
                updatedState.setPosition(updatedState.getPosition().add(i, i));
                updatedPositions.put(chosenClient.id, updatedState.getPosition());
                chosenClient.send(new GameUpdate(updatedState));

                Thread.sleep(CATCHUP_TIME);
            }

            Collection<PlayerState> playerStates = this.gameServer.playerStates.get().values();

            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                GameClient chosenClient = this.gameClients[i];
                PlayerState originalState = this.clientPlayerStates[i];
                Assertions.assertTrue(playerStates.contains(originalState));
            }

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());
    }

    @RepeatedTest(5)
    void testDisconnectingAllClients() {
        Assertions.assertFalse(this.gameServer.isSearching.get());
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());

        try {
            this.gameServer.disconnectClients();

            Thread.sleep(CATCHUP_TIME); // so that the server has time to disconnect clients

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(0, this.gameServer.connectedClients.get().size());
    }

    @RepeatedTest(5)
    void testDisconnectingHalfClients() {
        Assertions.assertFalse(this.gameServer.isSearching.get());
        Assertions.assertEquals(MAX_CONNECTIONS, this.gameServer.connectedClients.get().size());

        try {
            this.gameServer.disconnectClient(CLIENT_IDs[0], true);
            Assertions.assertEquals(MAX_CONNECTIONS - 1, this.gameServer.connectedClients.get().size());

            this.gameServer.disconnectClient(CLIENT_IDs[1], true);
            Assertions.assertEquals(MAX_CONNECTIONS - 2, this.gameServer.connectedClients.get().size());

            this.gameServer.disconnectClient(CLIENT_IDs[2], true);
            Assertions.assertEquals(MAX_CONNECTIONS - 3, this.gameServer.connectedClients.get().size());

            Thread.sleep(CATCHUP_TIME); // so that the server has time to disconnect clients

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(MAX_CONNECTIONS / 2, this.gameServer.connectedClients.get().size());
    }

    @Test
    void testStoppingServer_ZZZ() {
        try {
            this.gameServer.startClientSearch();
            Assertions.assertTrue(this.gameServer.isSearching.get());

            this.gameServer.startNewGame();
            Assertions.assertTrue(this.gameServer.isActive.get());

            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                Assertions.assertNotNull(this.gameClients[i]);
            }

            this.gameServer.stopServer();

            Thread.sleep(CATCHUP_TIME); // so that the server has time to stop

            Assertions.assertFalse(this.gameServer.isSearching.get());
            Assertions.assertFalse(this.gameServer.isActive.get());
            Assertions.assertEquals(0, this.gameServer.connectedClients.get().size());
            Assertions.assertTrue(this.gameServer.serverSocket.isClosed());

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }
    }
}
 */