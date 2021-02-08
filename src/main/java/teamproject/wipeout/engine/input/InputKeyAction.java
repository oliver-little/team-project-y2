package teamproject.wipeout.engine.input;

/**
 * InputKeyAction is an interface representing any action that will be triggered by a certain {@code KeyEvent}.
 * It is utilised by {@link InputHandler}.
 *
 * @see javafx.scene.input.KeyEvent
 */
@FunctionalInterface
public interface InputKeyAction {
    /** Method representing the action that will be triggered by a certain {@code KeyEvent}. */
    void performKeyAction();
}
