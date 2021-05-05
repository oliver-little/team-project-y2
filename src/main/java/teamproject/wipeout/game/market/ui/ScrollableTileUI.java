package teamproject.wipeout.game.market.ui;

//import java.awt.event.KeyEvent;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.util.Pair;

/**
 * Creates a pane with a ScrollPane inside a TilePane, with some given children.
 */
public class ScrollableTileUI extends ScrollPane {

    private TilePane tilePane;
    private Collection<Pair<Node, String>> allChildren;

    public ScrollableTileUI(Collection<Pair<Node, String>> children, Boolean toShowSearchBar) {
        super();

        allChildren = children;
        TextField searchBar = new TextField();
        if(toShowSearchBar) {
            searchBar.addEventFilter(KeyEvent.ANY, e -> {
                String text = searchBar.getText();

                for(Pair<Node, String> node: allChildren) {
                    if(node.getValue().contains(text)) {
                        if(!tilePane.getChildren().contains(node.getKey())) {
                            tilePane.getChildren().add(node.getKey());
                        }
                    } else {
                        tilePane.getChildren().remove(node.getKey());
                    }
                }
            });
        }

        tilePane = new TilePane();

        this.setFitToHeight(true);
        this.setFitToWidth(true);
        this.setPadding(new Insets(10));
        tilePane.setAlignment(Pos.TOP_CENTER);
        tilePane.setVgap(20);
        tilePane.setHgap(20);

        if(toShowSearchBar) {
            tilePane.getChildren().add(searchBar);
        }

        for(Pair<Node, String> node: allChildren) {
            tilePane.getChildren().add(node.getKey());
        }

        this.setContent(tilePane);
    }
}