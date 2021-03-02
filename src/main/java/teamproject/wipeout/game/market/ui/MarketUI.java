package teamproject.wipeout.game.market.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import teamproject.wipeout.engine.component.ui.DialogUIComponent;
import teamproject.wipeout.util.resources.ResourceType;

public class MarketUI extends AnchorPane implements DialogUIComponent {
    
    private Pane parent;

    public MarketUI() {
        super();

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "market-menu.css");
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        Tab buy = new Tab("Buy", new Label("Buy some items."));
        Tab sell = new Tab("Sell", new Label("Sell some items."));
        tabPane.getTabs().addAll(buy, sell);

        Button close = new Button("X");
        close.setOnAction(actionEvent -> this.parent.getChildren().remove(this));

        AnchorPane.setTopAnchor(tabPane, 5.0);
        AnchorPane.setLeftAnchor(tabPane, 5.0);
        AnchorPane.setRightAnchor(tabPane, 5.0);
        AnchorPane.setTopAnchor(close, 6.0);
        AnchorPane.setRightAnchor(close, 10.0);

        this.getChildren().addAll(tabPane, close);
    }

    public void setParent(Pane parent) {
        this.parent = parent;
    }

    public Parent getContent() {
        return this;
    }
}
