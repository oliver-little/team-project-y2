package teamproject.wipeout.util.threads;

/**
 * Allows you to create and run new server threads.
 * Priority of a background thread is set to 5.
 *
 * @see PriorityThread
 * @see Runnable
 */
public class ServerThread extends PriorityThread {
    /**
     * Creates an instance of {@code ServerThread}.
     * You need to start the thread by yourself!
     *
     * @param block {@link Runnable} which will be executed inside the created thread.
     */
    public ServerThread(Runnable block) {
        super(5, "server", block);
    }
}
