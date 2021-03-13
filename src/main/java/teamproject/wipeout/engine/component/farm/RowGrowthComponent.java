package teamproject.wipeout.engine.component.farm;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.item.Item;

import java.util.List;
import java.util.function.Consumer;

public class RowGrowthComponent implements GameComponent {

    protected final Consumer<FarmItem> growthUpdater;

    private List<FarmItem> cropRow;

    public RowGrowthComponent(List<FarmItem> cropRow, Consumer<FarmItem> updater) {
        this.cropRow = cropRow;
        this.growthUpdater = updater;
    }

    public void setCropRow(List<FarmItem> cropRow) {
        this.cropRow = cropRow;
    }

    public void incrementCurrentGrowth(int column, double increment) {
        FarmItem farmItem = this.cropRow.get(column);
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

        farmItem.growth += increment;
        this.growthUpdater.accept(farmItem);
    }

    public void updateGrowth(double timestep) {
        for (int i = 0; i < this.cropRow.size(); i++) {
            this.incrementCurrentGrowth(i, timestep);
        }
    }

    public String getType() {
        return "growth";
    }

}
