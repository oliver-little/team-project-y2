package teamproject.wipeout.game.farm;

import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;

public class FarmItem {

    public double growth;

    private final Item item;

    public FarmItem(Item item) {
        this.item = item;
        this.growth = 0.0;
    }

    public FarmItem(Item item, Double growth) {
        this.item = item;
        this.growth = growth;
    }

    public Item get() {
        return this.item;
    }

    public double getGrowthRate() {
        return this.item.getComponent(PlantComponent.class).growthRate;
    }

    /**
     * Calculates the growth stage based on a growth rate and current growth.
     *
     * @return Current growth stage
     */
    public int getCurrentGrowthStage() {
        double growthRate = this.item.getComponent(PlantComponent.class).growthRate;
        return (int) (this.growth / growthRate);
    }

}
