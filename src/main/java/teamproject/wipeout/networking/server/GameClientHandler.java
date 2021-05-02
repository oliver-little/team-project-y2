package teamproject.wipeout.networking.server;

import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.data.InitContainer;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.util.threads.UtilityThread;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handler dealing with a client connection on the server.
 * It provides facilities to listen to the updates from the client,
 * and to send updates back to the client.
 *
 * @see GameServer
 * @see GameUpdateHandler
 * @see teamproject.wipeout.networking.client.GameClient
 */
public class GameClientHandler {

    public final Integer clientID;
    public final String clientName;
    public final Integer farmID;

    protected final Socket clientSocket;
    protected final ObjectInputStream in;
    protected final ObjectOutputStream out;

    private final ConcurrentLinkedQueue<GameUpdate> sendQueue;
    private final GameUpdateHandler updater;

    /**
     * Initializes {@link GameClientHandler} and processes the initial {@link PlayerState} of the newly connected client.
     *
     * @param socket            {@link Socket} representing the connection with the client
     * @param server            Current {@link GameServer} instance
     * @param clientInfo        ID of the client and ID of client's farm
     * @param clientSpriteSheet Chosen sprite sheet for the client
     * @param updater           {@link GameUpdateHandler} dealing with incoming {@link GameUpdate}s
     * @throws IOException            Thrown when the {@code Socket} cannot be read from(= get updates)
     *                                or written to(= send updates).
     * @throws ClassNotFoundException Problem with reading data received from the client.
     */
    static public GameClientHandler allowConnection(Socket socket, GameServer server, int[] clientInfo, String clientSpriteSheet, GameUpdateHandler updater)
            throws IOException, ClassNotFoundException, ClientConnectionException {

        GameClientHandler newInstance = new GameClientHandler(socket, server, clientInfo, clientSpriteSheet, updater);

        newInstance.startReceivingUpdates();
        newInstance.startSendingUpdates();

        return newInstance;
    }

    /**
     * Declines connection with a {@code GameClient} through a given socket of the client.
     *
     * @param socket   {@link Socket} representing the connection with the client
     * @param serverID ID of the server
     * @throws IOException Network problem
     */
    static public void denyConnection(Socket socket, Integer serverID) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        GameUpdate declineUpdate = new GameUpdate(GameUpdateType.DECLINE, serverID);

        outputStream.writeObject(declineUpdate);
        outputStream.flush();
    }

    /**
     * Default initializer for {@code GameClientHandler}
     *
     * @param socket            {@link Socket} representing the connection with the client
     * @param server            Current {@link GameServer} instance
     * @param clientInfo        ID of the client and ID of client's farm
     * @param clientSpriteSheet Chosen sprite sheet for the client
     * @param updater           {@link GameUpdateHandler} dealing with incoming {@link GameUpdate}s
     * @throws IOException               Thrown when the {@code Socket} cannot be read from(= get updates) or written to(= send updates).
     * @throws ClassNotFoundException    Problem with reading data received from the client.
     * @throws ClientConnectionException Problem with connecting the client.
     */
    protected GameClientHandler(Socket socket, GameServer server, int[] clientInfo, String clientSpriteSheet, GameUpdateHandler updater)
            throws IOException, ClassNotFoundException, ClientConnectionException {

        int clientID = clientInfo[0];
        int farmID = clientInfo[1];

        this.clientSocket = socket;

        // At first, output stream must be created!
        this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
        // Creating an input stream before the output stream results in an infinite loop!
        this.in = new ObjectInputStream(this.clientSocket.getInputStream());

        // Accept connection
        InitContainer initContainer = new InitContainer(server.gameMode, server.gameModeValue, clientID, farmID, clientSpriteSheet);
        this.out.writeObject(new GameUpdate(GameUpdateType.ACCEPT, server.id, initContainer));
        this.out.flush();

        // Client ID is sent by the client -> sitting in the input stream
        GameUpdate handshake = (GameUpdate) this.in.readObject();

        if (handshake.type != GameUpdateType.ACCEPT) {
            throw new ClientConnectionException("Client did not accept connection");
        }

        if (clientID != handshake.originID) {
            throw new ClientConnectionException("Client connection has been altered");
        }

        this.clientID = clientID;
        this.clientName = (String) handshake.content;
        this.farmID = farmID;

        this.sendQueue = new ConcurrentLinkedQueue<GameUpdate>();
        this.updater = updater;
    }

    /**
     * Sends a {@code GameUpdate} to the particular client.
     *
     * @param update {@link GameUpdate} to be sent to the client
     */
    public void updateWith(GameUpdate update) {
        this.sendQueue.add(update.deepClone());
    }

    /**
     * Disconnects the server from the particular client.
     *
     * @param serverSide Specifies whether the disconnect command came from the server.
     */
    public void closeConnection(boolean serverSide) {
        this.sendQueue.clear();

        if (serverSide) {
            GameUpdate disconnect = new GameUpdate(GameUpdateType.DISCONNECT, this.clientID);
            this.sendQueue.add(disconnect);

        } else {
            this.terminateConnection();
        }
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameClientHandler that = (GameClientHandler) o;
        return this.clientID.equals(that.clientID);
    }

    @Override
    public int hashCode() {
        return this.clientID.hashCode();
    }

    /**
     * Starts listening to updates coming from the particular client.
     * The listener is created on a separate {@link UtilityThread}.
     */
    private void startReceivingUpdates() {
        new UtilityThread(() -> {
            while (!this.clientSocket.isClosed()) {
                try {
                    GameUpdate receivedUpdate;
                    Object object = this.in.readObject();
                    if (object instanceof GameUpdate) {
                        receivedUpdate = (GameUpdate) object;
                    } else {
                        continue;
                    }
                    this.updater.updateWith(receivedUpdate);

                    if (receivedUpdate.type == GameUpdateType.DISCONNECT) {
                        break;
                    }

                } catch (EOFException ignore) {
                    // The client had a "hard disconnect" (= did not send a disconnect signal)
                    break;

                } catch (IOException | ClassNotFoundException ignore) {
                    if (!this.clientSocket.isClosed()) {
                        try {
                            this.in.reset();

                        } catch (IOException e) {
                            // Do NOT let one corrupted packet cause the game crash
                        }

                    } else {
                        break;
                    }
                }
            }
        }).start();
    }

    /**
     * Starts sending updates from the server back to the client.
     * Updates to be sent are taken from {@code LinkedList<GameUpdate> sendQueue} (FIFO order).
     * The updates are sent on a separate {@link UtilityThread}.
     */
    private void startSendingUpdates() {
        new UtilityThread(() -> {
            while (!this.clientSocket.isClosed()) {
                try {
                    GameUpdate gameUpdate;
                    if ((gameUpdate = this.sendQueue.poll()) == null) {
                        continue;
                    }

                    this.out.writeObject(gameUpdate);
                    this.out.flush();

                    if (gameUpdate.type == GameUpdateType.DISCONNECT && gameUpdate.originID == this.clientID) {
                        this.terminateConnection();
                        break;
                    }

                } catch (EOFException ignore) {
                    // The client had a "hard disconnect" (= did not send a disconnect signal).
                    break;

                } catch (IOException ignore) {
                    if (!this.clientSocket.isClosed()) {
                        try {
                            this.out.reset();

                        } catch (IOException e) {
                            // Do NOT let one corrupted packet cause the game crash
                        }

                    } else {
                        break;
                    }
                }
            }
        }).start();
    }

    /**
     * Closes client socket and client object streams.
     */
    private void terminateConnection() {
        try {
            this.out.close();
            this.in.close();
            this.clientSocket.close();

        } catch (IOException ignore) {
            // We don't care about the client anymore
        }
    }

}
