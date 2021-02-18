package teamproject.wipeout.util.threads;

/**
 * Allows you to create and run new utility threads.
 * Priority of a background thread is set to 4.
 *
 * @see PriorityThread
 * @see Runnable
 */
public class UtilityThread extends PriorityThread {
    /**
     * Creates an instance of {@code UtilityThread}.
     * You need to start the thread by yourself!
     *
     * @param block {@link Runnable} which will be executed inside the created thread.
     */
    public UtilityThread(Runnable block) {
        super(4, "utility", block);
    }
}
