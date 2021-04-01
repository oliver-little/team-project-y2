package teamproject.wipeout.game.farm;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;

/**
 * Wrapper of an {@link Item} for purposes of a {@link FarmData} instance.
 */
public class FarmItem {

    public final DoubleProperty growth;

    private final Item item;

    /**
     * Initializes {@code FarmItem} with a given item
     * and a growth value of {@code 0.0}.
     *
     * @param item {@link Item} with which the {@code FarmItem} is initialized.
     */
    public FarmItem(Item item) {
        this.item = item;
        this.growth = new SimpleDoubleProperty(0.0);
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
        this.growth = new SimpleDoubleProperty(growth);
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
     * Gets the max growth stage of the {@link Item} from the {@link PlantComponent}.
     *
     * @return Max growth stage in the form of an {@code int}.
     */
    public int getMaxGrowthStage() {
        return this.item.getComponent(PlantComponent.class).maxGrowthStage;
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
        double growthRate = this.getGrowthRate();
        return (int) (this.growth.get() / growthRate);
    }

    /**
     * Calculates the current growth percentage based on the growth rate,
     * number of growth stages, and the current growth.
     *
     * @return Current growth percentage in the form of an {@code int}.
     */
    public int getCurrentGrowthPercentage() {
        PlantComponent plant = this.item.getComponent(PlantComponent.class);
        double maxGrowth = plant.maxGrowthStage * plant.growthRate;
        double growthPercentage = ((this.growth.get() / maxGrowth) * 100);
        if (growthPercentage >= 100.0) {
            return 100;
        } else {
            return (int) growthPercentage;
        }
    }

    /**
     * Checks whether the plant is fully grown.
     *
     * @return {@code true} when the plant is fully grown, otherwise {@code false}.
     */
    public boolean isFullyGrown() {
        if (this.item == null) {
            return false;
        }
        PlantComponent plant = this.item.getComponent(PlantComponent.class);
        return this.growth.get() >= plant.maxGrowthStage * plant.growthRate;
    }

}
