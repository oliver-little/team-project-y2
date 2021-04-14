package teamproject.wipeout.networking.server;

import javafx.util.Pair;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketPriceUpdater;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.state.*;
import teamproject.wipeout.util.threads.BackgroundThread;
import teamproject.wipeout.util.threads.ServerThread;
import teamproject.wipeout.util.threads.UtilityThread;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@code GameServer} class represents the server-part of the client-server network architecture.
 * It provides you with methods to initialize a game server, multicast the server's IP address,
 * and serve its clients.
 *
 * @see GameClientHandler
 * @see teamproject.wipeout.networking.client.GameClient
 */
public class GameServer {

    // Constants important for the whole ...networking. package
//<<<<<<< HEAD
//    public static final int HANDSHAKE_PORT = 9919;
//    public static final String HANDSHAKE_GROUP = "229.1.2.3";
//    public static final int GAME_PORT = 8780;
//=======
    public static final int HANDSHAKE_PORT = 1025;
    public static final String HANDSHAKE_GROUP = "229.22.29.51";
//>>>>>>> develop
    public static final int MAX_CONNECTIONS = 4;
    public static final int MULTICAST_DELAY = 500; // = 0.5 second

    private static final Integer[] ALL_FARM_IDS = new Integer[]{1, 2, 3, 4}; // TODO where to put private static final?

    public final String name;
    public final Integer id;
    public final short serverPort;

    protected DatagramSocket searchSocket;
    protected boolean isSearching;

    protected final ServerSocket serverSocket;
    protected ServerThread newConnectionsThread;

    protected boolean isActive;
    protected long gameStartTime;

    protected final AtomicReference<ArrayList<GameClientHandler>> connectedClients;

    protected final AtomicReference<HashMap<Integer, PlayerState>> playerStates;

    protected final AtomicReference<Pair<Integer, AnimalState>> animalBoss;

    protected final AtomicReference<HashMap<Integer, FarmState>> farmStates;
    protected final AtomicReference<ArrayList<Integer>> availableFarms;

    protected final Market serverMarket;

    private final HashSet<Integer> generatedIDs;

    // Atomic variables above used because of multi-threading

    /**
     * Default initializer for {@code GameServer}.
     * The server is created and starts accepting new connections.
     *
     * @param name Name we want to give to the {@code GameServer}.
     * @throws IOException Network problem
     */
    public GameServer(String name) throws IOException, ReflectiveOperationException {
        this.name = name;
        this.id = name.hashCode();

        this.isSearching = false;

        ServerSocket interimServerSocket = null;
        short interimServerPort = -1;
        for (short port = 1026; port < 10000; port++) {
            try {
                interimServerSocket = new ServerSocket(port);
            } catch (IOException ignore) {
                continue;
            }
            interimServerPort = port;
            break;
        }
        if (interimServerSocket == null || interimServerPort < 1026) {
            throw new IOException("Address already in use. There are no available ports left.");
        }

        this.serverPort = interimServerPort;
        this.serverSocket = interimServerSocket;

        this.isActive = false;
        this.gameStartTime = -1;

        this.connectedClients = new AtomicReference<ArrayList<GameClientHandler>>(new ArrayList<GameClientHandler>());

        this.playerStates = new AtomicReference<HashMap<Integer, PlayerState>>(new HashMap<Integer, PlayerState>());

        this.animalBoss = new AtomicReference<Pair<Integer, AnimalState>>(null);

        this.farmStates = new AtomicReference<HashMap<Integer, FarmState>>(new HashMap<Integer, FarmState>());
        this.availableFarms = new AtomicReference<ArrayList<Integer>>(new ArrayList<Integer>(Arrays.asList(ALL_FARM_IDS)));

        this.serverMarket = new Market(new ItemStore("items.json"), false);
        this.serverMarket.serverIDGetter = () -> this.id;
        this.serverMarket.serverUpdater = (gameUpdate) -> {
            try {
                this.updateClients(gameUpdate);

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        };
        new MarketPriceUpdater(this.serverMarket, false);

        this.generatedIDs = new HashSet<Integer>();

        this.handleNewConnections();
    }

    /**
     * Executed when the child process supposed to run a {@code GameServer} is started.
     *
     * @param args {@code String[]} containing only the GameServer's name
     */
    public static void main(String[] args) {
        try {
            // Create a GameServer
            String serverName = args[0];
            GameServer server = new GameServer(serverName);

            // Start multicasting the server's IP address and accepting new client connections
            server.startClientSearch();

            // Listen to the child process' input(= ProcessMessages send by the parent process)
            GameServer.listenToProcessMessages(server);

        } catch (IOException | ReflectiveOperationException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Starts multicasting the server's IP address using suitable network interfaces.
     *
     * @throws IOException Network problem
     */
    public void startClientSearch() throws IOException {
        if (this.isSearching) {
            return;
        }
        this.isSearching = true;
        this.searchSocket = new DatagramSocket();
        this.startMulticasting();
    }

    /**
     * Stops multicasting the server's IP address.
     */
    public void stopClientSearch() {
        this.isSearching = false;
        this.searchSocket.close();
        this.searchSocket = null;
    }

    /**
     * Starts a game session and rejects all new connection attempts.
     */
    public void startNewGame() {
        if (this.isActive) {
            this.stopGame();
        }
        this.isActive = true;
        this.calibrateClocks();
    }

    /**
     * Stops a running game session and starts accepting new connection attempts.
     */
    public void stopGame() {
        this.playerStates.set(new HashMap<Integer, PlayerState>());
        this.farmStates.set(new HashMap<Integer, FarmState>());
        this.availableFarms.set(new ArrayList<Integer>(Arrays.asList(ALL_FARM_IDS)));
        this.gameStartTime = -1;
        this.isActive = false;
    }

    /**
     * Sends a given {@code GameUpdate} instance to all the connected clients
     * except the client who created the {@code GameUpdate} instance.
     *
     * @param update Instance of a {@link GameUpdate} to be sent.
     * @throws IOException The {@code GameUpdate} cannot be sent.
     */
    public void updateClients(GameUpdate update) throws IOException {
        for (GameClientHandler client : this.connectedClients.get()) {
            if (!client.clientID.equals(update.originClientID)) {
                client.updateWith(update);
            }
        }
    }

    /**
     * Disconnects a client with the given ID.
     *
     * @param deleteClientID ID of the client who will be disconnected
     * @param serverSide     Is the disconnect command coming from the server?
     * @throws IOException Problem with closing the connection
     */
    public void disconnectClient(Integer deleteClientID, boolean serverSide) throws IOException {
        ArrayList<GameClientHandler> clientHandlers = this.connectedClients.getAcquire();

        for (GameClientHandler client : clientHandlers) {
            if (client.clientID.equals(deleteClientID)) {
                client.closeConnection(serverSide);
            }
        }
        clientHandlers.removeIf((client) -> client.clientID.equals(deleteClientID));
        this.handlePlayerStateDelete(deleteClientID);
        this.handleFarmStateDelete(deleteClientID);

        this.connectedClients.setRelease(clientHandlers);
        this.generatedIDs.remove(deleteClientID);
    }

    /**
     * Disconnects all connected clients (even the owner of the server!).
     *
     * @throws IOException Problem with closing connections
     */
    public void disconnectClients() throws IOException {
        ArrayList<GameClientHandler> clientHandlers = this.connectedClients.getAcquire();

        for (GameClientHandler client : clientHandlers) {
            client.closeConnection(true);
        }
        this.stopGame();

        this.connectedClients.setRelease(new ArrayList<GameClientHandler>());
        this.generatedIDs.clear();
    }

    /**
     * Stops the server and disconnects its clients.
     *
     * @throws IOException Problem with closing connections
     */
    public void stopServer() throws IOException {
        if (this.isSearching) {
            this.stopClientSearch();
        }

        if (this.isActive) {
            this.stopGame();
        }

        this.disconnectClients();
        this.serverSocket.close();
    }

    /**
     * Listens to the child process' input(= {@link ProcessMessage}s from the parent process)
     * and responds to it. Creates a separate {@link BackgroundThread} for the listener.
     *
     * @param server {@link GameServer} running in the current child process.
     */
    private static void listenToProcessMessages(GameServer server) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        new BackgroundThread(() -> {
            try {
                writer.write(Short.toString(server.serverPort) + '\n');
                writer.flush();

                String line;
                while ((line = reader.readLine()) != null) {
                    ProcessMessage message = ProcessMessage.fromRawValue(line);
                    if (message == null) {
                        continue;
                    }
                    switch (message) {
                        case CONFIRMATION:
                            writer.write(ProcessMessage.CONFIRMATION.rawValue + '\n');
                            writer.flush();
                            break;
                        case START_GAME:
                            server.startNewGame();
                            writer.write(message.rawValue + ProcessMessage.CONFIRMATION.rawValue + '\n');
                            writer.flush();
                            break;
                        case STOP_GAME:
                            server.stopGame();
                            writer.write(message.rawValue + ProcessMessage.CONFIRMATION.rawValue + '\n');
                            writer.flush();
                            break;
                        case STOP_SERVER:
                            server.stopServer();
                            System.exit(0);
                            break;
                    }
                }

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }).start();
    }

    /**
     * Synchronizes client game clocks
     */
    private void calibrateClocks() {
        try {
            Long currentTime = System.currentTimeMillis();
            this.updateClients(new GameUpdate(GameUpdateType.CLOCK_CALIB, this.id, currentTime));

            this.gameStartTime = currentTime;

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Processes the received {@code GameUpdate}
     *
     * @param update Received {@link GameUpdate}
     */
    private void clientUpdateArrived(GameUpdate update) throws IOException {
        switch (update.type) {
            case PLAYER_STATE:
                this.handlePlayerStateUpdate((PlayerState) update.content);
                break;
            case ANIMAL_STATE:
                this.handleAnimalStateUpdate(update.originClientID, (AnimalState) update.content);
                break;
            case FARM_STATE:
                this.handleFarmStateUpdate(update.originClientID, (FarmState) update.content);
                break;
            case FARM_ID:
                this.handleFarmRequest(update.originClientID);
                return;
            case REQUEST:
                this.handleRequest((MarketOperationRequest) update.content, update.originClientID);
                return;
            case DISCONNECT:
                this.disconnectClient(update.originClientID, false);
                break;
            default:
                break;
        }
        this.updateClients(update);
    }

    /**
     * Handles a given {@code PlayerState} update.
     *
     * @param state {@link PlayerState} to be processed.
     */
    private void handlePlayerStateUpdate(PlayerState state) {
        HashMap<Integer, PlayerState> currentPlayerStates = this.playerStates.getAcquire();
        currentPlayerStates.put(state.getPlayerID(), state);
        this.playerStates.setRelease(currentPlayerStates);
    }

    /**
     * Delete a {@code PlayerState} with the given ID.
     *
     * @param clientID ID of the {@link PlayerState} to be deleted.
     */
    private void handlePlayerStateDelete(Integer clientID) {
        HashMap<Integer, PlayerState> currentPlayerStates = this.playerStates.getAcquire();
        currentPlayerStates.remove(clientID);
        this.playerStates.setRelease(currentPlayerStates);
    }

    /**
     * Handles a given {@code AnimalState} update.
     *
     * @param state {@link AnimalState} to be processed.
     */
    private void handleAnimalStateUpdate(Integer clientID, AnimalState state) {
        Pair<Integer, AnimalState> currentAnimal = this.animalBoss.getAcquire();
        if (currentAnimal == null) {
            this.animalBoss.setRelease(new Pair<Integer, AnimalState>(clientID, state));
            return;
        } else if (!currentAnimal.getKey().equals(clientID)) {
            return;
        }
        currentAnimal.getValue().updateStateFrom(state);
        this.animalBoss.setRelease(currentAnimal);
    }

    /**
     * Handles a given {@code FarmState} update.
     *
     * @param state {@link FarmState} to be processed.
     */
    private void handleFarmStateUpdate(Integer clientID, FarmState state) {
        HashMap<Integer, FarmState> currentFarmStates = this.farmStates.getAcquire();
        currentFarmStates.put(clientID, state);
        this.farmStates.setRelease(currentFarmStates);
    }

    /**
     * Delete a {@code FarmState} with the given ID.
     *
     * @param clientID ID of the client owning the {@link FarmState} to be deleted.
     */
    private void handleFarmStateDelete(Integer clientID) {
        HashMap<Integer, FarmState> currentFarmStates = this.farmStates.getAcquire();
        currentFarmStates.remove(clientID);
        this.farmStates.setRelease(currentFarmStates);
    }

    /**
     *
     */
    private void handleRequest(MarketOperationRequest request, Integer clientID) throws IOException {
        if (request.buy) {
            this.serverMarket.buyItem(request.itemID, request.quantity);

        } else {
            this.serverMarket.sellItem(request.itemID, request.quantity);
        }

        for (GameClientHandler clientHandler : this.connectedClients.get()) {
            if (clientHandler.clientID.equals(clientID)) {
                MarketOperationResponse response = new MarketOperationResponse(request, true);
                clientHandler.updateWith(new GameUpdate(GameUpdateType.RESPONSE, this.id, response));
                return;
            }
        }
    }

    /**
     *
     */
    private Integer handleFarmRequest(Integer clientID) throws IOException {
        Integer farmID = -1;
        try {
            ArrayList<Integer> farms = this.availableFarms.getAcquire();
            farmID = farms.remove(0);
            this.availableFarms.setRelease(farms);
        } catch (IndexOutOfBoundsException | UnsupportedOperationException ignore) {}

        return farmID;
    }

    /**
     * Starts accepting (or rejecting) attempted connections to the server
     * in a separate {@link ServerThread}. Only one attempted connections handler can be running.
     */
    private void handleNewConnections() {
        if (this.newConnectionsThread != null) {
            return;
        }

        this.newConnectionsThread = new ServerThread(() -> {
            while (!this.serverSocket.isClosed()) {
                try {
                    Socket clientSocket = this.serverSocket.accept();
                    ArrayList<GameClientHandler> clientHandlers = this.connectedClients.getAcquire();

                    // (1.) Game session is not active and the number of clients is under the limit
                    if (!this.isActive && clientHandlers.size() < MAX_CONNECTIONS) {
                        GameClientHandler client = GameClientHandler.allowConnection(this.id, this.generateClientID(), clientSocket, this::clientUpdateArrived);

                        client.updateWith(new GameUpdate(GameUpdateType.FARM_ID, this.id, this.handleFarmRequest(client.clientID)));

                        if (clientHandlers.add(client)) { // Duplicate clients are NOT added
                            this.sendFirstUpdatesToClient(client, clientHandlers);
                        }

                        HashMap<Integer, String> newClientMap = new HashMap<Integer, String>();
                        newClientMap.put(client.clientID, client.clientName);
                        this.updateClients(new GameUpdate(GameUpdateType.CONNECTED, client.clientID, newClientMap));

                    } else { // (2.) Otherwise deny attempts to connect
                        GameClientHandler.denyConnection(clientSocket, this.id);
                    }

                    this.connectedClients.setRelease(clientHandlers);

                } catch (IOException | ClassNotFoundException exception) {
                    if (!this.serverSocket.isClosed()) {
                        exception.printStackTrace();
                    } else {
                        this.newConnectionsThread = null;
                        break;
                    }
                }
            }
        });
        this.newConnectionsThread.start();
    }

    private int generateClientID() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randID = random.nextInt(128);
        while (this.generatedIDs.contains(randID)) {
            randID = random.nextInt(128);
        }
        return randID;
    }

    private void sendFirstUpdatesToClient(GameClientHandler client, ArrayList<GameClientHandler> clientHandlers) throws IOException {
        HashMap<Integer, String> connectedClients = new HashMap<Integer, String>();
        for (GameClientHandler clientHandler : clientHandlers) {
            connectedClients.put(clientHandler.clientID, clientHandler.clientName);
        }
        client.updateWith(new GameUpdate(GameUpdateType.CONNECTED, this.id, connectedClients));

        client.updateWith(this.playerStates.get().values());

        for (Entry<Integer, FarmState> entry : this.farmStates.get().entrySet()) {
            GameUpdate gameUpdate = new GameUpdate(GameUpdateType.FARM_STATE, entry.getKey(), entry.getValue());
            client.updateWith(gameUpdate);
        }
    }

    /**
     * Multicasts a packet via {@code this.searchSocket} on a separate {@link UtilityThread} thread.
     */
    private void startMulticasting() {
        new UtilityThread(() -> {
            while (this.isSearching) {
                try {
                    // Construct packet which will be multicasted (packet contains server name and address)
                    byte[] portBytes = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(this.serverPort).array();
                    byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);

                    byte[] packetBytes = new byte[128];
                    System.arraycopy(portBytes, 0, packetBytes, 0, portBytes.length);
                    System.arraycopy(nameBytes, 0, packetBytes, portBytes.length, nameBytes.length);

                    DatagramPacket packet = new DatagramPacket(
                            packetBytes, packetBytes.length,
                            InetAddress.getByName(HANDSHAKE_GROUP),
                            HANDSHAKE_PORT
                    );

                    this.searchSocket.send(packet);
                    Thread.sleep(MULTICAST_DELAY); // == sends the packet each 0.5 second

                } catch (IOException | InterruptedException exception) {
                    if (this.isSearching) {
                        exception.printStackTrace();
                    } else {
                        break;
                    }
                }
            }
        }).start();
    }

}
