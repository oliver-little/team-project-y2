package teamproject.wipeout.networking.server;

import teamproject.wipeout.networking.data.GameUpdatable;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.state.PlayerState;
import teamproject.wipeout.util.threads.BackgroundThread;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Handler dealing with a client connection on the server.
 * It provides facilities to listen to the updates from the client,
 * and to send updates to the client.
 *
 * @see GameServer
 * @see GameUpdatable
 * @see teamproject.wipeout.networking.client.GameClient
 */
public class GameClientHandler {

    public final Integer clientID;

    protected final Socket clientSocket;
    protected final ObjectInputStream in;
    protected final ObjectOutputStream out;

    protected final GameUpdatable updater;

    /**
     * Default initializer for {@code GameClientHandler}
     *
     * @param socket {@link Socket} representing the connection with the client.
     * @throws IOException Thrown when the {@code Socket} cannot be read from(= get updates),
     *                     written to(= send updates) or when the client declines to connect.
     */
    protected GameClientHandler(Integer serverID, Socket socket, GameUpdatable updater) throws IOException, ClassNotFoundException {
        this.clientSocket = socket;
        this.updater = updater;

        // At first, output stream must be created!
        this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
        // Creating an input stream before the output stream results in an infinite loop!
        this.in = new ObjectInputStream(this.clientSocket.getInputStream());

        // Accept connection
        this.out.writeObject(new GameUpdate(GameUpdateType.ACCEPT, serverID));
        this.out.reset();

        // Client ID is sent by the client -> sitting in the input stream
        GameUpdate handshake = (GameUpdate) this.in.readObject();
        if (handshake.type != GameUpdateType.ACCEPT) {
            throw new IOException("Client did not accepted connection");
        }
        this.clientID = handshake.originClientID;
    }

    /**
     * Static method which initializes {@code GameClientHandler}
     * and handles the initial {@link PlayerState} of the connected client.
     *
     * @param socket {@link Socket} representing the connection with the client.
     * @throws IOException Thrown when the {@code Socket} cannot be read from(= get updates),
     *                     *                     written to(= send updates) or when the client declines to connect.
     */
    static public GameClientHandler allowConnection(Integer serverID, Socket socket, GameUpdatable updater)
            throws IOException, ClassNotFoundException {

        GameClientHandler newInstance = new GameClientHandler(serverID, socket, updater);

        newInstance.startReceivingUpdates();

        return newInstance;
    }

    /**
     * Static method which declines connection with a {@code GameClient}.
     *
     * @param socket {@link Socket} representing the connection with the client.
     * @throws IOException Network problem
     */
    static public void denyConnection(Socket socket, Integer serverID) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        GameUpdate decline = new GameUpdate(GameUpdateType.DECLINE, serverID, null);
        outputStream.writeObject(decline);
        outputStream.reset();
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

                } catch (EOFException theEnd) {
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

    /**
     * Sends a {@code GameUpdate} to the particular connected client.
     *
     * @param update {@link GameUpdate} to be sent to the client.
     * @throws IOException Thrown when the {@code GameUpdate} cannot be sent.
     */
    public void updateWith(GameUpdate update) throws IOException {
        this.out.writeObject(update);
        this.out.reset();
    }

    /**
     * Sends an {@code ArrayList<PlayerState>} in the form of a {@link GameUpdate}
     * to the particular connected client.
     *
     * @param playerStates {@code ArrayList<PlayerState>} to be sent to the client.
     * @throws IOException Thrown when the {@code ArrayList<PlayerState>} cannot be sent.
     */
    public void updateWith(Collection<PlayerState> playerStates) throws IOException {
        for (PlayerState playerState : playerStates.toArray(new PlayerState[0])) {
            this.out.writeObject(new GameUpdate(playerState));
        }
        this.out.reset();
    }

    /**
     * Disconnects the server from the particular client.
     *
     * @param serverSide Specifies whether the disconnect command comes from the server's side.
     * @throws IOException Thrown when there is a problem with closing the connection(= sending
     *                     the disconnect message).
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

}
