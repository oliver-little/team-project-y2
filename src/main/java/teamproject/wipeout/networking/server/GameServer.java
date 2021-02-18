package teamproject.wipeout.networking.server;

import teamproject.wipeout.game.logic.PlayerState;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.util.threads.BackgroundThread;
import teamproject.wipeout.util.threads.ServerThread;
import teamproject.wipeout.util.threads.UtilityThread;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
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
    public static final int HANDSHAKE_PORT = 9919;
    public static final String HANDSHAKE_GROUP = "229.1.2.3";
    public static final int GAME_PORT = 9913;
    public static final int MAX_CONNECTIONS = 6;
    public static final int MULTICAST_DELAY = 500; // = 0.5 second

    public final String name;

    protected DatagramSocket searchSocket;
    protected final AtomicBoolean isSearching;

    protected final ServerSocket serverSocket;
    protected final AtomicBoolean isActive;
    protected ServerThread newConnectionsThread;

    protected final AtomicReference<ArrayList<PlayerState>> playerStates;
    protected final AtomicReference<HashSet<GameClientHandler>> connectedClients;

    // Atomic variables above used because of multi-threading

    /**
     * Default initializer for {@code GameServer}.
     * The server is created and starts accepting new connections.
     *
     * @param name Name we want to give to the {@code GameServer}.
     * @throws IOException Network problem
     */
    public GameServer(String name) throws IOException {
        this.name = name;

        this.isSearching = new AtomicBoolean(false);

        this.serverSocket = new ServerSocket(GAME_PORT);
        this.isActive = new AtomicBoolean(false);

        this.playerStates = new AtomicReference<ArrayList<PlayerState>>(new ArrayList<PlayerState>());
        this.connectedClients = new AtomicReference<HashSet<GameClientHandler>>(new HashSet<GameClientHandler>());

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

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Starts multicasting the server's IP address using suitable network interfaces.
     *
     * @throws IOException Network problem
     */
    public void startClientSearch() throws IOException {
        if (this.isSearching.get()) {
            return;
        }
        this.isSearching.set(true);

        this.searchSocket = new DatagramSocket();

        // Construct packet which will be multicasted (packet contains server name and address)
        byte[] nameBytes = this.name.getBytes();
        DatagramPacket packet = new DatagramPacket(
                nameBytes, nameBytes.length,
                InetAddress.getByName(HANDSHAKE_GROUP),
                HANDSHAKE_PORT
        );

        this.startMulticasting(packet);
    }

    /**
     * Stops multicasting the server's IP address.
     */
    public void stopClientSearch() {
        this.isSearching.set(false);
        this.searchSocket.close();
        this.searchSocket = null;
    }

    /**
     * Starts a game session and rejects all new connection attempts.
     */
    public void startNewGame() {
        if (this.isActive.get()) {
            return;
        }

        this.isActive.set(true);
    }

    /**
     * Stops a running game session and starts accepting new connection attempts.
     */
    public void stopGame() {
        this.isActive.set(false);
        this.playerStates.set(new ArrayList<PlayerState>());
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
    public void disconnectClient(String deleteClientID, boolean serverSide) throws IOException {
        HashSet<GameClientHandler> clientHandlers = this.connectedClients.getAcquire();

        for (GameClientHandler client : clientHandlers) {
            if (client.clientID.equals(deleteClientID)) {
                client.closeConnection(serverSide);
            }
        }
        clientHandlers.removeIf((client) -> client.clientID.equals(deleteClientID));
        this.handlePlayerStateDelete(deleteClientID);

        this.connectedClients.setRelease(clientHandlers);
    }

    /**
     * Disconnects all connected clients (even the owner of the server!).
     *
     * @throws IOException Problem with closing connections
     */
    public void disconnectClients() throws IOException {
        HashSet<GameClientHandler> clientHandlers = this.connectedClients.getAcquire();

        for (GameClientHandler client : clientHandlers) {
            client.closeConnection(true);
        }
        this.playerStates.set(new ArrayList<PlayerState>());

        this.connectedClients.setRelease(new HashSet<GameClientHandler>());
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

        if (this.isActive.get()) {
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
                String line;
                while ((line = reader.readLine()) != null) {
                    ProcessMessage message = ProcessMessage.fromRawValue(line);
                    if (message == null) {
                        continue;
                    }
                    switch (message) {
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
                System.out.println(exception.toString());
            }
        }).start();
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
        ArrayList<PlayerState> currentPlayerStates = this.playerStates.getAcquire();
        int pIndex = currentPlayerStates.indexOf(state);
        if (pIndex < 0) {
            currentPlayerStates.add(state);
        } else {
            currentPlayerStates.set(pIndex, state);
        }
        this.playerStates.setRelease(currentPlayerStates);
    }

    /**
     * Delete a {@code PlayerState} with the given ID.
     *
     * @param stateID ID of the {@link PlayerState} to be deleted.
     */
    private void handlePlayerStateDelete(String stateID) {
        ArrayList<PlayerState> currentPlayerStates = this.playerStates.getAcquire();
        currentPlayerStates.removeIf((state) -> state.getID().equals(stateID));
        this.playerStates.setRelease(currentPlayerStates);
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
                    HashSet<GameClientHandler> clientHandlers = this.connectedClients.getAcquire();

                    // (1.) Game session is not active and the number of clients is under the limit
                    if (!this.isActive.get() && clientHandlers.size() < MAX_CONNECTIONS) {
                        GameClientHandler client =
                                GameClientHandler.allowConnection(this.name, clientSocket, this::clientUpdateArrived);

                        if (clientHandlers.add(client)) { // Duplicate clients are NOT added
                            client.updateWith(this.playerStates.get());
                        }

                    } else { // (2.) Otherwise deny attempts to connect
                        GameClientHandler.denyConnection(clientSocket);
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

    /**
     * Broadcasts the given packet via the given socket on a separate {@link UtilityThread} thread.
     *
     * @param packet       {@link DatagramPacket} to be multicasted
     */
    private void startMulticasting(DatagramPacket packet) {
        new UtilityThread(() -> {
            while (this.isSearching.get()) {
                try {
                    this.searchSocket.send(packet);
                    Thread.sleep(MULTICAST_DELAY); // == sends the packet each 0.5 second
                } catch (IOException | InterruptedException exception) {
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
