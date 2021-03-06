package teamproject.wipeout.game.market.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

/**
 * Creates a tabbed screen with items to buy and sell
 */
public class MarketUI extends AnchorPane {

    public Runnable onUIClose;
    
    private Pane parent;

    public MarketUI(Collection<Item> items, Market market, Player player, SpriteManager spriteManager) {
        super();

        try {
            InputStream path = new FileInputStream(ResourceLoader.get(ResourceType.STYLESHEET, "fonts/Kalam-Regular.ttf"));
            Font.loadFont(path, 12);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        


        this.getStylesheets().add(ResourceType.STYLESHEET.path + "market-menu.css");
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        List<Node> tiles = new ArrayList<>();

        for (Item item : items) {
            tiles.add(new MarketItemUI(item, market, player, spriteManager));
        }

        ScrollableTileUI seedMenu = new ScrollableTileUI(tiles);
        Tab seeds = new Tab("Seeds", seedMenu);
        Tab plants = new Tab("Plants & Veg", new Label("Fruit, Veg & Other Farmables"));
        Tab tools = new Tab("Tools", new Label("Usable Tools"));
        Tab tasks = new Tab("Tasks", new Label("Purchasable Tasks"));
        tabPane.getTabs().addAll(seeds, plants, tools, tasks);

        Button close = new Button("X");
        close.setOnAction(actionEvent -> {
            if (this.onUIClose != null) {
                this.onUIClose.run();
            }
            this.parent.getChildren().remove(this);
        });

        AnchorPane.setBottomAnchor(tabPane, 50.0);
        AnchorPane.setTopAnchor(tabPane, 50.0);
        AnchorPane.setLeftAnchor(tabPane, 100.0);
        AnchorPane.setRightAnchor(tabPane, 100.0);
        AnchorPane.setTopAnchor(close, 60.0);
        AnchorPane.setRightAnchor(close, 100.0);

        this.getChildren().addAll(tabPane, close);
    }

    public void setParent(Pane parent) {
        this.parent = parent;
    }

    public Parent getContent() {
        return this;
    }
}
