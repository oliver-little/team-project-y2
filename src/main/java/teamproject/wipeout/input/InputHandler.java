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
    private HashSet<KeyCode> performingActions;

    public InputHandler(Scene scene) {
        this.inputScene = scene;
        this.performingActions = new HashSet<KeyCode>();
    }

    public void onKeyPress(KeyCode key, InputKeyAction onPress) {
        inputScene.addEventHandler(KeyEvent.KEY_PRESSED, (pressedKey) -> {
            KeyCode pressedKeyCode = pressedKey.getCode();
            if (this.performingActions.contains(pressedKeyCode)) {
                return;
            }
            if (pressedKeyCode == key) {
                this.performingActions.add(key);
                onPress.performKeyAction();
            }
        });
    }

    public void onKeyRelease(KeyCode key, InputKeyAction action) {
        inputScene.addEventHandler(KeyEvent.KEY_RELEASED, (releasedKey) -> {
            if (releasedKey.getCode() == key) {
                action.performKeyAction();
                this.performingActions.remove(key);
            }
        });
    }

    public void addKeyAction(KeyCode key, InputKeyAction onPress, InputKeyAction onRelease) {
        this.onKeyPress(key, onPress);
        this.onKeyRelease(key, onRelease);
    }

    public void onMouseClick(MouseButton button, InputMouseAction action) {
        inputScene.addEventHandler(MouseEvent.MOUSE_CLICKED, (mouseClick) -> {
            if (mouseClick.getButton() == button) {
                action.performMouseClickAction(mouseClick.getSceneX(), mouseClick.getSceneY());
            }
        });
    }

}
