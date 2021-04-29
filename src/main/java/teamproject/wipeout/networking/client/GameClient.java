package teamproject.wipeout.networking.client;

import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.server.GameServer;
import teamproject.wipeout.networking.state.*;
import teamproject.wipeout.util.threads.ServerThread;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * {@code GameClient} class represents the client-part of the client-server network architecture.
 * It provides you with methods to initialize a client and connect to a particular game server.
 * Use {@link ServerDiscovery} to find available game servers.
 *
 * @see GameServer
 */
public class GameClient {

    public final String clientName;
    public final SimpleMapProperty<Integer, String> connectedClients;
    public final HashSet<PlayerState> tempPlayerStates;

    public String currentPlayerSpriteSheet;
    public Integer myFarmID;
    public Map<Integer, FarmEntity> farmEntities;
    public Consumer<Long> clockCalibration;

    protected final Socket clientSocket;
    protected final AtomicBoolean isActive; // Atomic because of use in multiple threads

    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    private final HashMap<Integer, Player> players;

    private Integer clientID;
    private WorldEntity worldEntity;
    private NewPlayerAction newPlayerAction;
    private Runnable onDisconnect;

    /**
     * Connects to a specified game server and starts
     * listening for incoming updates({@link GameUpdate}) from the game server.
     * It does not allow the client to connect to multiple game servers simultaneously.
     *
     * @param server     {@link InetSocketAddress} of the game server you want to connect to.
     * @param clientName Client's/player's name
     * @throws IOException            Problem with establishing a connection to the given server.
     * @throws ClassNotFoundException Problem with reading data received from the server.
     */
    public static GameClient openConnection(InetSocketAddress server, String clientName)
            throws IOException, ClassNotFoundException {

        GameClient client = new GameClient(clientName);

        // Connect the socket
        client.clientSocket.connect(server, 3000); // 3s timeout

        client.in = new ObjectInputStream(client.clientSocket.getInputStream());
        client.out = new ObjectOutputStream(client.clientSocket.getOutputStream());

        GameUpdate receivedUpdate = (GameUpdate) client.in.readObject();

        if (receivedUpdate.type == GameUpdateType.DECLINE) {
            return null;
        }

        client.clientID = (Integer) receivedUpdate.content;
        client.isActive.set(true);

        // Send my ID and my latest playerState
        client.out.writeObject(new GameUpdate(GameUpdateType.ACCEPT, client.clientID, client.clientName));
        client.out.flush();

        client.startReceivingUpdates();

        return client;
    }

    /**
     * Default initializer for {@code GameClient}
     */
    protected GameClient(String clientName) {
        this.clientName = clientName;
        this.connectedClients = new SimpleMapProperty<Integer, String>(FXCollections.observableHashMap());
        this.tempPlayerStates = new HashSet<PlayerState>();

        this.currentPlayerSpriteSheet = null;
        this.myFarmID = null;
        this.farmEntities = null;
        this.clockCalibration = null;

        this.clientSocket = new Socket();
        this.isActive = new AtomicBoolean(false);
        this.out = null;
        this.in = null;

        this.clientID = null;
        this.worldEntity = null;
        this.newPlayerAction = null;
        this.onDisconnect = null;

        this.players = new HashMap<Integer, Player>();
    }

    /**
     * {@code clientID} variable getter
     *
     * @return Client ID
     */
    public Integer getID() {
        return this.clientID;
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
     * Adds current player to the {@code Map} of connected players.
     *
     * @param currentPlayer {@link CurrentPlayer} that will be added into connected players
     */
    public void addCurrentPlayer(CurrentPlayer currentPlayer) {
        this.players.put(currentPlayer.playerID, currentPlayer);
    }

    /**
     * Sets the current world entity.
     *
     * @param worldEntity {@link WorldEntity} to be set
     */
    public void setWorldEntity(WorldEntity worldEntity) {
        this.worldEntity = worldEntity;
    }

    /**
     * Sets an action that will be executed when a new player connects to the same game server.
     *
     * @param newPlayerAction {@link NewPlayerAction} to be executed
     */
    public void setNewPlayerAction(NewPlayerAction newPlayerAction) {
        this.newPlayerAction = newPlayerAction;
        for (PlayerState state : this.tempPlayerStates) {
            if (!this.players.containsKey(state.getPlayerID())) {
                this.createPlayerFromState(state);
            }
        }
        this.tempPlayerStates.clear();
    }

    /**
     * Sets an action that will be executed when the current player is disconnected from the game server.
     *
     * @param onDisconnect {@link Runnable} to be executed
     */
    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    /**
     * Sends a given {@code GameUpdate} instance to the connected game server.
     * If the client is not connected to a server, nothing happens.
     *
     * @param update Instance of a {@link GameUpdate} to be sent to the connected server
     */
    public void send(GameUpdate update) {
        if (!this.isActive.get()) {
            return;
        }

        try {
            this.out.writeObject(update.deepClone());
            this.out.flush();
        } catch (IOException ignore) {
            this.runOnDisconnect();
            this.closeConnection(false);
        }
    }

    /**
     * Sends a given {@code PlayerState} instance to the connected game server wrapped inside a {@link GameUpdate}.
     * Processes the updated {@code PlayerState} instance inside the {@link GameClient} for gameplay purposes.
     * If the client is not connected to a server, only the processing happens.
     *
     * @param updatedState Instance of a {@link PlayerState} to be sent to the connected server
     */
    public void send(PlayerState updatedState) {
        this.send(new GameUpdate(updatedState));
        this.handlePlayerStateUpdate(updatedState);
    }

    /**
     * Starts listening to updates coming from the connected game server on a separate {@link ServerThread}.
     * Stops when client is disconnected from the game server.
     */
    public void startReceivingUpdates() {
        new ServerThread(() -> {
            while (this.isActive.get()) {
                try {
                    GameUpdate receivedUpdate;
                    Object object = this.in.readObject();
                    if (object instanceof GameUpdate) {
                        receivedUpdate = (GameUpdate) object;
                    } else {
                        continue;
                    }

                    switch (receivedUpdate.type) {
                        case CONNECTED:
                            this.handleReceivedClientConnections((HashMap<Integer, String>) receivedUpdate.content);
                            break;
                        case PLAYER_SPRITE:
                            this.currentPlayerSpriteSheet = (String) receivedUpdate.content;
                            break;
                        case FARM_ID:
                            this.myFarmID = (Integer) receivedUpdate.content;
                            break;
                        case CLOCK_CALIB:
                            this.clockCalibration.accept((Long) receivedUpdate.content);
                            break;
                        case PLAYER_STATE:
                            this.handlePlayerStateUpdate((PlayerState) receivedUpdate.content);
                            break;
                        case ANIMAL_STATE:
                            if (!(receivedUpdate.originID == this.clientID)) {
                                this.worldEntity.myAnimal.updateFromState((AnimalState) receivedUpdate.content);
                            }
                            break;
                        case FARM_STATE:
                            FarmState fState = (FarmState) receivedUpdate.content;
                            this.farmEntities.get(fState.getFarmID()).updateFromState(fState);
                            break;
                        case MARKET_STATE:
                            MarketState mState = (MarketState) receivedUpdate.content;
                            this.worldEntity.getMarket().updateFromState(mState);
                            break;
                        case WORLD_STATE:
                            WorldState wState = (WorldState) receivedUpdate.content;
                            this.worldEntity.updateFromState(wState);
                            break;
                        case DISCONNECT:
                            this.handlePlayerDisconnect(receivedUpdate.originID);
                            break;
                        case SERVER_STOP:
                            if (this.worldEntity != null && !this.worldEntity.isGameplayActive()) {
                                this.runOnDisconnect();
                            }
                            this.closeConnection(false);
                            break;
                        default:
                            break;
                    }

                } catch (OptionalDataException | UTFDataFormatException | StreamCorruptedException ignore) {
                    // Do NOT let one corrupted packet cause the game crash
                } catch (IOException | ClassNotFoundException ignore) {
                    // The server had a "hard disconnect" (= did not send a disconnect signal)/ other malfunction
                    this.runOnDisconnect();
                    this.closeConnection(false);
                    break;
                }
            }
        }).start();
    }

    /**
     * Method which disconnects the client from the game server it is currently connected to.
     * If the client is not connected to a server, nothing happens.
     *
     * @param clientSide Specifies whether the disconnect command comes from the client's side.
     */
    public void closeConnection(boolean clientSide) {
        if (!this.isActive.get()) {
            return;
        }
        this.isActive.set(false);

        if (clientSide) {
            try {
                this.out.writeObject(new GameUpdate(GameUpdateType.DISCONNECT, this.clientID));
                this.out.flush();
            } catch (IOException ignore) {
                // Disconnecting, we don't care about exceptions at this point
            }
        }

        if (!this.clientSocket.isClosed()) {
            try {

                this.out.close();
                this.in.close();
                this.clientSocket.close();
            } catch (IOException ignore) {
                // Disconnecting, we don't care about exceptions at this point
            }
        }

        this.connectedClients.clear();
    }

    /**
     * Processes clients that are currently connected to the same game server.
     *
     * @param clients {@code Map<Integer, String>} of clients - (Integer = Client ID, String = Client name)
     */
    private void handleReceivedClientConnections(Map<Integer, String> clients) {
        if (clients.size() != 1) {
            this.connectedClients.clear();
        }
        this.connectedClients.putAll(clients);
    }

    /**
     * Creates a new {@link Player} from a given {@link PlayerState}.
     *
     * @param playerState {@code PlayerState} used to create a new {@code Player}
     */
    private void createPlayerFromState(PlayerState playerState) {
        Player newPlayer = this.newPlayerAction.createWith(playerState);
        if (newPlayer != null) {
            this.players.put(newPlayer.playerID, newPlayer);
            this.worldEntity.addPlayer(newPlayer);
        }
    }

    /**
     * Processes a given {@link PlayerState} update.
     *
     * @param playerState {@code PlayerState} to be processed
     */
    private void handlePlayerStateUpdate(PlayerState playerState) {
        if (!this.players.containsKey(playerState.getPlayerID())) {
            if (this.newPlayerAction == null) {
                this.tempPlayerStates.add(playerState);
                return;
            }
            this.createPlayerFromState(playerState);

        } else {
            this.players.get(playerState.getPlayerID()).updateFromState(playerState);
        }
    }

    /**
     * Handles disconnecting of any player(= client).
     *
     * @param disconnectedClientID {@code PlayerState} to be processed
     */
    private void handlePlayerDisconnect(Integer disconnectedClientID) {
        if (disconnectedClientID.equals(this.clientID)) {
            this.runOnDisconnect();
            this.closeConnection(false);

        } else {
            this.connectedClients.remove(disconnectedClientID);
            Player removedPlayer = this.players.remove(disconnectedClientID);
            if (this.worldEntity != null && removedPlayer != null) {
                this.worldEntity.removePlayer(removedPlayer);
            }
        }
    }

    /**
     * Executes {@code Runnable onDisconnect} action if it is not null.
     */
    private void runOnDisconnect() {
        if (this.onDisconnect != null) {
            this.onDisconnect.run();
        }
    }

}
