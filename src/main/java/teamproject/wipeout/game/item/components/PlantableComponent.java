package teamproject.wipeout.game.item.components;

import java.util.Map;

/**
 * Defines item's plantable properties.
 */
public class PlantableComponent implements ItemComponent {

    public final String seedSpriteSheetName;
    public final String seedSpriteSetName;
    public final String growthSpriteSheetName;
    public final String growthSpriteSetName;
    public final double growthRate;

    /**
     * Creates a {@code PlantableComponent} from a given {@code JSON Map} data.
     *
     * @param data {@code JSON Map} data
     */
    public PlantableComponent(Map<String, Object> data) {
        this.seedSpriteSheetName = (String) data.get("seedSpriteSheetName");
        this.seedSpriteSetName = (String) data.get("seedSpriteSetName");
        this.growthSpriteSheetName = (String) data.get("growthSpriteSheetName");
        this.growthSpriteSetName = (String) data.get("growthSpriteSetName");
        this.growthRate = (Double) data.get("growthRate");
    }

    /**
     * ItemComponent interface implementation
     *
     * @return Component's type
     */
    public String getType() {
        return "plantable";
    }

}
