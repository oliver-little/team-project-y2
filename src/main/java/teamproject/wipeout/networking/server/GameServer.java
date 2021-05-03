package teamproject.wipeout.networking.server;

import teamproject.wipeout.game.UI.GameMode;
import teamproject.wipeout.game.farm.FarmData;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketPriceUpdater;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.data.MarketOperationRequest;
import teamproject.wipeout.networking.state.FarmState;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.util.resources.PlayerSpriteSheetManager;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public static final int PORT_BYTE_LENGTH = 2; // 2 bytes needed to store number of type 'short'
    public static final int SERVER_NAME_BYTE_LENGTH = 62;

    public final Integer id;
    public final String name;
    public final GameMode gameMode;
    public final long gameModeValue;
    public final short serverPort;

    protected final AtomicBoolean isSearching; // Atomic because of use on multiple threads
    protected final AtomicBoolean isActive; // Atomic because of use on multiple threads

    private final ServerSocket serverSocket;
    private final List<GameClientHandler> connectedClients;
    private final HashSet<Integer> generatedIDs;
    private final PlayerSpriteSheetManager playerSpriteSheetManager;
    private final CopyOnWriteArrayList<Integer> availableFarms;
    private final ConcurrentHashMap<Integer, PlayerState> playerStates;
    private final ConcurrentHashMap<Integer, FarmState> farmStates;
    private final Market serverMarket;

    private DatagramSocket searchSocket;
    private ServerThread newConnectionsThread;

    /**
     * Executed when the child process that is supposed to run the {@code GameServer} is started.
     *
     * @param args {@code String[]} containing the GameServer's name, gameplay duration and game mode
     */
    public static void main(String[] args) {
        try {
            // Create a GameServer
            String serverName = args[0];
            GameMode gameMode = GameMode.fromName(args[1]);
            long gameModeValue = Long.parseLong(args[2]);

            GameServer server = new GameServer(serverName, gameMode, gameModeValue);

            // Start multicasting the server's IP address and accepting new client connections
            server.startClientSearch();

            // Listen to the child process' input(= ProcessMessages send by the parent process)
            server.listenToProcessMessages();

        } catch (IOException | ReflectiveOperationException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Default initializer for {@code GameServer}.
     * The server is created, starts accepting new connections and serving them.
     *
     * @param name          Name we want to give to the {@code GameServer}.
     * @param gameMode      Chosen {@link GameMode}
     * @param gameModeValue Duration of the gameplay
     * @throws IOException                  Network problem / No available ports
     * @throws ReflectiveOperationException Problem with "items.json" file of the server market
     */
    public GameServer(String name, GameMode gameMode, long gameModeValue)
            throws IOException, ReflectiveOperationException {

        this.id = name.hashCode();
        this.name = name;
        this.gameMode = gameMode;
        this.gameModeValue = gameModeValue;

        this.isSearching = new AtomicBoolean(false);
        this.isActive = new AtomicBoolean(false);

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

        this.connectedClients = Collections.synchronizedList(new ArrayList<GameClientHandler>());
        this.generatedIDs = new HashSet<Integer>();
        this.playerSpriteSheetManager = new PlayerSpriteSheetManager();
        this.availableFarms = new CopyOnWriteArrayList<Integer>(Arrays.asList(FarmData.ALL_FARM_IDS));
        this.playerStates = new ConcurrentHashMap<Integer, PlayerState>();
        this.farmStates = new ConcurrentHashMap<Integer, FarmState>();

        this.serverMarket = new Market(new ItemStore("items.json"), false);
        this.serverMarket.serverIDGetter = () -> this.id;
        this.serverMarket.serverUpdater = (gameUpdate) -> this.updateClients(gameUpdate);
        new MarketPriceUpdater(this.serverMarket, false);

        this.searchSocket = null;
        this.newConnectionsThread = null;

        this.handleNewConnections();
    }

    /**
     * {@code connectedClients} getter
     *
     * @return {@code List} of currently connected {@link GameClientHandler}s (= clients)
     */
    protected List<GameClientHandler> getConnectedClients() {
        return this.connectedClients;
    }

    /**
     * Starts multicasting the server's IP address using suitable network interfaces.
     *
     * @throws IOException Network problem
     */
    public void startClientSearch() throws IOException {
        if (!this.isSearching.compareAndSet(false, true)) {
            return;
        }
        this.searchSocket = new DatagramSocket();
        this.startMulticasting();
    }

    /**
     * Stops multicasting the server's IP address.
     */
    public void stopClientSearch() {
        this.isSearching.set(false);
        if (this.searchSocket != null) {
            this.searchSocket.close();
            this.searchSocket = null;
        }
    }

    /**
     * Starts a game session and rejects all new connection attempts.
     */
    public void startGame() {
        if (!this.isActive.compareAndSet(false, true)) {
            return;
        }
        this.stopClientSearch();
        this.calibrateClocks();
    }

    /**
     * Stops the server and disconnects its clients.
     *
     * @throws IOException Problem with closing connections
     */
    public void stopServer() throws IOException {
        if (this.isSearching.get()) {
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
     */
    protected void updateClients(GameUpdate update) {
        synchronized(this.connectedClients) {
            for (GameClientHandler client : this.connectedClients) {
                if (!client.clientID.equals(update.originID)) {
                    client.updateWith(update);
                }
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
        synchronized(this.connectedClients) {
            GameClientHandler clientToDisconnect = null;
            for (GameClientHandler client : this.connectedClients) {
                if (client.clientID.equals(deleteClientID)) {
                    clientToDisconnect = client;
                }
            }

            if (clientToDisconnect != null) {
                clientToDisconnect.closeConnection(false);
                this.addAvailableFarm(clientToDisconnect.farmID);
                this.connectedClients.remove(clientToDisconnect);
            }
        }
        this.handlePlayerStateDelete(deleteClientID);
        this.handleFarmStateDelete(deleteClientID);

        this.generatedIDs.remove(deleteClientID);
    }

    /**
     * Sends the {@link ProcessMessage#STOP_SERVER} message to all connected clients.
     * (Even to the host of the server!)
     */
    protected void serverStopping() {
        this.isActive.set(false);

        synchronized(this.connectedClients) {
            GameUpdate stopServer = new GameUpdate(GameUpdateType.SERVER_STOP, this.id);
            for (GameClientHandler client : this.connectedClients) {
                client.updateWith(stopServer);
            }

            this.connectedClients.clear();
        }
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
     * Synchronize gameplay clocks across clients, which also starts the game.
     */
    private void calibrateClocks() {
        Long currentTime = System.currentTimeMillis();
        this.updateClients(new GameUpdate(GameUpdateType.CLOCK_CALIB, this.id, currentTime));
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
        this.playerStates.put(state.getPlayerID(), state);
    }

    /**
     * Delete a {@code PlayerState} with the given ID.
     *
     * @param clientID ID of the {@link PlayerState} to be deleted
     */
    private void handlePlayerStateDelete(Integer clientID) {
        this.playerStates.remove(clientID);
    }

    /**
     * @return Farm ID that has not been used yet. If there is no farm ID left, it returns {@code -1}.
     */
    private int handleFarmRequest() {
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
        this.availableFarms.add(farmID);
    }

    /**
     * Remove a farm ID from the list of available farm IDs.
     *
     * @return Available farm ID
     */
    private int removeAvailableFarm() throws IndexOutOfBoundsException, UnsupportedOperationException {
        int farmsSize = this.availableFarms.size();
        if (farmsSize == 0) {
            return -1;
        }

        int randIndex = ThreadLocalRandom.current().nextInt(farmsSize);

        return this.availableFarms.remove(randIndex);
    }

    /**
     * Handles a given {@code FarmState} update.
     *
     * @param state {@link FarmState} to be processed.
     */
    private void handleFarmStateUpdate(Integer clientID, FarmState state) {
        this.farmStates.put(clientID, state);
    }

    /**
     * Delete a {@code FarmState} with the given ID.
     *
     * @param clientID ID of the client owning the {@link FarmState} to be deleted.
     */
    private void handleFarmStateDelete(Integer clientID) {
        this.farmStates.remove(clientID);
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

                    // (1.) Game session is not active and the number of clients is under the limit
                    synchronized(this.connectedClients) {
                        if (!this.isActive.get() && this.connectedClients.size() < MAX_CONNECTIONS) {
                            int clientID = this.generateClientID();
                            int farmID = this.handleFarmRequest();
                            int[] clientInfo = new int[]{clientID, farmID};
                            String clientSpriteSheet = this.playerSpriteSheetManager.getPlayerSpriteSheet();

                            GameClientHandler client = GameClientHandler.allowConnection(
                                    clientSocket, this,
                                    clientInfo, clientSpriteSheet,
                                    (update) -> this.clientUpdateArrived(update)
                            );

                            if (!this.connectedClients.contains(client)) { // Duplicate clients are NOT added
                                this.connectedClients.add(client);
                                this.sendFirstUpdateToClient(client, this.connectedClients);
                            }

                            HashMap<Integer, String> newClientMap = new HashMap<Integer, String>();
                            newClientMap.put(client.clientID, client.clientName);
                            this.updateClients(new GameUpdate(GameUpdateType.CONNECTED, client.clientID, newClientMap));

                        } else { // (2.) Otherwise deny attempts to connect
                            GameClientHandler.denyConnection(clientSocket, this.id);
                        }
                    }

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

        // Update newly connected client with all current player states
        for (PlayerState playerState : this.playerStates.values()) {
            client.updateWith(new GameUpdate(playerState));
        }

        for (Entry<Integer, FarmState> entry : this.farmStates.entrySet()) {
            GameUpdate gameUpdate = new GameUpdate(GameUpdateType.FARM_STATE, entry.getKey(), entry.getValue());
            client.updateWith(gameUpdate);
        }
    }

    /**
     * Multicast a packet via the {@code searchSocket} on a separate {@link UtilityThread} thread.
     */
    private void startMulticasting() {
        new UtilityThread(() -> {
            while (this.isSearching.get()) {
                try {
                    // Construct packet which will be multicasted (packet contains server name and address)
                    byte[] portBytes = ByteBuffer.allocate(PORT_BYTE_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putShort(this.serverPort).array();
                    byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);

                    byte[] packetBytes = new byte[SERVER_NAME_BYTE_LENGTH + PORT_BYTE_LENGTH];
                    System.arraycopy(portBytes, 0, packetBytes, 0, portBytes.length);
                    System.arraycopy(nameBytes, 0, packetBytes, portBytes.length, nameBytes.length);

                    DatagramPacket packet = new DatagramPacket(
                            packetBytes, packetBytes.length,
                            InetAddress.getByName(HANDSHAKE_GROUP),
                            HANDSHAKE_PORT
                    );

                    this.searchSocket.send(packet);
                    Thread.sleep(MULTICAST_DELAY); // == sends the packet each 0.5 second

                } catch (IOException | InterruptedException | NullPointerException exception) {
                    if (this.isSearching.get()) {
                        exception.printStackTrace();

                    } else {
                        break;
                    }
                }
            }
        }).start();
    }

}
