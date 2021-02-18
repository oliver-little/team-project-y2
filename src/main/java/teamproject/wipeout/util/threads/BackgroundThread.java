package teamproject.wipeout.util.threads;

/**
 * Allows you to create and run new background threads.
 * Priority of a background thread is set to 3.
 *
 * @see PriorityThread
 * @see Runnable
 */
public class BackgroundThread extends PriorityThread {
    /**
     * Creates an instance of {@code BackgroundThread}.
     * You need to start the thread by yourself!
     *
     * @param block {@link Runnable} which will be executed inside the created thread.
     */
    public BackgroundThread(Runnable block) {
        super(3, "background", block);
    }
}
