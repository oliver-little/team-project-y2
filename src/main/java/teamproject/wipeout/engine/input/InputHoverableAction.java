package teamproject.wipeout.engine.input;

/**
 * InputHoverableAction is a functional interface representing any action
 * that will be triggered by a mouse hover.
 * It is utilised by {@link InputHandler}.
 *
 */
@FunctionalInterface
public interface InputHoverableAction {
    /**
     * Method representing the action that will be triggered by a mouse hover.
     *
     * @param x is the X-coordinate at which the {@code MouseEvent} occurred.
     * @param y is the Y-coordinate at which the {@code MouseEvent} occurred.
     */
    void performMouseHoverAction(double x, double y);
}
