package teamproject.wipeout.engine.component;

import javafx.util.Pair;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantableComponent;

import java.util.ArrayList;

public class RowGrowthComponent implements GameComponent {

    public static final int GROWTH_STAGES = 4;

    public final ArrayList<Pair<Item, Double>> cropRow;

    public RowGrowthComponent(ArrayList<Pair<Item, Double>> cropRow) {
        this.cropRow = cropRow;
    }

    public void incrementCurrentGrowth(int column, double increment) {
        Pair<Item, Double> pair = this.cropRow.get(column);
        if (pair == null) {
            return;
        }
        PlantableComponent crop = pair.getKey().getComponent(PlantableComponent.class);
        double growth = pair.getValue();

        if (growth >= (GROWTH_STAGES * crop.growthRate)) {
            return;
        }
        growth += increment;
        this.cropRow.set(column, new Pair<Item, Double>(pair.getKey(), growth));
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
