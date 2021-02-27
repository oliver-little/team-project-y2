package teamproject.wipeout.engine.input;

import javafx.scene.input.MouseButton;

/**
 * InputMouseAction is a functional interface representing any action
 * that will be triggered by a certain {@code MouseEvent}.
 * It is utilised by {@link InputHandler}.
 *
 * @see javafx.scene.input.MouseEvent
 */
@FunctionalInterface
public interface InputClickableAction {
    /**
     * Method representing the action that will be triggered by a certain {@code MouseEvent}.
     *
     * @param x is the X-coordinate at which the {@code MouseEvent} occurred.
     * @param y is the Y-coordinate at which the {@code MouseEvent} occurred.
     */
    void performMouseClickAction(double x, double y, MouseButton mouseButton);
}
