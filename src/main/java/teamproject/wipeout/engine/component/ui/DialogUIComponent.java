package teamproject.wipeout.engine.component.ui;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Interface for components added to the game's UI through UISystem. The interface adds 
 * functionality for the UI component to track its parent so it can remove itself after it is created.
 */
public interface DialogUIComponent {
    /**
     * Sets the parent Pane of this UI object
     * 
     * @param parent The parent pane
     */
    void setParent(Pane parent);

    /**
     * Gets the content this UI component contains
     * 
     * @return The UI
     */
    Node getContent();
}
