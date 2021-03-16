package teamproject.wipeout.game.item.components;

import java.util.Map;

/**
 * Defines sabotage/potion properties.
 */
public class SabotageComponent implements ItemComponent {

    /**
     * Where can the sabotage effect/potion be used.
     */
    public enum Usage {
        FARM,
        PLAYER,
        NONE
    }

    public final Usage usage;

    /**
     * Creates a {@code SabotageComponent} from a given {@code JSON Map} data.
     *
     * @param data {@code JSON Map} data
     */
    public SabotageComponent(Map<String, Object> data) {
        this.usage = Usage.valueOf(data.get("usage").toString());
    }

    /**
     * ItemComponent interface implementation
     *
     * @return Component's type
     */
    public String getType() {
        return "sabotage";
    }
}
