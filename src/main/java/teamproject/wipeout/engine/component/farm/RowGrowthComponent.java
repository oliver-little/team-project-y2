package teamproject.wipeout.engine.component.farm;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.item.Item;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RowGrowthComponent implements GameComponent {

    private List<FarmItem> farmRow;

    private final Supplier<Double> growthMultiplier;

    public RowGrowthComponent(List<FarmItem> farmRow, Supplier<Double> growthMultiplier) {
        this.farmRow = farmRow;
        this.growthMultiplier = growthMultiplier;
    }

    public void setFarmRow(List<FarmItem> farmRow) {
        this.farmRow = farmRow;
    }

    public void incrementCurrentGrowth(int column, double increment) {
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

    public void updateGrowth(double timestep) {
        for (int i = 0; i < this.farmRow.size(); i++) {
            this.incrementCurrentGrowth(i, timestep);
        }
    }

    public String getType() {
        return "growth";
    }

}
