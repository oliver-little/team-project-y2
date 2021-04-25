package teamproject.wipeout.networking.server;

import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.util.threads.BackgroundThread;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;

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

    private final GameUpdateHandler updater;

    /**
     * Default initializer for {@code GameClientHandler}
     *
     * @param socket   {@link Socket} representing the connection with the client
     * @param serverID ID of the server
     * @param clientID ID of the client
     * @param farmID   ID of client's farm
     * @param updater  {@link GameUpdateHandler} dealing with incoming {@link GameUpdate}s
     * @throws IOException               Thrown when the {@code Socket} cannot be read from(= get updates) or written to(= send updates).
     * @throws ClassNotFoundException    Problem with reading data received from the client.
     * @throws ClientConnectionException Problem with connecting the client.
     */
    protected GameClientHandler(Socket socket, Integer serverID, Integer clientID, Integer farmID, GameUpdateHandler updater)
            throws IOException, ClassNotFoundException, ClientConnectionException {

        this.clientSocket = socket;

        // At first, output stream must be created!
        this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
        // Creating an input stream before the output stream results in an infinite loop!
        this.in = new ObjectInputStream(this.clientSocket.getInputStream());

        // Accept connection
        this.out.writeObject(new GameUpdate(GameUpdateType.ACCEPT, serverID, clientID));
        this.out.reset();

        // Client ID is sent by the client -> sitting in the input stream
        GameUpdate handshake = (GameUpdate) this.in.readObject();
        if (handshake.type != GameUpdateType.ACCEPT) {
            throw new ClientConnectionException("Client did not accept connection");
        }
        if (!clientID.equals(handshake.originID)) {
            throw new ClientConnectionException("Client connection has been altered");
        }

        this.clientID = clientID;
        this.clientName = (String) handshake.content;
        this.farmID = farmID;

        this.updater = updater;
    }

    /**
     * Initializes {@link GameClientHandler} and processes the initial {@link PlayerState} of the newly connected client.
     *
     * @param socket   {@link Socket} representing the connection with the client
     * @param serverID ID of the server
     * @param clientID ID of the client
     * @param farmID   ID of client's farm
     * @param updater  {@link GameUpdateHandler} dealing with incoming {@link GameUpdate}s
     * @throws IOException            Thrown when the {@code Socket} cannot be read from(= get updates)
     *                                or written to(= send updates).
     * @throws ClassNotFoundException Problem with reading data received from the client.
     */
    static public GameClientHandler allowConnection(Socket socket, Integer serverID, Integer clientID, Integer farmID, GameUpdateHandler updater)
            throws IOException, ClassNotFoundException, ClientConnectionException {

        GameClientHandler newInstance = new GameClientHandler(socket, serverID, clientID, farmID, updater);

        newInstance.startReceivingUpdates();

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
        outputStream.reset();
    }

    /**
     * Sends a {@code GameUpdate} to the particular client.
     *
     * @param update {@link GameUpdate} to be sent to the client
     * @throws IOException Thrown when the {@code GameUpdate} cannot be sent.
     */
    public void updateWith(GameUpdate update) throws IOException {
        this.out.writeObject(update);
        this.out.reset(); // TODO Handle stream reset exception
    }

    /**
     * Sends an {@code Collection<PlayerState>} in the form of
     * a multiple {@link GameUpdate}s to the particular client.
     *
     * @param playerStates {@code Collection<PlayerState>} to be sent to the client
     * @throws IOException Thrown when the {@code GameUpdate} cannot be sent.
     */
    public void updateWith(Collection<PlayerState> playerStates) throws IOException {
        for (PlayerState playerState : playerStates.toArray((size) -> new PlayerState[size])) {
            this.out.writeObject(new GameUpdate(playerState));
        }
        this.out.reset();
    }

    /**
     * Disconnects the server from the particular client.
     *
     * @param serverSide Specifies whether the disconnect command came from the server.
     * @throws IOException Thrown when there is a problem with closing the connection.
     */
    public void closeConnection(boolean serverSide) throws IOException {
        if (serverSide) {
            GameUpdate disconnect = new GameUpdate(GameUpdateType.DISCONNECT, this.clientID);
            this.out.writeObject(disconnect);
            this.out.reset();
        }

        if (!this.clientSocket.isClosed()) {
            this.out.close();
            this.in.close();
            this.clientSocket.close();
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
     * The listener is created on a separate {@link BackgroundThread}.
     */
    private void startReceivingUpdates() {
        new BackgroundThread(() -> {
            while (!this.clientSocket.isClosed()) {
                try {
                    GameUpdate receivedUpdate = null;
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
                } catch (IOException | ClassNotFoundException exception) {
                    if (!this.clientSocket.isClosed()) {
                        exception.printStackTrace();
                    } else {
                        break;
                    }
                }
            }
        }).start();
    }

}
