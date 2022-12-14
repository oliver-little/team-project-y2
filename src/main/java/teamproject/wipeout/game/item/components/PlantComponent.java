package teamproject.wipeout.game.item.components;

import java.util.Map;

/**
 * Defines plant properties.
 */
public class PlantComponent implements ItemComponent {

    public final String spriteSheetName;
    public final String spriteSetName;
    public final int grownItemID;
    public final int maxGrowthStage; // counting from 0., 1., 2.,...
    public final double growthRate;
    public final int maxDrop;
    public final int minDrop;

    public final int width;
    public final int height;
    public final boolean isTree;

    /**
     * Creates a {@code PlantComponent} from a given {@code JSON Map} data.
     *
     * @param data {@code JSON Map} data
     */
    public PlantComponent(Map<String, Object> data) {
        this.spriteSheetName = (String) data.get("spriteSheetName");
        this.spriteSetName = (String) data.get("spriteSetName");
        this.grownItemID = ((Double) data.get("grownItemID")).intValue();
        this.maxGrowthStage = ((Double) data.get("maxGrowthStage")).intValue();
        this.growthRate = (Double) data.get("growthRate");
        this.maxDrop = ((Double) data.get("maxDrop")).intValue();
        this.minDrop = ((Double) data.get("minDrop")).intValue();

        Double width = (Double) data.get("width");
        Double height = (Double) data.get("height");
        Boolean isTree = (Boolean) data.get("isTree");

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
        if (isTree == null) {
            this.isTree = false;
        } else {
            this.isTree = true;
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
