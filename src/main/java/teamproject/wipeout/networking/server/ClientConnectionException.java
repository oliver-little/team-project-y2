package teamproject.wipeout.networking.server;

/**
 * Exception representing an issue with client connection.
 */
public class ClientConnectionException extends Exception {

    /**
     * Exception representing an issue with client connection.
     *
     * @param message Description of the issue
     */
    public ClientConnectionException(String message) {
        super(message);
    }

}