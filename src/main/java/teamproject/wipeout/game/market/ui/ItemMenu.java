package teamproject.wipeout.game.market.ui;

import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.market.MarketItem;

public class ItemMenu extends ScrollPane {
    public ItemMenu() {
        super();

        TilePane tilePane = new TilePane();

        this.setFitToHeight(true);
        this.setFitToWidth(true);
        this.setPadding(new Insets(10));
        tilePane.setAlignment(Pos.TOP_CENTER);
        tilePane.setVgap(20);
        tilePane.setHgap(20);

        //for (Item item : displayItems) {
        for (int i = 0; i < 15; i ++) {
            //tilePane.getChildren().add(new MarketItemUI(item, marketItems.get(item.id)));
            tilePane.getChildren().add(new Rectangle(200, 150, Color.BLACK));
        
        }

        this.setContent(tilePane);
    }
}