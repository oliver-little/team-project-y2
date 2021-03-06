package teamproject.wipeout.game.market.ui;

import java.util.Collection;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;

/**
 * Creates a pane with a ScrollPane inside a TilePane, with some given children.
 */
public class ScrollableTileUI extends ScrollPane {

    private TilePane tilePane;

    public ScrollableTileUI(Collection<Node> children) {
        super();

        tilePane = new TilePane();

        this.setFitToHeight(true);
        this.setFitToWidth(true);
        this.setPadding(new Insets(10));
        tilePane.setAlignment(Pos.TOP_CENTER);
        tilePane.setVgap(20);
        tilePane.setHgap(20);

        tilePane.getChildren().addAll(children);

        this.setContent(tilePane);
    }
}