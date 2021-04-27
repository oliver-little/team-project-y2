package teamproject.wipeout.engine.component.farm;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.item.Item;

import java.util.List;
import java.util.function.Supplier;

/**
 * Wrapper for a farm's row growth functionality; contains the farm row,
 * and growth multiplier supplier.
 *
 * @see GameComponent
 */
public class RowGrowthComponent implements GameComponent {

    private List<FarmItem> farmRow;

    private final Supplier<Double> growthMultiplier;

    /**
     * Creates an instance of a {@code RowGrowthComponent}.
     *
     * @param farmRow          {@code List} of {@link FarmItem}s that represent a farm row
     * @param growthMultiplier {@code Supplier} of current growth multiplier for the farm
     */
    public RowGrowthComponent(List<FarmItem> farmRow, Supplier<Double> growthMultiplier) {
        this.farmRow = farmRow;
        this.growthMultiplier = growthMultiplier;
    }

    /**
     * @return {@code String} type of the {@code RowGrowthComponent}
     */
    public String getType() {
        return "farm-growth";
    }

    /**
     * {@code farmRow} setter
     *
     * @param farmRow New {@code List} of {@link FarmItem}s
     */
    public void setFarmRow(List<FarmItem> farmRow) {
        this.farmRow = farmRow;
    }

    /**
     * Increments growth value of all {@code FarmItem}s in the component's row.
     *
     * @param timestep {@code double} timestep for the growth increment
     */
    public void updateGrowth(double timestep) {
        for (int i = 0; i < this.farmRow.size(); i++) {
            this.incrementCurrentGrowth(i, timestep);
        }
    }

    /**
     * Increments growth value of a {@code FarmItem} in the given column.
     *
     * @param column    Column of the {@code FarmItem} that needs growing
     * @param increment Value with which the {@code FarmItem}'s growth will be incremented
     */
    private void incrementCurrentGrowth(int column, double increment) {
        FarmItem farmItem = this.farmRow.get(column);
        if (farmItem == null) {
            return;
        }
        Item item = farmItem.get();
        if (item == null) {
            return;
        }

        if (farmItem.isFullyGrown()) {
            return;
        }

        double newGrowth = farmItem.growth.get() + (increment * this.growthMultiplier.get());
        farmItem.growth.set(newGrowth);
    }

}
