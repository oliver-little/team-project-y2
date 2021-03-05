package teamproject.wipeout.game.market.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import teamproject.wipeout.engine.component.ui.DialogUIComponent;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

public class MarketUI extends AnchorPane implements DialogUIComponent {
    
    private Pane parent;

    public MarketUI() {
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
        Tab seeds = new Tab("Seeds", new ItemMenu());
        Tab plants = new Tab("Plants & Veg", new Label("Fruit, Veg & Other Farmables"));
        Tab tools = new Tab("Tools", new Label("Usable Tools"));
        Tab tasks = new Tab("Tasks", new Label("Purchasable Tasks"));
        tabPane.getTabs().addAll(seeds, plants, tools, tasks);

        Button close = new Button("X");
        close.setOnAction(actionEvent -> this.parent.getChildren().remove(this));


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
