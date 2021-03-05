package teamproject.wipeout.game.farm;

import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;

/**
 * Wrapper of an {@link Item} for purposes of a {@link FarmData} instance.
 */
public class FarmItem {

    public double growth;

    private final Item item;

    /**
     * Initializes {@code FarmItem} with a given item
     * and a growth value of {@code 0.0}.
     *
     * @param item {@link Item} with which the {@code FarmItem} is initialized.
     */
    public FarmItem(Item item) {
        this.item = item;
        this.growth = 0.0;
    }

    /**
     * Initializes {@code FarmItem} with a given item
     * and a given growth value.
     *
     * @param item {@link Item} with which the {@code FarmItem} is initialized.
     * @param growth Item's growth value
     */
    public FarmItem(Item item, Double growth) {
        this.item = item;
        this.growth = growth;
    }

    /**
     * Gets the underlying {@code Item}.
     *
     * @return Underlying {@link Item}
     */
    public Item get() {
        return this.item;
    }

    /**
     * Gets the growth rate of the {@link Item} from the {@link PlantComponent}.
     *
     * @return Growth rate in the form of a {@code double}.
     */
    public double getGrowthRate() {
        return this.item.getComponent(PlantComponent.class).growthRate;
    }

    /**
     * Calculates the current growth stage based on the growth rate and the current growth.
     *
     * @return Current growth stage in the form of an {@code int}.
     */
    public int getCurrentGrowthStage() {
        double growthRate = this.item.getComponent(PlantComponent.class).growthRate;
        return (int) (this.growth / growthRate);
    }

    /**
     * Calculates the current growth percentage based on the growth rate,
     * number of growth stages, and the current growth.
     *
     * @return Current growth percentage in the form of an {@code int}.
     */
    public int getCurrentGrowthPercentage() {
        double growthRate = this.getGrowthRate();
        double maxGrowth = RowGrowthComponent.GROWTH_STAGES * growthRate;
        double growthPercentage = ((this.growth / maxGrowth) * 100);
        if (growthPercentage >= 100.0) {
            return 100;
        } else {
            return (int) growthPercentage;
        }
    }

}
