package teamproject.wipeout.engine.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.HashSet;

/**
 * InputHandler is a class for dealing with the keyboard and mouse input of a given {@code Scene}.
 * All input event listeners can be temporarily disabled.
 *
 * @see InputKeyAction
 * @see InputMouseAction
 */
public class InputHandler {

    private final Scene inputScene;
    private boolean disableInput;

    // Set of keys that are being pressed at a certain point in time.
    // Used to prevent repeated performKeyAction calls.
    private final HashSet<KeyCode> performingKeyActions;

    // Control variable used to track mouse dragging.
    // Prevents all mouse click actions while the mouse is being dragged.
    private MouseButton isDragging;

    // Workaround for onMouseClick being called after the onMouseDrag.releaseAction is called.
    private boolean onMouseClickExists;

    /**
     * Default InputHandler initializer.
     *
     * @param scene {@code Scene} whose input will be listened to.
     */
    public InputHandler(Scene scene) {
        this.inputScene = scene;
        this.disableInput = false;

        this.performingKeyActions = new HashSet<KeyCode>();
        this.isDragging = null;

        this.onMouseClickExists = false;
    }

    /**
     * {@code disableInput} variable getter.
     *
     * @return boolean value of the {@code disableInput} variable.
     */
    public boolean getDisableInput() {
        return this.disableInput;
    }

    /**
     * {@code disableInput} variable setter
     * @param disabled New value of the {@code disableInput} variable.
     */
    public void setDisableInput(boolean disabled) {
        this.disableInput = disabled;
        if (disabled) {
            this.performingKeyActions.clear();
            this.isDragging = null;
        }
    }

    /**
     * Registers a listener for the specified key press.
     *
     * @param key       Key whose press will be listened to. The key is of type {@link KeyCode}.
     * @param onPress   Action performed when the key is pressed.
     *                  The action is of type {@link InputKeyAction#performKeyAction()}.
     */
    public void onKeyPress(KeyCode key,
                           InputKeyAction onPress) {
        // Register inputScene's listener
        this.inputScene.addEventFilter(KeyEvent.KEY_PRESSED, (pressedKey) -> {
            // Do nothing when input is disabled.
            if (this.disableInput) {
                return;
            }

            KeyCode pressedKeyCode = pressedKey.getCode();

            // Do nothing if the key action was already performed.
            if (this.performingKeyActions.contains(pressedKeyCode)) {
                return;
            }

            // Do something when the pressed key is equal to the key which is being listened to.
            if (pressedKeyCode == key) {
                this.performingKeyActions.add(key);
                onPress.performKeyAction();
            }
        });
    }

    /**
     * Registers a listener for the specified key release.
     *
     * @param key           Key whose release will be listened to. The key is of type {@link KeyCode}.
     * @param onRelease     Action performed when the key is released. The action is of type {@link InputKeyAction}.
     */
    public void onKeyRelease(KeyCode key,
                             InputKeyAction onRelease) {
        // Register inputScene's listener
        this.inputScene.addEventFilter(KeyEvent.KEY_RELEASED, (releasedKey) -> {
            // Do nothing when input is disabled.
            if (this.disableInput) {
                return;
            }

            // Do something when the released key is equal to the key which is being listened to.
            if (releasedKey.getCode() == key) {
                onRelease.performKeyAction();
                this.performingKeyActions.remove(key);
            }
        });
    }

    /**
     * Registers a listener for the specified key.
     * The function merges {@link InputHandler#onKeyPress(KeyCode key, InputKeyAction onPress)} and
     * {@link InputHandler#onKeyRelease(KeyCode key, InputKeyAction onRelease)} into one function call.
     *
     * @param key           Key whose press and release will be listened to. The key is of type {@link KeyCode}.
     * @param onPress       Action performed when the key is pressed. The action is of type {@link InputKeyAction}.
     * @param onRelease     Action performed when the key is released. The action is of type {@link InputKeyAction}.
     */
    public void addKeyAction(KeyCode key,
                             InputKeyAction onPress,
                             InputKeyAction onRelease) {
        this.onKeyPress(key, onPress);
        this.onKeyRelease(key, onRelease);
    }

    /**
     * Registers a listener for the specified mouse button click.
     * Click is registered when the mouse button is released.
     *
     * @param button    Mouse button whose click (= release) will be listened to.
     *                  Mouse button is of type {@link MouseButton}.
     * @param action    Action performed when the mouse button is clicked (= released).
     *                  The action is of type {@link InputMouseAction#performMouseClickAction(double x, double y)}
     *                  with {@code double x} and {@code double y} being the mouse click coordinates
     *                  inside the {@code scene}.
     */
    public void onMouseClick(InputMouseAction action) {
        this.onMouseClickExists = true;
        // Register inputScene's listener
        this.inputScene.addEventFilter(MouseEvent.MOUSE_CLICKED, (mouseClick) -> {
            // Do nothing when input is disabled.
            if (this.disableInput) {
                return;
            }

            MouseButton mouseButton = mouseClick.getButton();

            if (this.isDragging != null) {
                // MOUSE_CLICKED is called even after MOUSE_RELEASED in the onMouseDrag method
                // so to not call both, isDragging is set to null in this method rather than onMouseDrag.
                // onMouseDrag ended if the clicked (= released) mouse button is equal to the mouse button pressed
                // while the mouse was dragged. Do something if the clicked (= released) mouse button is equal
                // to the mouse button which is being listened to.
                if (this.isDragging == mouseButton) {
                    this.isDragging = null;
                }
                // Otherwise ignore other mouse button clicks while the mouse is dragged.
                return;
            }

            // Do something if the isDragging is null and when the clicked (= released) mouse button is equal
            // to the mouse button which is being listened to.
            action.performMouseClickAction(mouseClick.getSceneX(), mouseClick.getSceneY(), mouseButton);
        });
    }

    /**
     * Registers a listener for the specified mouse button press and drag.
     * Actions {@code pressAction}, {@code dragAction} and {@code releaseAction} are all
     * of type {@link InputMouseAction#performMouseClickAction(double x, double y)} with {@code double x}
     * and {@code double y} being the mouse click coordinates inside the {@code scene}.
     *
     * @param button        Mouse button whose press and drag will be listened to.
     *                      Mouse button is of type {@link MouseButton}.
     * @param pressAction   Action performed when the mouse button is pressed (= dragging starts).
     * @param dragAction    Action performed when the mouse is dragged while the mouse button is being pressed.
     *                      The action is called every time the mouse cursor coordinates change.
     * @param releaseAction Action performed when the mouse button is released (= dragging ends).
     */
    public void onMouseDrag(MouseButton button,
                            InputMouseAction pressAction,
                            InputMouseAction dragAction,
                            InputMouseAction releaseAction) {
        // Register inputScene's listener which covers start of the mouse drag and the dragging itself.
        this.inputScene.addEventFilter(MouseEvent.MOUSE_DRAGGED, (mouseClick) -> {
            // Do nothing when input is disabled.
            if (this.disableInput) {
                return;
            }

            // Do something when the pressed mouse button is equal to the mouse button which is being listened to.
            double mouseClickX = mouseClick.getSceneX();
            double mouseClickY = mouseClick.getSceneY();

            if (this.isDragging == null) {
                // Mouse drag start
                this.isDragging = button;
                pressAction.performMouseClickAction(mouseClickX, mouseClickY, mouseClick.getButton());
            } else {
                // Mouse drag happening
                dragAction.performMouseClickAction(mouseClickX, mouseClickY, mouseClick.getButton());
            }
        });

        // Register inputScene's listener which covers end of the mouse drag.
        this.inputScene.addEventFilter(MouseEvent.MOUSE_RELEASED, (mouseRelease) -> {
            // Do nothing when input is disabled.
            if (this.disableInput) {
                return;
            }

            // Do something when the released mouse button is equal to both:
            // 1. the mouse button which is being listened to
            // and 2. the button that is being pressed during mouse dragging.
            if (this.isDragging == mouseRelease.getButton()) {
                // Mouse drag end
                releaseAction.performMouseClickAction(mouseRelease.getSceneX(), mouseRelease.getSceneY(), mouseRelease.getButton());

                // If no onMouseClick listener exists (= onMouseClick won't be called after this method)
                // then set isDragging to null
                if (!this.onMouseClickExists) {
                    this.isDragging = null;
                }
            }
        });
    }

}
