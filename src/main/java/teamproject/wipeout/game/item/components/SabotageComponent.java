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
        AI
    }

    public final SabotageType type;
    public final double duration; //In seconds
    public final double multiplier;

    /**
     * Creates a {@code SabotageComponent} from a given {@code JSON Map} data.
     *
     * @param data {@code JSON Map} data
     */
    public SabotageComponent(Map<String, Object> data) {
        this.type = SabotageType.valueOf(data.get("sabotage-type").toString());
        this.duration = (Double) data.get("duration");
        this.multiplier = (Double) data.get("multiplier");
    }

    /**
     * Creates a {@code SabotageComponent} with provided parameters
     * @param type The type of sabotage
     * @param duration The duration of the sabotage (s)
     * @param multiplier The multiplier of the sabotage
     */
    public SabotageComponent(SabotageType type, double duration, double multiplier) {
        this.type = type;
        this.duration = duration;
        this.multiplier = multiplier;
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
