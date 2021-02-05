package teamproject.wipeout.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.HashSet;

// Maybe singleton later?
public class InputHandler {

    private final Scene inputScene;
    private final HashSet<KeyCode> performingKeyActions;
    private MouseButton isDragging;

    public InputHandler(Scene scene) {
        this.inputScene = scene;
        this.performingKeyActions = new HashSet<KeyCode>();
        this.isDragging = null;
    }

    public void onKeyPress(KeyCode key, InputKeyAction onPress) {
        inputScene.addEventFilter(KeyEvent.KEY_PRESSED, (pressedKey) -> {
            KeyCode pressedKeyCode = pressedKey.getCode();
            if (this.performingKeyActions.contains(pressedKeyCode)) {
                return;
            }
            if (pressedKeyCode == key) {
                this.performingKeyActions.add(key);
                onPress.performKeyAction();
            }
        });
    }

    public void onKeyRelease(KeyCode key, InputKeyAction action) {
        inputScene.addEventFilter(KeyEvent.KEY_RELEASED, (releasedKey) -> {
            if (releasedKey.getCode() == key) {
                action.performKeyAction();
                this.performingKeyActions.remove(key);
            }
        });
    }

    public void addKeyAction(KeyCode key, InputKeyAction onPress, InputKeyAction onRelease) {
        this.onKeyPress(key, onPress);
        this.onKeyRelease(key, onRelease);
    }

    public void onMouseClick(MouseButton button, InputMouseAction action) {
        inputScene.addEventHandler(MouseEvent.MOUSE_CLICKED, (mouseClick) -> {
            MouseButton mouseButton = mouseClick.getButton();

            // MOUSE_CLICKED is called even after MOUSE_RELEASED in the onMouseDrag function
            // so to not call both, isDragging is set to false in this method rather than onMouseDrag.
            if (this.isDragging != null) {
                if (this.isDragging == mouseButton) {
                    this.isDragging = null;
                }
                return;
            }

            if (mouseButton == button) {
                action.performMouseClickAction(mouseClick.getSceneX(), mouseClick.getSceneY());
            }
        });
    }

    public void onMouseDrag(MouseButton button, InputMouseAction dragAction, InputMouseAction releaseAction) {
        inputScene.addEventHandler(MouseEvent.MOUSE_DRAGGED, (mouseClick) -> {
            if (mouseClick.getButton() == button) {
                this.isDragging = button;
                dragAction.performMouseClickAction(mouseClick.getSceneX(), mouseClick.getSceneY());
            }
        });

        inputScene.addEventHandler(MouseEvent.MOUSE_RELEASED, (mouseRelease) -> {
            if (this.isDragging == mouseRelease.getButton()) {
                releaseAction.performMouseClickAction(mouseRelease.getSceneX(), mouseRelease.getSceneY());
            }
        });
    }

}
