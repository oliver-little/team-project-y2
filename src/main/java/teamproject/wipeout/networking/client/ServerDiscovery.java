package teamproject.wipeout.networking.client;

import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import teamproject.wipeout.networking.server.GameServer;
import teamproject.wipeout.util.threads.UtilityThread;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code ServerDiscovery} allows for finding all the available game servers in the Local Area Network.
 */
public class ServerDiscovery {

    public static final int REFRESH_DELAY = GameServer.MULTICAST_DELAY + 250;

    protected final SimpleMapProperty<String, InetSocketAddress> availableServers;
    protected final HashMap<String, Long> lastHeardServers;
    protected final InetSocketAddress searchGroup;
    protected final AtomicBoolean isActive; // Atomic because of use on multiple threads

    protected MulticastSocket multicastSocket;

    private ScheduledExecutorService executorService;

    /**
     * Default initializer for {@code ServerDiscovery}
     *
     * @throws UnknownHostException When {@code ServerDiscovery} instance cannot be initialized properly.
     */
    public ServerDiscovery() throws UnknownHostException {
        this.availableServers = new SimpleMapProperty<String, InetSocketAddress>(FXCollections.observableHashMap());
        this.lastHeardServers = new HashMap<String, Long>();
        this.searchGroup = new InetSocketAddress(InetAddress.getByName(GameServer.HANDSHAKE_GROUP), GameServer.HANDSHAKE_PORT);
        this.isActive = new AtomicBoolean(false);

        this.multicastSocket = null;

        this.executorService = null;
    }

    /**
     * {@code availableServers} getter
     *
     * @return {@code ObservableMap<String, InetAddress>} of server names and addresses
     */
    public ObservableMap<String, InetSocketAddress> getAvailableServers() {
        return this.availableServers.get();
    }

    /**
     * {@code isActive} getter
     *
     * @return {@code true} if the game server search is ongoing. <br> Otherwise {@code false}.
     */
    public boolean getIsActive() {
        return this.isActive.get();
    }

    /**
     * Starts looking for available game servers. {@link UtilityThread} is utilised for this action.
     *
     * @throws IOException Network problem
     */
    public void startLookingForServers() throws IOException {
        if (!this.isActive.compareAndSet(false, true)) {
            // isActive was already true
            return;
        }
        this.availableServers.clear();
        this.lastHeardServers.clear();

        this.multicastSocket = new MulticastSocket(GameServer.HANDSHAKE_PORT);

        // Start receiving multicasts for all suitable interfaces
        for (NetworkInterface networkInterface : this.getNetworkInterfaces()) {
            this.multicastSocket.joinGroup(this.searchGroup, networkInterface);
        }
        this.receiveMulticasts();

        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleWithFixedDelay(() -> {
            long currentTime = System.currentTimeMillis();
            String removeServerNamed = null;
            for (Map.Entry<String, Long> entry : this.lastHeardServers.entrySet()) {
                if (currentTime - entry.getValue() > REFRESH_DELAY) {
                    removeServerNamed = entry.getKey();
                    break;
                }
            }
            this.lastHeardServers.remove(removeServerNamed);
            this.availableServers.remove(removeServerNamed);

        }, REFRESH_DELAY, REFRESH_DELAY, TimeUnit.MILLISECONDS);
    }

    /**
     * Obtains suitable network interfaces for listening to incoming server "discovery" packets.
     *
     * @return {@code List} of the suitable {@code NetworkInterfaces}.
     * @throws SocketException Network problem / No configured network interfaces.
     */
    private List<NetworkInterface> getNetworkInterfaces() throws SocketException {
        ArrayList<NetworkInterface> multicastInterfaces = new ArrayList<NetworkInterface>();

        // Gather all suitable interfaces for multicasting the game server address
        Enumeration<NetworkInterface> allInterfaces = NetworkInterface.getNetworkInterfaces();
        while (allInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = allInterfaces.nextElement();
            networkInterface.inetAddresses().forEach((inetAddress) -> {
                if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                    multicastInterfaces.add(networkInterface);
                }
            });
        }

        return multicastInterfaces;
    }

    /**
     * Starts receiving packets that are multicasted by available game servers.
     * {@link UtilityThread} is utilised for this action.
     */
    private void receiveMulticasts() {
        new UtilityThread(() -> {
            // Construct the packet which will be used to receive multicast packets
            // (packet contains a server name and an address)
            byte[] packetBytes = new byte[GameServer.SERVER_NAME_BYTE_LENGTH + GameServer.PORT_BYTE_LENGTH];
            DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length);

            while (this.isActive.get()) {
                try {
                    this.multicastSocket.receive(packet);

                    byte[] portBytes = new byte[GameServer.PORT_BYTE_LENGTH];
                    byte[] nameBytes = new byte[GameServer.SERVER_NAME_BYTE_LENGTH];

                    System.arraycopy(packetBytes, 0, portBytes, 0, portBytes.length);
                    System.arraycopy(packetBytes, portBytes.length, nameBytes, 0, nameBytes.length);

                    short serverPort = ByteBuffer.wrap(portBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    String serverName = new String(nameBytes, StandardCharsets.UTF_8).trim();

                    if (!this.availableServers.containsKey(serverName)) {
                        InetAddress serverAddress = packet.getAddress();
                        InetSocketAddress socketAddress = new InetSocketAddress(serverAddress, serverPort);
                        this.availableServers.put(serverName, socketAddress);
                    }

                    this.lastHeardServers.put(serverName, System.currentTimeMillis());

                } catch (IOException exception) {
                    if (this.isActive.get()) {
                        exception.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * Stop looking for available game servers and clear available servers.
     */
    public void stopLookingForServers() {
        this.isActive.set(false);
        this.executorService.shutdown();

        if (this.multicastSocket != null) {
            this.multicastSocket.close();
            this.multicastSocket = null;
        }
    }

}
