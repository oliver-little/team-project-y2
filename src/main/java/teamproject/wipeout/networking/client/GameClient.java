package teamproject.wipeout.networking.client;

import teamproject.wipeout.game.logic.PlayerState;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.server.GameServer;
import teamproject.wipeout.util.threads.ServerThread;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code GameClient} class represents the client-part of the client-server network architecture.
 * It provides you with methods to initialize a client and connect to a particular game server.
 * Use {@link ServerDiscovery} to find available game servers.
 *
 * @see GameServer
 */
public class GameClient {

    public final String id;

    protected final Socket clientSocket;
    protected final AtomicBoolean isActive; // Atomic because of use in multiple threads

    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    protected final ArrayList<PlayerState> playerStates;

    /**
     * Default initializer for {@code GameClient}
     *
     * @param id ID of the current player (= client)
     */
    protected GameClient(String id) {
        this.id = id;
        this.clientSocket = new Socket();
        this.isActive = new AtomicBoolean(false);
        this.playerStates = new ArrayList<PlayerState>();
    }

    /**
     * {@code isActive} variable getter
     *
     * @return {@code true} if the client is active. <br> Otherwise {@code false}.
     */
    public boolean getIsActive() {
        return this.isActive.get();
    }

    /**
     * {@code playerStates} variable getter. Can be also an empty {@code ArrayList}.
     *
     * @return {@code ArrayList<PlayerState>} of the latest available player states received from the game server.
     * @see PlayerState
     */
    public ArrayList<PlayerState> getPlayerStates() {
        // Return playerStates without the client's playerState
        // Predicate<PlayerState> predicate = (playerState) -> !playerState.getID().equals(this.id);
        // Stream<PlayerState> filteredPlayers = this.playerStates.stream().filter(predicate);
        return this.playerStates; // filteredPlayers.collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Connects to a specified game server, sends client's ID and starts
     * listening for incoming updates({@link GameUpdate}) from the game server.
     * It does not allow the client to connect to multiple game servers simultaneously.
     *
     * @param playerState Current state of the player in the form of a {@link PlayerState}.
     * @param server      {@link InetAddress} of the game server you want to connect to.
     * @throws IOException Problem with establishing a connection to the given server.
     */
    public static GameClient openConnection(PlayerState playerState, InetSocketAddress server) throws IOException, ClassNotFoundException {
        GameClient client = new GameClient(playerState.getID());

        // Connect the socket
        client.clientSocket.connect(server, 3000); // 3s timeout

        client.in = new ObjectInputStream(client.clientSocket.getInputStream());
        client.out = new ObjectOutputStream(client.clientSocket.getOutputStream());

        GameUpdateType acceptedStatus = ((GameUpdate) client.in.readObject()).type;

        if (acceptedStatus == GameUpdateType.DECLINE) {
            return null;
        }

        client.isActive.set(true);

        // Send my ID and my latest playerState
        client.out.writeObject(new GameUpdate(GameUpdateType.ACCEPT, client.id));
        client.send(new GameUpdate(playerState));

        client.startReceivingUpdates();

        return client;
    }

    /**
     * Sends a given {@code GameUpdate} instance to the connected game server.
     * If the client is not connected to a server, nothing happens.
     *
     * @param update Instance of a {@link GameUpdate} to be sent to the connected server
     * @throws IOException Problem with sending the given {@code GameUpdate} to the connected server.
     */
    public void send(GameUpdate update) throws IOException {
        if (!this.isActive.get()) {
            return;
        }
        this.out.writeObject(update);
        this.out.reset();
    }

    /**
     * Sends a given {@code PlayerState} instance to the connected game server wrapped inside a {@link GameUpdate}.
     * If the client is not connected to a server, nothing happens.
     *
     * @param updatedState Instance of a {@link PlayerState} to be sent to the connected server
     * @throws IOException Problem with sending the given {@code GameUpdate} to the connected server.
     */
    public void send(PlayerState updatedState) throws IOException {
        if (!this.isActive.get()) {
            return;
        }
        this.out.writeObject(new GameUpdate(updatedState));
        this.out.reset();

        this.handlePlayerStateUpdate(updatedState.carbonCopy());
    }

    /**
     * Method which disconnects the client from the game server it is currently connected to.
     *
     * @param clientSide Specifies whether the disconnect command comes from the client's side.
     * @throws IOException Problem with closing the connection to the server.
     */
    public void closeConnection(boolean clientSide) throws IOException {
        if (!this.isActive.get()) {
            return;
        }
        this.isActive.set(false);

        if (clientSide) {
            this.out.writeObject(new GameUpdate(GameUpdateType.DISCONNECT, this.id));
            this.out.reset();
        }

        if (!this.clientSocket.isClosed()) {
            this.out.close();
            this.in.close();
            this.clientSocket.close();
        }
    }

    /**
     * Starts listening to updates coming from the game server
     * on a separate {@link ServerThread}.
     */
    private void startReceivingUpdates() {
        new ServerThread(() -> {
            while (this.isActive.get()) {
                try {
                    GameUpdate receivedUpdate = (GameUpdate) in.readObject();
                    switch (receivedUpdate.type) {
                        case PLAYER_STATE:
                            this.handlePlayerStateUpdate((PlayerState) receivedUpdate.content);
                            break;
                        case DISCONNECT:
                            String disconnectedClientID = receivedUpdate.originClientID;
                            if (disconnectedClientID.equals(this.id)) {
                                this.closeConnection(false);
                                return;
                            } else {
                                this.playerStates.removeIf((state) -> state.getID().equals(disconnectedClientID));
                            }
                            break;
                        default:
                            break;
                    }

                } catch (OptionalDataException | StreamCorruptedException ignore) {
                    // Do NOT let one corrupted packet cause the game to crash
                } catch (EOFException ignore) {
                    // The server had a "hard disconnect" (= did not send a disconnect signal)
                    break;
                } catch (IOException | ClassNotFoundException exception) {
                    if (this.isActive.get()) {
                        try {
                            this.in.reset();

                        } catch (IOException resetException) {
                            exception.printStackTrace();
                            resetException.printStackTrace();
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }).start();
    }

    /**
     * Processes a given {@link PlayerState} update.
     *
     * @param state {@code PlayerState} to be processed.
     */
    private void handlePlayerStateUpdate(PlayerState state) {
        int pIndex = this.playerStates.indexOf(state);
        if (pIndex < 0) {
            // Add to the ArrayList if the state is totally new
            this.playerStates.add(state);
        } else {
            // Otherwise, update an existing state in the ArrayList
            this.playerStates.set(pIndex, state);
        }
    }

}
