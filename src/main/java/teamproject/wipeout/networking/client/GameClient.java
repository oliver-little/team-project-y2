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
import java.net.InetAddress;
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

    public Integer myFarmID;
    public Map<Integer, FarmEntity> farmEntities;
    public Consumer<Long> clockCalibration;

    protected final Socket clientSocket;
    protected final AtomicBoolean isActive; // Atomic because of use in multiple threads
    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    private Integer clientID;
    private WorldEntity worldEntity;
    private NewPlayerAction newPlayerAction;
    private Runnable onDisconnect;

    private final HashMap<Integer, Player> players;

    /**
     * Default initializer for {@code GameClient}
     */
    protected GameClient(String clientName) {
        this.clientName = clientName;
        this.connectedClients = new SimpleMapProperty<Integer, String>(FXCollections.observableHashMap());
        this.tempPlayerStates = new HashSet<PlayerState>();

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

    public void addCurrentPlayer(CurrentPlayer currentPlayer) {
        this.players.put(currentPlayer.playerID, currentPlayer);
    }

    public void setNewPlayerAction(NewPlayerAction newPlayerAction) {
        this.newPlayerAction = newPlayerAction;
        for (PlayerState state : this.tempPlayerStates) {
            if (!this.players.containsKey(state.getPlayerID())) {
                this.createPlayerFromState(state);
            }
        }
        this.tempPlayerStates.clear();
    }

    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    /**
     * Connects to a specified game server, sends client's ID and starts
     * listening for incoming updates({@link GameUpdate}) from the game server.
     * It does not allow the client to connect to multiple game servers simultaneously.
     *
     * @param server {@link InetAddress} of the game server you want to connect to.
     * @param clientName Player's name
     * @throws IOException Problem with establishing a connection to the given server.
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

        client.startReceivingUpdates();

        return client;
    }

    public void setWorldEntity(WorldEntity worldEntity) {
        this.worldEntity = worldEntity;
    }

    /**
     * Sends a given {@code GameUpdate} instance to the connected game server.
     * If the client is not connected to a server, nothing happens.
     *
     * @param update Instance of a {@link GameUpdate} to be sent to the connected server
     * @throws IOException Problem with sending the given {@code GameUpdate} to the connected server.
     */
    public void send(GameUpdate update) {
        if (!this.isActive.get()) {
            return;
        }
        try {
            this.out.writeObject(update);
            this.out.reset();
        } catch (IOException ignore) {
            this.runOnDisconnect();
            this.closeConnection(false);
        }
    }

    /**
     * Sends a given {@code PlayerState} instance to the connected game server wrapped inside a {@link GameUpdate}.
     * If the client is not connected to a server, nothing happens.
     *
     * @param updatedState Instance of a {@link PlayerState} to be sent to the connected server
     */
    public void send(PlayerState updatedState) {
        this.send(new GameUpdate(updatedState));
        this.handlePlayerStateUpdate(updatedState);
    }

    /**
     * Method which disconnects the client from the game server it is currently connected to.
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
                this.out.reset();
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
     * Starts listening to updates coming from the game server
     * on a separate {@link ServerThread}.
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
                        case PLAYER_STATE:
                            this.handlePlayerStateUpdate((PlayerState) receivedUpdate.content);
                            break;
                        case ANIMAL_STATE:
                            if (!receivedUpdate.originClientID.equals(this.clientID)) {
                                this.worldEntity.myAnimal.updateFromState((AnimalState) receivedUpdate.content);
                            }
                            break;
                        case FARM_STATE:
                            FarmState fState = (FarmState) receivedUpdate.content;
                            this.farmEntities.get(fState.getFarmID()).updateFromState(fState);
                            break;
                        case FARM_ID:
                            this.myFarmID = (Integer) receivedUpdate.content;
                            break;
                        case MARKET_STATE:
                            MarketState mState = (MarketState) receivedUpdate.content;
                            this.worldEntity.getMarket().updateFromState(mState);
                            break;
                        case WORLD_STATE:
                            WorldState wState = (WorldState) receivedUpdate.content;
                            this.worldEntity.updateFromState(wState);
                            break;
                        case CLOCK_CALIB:
                            this.clockCalibration.accept((Long) receivedUpdate.content);
                            break;
                        case RESPONSE:
                            MarketOperationResponse response = (MarketOperationResponse) receivedUpdate.content;
                            this.worldEntity.getMarket().responseArrived(response);
                            break;
                        case DISCONNECT:
                            this.handleDisconnectPlayer(receivedUpdate.originClientID);
                            break;
                        case SERVER_STOP:
                            if (!this.worldEntity.isGameplayActive()) {
                                this.runOnDisconnect();
                            }
                            this.closeConnection(false);
                            break;
                        default:
                            break;
                    }

                } catch (OptionalDataException | UTFDataFormatException | StreamCorruptedException ignore) {
                    // Do NOT let one corrupted packet cause the game to crash
                } catch (IOException | ClassNotFoundException ignore) {
                    // The server had a "hard disconnect" (= did not send a disconnect signal)
                    this.runOnDisconnect();
                    this.closeConnection(false);
                    break;
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
        if (!this.players.containsKey(state.getPlayerID())) {
            if (this.newPlayerAction == null) {
                this.tempPlayerStates.add(state);
                return;
            }
            this.createPlayerFromState(state);

        } else {
            this.players.get(state.getPlayerID()).updateFromState(state);
        }
    }

    private void createPlayerFromState(PlayerState state) {
        Player newPlayer = this.newPlayerAction.createWith(state);
        if (newPlayer != null) {
            this.players.put(newPlayer.playerID, newPlayer);
            this.worldEntity.addPlayer(newPlayer);
        }
    }

    private void handleReceivedClientConnections(Map<Integer, String> clients) {
        if (clients.size() != 1) {
            this.connectedClients.clear();
        }
        this.connectedClients.putAll(clients);
    }

    private void handleDisconnectPlayer(Integer disconnectedClientID) {
        if (disconnectedClientID.equals(this.clientID)) {
            this.runOnDisconnect();
            this.closeConnection(false);
        } else {
            this.connectedClients.remove(disconnectedClientID);
            Player removedPlayer = this.players.remove(disconnectedClientID);
            if (this.worldEntity != null) {
                this.worldEntity.removePlayer(removedPlayer);
            }
        }
    }

    private void runOnDisconnect() {
        if (this.onDisconnect != null) {
            this.onDisconnect.run();
        }
    }

}
