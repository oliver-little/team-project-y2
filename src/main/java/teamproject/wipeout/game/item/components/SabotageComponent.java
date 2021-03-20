package teamproject.wipeout.game.item.components;

import java.util.Map;

import teamproject.wipeout.engine.component.GameComponent;

/**
 * Defines sabotage/potion properties.
 */
public class SabotageComponent implements GameComponent, ItemComponent {

    /**
     * Where can the sabotage effect/potion be used.
     */
    public enum SabotageType {
        SPEED,
        GROWTHRATE,
        REPUTATION,
        AI
    }

    public final SabotageType type;
    public final double duration; //In seconds

    /**
     * Creates a {@code SabotageComponent} from a given {@code JSON Map} data.
     *
     * @param data {@code JSON Map} data
     */
    public SabotageComponent(Map<String, Object> data) {
        this.type = SabotageType.valueOf(data.get("sabotage-type").toString());
        this.duration = (Double) data.get("duration");
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
