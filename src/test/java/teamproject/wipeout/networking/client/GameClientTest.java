package teamproject.wipeout.networking.client;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import org.junit.jupiter.api.*;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.server.GameServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameClientTest {

    private static final Integer CLIENT_ID = 123;
    private static final Integer DUMMY_CLIENT_ID = 999;
    private static final String SERVER_NAME = "TestServer#1";
    private static final int CATCHUP_TIME = 80;

    private GameClient gameClient;
    private Player clientPlayer;

    private GameServer gameServer;
    private InetSocketAddress serverAddress;

    private Player playerWaitingForFarmID;
    private GameClient clientWaitingForFarmID;
    private final Consumer<Pair<GameClient, Integer>> farmIDReceived = (farmPair) -> {
        this.playerWaitingForFarmID.getCurrentState().assignFarm(farmPair.getValue());
        try {
            if (this.clientWaitingForFarmID == null) {
                this.gameClient.send(new GameUpdate(this.playerWaitingForFarmID.getCurrentState()));
            } else {
                this.clientWaitingForFarmID.send(new GameUpdate(this.playerWaitingForFarmID.getCurrentState()));
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    };

    private HashSet<PlayerState> newPlayers;
    private final NewPlayerAction newPlayerAction = (newPlayer) -> {
        newPlayers.add(newPlayer);
        return new Player(new GameScene(), newPlayer.getPlayerID(), "Test"+newPlayer.getPlayerID(), newPlayer.getPosition(), null, null);
    };

    @BeforeAll
    void initializeGameClient() throws IOException, InterruptedException, ReflectiveOperationException {
        this.clientPlayer = new Player(new GameScene(), CLIENT_ID, "Test", Point2D.ZERO, null, null);
        this.newPlayers = new HashSet<>();

        this.gameServer = new GameServer(SERVER_NAME);
        this.gameServer.startClientSearch();

        ServerDiscovery serverDiscovery = new ServerDiscovery((name, address) -> {
            this.serverAddress = address;
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
        this.playerWaitingForFarmID = this.clientPlayer;
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
            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer, new HashMap<>(), this.farmIDReceived, this.newPlayerAction);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            Assertions.assertTrue(this.gameClient.getIsActive(),
                    "The client is not active despite opening a connection.");

            PlayerState dummyPlayerState = new PlayerState(DUMMY_CLIENT_ID, Point2D.ZERO, Point2D.ZERO, 0.0);
            this.gameServer.updateClients(new GameUpdate(dummyPlayerState));
            this.gameServer.updateClients(new GameUpdate(this.clientPlayer.getCurrentState()));

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            Set<Integer> states = this.gameClient.players.keySet();
            Assertions.assertEquals(2, states.size());
            Assertions.assertTrue(states.contains(dummyPlayerState.getPlayerID()));

        } catch (IOException | InterruptedException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testFailToOpenConnection() throws IOException {
        try {
            this.gameServer.startNewGame(); // will not allow new connections

            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer, new HashMap<>(), this.farmIDReceived, this.newPlayerAction);
            Assertions.assertNull(this.gameClient,
                    "Connected despite the server not allowing connections");

            this.gameServer.stopGame(); // will allow new connections

            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer, new HashMap<>(), this.farmIDReceived, this.newPlayerAction);
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
            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer, new HashMap<>(), this.farmIDReceived, this.newPlayerAction);
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
            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer, new HashMap<>(), this.farmIDReceived, this.newPlayerAction);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            PlayerState dummyPlayerState1 = new PlayerState(DUMMY_CLIENT_ID - 1, Point2D.ZERO, Point2D.ZERO, 0.0);
            PlayerState dummyPlayerState2 = new PlayerState(DUMMY_CLIENT_ID - 10, Point2D.ZERO, Point2D.ZERO, 0.0);

            this.gameClient.send(new GameUpdate(dummyPlayerState1));
            this.gameClient.send(new GameUpdate(this.clientPlayer.getCurrentState()));
            this.gameClient.send(new GameUpdate(dummyPlayerState2));

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            this.gameClient.closeConnection(true);

            Thread.sleep(CATCHUP_TIME); // time for the client to disconnect

            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer, new HashMap<>(), this.farmIDReceived, this.newPlayerAction);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            Set<Integer> states = this.gameClient.players.keySet();

            Assertions.assertEquals(3, states.size(),
                    "Incorrect number of received player states");
            Assertions.assertTrue(states.contains(dummyPlayerState1.getPlayerID()),
                    "Incorrectly received player state 1");
            Assertions.assertTrue(states.contains(dummyPlayerState2.getPlayerID()),
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
            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer, new HashMap<>(), this.farmIDReceived, this.newPlayerAction);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            PlayerState dummyPlayerState1 = new PlayerState(DUMMY_CLIENT_ID - 1, Point2D.ZERO, Point2D.ZERO, 0.0);
            PlayerState dummyPlayerState2 = new PlayerState(DUMMY_CLIENT_ID - 10, Point2D.ZERO, Point2D.ZERO, 0.0);
            this.gameClient.send(new GameUpdate(dummyPlayerState1));
            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            this.gameClient.send(new GameUpdate(this.clientPlayer.getCurrentState()));
            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            this.gameClient.send(new GameUpdate(dummyPlayerState2));
            Thread.sleep(CATCHUP_TIME); // time for the client to receive update

            Set<Integer> states = this.gameClient.players.keySet();

            Assertions.assertEquals(3, states.size(),
                    "Incorrect number of received player states");
            Assertions.assertTrue(states.contains(dummyPlayerState1.getPlayerID()),
                    "Incorrectly received player state 1");
            Assertions.assertTrue(states.contains(dummyPlayerState2.getPlayerID()),
                    "Incorrectly received player state 2");

            this.gameServer.disconnectClients();

            Thread.sleep(CATCHUP_TIME); // time for the client to disconnect

            Assertions.assertFalse(this.gameClient.getIsActive());

            PlayerState dummyPlayerState3 = new PlayerState(DUMMY_CLIENT_ID - 2, Point2D.ZERO, Point2D.ZERO, 0.0);
            PlayerState dummyPlayerState4 = new PlayerState(DUMMY_CLIENT_ID - 20, Point2D.ZERO, Point2D.ZERO, 0.0);
            this.gameClient.send(new GameUpdate(dummyPlayerState3));
            this.gameClient.send(new GameUpdate(dummyPlayerState4));

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            Set<Integer> afterStates = this.gameClient.players.keySet();

            Assertions.assertFalse(afterStates.contains(dummyPlayerState3.getPlayerID()),
                    "Correctly received player state 3 despite closed connection");
            Assertions.assertFalse(afterStates.contains(dummyPlayerState4.getPlayerID()),
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
            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer, new HashMap<>(), this.farmIDReceived, this.newPlayerAction);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            PlayerState dummyPlayerState1 = new PlayerState(DUMMY_CLIENT_ID - 2, Point2D.ZERO, Point2D.ZERO, 0.0);

            this.gameClient.send(dummyPlayerState1);
            this.gameClient.send(this.clientPlayer.getCurrentState());

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            Set<Integer> players = this.gameClient.players.keySet();

            Assertions.assertTrue(players.contains(dummyPlayerState1.getPlayerID()),
                    "Incorrectly received player state");

            dummyPlayerState1.setPosition(dummyPlayerState1.getPosition().add(1, 1));
            this.gameClient.send(new GameUpdate(dummyPlayerState1));

            Thread.sleep(CATCHUP_TIME); // time for the client to receive updates

            Set<Integer> updatedPlayers = this.gameClient.players.keySet();

            Assertions.assertEquals(2, updatedPlayers.size(),
                    "Incorrect number of received player states");

            Assertions.assertTrue(updatedPlayers.contains(dummyPlayerState1.getPlayerID()),
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
            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer, new HashMap<>(), this.farmIDReceived, this.newPlayerAction);
            Assertions.assertNotNull(this.gameClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect

            Player secondPlayer = new Player(new GameScene(), DUMMY_CLIENT_ID, "Test", Point2D.ZERO, null, null);
            this.playerWaitingForFarmID = secondPlayer;
            GameClient secondClient = GameClient.openConnection(this.serverAddress, secondPlayer, new HashMap<>(), this.farmIDReceived, this.newPlayerAction);
            this.clientWaitingForFarmID = secondClient;
            Assertions.assertNotNull(secondClient);

            Thread.sleep(CATCHUP_TIME); // time for the client to connect
            this.playerWaitingForFarmID = this.clientPlayer;
            this.clientWaitingForFarmID = null;

            Set<Integer> players = this.gameClient.players.keySet();

            Assertions.assertEquals(2, players.size());
            Assertions.assertTrue(players.contains(secondPlayer.playerID));

            secondClient.closeConnection(true);

            Thread.sleep(CATCHUP_TIME); // time for the client to disconnect

            Set<Integer> disconnectedPlayers = this.gameClient.players.keySet();

            Assertions.assertEquals(1, disconnectedPlayers.size());

        } catch (IOException | InterruptedException | ClassNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testDisconnectByServer() {
        try {
            this.gameClient = GameClient.openConnection(this.serverAddress, this.clientPlayer, new HashMap<>(), this.farmIDReceived, this.newPlayerAction);
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