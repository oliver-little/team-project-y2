package teamproject.wipeout.engine.component.farm;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.item.Item;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RowGrowthComponent implements GameComponent {

    private final Supplier<Double> growthMultiplier;
    private final Consumer<FarmItem> growthUpdater;

    private List<FarmItem> farmRow;

    public RowGrowthComponent(List<FarmItem> farmRow, Supplier<Double> growthMultiplier, Consumer<FarmItem> updater) {
        this.farmRow = farmRow;
        this.growthMultiplier = growthMultiplier;
        this.growthUpdater = updater;
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

        if (farmItem.getCurrentGrowthStage() == farmItem.getMaxGrowthStage()) {
            return;
        }

        farmItem.growth += increment * this.growthMultiplier.get();
        this.growthUpdater.accept(farmItem);
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
