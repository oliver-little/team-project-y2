package teamproject.wipeout.engine.component.farm;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.game.farm.FarmItem;

import java.util.List;

/**
 * Wrapper for a {@link FarmItem} sprite; contains the sprite renderer,
 * and reference to the position inside a farm row which references to the {@code FarmItem}
 * that needs to be rendered.
 *
 * @see GameComponent
 */
public class FarmSpriteComponent implements GameComponent {

    public final SpriteRenderable spriteRenderer;

    private final int column;

    private List<FarmItem> farmRow;
    private int lastGrowthStage;
    private double lastYOffset;

    /**
     * Creates an instance of a {@code FarmSpriteComponent}.
     *
     * @param spriteRenderer {@link SpriteRenderable} that will be rendering a {@code FarmItem}
     * @param farmRow        Farm row that contains the {@code FarmItem}
     * @param column         Index of the {@code FarmItem} in the farm row
     */
    public FarmSpriteComponent(SpriteRenderable spriteRenderer, List<FarmItem> farmRow, int column) {
        this.spriteRenderer = spriteRenderer;

        this.column = column;

        this.farmRow = farmRow;
        this.lastGrowthStage = -1;
        this.lastYOffset = 0;
    }

    /**
     * @return {@code String} type of the {@code FarmSpriteComponent}
     */
    public String getType() {
        return "farm-sprite";
    }

    /**
     * Gets {@code FarmItem} tied to the {@code FarmSpriteComponent}
     *
     * @return {@link FarmItem}
     */
    public FarmItem getItem() {
        return this.farmRow.get(this.column);
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
     * {@code lastGrowthStage} getter
     *
     * @return {@code int} value of the {@code lastGrowthStage}
     */
    public int getLastGrowthStage() {
        return this.lastGrowthStage;
    }

    /**
     * {@code lastGrowthStage} setter
     *
     * @param lastGrowthStage New {@code int} value of the {@code lastGrowthStage}
     */
    public void setLastGrowthStage(int lastGrowthStage) {
        this.lastGrowthStage = lastGrowthStage;
    }

    /**
     * {@code lastYOffset} getter
     *
     * @return {@code double} value of the {@code lastYOffset}
     */
    public double getLastYOffset() {
        return this.lastYOffset;
    }

    /**
     * {@code lastYOffset} setter
     *
     * @return New {@code double} value of the {@code lastYOffset}
     */
    public void setLastYOffset(double lastYOffset) {
        this.lastYOffset = lastYOffset;
    }

}
