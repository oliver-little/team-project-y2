package teamproject.wipeout.networking.client;

import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import teamproject.wipeout.networking.server.GameServer;
import teamproject.wipeout.util.threads.UtilityThread;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code ServerDiscovery} allows for finding all the available game servers in the Local Area Network.
 */
public class ServerDiscovery {

    protected InetSocketAddress searchGroup;
    protected MulticastSocket multicastSocket;
    protected final AtomicBoolean isActive; // Atomic because of use in multiple threads

    protected final SimpleMapProperty<String, InetSocketAddress> availableServers;
    protected final HashMap<String, Long> lastHeardServers;

    private ScheduledExecutorService executorService;

    /**
     * Default initializer for {@code ServerDiscovery}
     *
     * @throws UnknownHostException {@code ServerDiscovery} instance cannot be initialized properly.
     */
    public ServerDiscovery() throws UnknownHostException {
        this.availableServers = new SimpleMapProperty<String, InetSocketAddress>(FXCollections.observableHashMap());
        this.lastHeardServers = new HashMap<String, Long>();

        this.searchGroup = new InetSocketAddress(InetAddress.getByName(GameServer.HANDSHAKE_GROUP), GameServer.HANDSHAKE_PORT);
        this.isActive = new AtomicBoolean(false);
    }

    /**
     * {@code isActive} variable getter
     *
     * @return {@code true} if the game server search is ongoing. <br> Otherwise {@code false}.
     */
    public boolean getIsActive() {
        return this.isActive.get();
    }

    /**
     * {@code availableServers} variable getter
     *
     * @return {@code ObservableMap<String, InetAddress>} of a particular server name and its address
     */
    public ObservableMap<String, InetSocketAddress> getAvailableServers() {
        return this.availableServers.get();
    }

    public static ArrayList<NetworkInterface> getNetworkInterfaces() throws SocketException {
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
     * Starts looking for available game servers using suitable network interfaces.
     * It utilises {@link UtilityThread} for this.
     *
     * @throws IOException Network problem
     */
    public void startLookingForServers() throws IOException {
        if (this.isActive.get()) {
            return;
        }
        this.isActive.set(true);
        this.availableServers.clear();

        ArrayList<NetworkInterface> multicastInterfaces = ServerDiscovery.getNetworkInterfaces();

        this.multicastSocket = new MulticastSocket(GameServer.HANDSHAKE_PORT);

        // Start receiving multicasts for all suitable interfaces
        for (NetworkInterface networkInterface : multicastInterfaces) {
            this.multicastSocket.joinGroup(this.searchGroup, networkInterface);
        }

        this.receiveMulticasts();

        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleWithFixedDelay(() -> {
            long currentTime = System.currentTimeMillis();
            String removeServerNamed = null;
            for (Map.Entry<String, Long> entry : this.lastHeardServers.entrySet()) {
                if (currentTime - entry.getValue() > 505) {
                    removeServerNamed = entry.getKey();
                    break;
                }
            }
            this.lastHeardServers.remove(removeServerNamed);
            this.availableServers.remove(removeServerNamed);

        }, 505, 505, TimeUnit.MILLISECONDS);
    }

    private void receiveMulticasts() {
        new UtilityThread(() -> {
            // Construct packet which will be used to receive multicast packets (packet contains server name and address)
            byte[] nameBytes = new byte[128]; // TODO: Server name limited in length?
            DatagramPacket packet = new DatagramPacket(nameBytes, nameBytes.length);

            try {
                while (this.isActive.get()) {
                    this.multicastSocket.receive(packet);
                    String serverName = new String(nameBytes).trim();

                    if (!this.availableServers.containsKey(serverName)) {
                        InetAddress serverAddress = packet.getAddress();
                        InetSocketAddress socketAddress = new InetSocketAddress(serverAddress, GameServer.GAME_PORT);
                        this.availableServers.put(serverName, socketAddress);
                    }

                    this.lastHeardServers.put(serverName, System.currentTimeMillis());
                }

            } catch (IOException exception) {
                if (this.isActive.get()) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Stops looking for available game servers.
     */
    public void stopLookingForServers() {
        this.isActive.set(false);
        this.executorService.shutdown();
        this.multicastSocket.close();
        this.multicastSocket = null;
    }

}
