package teamproject.wipeout.game.item.components;

import java.util.Map;

/**
 * Defines usable item properties.
 */
public class UsableComponent implements ItemComponent {

    public final int health;
    
    /**
     * Creates a {@code UsableComponent} from a given {@code JSON Map} data.
     *
     * @param data {@code JSON Map} data
     */
    public UsableComponent(Map<String, Object> data) {
        this.health = ((Double) data.get("health")).intValue();
    }

    /**
     * ItemComponent interface implementation
     *
     * @return Component's type
     */
    public String getType() {
        return "usable";
    }
}
