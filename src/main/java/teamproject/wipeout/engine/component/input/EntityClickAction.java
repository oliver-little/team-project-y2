package teamproject.wipeout.engine.component.input;

import javafx.scene.input.MouseButton;
import teamproject.wipeout.engine.entity.GameEntity;

@FunctionalInterface
public interface EntityClickAction {
    /**
     * Method representing the action that will be triggered by a certain {@code MouseEvent}.
     *
     * @param x is the X-coordinate at which the {@code MouseEvent} occurred.
     * @param y is the Y-coordinate at which the {@code MouseEvent} occurred.
     * @param mouseButton is the mouse button pressed that triggered the {@code MouseEvent}.
     * @param clickedEntity is the entity that was clicked when this {@code MouseEvent} occurred.
     */
    void performMouseClickAction(double x, double y, MouseButton mouseButton, GameEntity clickedEntity);
}
