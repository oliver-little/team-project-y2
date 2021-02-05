package teamproject.wipeout.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.HashSet;

public class InputHandler {

    private final Scene inputScene;
    boolean disableInput;

    private final HashSet<KeyCode> performingKeyActions;
    private MouseButton isDragging;
    private boolean onMouseClickExists;

    public InputHandler(Scene scene) {
        this.inputScene = scene;
        this.disableInput = false;

        this.performingKeyActions = new HashSet<KeyCode>();
        this.isDragging = null;
        this.onMouseClickExists = false;
    }

    public void onKeyPress(KeyCode key, InputKeyAction onPress) {
        this.inputScene.addEventFilter(KeyEvent.KEY_PRESSED, (pressedKey) -> {
            if (this.disableInput) {
                return;
            }

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
        this.inputScene.addEventFilter(KeyEvent.KEY_RELEASED, (releasedKey) -> {
            if (this.disableInput) {
                return;
            }

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
        this.onMouseClickExists = true;
        this.inputScene.addEventFilter(MouseEvent.MOUSE_CLICKED, (mouseClick) -> {
            if (this.disableInput) {
                return;
            }

            MouseButton mouseButton = mouseClick.getButton();

            if (this.isDragging != null) {
                if (mouseButton == button && this.isDragging == mouseButton) {
                    this.isDragging = null;
                }
                return;
            }


            if (mouseButton == button) {
                // MOUSE_CLICKED is called even after MOUSE_RELEASED in the onMouseDrag function
                // so to not call both, isDragging is set to false in this method rather than onMouseDrag.
                action.performMouseClickAction(mouseClick.getSceneX(), mouseClick.getSceneY());
            }
        });
    }

    public void onMouseDrag(MouseButton button, InputMouseAction pressAction, InputMouseAction dragAction, InputMouseAction releaseAction) {
        this.inputScene.addEventFilter(MouseEvent.MOUSE_DRAGGED, (mouseClick) -> {
            if (this.disableInput) {
                this.isDragging = null;
                return;
            }

            if (mouseClick.getButton() == button) {
                double mouseClickX = mouseClick.getSceneX();
                double mouseClickY = mouseClick.getSceneY();

                if (this.isDragging == null) {
                    this.isDragging = button;
                    pressAction.performMouseClickAction(mouseClickX, mouseClickY);
                } else {
                    dragAction.performMouseClickAction(mouseClickX, mouseClickY);
                }
            }
        });

        this.inputScene.addEventFilter(MouseEvent.MOUSE_RELEASED, (mouseRelease) -> {
            if (this.disableInput) {
                this.isDragging = null;
                return;
            }

            if (mouseRelease.getButton() == button && this.isDragging == mouseRelease.getButton()) {
                releaseAction.performMouseClickAction(mouseRelease.getSceneX(), mouseRelease.getSceneY());
                if (!this.onMouseClickExists) {
                    this.isDragging = null;
                }
            }
        });
    }

}
