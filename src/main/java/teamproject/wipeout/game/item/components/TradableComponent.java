package teamproject.wipeout.game.item.components;

import java.util.Map;

/**
 * Defines item's tradable properties.
 */
public class TradableComponent implements ItemComponent {

    public final double defaultBuyPrice;
    public final double defaultSellPrice;

    /**
     * Creates a {@code TradableComponent} from a given {@code JSON Map} data.
     *
     * @param data {@code JSON Map} data
     */
    public TradableComponent(Map<String, Object> data) {
        this.defaultBuyPrice = (Double) data.get("defaultBuyPrice");
        this.defaultSellPrice = (Double) data.get("defaultSellPrice");
    }

    /**
     * ItemComponent interface implementation
     *
     * @return Component's type
     */
    public String getType() {
        return "tradable";
    }

}
