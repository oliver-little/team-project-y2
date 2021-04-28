package teamproject.wipeout.networking.server;

import javafx.util.Pair;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketPriceUpdater;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.state.AnimalState;
import teamproject.wipeout.networking.state.FarmState;
import teamproject.wipeout.networking.state.MarketOperationRequest;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.util.threads.BackgroundThread;
import teamproject.wipeout.util.threads.ServerThread;
import teamproject.wipeout.util.threads.UtilityThread;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
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

    // Constants important for the whole networking package
    public static final int HANDSHAKE_PORT = 1025;
    public static final String HANDSHAKE_GROUP = "229.22.29.51";
    public static final int MULTICAST_DELAY = 500; // = 0.5 second
    public static final int MAX_CONNECTIONS = 4;

    // Constants important only for the game server
    private static final Integer[] ALL_FARM_IDS = new Integer[]{1, 2, 3, 4};

    public final Integer id;
    public final String name;
    public final short serverPort;

    protected final AtomicReference<ArrayList<GameClientHandler>> connectedClients;
    protected final AtomicReference<ArrayList<Integer>> availableFarms;
    protected final AtomicReference<HashMap<Integer, PlayerState>> playerStates;
    protected final AtomicReference<HashMap<Integer, FarmState>> farmStates;
    protected final AtomicReference<Pair<Integer, AnimalState>> animalState;
    // Atomic variables above are used because of multithreading.
    protected final Market serverMarket;

    protected DatagramSocket searchSocket;
    protected boolean isSearching;

    protected long gameStartTime;
    protected boolean isActive;

    private final ServerSocket serverSocket;
    private final HashSet<Integer> generatedIDs;

    private ServerThread newConnectionsThread;

    /**
     * Default initializer for {@code GameServer}.
     * The server is created, starts accepting new connections and serving them.
     *
     * @param name Name we want to give to the {@code GameServer}.
     * @throws IOException                  Network problem / No available ports
     * @throws ReflectiveOperationException Problem with "items.json" file of the server market
     */
    public GameServer(String name) throws IOException, ReflectiveOperationException {
        this.id = name.hashCode();
        this.name = name;

        // Look for available port
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

        this.connectedClients = new AtomicReference<ArrayList<GameClientHandler>>(new ArrayList<GameClientHandler>());
        this.availableFarms = new AtomicReference<ArrayList<Integer>>(new ArrayList<Integer>(Arrays.asList(ALL_FARM_IDS)));
        this.playerStates = new AtomicReference<HashMap<Integer, PlayerState>>(new HashMap<Integer, PlayerState>());
        this.farmStates = new AtomicReference<HashMap<Integer, FarmState>>(new HashMap<Integer, FarmState>());
        this.animalState = new AtomicReference<Pair<Integer, AnimalState>>(null);

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

        this.searchSocket = null;
        this.isSearching = false;

        this.gameStartTime = -1;
        this.isActive = false;

        this.generatedIDs = new HashSet<Integer>();

        this.newConnectionsThread = null;

        this.handleNewConnections();
    }

    /**
     * Executed when the child process that is supposed to run the {@code GameServer} is started.
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
            server.listenToProcessMessages();

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
        if (this.searchSocket != null) {
            this.searchSocket.close();
            this.searchSocket = null;
        }
    }

    /**
     * Starts a game session and rejects all new connection attempts.
     */
    public void startGame() {
        if (this.isActive) {
            return;
        }
        this.isActive = true;
        this.stopClientSearch();
        this.calibrateClocks();
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

        this.serverStopping();
        this.serverSocket.close();
    }

    /**
     * Sends a given {@code GameUpdate} instance to all the connected clients
     * except the client who created the {@code GameUpdate} instance.
     *
     * @param update Instance of a {@link GameUpdate} to be sent
     * @throws IOException The {@code GameUpdate} cannot be sent
     */
    protected void updateClients(GameUpdate update) throws IOException {
        for (GameClientHandler client : this.connectedClients.get()) {
            if (!client.clientID.equals(update.originID)) {
                client.updateWith(update);
            }
        }
    }

    /**
     * Disconnects a client with a given ID.
     *
     * @param deleteClientID ID of the client who will be disconnected
     * @throws IOException Problem with closing the connection
     */
    protected void disconnectClient(Integer deleteClientID) throws IOException {
        ArrayList<GameClientHandler> clientHandlers = this.connectedClients.getAcquire();

        for (GameClientHandler client : clientHandlers) {
            if (client.clientID.equals(deleteClientID)) {
                client.closeConnection(false);
                this.addAvailableFarm(client.farmID);
            }
        }
        clientHandlers.removeIf((client) -> client.clientID.equals(deleteClientID));
        this.handlePlayerStateDelete(deleteClientID);
        this.handleFarmStateDelete(deleteClientID);

        this.connectedClients.setRelease(clientHandlers);
        this.generatedIDs.remove(deleteClientID);
    }

    /**
     * Sends the {@link ProcessMessage}{@code .SERVER_STOP} message to all connected clients.
     * (Even to the host of the server!)
     */
    protected void serverStopping() {
        this.isActive = false;
        ArrayList<GameClientHandler> clientHandlers = this.connectedClients.getAcquire();

        GameUpdate stopServer = new GameUpdate(GameUpdateType.SERVER_STOP, this.id);
        for (GameClientHandler client : clientHandlers) {
            try {
                client.updateWith(stopServer);
            } catch (IOException ignore) {
                // Stopping server so it's not important
            }
        }

        this.connectedClients.setRelease(new ArrayList<GameClientHandler>());
    }

    /**
     * Listens to the child process' input(= {@link ProcessMessage}s from the parent process)
     * and responds to it. Creates a separate {@link BackgroundThread} for the listener.
     */
    private void listenToProcessMessages() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        new BackgroundThread(() -> {
            try {
                writer.write(Short.toString(this.serverPort) + '\n');
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
                            this.startGame();
                            writer.write(message.rawValue + ProcessMessage.CONFIRMATION.rawValue + '\n');
                            writer.flush();
                            break;
                        case STOP_SERVER:
                            this.stopServer();
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
     * Synchronize gameplay clocks across clients.
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
     * Processes received {@code GameUpdate}.
     *
     * @param update Received {@link GameUpdate}
     * @throws IOException Thrown when the {@code GameUpdate} isn't redistributed to the rest of our clients.
     */
    private void clientUpdateArrived(GameUpdate update) throws IOException {
        switch (update.type) {
            case PLAYER_STATE:
                this.handlePlayerStateUpdate((PlayerState) update.content);
                break;
            case ANIMAL_STATE:
                this.handleAnimalStateUpdate(update.originID, (AnimalState) update.content);
                break;
            case FARM_STATE:
                this.handleFarmStateUpdate(update.originID, (FarmState) update.content);
                break;
            case REQUEST:
                this.handleRequest((MarketOperationRequest) update.content);
                return;
            case DISCONNECT:
                this.disconnectClient(update.originID);
                break;
            default:
                break;
        }
        this.updateClients(update);
    }

    /**
     * Handles a given {@code PlayerState} update.
     *
     * @param state {@link PlayerState} to be processed
     */
    private void handlePlayerStateUpdate(PlayerState state) {
        HashMap<Integer, PlayerState> currentPlayerStates = this.playerStates.getAcquire();
        currentPlayerStates.put(state.getPlayerID(), state);
        this.playerStates.setRelease(currentPlayerStates);
    }

    /**
     * Delete a {@code PlayerState} with the given ID.
     *
     * @param clientID ID of the {@link PlayerState} to be deleted
     */
    private void handlePlayerStateDelete(Integer clientID) {
        HashMap<Integer, PlayerState> currentPlayerStates = this.playerStates.getAcquire();
        currentPlayerStates.remove(clientID);
        this.playerStates.setRelease(currentPlayerStates);
    }

    /**
     * @return Farm ID that has not been used yet. If there is no farm ID left, it returns {@code -1}.
     */
    private int handleFarmRequest() throws IOException {
        try {
            return this.removeAvailableFarm();
        } catch (IndexOutOfBoundsException | UnsupportedOperationException ignore) {
            return -1;
        }
    }

    /**
     * Add a given farm ID to the list of available farm IDs.
     *
     * @param farmID Farm ID to be added
     */
    private void addAvailableFarm(Integer farmID) {
        ArrayList<Integer> farms = this.availableFarms.getAcquire();
        farms.add(0, farmID);
        this.availableFarms.setRelease(farms);
    }

    /**
     * Remove a farm ID from the list of available farm IDs.
     *
     * @return Available farm ID
     */
    private int removeAvailableFarm() throws IndexOutOfBoundsException, UnsupportedOperationException {
        ArrayList<Integer> farms = this.availableFarms.getAcquire();
        int farmID = farms.remove(0);
        this.availableFarms.setRelease(farms);
        return farmID;
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
     * Handles a given {@code AnimalState} update.
     *
     * @param state {@link AnimalState} to be processed
     */
    private void handleAnimalStateUpdate(Integer clientID, AnimalState state) {
        Pair<Integer, AnimalState> currentAnimal = this.animalState.getAcquire();
        if (currentAnimal == null) {
            this.animalState.setRelease(new Pair<Integer, AnimalState>(clientID, state));
            return;
        } else if (!currentAnimal.getKey().equals(clientID)) {
            return;
        }
        currentAnimal.getValue().updateStateFrom(state);
        this.animalState.setRelease(currentAnimal);
    }

    /**
     * Handles a given {@code MarketOperationRequest}.
     *
     * @param request {@link MarketOperationRequest} to be processed
     */
    private void handleRequest(MarketOperationRequest request) {
        if (request.getIsBuying()) {
            this.serverMarket.buyItem(request.getItemID(), request.getQuantity());

        } else {
            this.serverMarket.sellItem(request.getItemID(), request.getQuantity());
        }
    }

    /**
     * Starts accepting (or rejecting) server connection attempts on a separate {@link ServerThread}.
     * Only one attempted connections handler will be running.
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
                        Integer clientID = this.generateClientID();
                        Integer farmID = this.handleFarmRequest();
                        GameClientHandler client = GameClientHandler.allowConnection(clientSocket, this.id, clientID, farmID, this::clientUpdateArrived);

                        client.updateWith(new GameUpdate(GameUpdateType.FARM_ID, this.id, farmID));

                        if (clientHandlers.add(client)) { // Duplicate clients are NOT added
                            this.sendFirstUpdateToClient(client, clientHandlers);
                        }

                        HashMap<Integer, String> newClientMap = new HashMap<Integer, String>();
                        newClientMap.put(client.clientID, client.clientName);
                        this.updateClients(new GameUpdate(GameUpdateType.CONNECTED, client.clientID, newClientMap));

                    } else { // (2.) Otherwise deny attempts to connect
                        GameClientHandler.denyConnection(clientSocket, this.id);
                    }

                    this.connectedClients.setRelease(clientHandlers);
                } catch (ClientConnectionException ignore) {
                    // Client denied the connection or changed its client ID illegally
                    continue;
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

    /**
     * @return Unique client ID in range [0, 127]
     */
    private int generateClientID() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randID = random.nextInt(128);
        while (this.generatedIDs.contains(randID)) {
            randID = random.nextInt(128);
        }
        return randID;
    }

    /**
     * Sends currently available states from other clients to the given client.
     *
     * @param client         Client that will receive information
     * @param clientHandlers {@code List} with the rest of the clients
     * @throws IOException When the update fails to be sent.
     */
    private void sendFirstUpdateToClient(GameClientHandler client, List<GameClientHandler> clientHandlers) throws IOException {
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
     * Multicast a packet via the {@code searchSocket} on a separate {@link UtilityThread} thread.
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
