package teamproject.wipeout.engine.system;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.Test;
import teamproject.wipeout.engine.component.ui.DialogUIComponent;
import teamproject.wipeout.engine.component.ui.UIComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UISystemTest {
    @Test
    public void testAddUIComponent() {
        GameScene scene = new GameScene();
        StackPane root = new StackPane();
        UISystem ui = new UISystem(scene, root);
        GameEntity entity = scene.createEntity();

        assertEquals(0, root.getChildren().size());

        TestUI newNode = new TestUI();
        entity.addComponent(new UIComponent(newNode));

        assertEquals(false, entity.hasComponent(UIComponent.class));
        assertEquals(1, root.getChildren().size());
        assertEquals(newNode.getContent(), root.getChildren().get(0));
    }

    /**
     * Class with no content to test UISystem
     */
    public class TestUI implements DialogUIComponent {
        
        private Pane parent;
        private Rectangle rect;

        public TestUI() {
            rect = new Rectangle();
        }

        public void setParent(Pane parent) {
            this.parent = parent;
        }

        public Node getContent() {
            return rect;
        }
    }
}
