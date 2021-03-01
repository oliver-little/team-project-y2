package teamproject.wipeout;

import javafx.scene.Parent;

/**
 * This is an interface for classes which have a root node in the scene graph.
 */
public interface Controller {
	void cleanup();
	Parent getContent();
}