package teamproject.wipeout.game.item.components;

import java.util.Map;

/**
 * Defines seed properties.
 */
public class SeedComponent implements ItemComponent {

    public final String spriteSheetName;
    public final String spriteSetName;

    /**
     * Creates a {@code SeedComponent} from a given {@code JSON Map} data.
     *
     * @param data {@code JSON Map} data
     */
    public SeedComponent(Map<String, Object> data) {
        this.spriteSheetName = (String) data.get("spriteSheetName");
        this.spriteSetName = (String) data.get("spriteSetName");
    }

    /**
     * ItemComponent interface implementation
     *
     * @return Component's type
     */
    public String getType() {
        return "seed";
    }

}
