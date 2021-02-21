package teamproject.wipeout.networking.client;

import javafx.geometry.Point2D;
import org.junit.jupiter.api.*;
import teamproject.wipeout.game.logic.PlayerState;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.server.GameServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameClientTest {

    private static final String CLIENT_ID = "123-456-789";
    private static final String SERVER_ID = "TestServer#1";
    private static final int CATCHUP_TIME = 50;

    private GameClient gameClient;
    private PlayerState clientPlayerState;

    private GameServer gameServer;
    private InetSocketAddress serverAddress;

    @BeforeAll
    void initializeGameClient() throws IOException, InterruptedException {
        this.clientPlayerState = new PlayerState(CLIENT_ID, Point2D.ZERO);

        this.gameServer = new GameServer(SERVER_ID);
        this.gameServer.startClientSearch();

        ServerDiscovery serverDiscovery = new ServerDiscovery((name, address) -> {
            this.serverAddress = new InetSocketAddress(address, GameServer.GAME_PORT);
        });

        serverDiscovery.startLookingForServers();
        Thread.sleep(505);
        serverDiscovery.stopLookingForServers();
    }

    @AfterAll
    void stopGameServer() throws IOException {
        this.gameServer.stopServer();
    }

    @BeforeEach
    void setUp() {
        this.gameClient = null;
    }

    @AfterEach
    void tearDown() throws IOException {
        if (this.gameClient.getIsActive()) {
            this.gameClient.closeConnection(true);
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testOpeningConnection() {
        try {
            this.gameClient = GameClient.openConnection(this.clientPlayerState, this.serverAddress);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            Assertions.assertTrue(this.gameClient.getIsActive(),
                    "The client is not active despite opening a connection.");

            PlayerState dummyPlayerState = new PlayerState("DummyID", Point2D.ZERO);
            this.gameServer.updateClients(new GameUpdate(dummyPlayerState));
            this.gameServer.updateClients(new GameUpdate(this.clientPlayerState));

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            ArrayList<PlayerState> states = this.gameClient.getPlayerStates();
            Assertions.assertEquals(2, states.size());
            Assertions.assertTrue(states.contains(dummyPlayerState));

        } catch (IOException | InterruptedException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testFailToOpenConnection() throws IOException {
        try {
            this.gameServer.startNewGame(); // will not allow new connections

            this.gameClient = GameClient.openConnection(this.clientPlayerState, this.serverAddress);
            Assertions.assertNull(this.gameClient,
                    "Connected despite the server not allowing connections");

            this.gameServer.stopGame(); // will allow new connections

            this.gameClient = GameClient.openConnection(this.clientPlayerState, this.serverAddress);
            Assertions.assertNotNull(this.gameClient,
                    "Not connected despite the server allowing connections");

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

        } catch (IOException | ClassNotFoundException | InterruptedException exception) {
            this.gameServer.stopGame(); // will allow new connections
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testClosingConnection() {
        try {
            this.gameClient = GameClient.openConnection(this.clientPlayerState, this.serverAddress);
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
    void testReceivingUpdatesOnConnect() {
        try {
            this.gameClient = GameClient.openConnection(this.clientPlayerState, this.serverAddress);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            PlayerState dummyPlayerState1 = new PlayerState("DummyID1", Point2D.ZERO);
            PlayerState dummyPlayerState2 = new PlayerState("DummyID2", Point2D.ZERO);

            this.gameClient.send(new GameUpdate(dummyPlayerState1));
            this.gameClient.send(new GameUpdate(this.clientPlayerState));
            this.gameClient.send(new GameUpdate(dummyPlayerState2));

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            this.gameClient.closeConnection(true);

            Thread.sleep(CATCHUP_TIME); // time for the client to disconnect

            this.gameClient = GameClient.openConnection(this.clientPlayerState, this.serverAddress);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            ArrayList<PlayerState> states = this.gameClient.getPlayerStates();

            Assertions.assertEquals(3, states.size(),
                    "Incorrect number of received player states");
            Assertions.assertTrue(states.contains(dummyPlayerState1),
                    "Incorrectly received player state 1");
            Assertions.assertTrue(states.contains(dummyPlayerState2),
                    "Incorrectly received player state 2");

            this.gameServer.disconnectClients();

        } catch (IOException | InterruptedException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testSendingUpdates() throws IOException {
        try {
            this.gameClient = GameClient.openConnection(this.clientPlayerState, this.serverAddress);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            PlayerState dummyPlayerState1 = new PlayerState("DummyID1", Point2D.ZERO);
            PlayerState dummyPlayerState2 = new PlayerState("DummyID2", Point2D.ZERO);
            this.gameClient.send(new GameUpdate(dummyPlayerState1));
            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            this.gameClient.send(new GameUpdate(this.clientPlayerState));
            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            this.gameClient.send(new GameUpdate(dummyPlayerState2));
            Thread.sleep(CATCHUP_TIME); // time for the client to receive update

            ArrayList<PlayerState> states = this.gameClient.getPlayerStates();

            Assertions.assertEquals(3, states.size(),
                    "Incorrect number of received player states");
            Assertions.assertTrue(states.contains(dummyPlayerState1),
                    "Incorrectly received player state 1");
            Assertions.assertTrue(states.contains(dummyPlayerState2),
                    "Incorrectly received player state 2");

            this.gameServer.disconnectClients();

            Thread.sleep(CATCHUP_TIME); // time for the client to disconnect

            Assertions.assertFalse(this.gameClient.getIsActive());

            PlayerState dummyPlayerState3 = new PlayerState("DummyID3", Point2D.ZERO);
            PlayerState dummyPlayerState4 = new PlayerState("DummyID4", Point2D.ZERO);
            this.gameClient.send(new GameUpdate(dummyPlayerState3));
            this.gameClient.send(new GameUpdate(dummyPlayerState4));

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            ArrayList<PlayerState> afterStates = this.gameClient.getPlayerStates();

            Assertions.assertFalse(afterStates.contains(dummyPlayerState3),
                    "Correctly received player state 3 despite closed connection");
            Assertions.assertFalse(afterStates.contains(dummyPlayerState4),
                    "Correctly received player state 4 despite closed connection");

        } catch (IOException | InterruptedException | ClassNotFoundException exception) {
            this.gameServer.disconnectClients();
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testReceivingUpdates() {
        try {
            this.gameClient = GameClient.openConnection(this.clientPlayerState, this.serverAddress);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            PlayerState dummyPlayerState1 = new PlayerState("DummyID3", Point2D.ZERO);

            this.gameClient.send(dummyPlayerState1);
            this.gameClient.send(this.clientPlayerState);

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            ArrayList<PlayerState> states = this.gameClient.getPlayerStates();

            Assertions.assertEquals(2, states.size(),
                    "Incorrect number of received player states");
            Assertions.assertTrue(states.contains(dummyPlayerState1),
                    "Incorrectly received player state");

            dummyPlayerState1.setPosition(dummyPlayerState1.getPosition().add(1, 1));
            this.gameClient.send(new GameUpdate(dummyPlayerState1));

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            ArrayList<PlayerState> updatedStates = this.gameClient.getPlayerStates();

            Assertions.assertEquals(2, updatedStates.size(),
                    "Incorrect number of received player states");

            int pIndex = updatedStates.indexOf(dummyPlayerState1);
            Assertions.assertTrue(pIndex > -1,
                    "Incorrectly received player state");
            Assertions.assertEquals(dummyPlayerState1, updatedStates.get(pIndex),
                    "Incorrectly received player state");

            this.gameServer.disconnectClients();

        } catch (IOException | InterruptedException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testReceivingOthersDisconnect() {
        try {
            this.gameClient = GameClient.openConnection(this.clientPlayerState, this.serverAddress);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            PlayerState secondState = new PlayerState("2ndClient", new Point2D(1, 1));
            GameClient secondClient = GameClient.openConnection(secondState, this.serverAddress);
            Assertions.assertNotNull(secondClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            ArrayList<PlayerState> states = this.gameClient.getPlayerStates();

            Assertions.assertEquals(2, states.size());
            Assertions.assertTrue(states.contains(secondState));

            secondClient.closeConnection(true);

            Thread.sleep(CATCHUP_TIME); // time for the client to disconnect

            ArrayList<PlayerState> disconnectedStates = this.gameClient.getPlayerStates();

            Assertions.assertEquals(1, disconnectedStates.size());

        } catch (IOException | InterruptedException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testDisconnectByServer() {
        try {
            this.gameClient = GameClient.openConnection(this.clientPlayerState, this.serverAddress);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME);

            this.gameServer.disconnectClients();

            Thread.sleep(CATCHUP_TIME);

            Assertions.assertFalse(this.gameClient.getIsActive(),
                    "The client is active despite closing the connection.");
            Assertions.assertTrue(this.gameClient.clientSocket.isClosed(),
                    "The client socket is not closed despite closing the connection.");

        } catch (IOException | ClassNotFoundException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

}