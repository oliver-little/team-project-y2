package teamproject.wipeout.engine.component.farm;

import java.util.List;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.game.farm.FarmItem;

public class FarmSpriteComponent implements GameComponent {

    public final SpriteRenderable spriteRenderer;

    private final int index;

    private double lastYOffset;
    private int lastGrowthStage;
    private List<FarmItem> farmRow;

    public FarmSpriteComponent(SpriteRenderable spriteRenderer, List<FarmItem> farmRow, int index) {
        this.spriteRenderer = spriteRenderer;
        this.farmRow = farmRow;
        this.index = index;
        this.lastGrowthStage = -1;
        this.lastYOffset = 0;
    }

    public FarmItem getItem() {
        return this.farmRow.get(index);
    }

    public List<FarmItem> getFarmRow() {
        return this.farmRow;
    }

    public void setFarmRow(List<FarmItem> farmRow) {
        this.farmRow = farmRow;
    }

    public int getIndex() {
        return this.index;
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
