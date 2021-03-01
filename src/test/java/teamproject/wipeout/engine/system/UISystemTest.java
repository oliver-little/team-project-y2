package teamproject.wipeout.engine.system;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import teamproject.wipeout.engine.component.UIComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;

public class UISystemTest {
    @Test
    public void testAddUIComponent() {
        GameScene scene = new GameScene();
        StackPane root = new StackPane();
        UISystem ui = new UISystem(scene, root);
        GameEntity entity = scene.createEntity();

        assertEquals(0, root.getChildren().size());

        Rectangle newNode = new Rectangle(10, 10);
        entity.addComponent(new UIComponent(newNode));

        assertEquals(false, entity.hasComponent(UIComponent.class));
        assertEquals(1, root.getChildren().size());
        assertEquals(newNode, root.getChildren().get(0));
    }
}
