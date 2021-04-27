package teamproject.wipeout.engine.component.farm;

import java.util.List;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.game.farm.FarmItem;

public class FarmSpriteComponent implements GameComponent {

    public final SpriteRenderable spriteRenderer;

    private final int index;

    private List<FarmItem> farmRow;
    private int lastGrowthStage;
    private double lastYOffset;

    public FarmSpriteComponent(SpriteRenderable spriteRenderer, List<FarmItem> farmRow, int index) {
        this.spriteRenderer = spriteRenderer;

        this.index = index;

        this.farmRow = farmRow;
        this.lastGrowthStage = -1;
        this.lastYOffset = 0;
    }

    public FarmItem getItem() {
        return this.farmRow.get(index);
    }

    public void setFarmRow(List<FarmItem> farmRow) {
        this.farmRow = farmRow;
    }

    public int getLastGrowthStage() {
        return this.lastGrowthStage;
    }

    public void setLastGrowthStage(int lastGrowthStage) {
        this.lastGrowthStage = lastGrowthStage;
    }

    public double getLastYOffset() {
        return this.lastYOffset;
    }

    public void setLastYOffset(double lastYOffset) {
        this.lastYOffset = lastYOffset;
    }

    public String getType() {
        return "farm-sprite";
    }
}
