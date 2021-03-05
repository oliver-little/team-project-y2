package teamproject.wipeout.game.item.components;

import java.util.Map;

/**
 * Defines plant properties.
 */
public class PlantComponent implements ItemComponent {

    public final String spriteSheetName;
    public final String spriteSetName;
    public final int width;
    public final int height;
    public final double growthRate;
    public final int grownItemID;

    /**
     * Creates a {@code PlantComponent} from a given {@code JSON Map} data.
     *
     * @param data {@code JSON Map} data
     */
    public PlantComponent(Map<String, Object> data) {
        this.spriteSheetName = (String) data.get("spriteSheetName");
        this.spriteSetName = (String) data.get("spriteSetName");
        this.growthRate = (Double) data.get("growthRate");
        this.grownItemID = ((Double) data.get("grownItemID")).intValue();

        Double width = (Double) data.get("width");
        Double height = (Double) data.get("height");
        if (width == null) {
            this.width = 1;
        } else {
            this.width = width.intValue();
        }
        if (height == null) {
            this.height = 1;
        } else {
            this.height = height.intValue();
        }
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
