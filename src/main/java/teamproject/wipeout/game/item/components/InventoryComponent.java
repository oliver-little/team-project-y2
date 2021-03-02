package teamproject.wipeout.game.item.components;

import java.util.Map;

/**
 * Defines item's inventory properties.
 */
public class InventoryComponent implements ItemComponent {

    public final String spriteSheetName;
    public final String spriteSetName;
    public final int stackSizeLimit;

    /**
     * Creates an {@code InventoryComponent} from a given {@code JSON Map} data.
     *
     * @param data {@code JSON Map} data
     */
    public InventoryComponent(Map<String, Object> data) {
        this.spriteSheetName = (String) data.get("spriteSheetName");
        this.spriteSetName = (String) data.get("spriteSetName");
        this.stackSizeLimit = ((Double) data.get("stackSizeLimit")).intValue();
    }

    /**
     * ItemComponent interface implementation
     *
     * @return Component's type
     */
    public String getType() {
        return "inventory";
    }

}
