package teamproject.wipeout.game.item.components;

import java.util.Map;

/**
 * Defines plant properties.
 */
public class PlantComponent implements ItemComponent {

    public final String spriteSheetName;
    public final String spriteSetName;
    public final double growthRate;

    /**
     * Creates a {@code PlantComponent} from a given {@code JSON Map} data.
     *
     * @param data {@code JSON Map} data
     */
    public PlantComponent(Map<String, Object> data) {
        this.spriteSheetName = (String) data.get("spriteSheetName");
        this.spriteSetName = (String) data.get("spriteSetName");
        this.growthRate = (Double) data.get("growthRate");
    }

    /**
     * ItemComponent interface implementation
     *
     * @return Component's type
     */
    public String getType() {
        return "plant";
    }

}
