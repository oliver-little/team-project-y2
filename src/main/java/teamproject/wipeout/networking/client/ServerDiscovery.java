package teamproject.wipeout.networking.client;

import teamproject.wipeout.networking.server.GameServer;
import teamproject.wipeout.util.threads.UtilityThread;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code ServerDiscovery} allows for finding all the available game servers in the Local Area Network.
 */
public class ServerDiscovery {

    protected InetSocketAddress searchGroup;
    protected MulticastSocket multicastSocket;
    protected final AtomicBoolean isActive; // Atomic because of use in multiple threads

    protected final HashMap<String, InetSocketAddress> foundServers;
    protected final NewServerDiscovery onDiscovery;

    /**
     * Default initializer for {@code ServerDiscovery}
     *
     * @param onDiscovery Action of type {@link NewServerDiscovery} that will be executed
     *                    when a new available game server is discovered.
     * @throws UnknownHostException {@code ServerDiscovery} instance cannot be initialized properly.
     */
    public ServerDiscovery(NewServerDiscovery onDiscovery) throws UnknownHostException {
        this.foundServers = new HashMap<String, InetSocketAddress>();

        this.searchGroup = new InetSocketAddress(
                InetAddress.getByName(GameServer.HANDSHAKE_GROUP), GameServer.HANDSHAKE_PORT
        );
        this.isActive = new AtomicBoolean(false);

        this.onDiscovery = onDiscovery;
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
     * {@code foundServers} variable getter
     *
     * @return {@code HashMap<String, InetAddress>} of a particular server name and its address
     */
    public HashMap<String, InetSocketAddress> getFoundServers() {
        return this.foundServers;
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
        this.foundServers.clear();

        ArrayList<NetworkInterface> multicastInterfaces = ServerDiscovery.getNetworkInterfaces();

        this.multicastSocket = new MulticastSocket(GameServer.HANDSHAKE_PORT);

        // Start receiving multicasts for all suitable interfaces
        for (NetworkInterface networkInterface : multicastInterfaces) {
            this.multicastSocket.joinGroup(this.searchGroup, networkInterface);
        }

        this.receiveMulticasts();
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

                    if (!this.foundServers.containsKey(serverName)) {
                        InetAddress serverAddress = packet.getAddress();
                        InetSocketAddress socketAddress = new InetSocketAddress(serverAddress, GameServer.GAME_PORT);
                        this.foundServers.put(serverName, socketAddress);
                        this.onDiscovery.discovered(serverName, socketAddress);
                    }
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
        this.multicastSocket.close();
        this.multicastSocket = null;
    }

}
