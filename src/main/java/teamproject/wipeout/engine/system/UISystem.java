package teamproject.wipeout.engine.system;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import teamproject.wipeout.engine.component.UIComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.FunctionalEntityCollector;

public class UISystem implements EventSystem {

    private FunctionalEntityCollector collector;
    private StackPane root;

    public UISystem(GameScene scene, StackPane root) {
        this.root = root;
        this.collector = new FunctionalEntityCollector(scene, (entity) -> this.getNodeFromEntity(entity), null, null);
    }
    
    public void cleanup() {
        this.collector.cleanup();
    }

    public StackPane getUIRoot() {
        return this.root;
    }

    public void setUIRoot(StackPane newRoot) {
        this.root = newRoot;
    }

    public void getNodeFromEntity(GameEntity entity) {
        if (entity.hasComponent(UIComponent.class)) {
            UIComponent ui = entity.getComponent(UIComponent.class);
            this.addUINode(ui.getNode());
            entity.removeComponent(UIComponent.class);
        }
    }

    public void addUINode(Node node) {
        this.root.getChildren().add(node);
    }
}
