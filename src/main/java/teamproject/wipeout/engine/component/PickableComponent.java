package teamproject.wipeout.engine.component;

import teamproject.wipeout.game.item.Item;

public class PickableComponent implements GameComponent {

    public Item item;

    public PickableComponent(Item item) {
        this.item = item;
    }

    public String getType() {
        return "item";
    }
}
