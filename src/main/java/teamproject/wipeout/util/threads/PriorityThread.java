package teamproject.wipeout.util.threads;

/**
 * Subclassing {@code PriorityThread} allows you to easily create
 * and run new threads with a given priority (and name).
 *
 * @see Thread
 * @see Runnable
 */
public abstract class PriorityThread extends Thread {

    protected final Runnable block;

    /**
     * Creates a {@code PriorityThread} instance
     * for the subclasses of {@code PriorityThread}.
     *
     * @param priority  {@code int} priority of the thread in range 1-10.
     *                  If the priority is outside that range it is set to the closest bound (1 or 10).
     * @param block     {@link Runnable} which will be executed inside the created thread.
     */
    public PriorityThread(int priority, String name, Runnable block) {
        if (priority < 1) {
            this.setPriority(1);
        } else {
            this.setPriority(Math.min(priority, 10));
        }

        this.setName(name);
        this.block = block;
    }

    @Override
    public void run() {
        this.block.run();
    }

}
