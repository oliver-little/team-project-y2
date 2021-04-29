package teamproject.wipeout.engine.input;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import teamproject.wipeout.engine.system.input.MouseHoverSystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * InputHandler is a class for dealing with the keyboard and mouse input of a given {@code Scene}.
 * All input event listeners can be temporarily disabled.
 *
 * @see InputKeyAction
 * @see InputClickableAction
 */
public class InputHandler {

    public MouseHoverSystem mouseHoverSystem;

    private final Scene inputScene;

    // Set of keys that are being pressed at a certain point in time.
    // Used to prevent repeated performKeyAction calls.
    private final HashSet<KeyCode> performingKeyActions;

    // Set of key press and release bindings, used to simulate keys when input is enabled/disabled.
    private final HashMap<KeyCode, Set<InputKeyAction>> keyPressBindings;
    private final HashMap<KeyCode, Set<InputKeyAction>> keyReleaseBindings;

    private boolean disableInput;

    /**
     * Default InputHandler initializer.
     *
     * @param scene {@code Scene} whose input will be listened to.
     */
    public InputHandler(Scene scene) {
        this.inputScene = scene;
        this.disableInput = false;

        this.performingKeyActions = new HashSet<KeyCode>();
        this.keyPressBindings = new HashMap<>();
        this.keyReleaseBindings = new HashMap<>();

        // Register keyPress listener
        this.inputScene.addEventFilter(KeyEvent.KEY_PRESSED, (pressedKey) -> {
            KeyCode pressedKeyCode = pressedKey.getCode();

            // Do nothing if the key action was already performed.
            if (this.performingKeyActions.contains(pressedKeyCode)) {
                return;
            }

            this.performingKeyActions.add(pressedKeyCode);

            // If input is enabled
            if (!this.disableInput) {
                // Complete all actions bound for this key
                if (keyPressBindings.get(pressedKeyCode) != null) {
                    for (InputKeyAction action : this.keyPressBindings.get(pressedKeyCode)) {
                        action.performKeyAction();
                    }
                }
            }
        });

        // Register keyRelease listener
        this.inputScene.addEventFilter(KeyEvent.KEY_RELEASED, (pressedKey) -> {
            KeyCode pressedKeyCode = pressedKey.getCode();

            // Do nothing if the game is not aware this key was pressed
            if (this.performingKeyActions.remove(pressedKeyCode)) {
                // If input is enabled
                if (!this.disableInput) {
                    // Complete all actions bound for this key
                    if (keyReleaseBindings.get(pressedKeyCode) != null) {
                        for (InputKeyAction action : this.keyReleaseBindings.get(pressedKeyCode)) {
                            action.performKeyAction();
                        }
                    }
                }
            }
        });
    }

    /**
     * Disables/enables the game input depending on the input parameter.
     *
     * @param disabled New value of the {@code disableInput} variable
     */
    public void disableInput(boolean disabled) {
        this.setDisableInput(disabled).run();
    }

    /**
     * {@code disableInput} setter
     *
     * @param disabled New value of the {@code disableInput} variable
     * @return A runnable to complete the action of disabling/enabling input when run
     */
    public Runnable setDisableInput(boolean disabled) {
        return () -> {
            this.disableInput = disabled;
            if (disabled) {
                // Simulate all keys releasing
                for (KeyCode key : this.performingKeyActions) {
                    if (this.keyReleaseBindings.get(key) != null) {
                        for (InputKeyAction action : this.keyReleaseBindings.get(key)) {
                            action.performKeyAction();
                        }
                    }
                }

                // Clear existing
                this.performingKeyActions.clear();

            } else {
                // Simulate all keys being pressed
                for (KeyCode key : this.performingKeyActions) {
                    if (this.keyPressBindings.get(key) != null) {
                        for (InputKeyAction action : this.keyPressBindings.get(key)) {
                            action.performKeyAction();
                        }
                    }
                }
            }
        };
    }

    /**
     * Registers a listener for the specified key press.
     *
     * @param key     Key whose press will be listened to. The key is of type {@link KeyCode}.
     * @param onPress Action performed when the key is pressed.
     *                The action is of type {@link InputKeyAction#performKeyAction()}.
     */
    public void onKeyPress(KeyCode key, InputKeyAction onPress) {

        if (!this.keyPressBindings.containsKey(key)) {
            this.keyPressBindings.put(key, new HashSet<>());
        }
        this.keyPressBindings.get(key).add(onPress);
    }

    /**
     * Registers a listener for the specified key release.
     *
     * @param key       Key whose release will be listened to. The key is of type {@link KeyCode}.
     * @param onRelease Action performed when the key is released. The action is of type {@link InputKeyAction}.
     */
    public void onKeyRelease(KeyCode key, InputKeyAction onRelease) {

        if (!this.keyReleaseBindings.containsKey(key)) {
            this.keyReleaseBindings.put(key, new HashSet<>());
        }
        this.keyReleaseBindings.get(key).add(onRelease);
    }

    /**
     * Registers a listener for the specified key.
     * The function merges {@link InputHandler#onKeyPress(KeyCode key, InputKeyAction onPress)} and
     * {@link InputHandler#onKeyRelease(KeyCode key, InputKeyAction onRelease)} into one function call.
     *
     * @param key       Key whose press and release will be listened to. The key is of type {@link KeyCode}.
     * @param onPress   Action performed when the key is pressed. The action is of type {@link InputKeyAction}.
     * @param onRelease Action performed when the key is released. The action is of type {@link InputKeyAction}.
     */
    public void addKeyAction(KeyCode key, InputKeyAction onPress, InputKeyAction onRelease) {
        this.onKeyPress(key, onPress);
        this.onKeyRelease(key, onRelease);
    }

    /**
     * Registers a listener for the specified mouse button click.
     * Click is registered when the mouse button is released.
     *
     * @param clickAction Action performed when the mouse button is clicked (= released).
     *                    The action is of type {@link InputClickableAction#performMouseClickAction(double x, double y, MouseButton button)}
     *                    with {@code double x} and {@code double y} being the mouse click coordinates inside the {@code scene}.
     */
    public void onMouseClick(InputClickableAction clickAction) {
        // Register inputScene's listener
        this.inputScene.addEventHandler(MouseEvent.MOUSE_CLICKED, (mouseClick) -> {
            // Do nothing when input is disabled.
            if (this.disableInput) {
                return;
            }

            MouseButton mouseButton = mouseClick.getButton();

            // Do something if the isDragging is null and when the clicked (= released) mouse button is equal
            // to the mouse button which is being listened to.
            clickAction.performMouseClickAction(mouseClick.getSceneX(), mouseClick.getSceneY(), mouseButton);
        });
    }

    /**
     * Registers a listener for the mouse hover.
     *
     * @param hoverAction Action performed when the mouse hover is "active".
     *                    The action is of type {@link InputHoverableAction#performMouseHoverAction(double x, double y)}
     *                    with {@code double x} and {@code double y} being the mouse coordinates inside the {@code scene}.
     */
    public void onMouseHover(InputHoverableAction hoverAction) {
        // Do nothing when input is disabled.
        EventHandler<MouseEvent> mouseHovering = (mouseMove) -> {
            // Do nothing when input is disabled.
            if (this.disableInput) {
                return;
            }

            hoverAction.performMouseHoverAction(mouseMove.getSceneX(), mouseMove.getSceneY());
        };
        this.inputScene.addEventHandler(MouseEvent.MOUSE_MOVED, mouseHovering);
    }

}
