package teamproject.wipeout.engine.component;

import javafx.scene.Node;

public class UIComponent implements GameComponent {
    
    private Node node;

    public UIComponent(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return this.node;
    }

    public String getType() {
        return "ui";
    }
}
