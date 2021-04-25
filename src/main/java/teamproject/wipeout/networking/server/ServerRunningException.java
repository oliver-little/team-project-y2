package teamproject.wipeout.networking.server;

/**
 * Exception representing an issue with running a server.
 */
public class ServerRunningException extends Exception {

    /**
     * Exception representing an issue with running a server.
     *
     * @param message Description of the issue
     */
    public ServerRunningException(String message) {
        super(message);
    }

}
