package teamproject.wipeout.game.market.ui;

//import java.awt.event.KeyEvent;
import javafx.scene.input.KeyEvent;

import java.util.Collection;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import teamproject.wipeout.game.UI.UIUtil;

/**
 * Creates a pane with a ScrollPane inside a TilePane, with some given children.
 */
public class ScrollableTileUI extends VBox {

    private ScrollPane scrollPane;
    private TilePane tilePane;
    private Collection<Pair<Node, String>> allChildren;

    public ScrollableTileUI(Collection<Pair<Node, String>> children, Boolean toShowSearchBar) {
        super();

        this.scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        this.allChildren = children;

        tilePane = new TilePane();
        tilePane.setPadding(new Insets(10, 0, 10, 0));

        tilePane.setAlignment(Pos.TOP_CENTER);
        tilePane.setVgap(20);
        tilePane.setHgap(20);

        if (toShowSearchBar) {
            VBox searchBox = new VBox(2);
            searchBox.setAlignment(Pos.CENTER);
            searchBox.setId("searchBox");
            searchBox.setPadding(new Insets(10));

            UIUtil.loadTitleFont();

            Text searchTitle = new Text("Search:");
            searchTitle.setFont(new Font("Kalam", 20));

            TextField searchBar = new TextField();
            if (toShowSearchBar) {
                searchBar.addEventFilter(KeyEvent.ANY, e -> {
                    String text = searchBar.getText().toLowerCase();
                    tilePane.getChildren().clear();
                    for (Pair<Node, String> node : allChildren) {
                        if (node.getValue().contains(text)) {
                            tilePane.getChildren().add(node.getKey());
                        }
                    }
                });
            }
            searchBox.getChildren().addAll(searchTitle, searchBar);
            this.getChildren().add(searchBox);
        }

        for (Pair<Node, String> node : allChildren) {
            tilePane.getChildren().add(node.getKey());
        }

        scrollPane.setContent(tilePane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        this.getChildren().add(scrollPane);
    }

    /**
     * Function to fix blurry text within a ScrollPane
     */
    public void fixBlurryText() {
        StackPane stackPane = (StackPane) this.scrollPane.lookup("ScrollPane .viewport");

        if (stackPane != null) {
            stackPane.setCache(false);
        }
    }
}