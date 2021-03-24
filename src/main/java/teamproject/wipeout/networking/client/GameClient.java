package teamproject.wipeout.networking.client;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.util.Pair;
import teamproject.wipeout.game.entity.AnimalEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.market.Market;
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
import java.util.ArrayList;
import java.util.HashMap;
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

    protected final Socket clientSocket;
    protected final AtomicBoolean isActive; // Atomic because of use in multiple threads

    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    public NewPlayerAction newPlayerAction;

    public final SimpleListProperty<String> connectedClients;
    public final HashMap<Integer, Player> players;
    public AnimalEntity myAnimal;
    public Market market;

    public Consumer<Long> clockCalibration;
    public Consumer<Pair<GameClient, Integer>> myFarmIDReceived;
    public Integer myFarmID;

    protected Map<Integer, FarmEntity> farmEntities;

    private Integer id;

    /**
     * Default initializer for {@code GameClient}
     */
    protected GameClient(String clientName) {
        this.id = null;
        this.clientName = clientName;
        this.clientSocket = new Socket();
        this.isActive = new AtomicBoolean(false);
        this.connectedClients = new SimpleListProperty<String>(FXCollections.observableList(new ArrayList<String>()));
        this.players = new HashMap<Integer, Player>();
        this.farmEntities = null;
    }

    public Integer getID() {
        return this.id;
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
        /*client.myFarmIDReceived = myFarmIDReceived;
        client.newPlayerAction = newPlayerAction;
        client.players.put(player.playerID, player);
        client.farmEntities = farms;*/

        // Connect the socket
        client.clientSocket.connect(server, 3000); // 3s timeout

        client.in = new ObjectInputStream(client.clientSocket.getInputStream());
        client.out = new ObjectOutputStream(client.clientSocket.getOutputStream());

        GameUpdate receivedUpdate = (GameUpdate) client.in.readObject();

        if (receivedUpdate.type == GameUpdateType.DECLINE) {
            return null;
        }

        client.id = (Integer) receivedUpdate.content;
        client.isActive.set(true);

        // Send my ID and my latest playerState
        client.out.writeObject(new GameUpdate(GameUpdateType.ACCEPT, client.id, client.clientName));

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
    public void startReceivingUpdates() {
        new ServerThread(() -> {
            while (this.isActive.get()) {
                try {
                    GameUpdate receivedUpdate = null;
                    Object object = this.in.readObject();
                    if (object instanceof GameUpdate) {
                        receivedUpdate = (GameUpdate) object;
                    } else {
                        continue;
                    }

                    switch (receivedUpdate.type) {
                        case CONNECTED:
                            this.handleReceivedClientConnections((String[]) receivedUpdate.content);
                            break;
                        case PLAYER_STATE:
                            this.handlePlayerStateUpdate((PlayerState) receivedUpdate.content);
                            break;
                        case ANIMAL_STATE:
                            if (!receivedUpdate.originClientID.equals(this.id)) {
                                this.myAnimal.updateFromState((AnimalState) receivedUpdate.content);
                            }
                            break;
                        case FARM_STATE:
                            FarmState fState = (FarmState) receivedUpdate.content;
                            this.farmEntities.get(fState.getFarmID()).updateFromState(fState);
                            break;
                        case FARM_ID:
                            this.myFarmID = (Integer) receivedUpdate.content;
                            //this.myFarmIDReceived.accept(new Pair<GameClient, Integer>(this, farmID));
                            break;
                        case MARKET_STATE:
                            MarketState mState = (MarketState) receivedUpdate.content;
                            this.market.updateFromState(mState);
                            break;
                        case CLOCK_CALIB:
                            if (!receivedUpdate.originClientID.equals(this.id)) {
                                this.clockCalibration.accept((Long) receivedUpdate.content);
                            }
                            break;
                        case RESPONSE:
                            MarketOperationResponse response = (MarketOperationResponse) receivedUpdate.content;
                            this.market.responseArrived(response);
                            break;
                        case DISCONNECT:
                            Integer disconnectedClientID = receivedUpdate.originClientID;
                            if (disconnectedClientID.equals(this.id)) {
                                this.closeConnection(false);
                                return;
                            } else {
                                this.players.remove(disconnectedClientID);
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
                        exception.printStackTrace();
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
        if (!this.players.containsKey(state.getPlayerID())) {
            Player newPlayer = this.newPlayerAction.createWith(state);
            if (newPlayer != null) {
                this.players.put(newPlayer.playerID, newPlayer);
            }
        } else {
            this.players.get(state.getPlayerID()).updateFromState(state);
        }
    }

    private void handleReceivedClientConnections(String[] clients) {
        if (clients.length != 1) {
            this.connectedClients.clear();
        }
        this.connectedClients.addAll(clients);
    }

}
