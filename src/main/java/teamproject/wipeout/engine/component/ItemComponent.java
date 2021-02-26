package teamproject.wipeout.engine.component;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.physics.FacingDirection;
import teamproject.wipeout.game.item.Item;

public class ItemComponent implements GameComponent {

    public Item item;

    public ItemComponent(Item item) {
        this.item = item;
    }

    public String getType() {
        return "item";
    }
}
