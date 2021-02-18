package teamproject.wipeout.networking.client;

import java.net.InetAddress;

/**
 * {@code NewServerDiscovery} is a functional interface representing an action that will be executed
 * when a new available game server is discovered.
 */
@FunctionalInterface
public interface NewServerDiscovery {
    /**
     * Method representing the action that will be executed when a new available game server is discovered.
     *
     * @param name   Name of the discovered server
     * @param server {@link InetAddress} of the discovered server
     */
    void discovered(String name, InetAddress server);
}
