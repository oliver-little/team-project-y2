package teamproject.wipeout.game.farm.entity;

import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.engine.component.render.ItemsRowRenderer;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.FarmItem;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents a row of items.
 */
public class ItemsRowEntity extends GameEntity {

    private final ItemsRowRenderer rowRenderer;
    private final RowGrowthComponent growthComponent;

    /**
     * Creates a new instance of {@code ItemsRowEntity}.
     *
     * @param scene The GameScene this entity is part of
     * @param row ArrayList of items in a certain row
     * @param growthUpdater Calls .accept() when growth is updated
     * @param spriteManager {@link SpriteManager} for the {@link ItemsRowRenderer}
     */
    public ItemsRowEntity(GameScene scene, List<FarmItem> row, Supplier<Double> growthMultiplier, Consumer<FarmItem> growthUpdater, SpriteManager spriteManager) {
        super(scene);

        this.growthComponent = new RowGrowthComponent(row, growthMultiplier, growthUpdater);
        this.rowRenderer = new ItemsRowRenderer(row, spriteManager);

        this.addComponent(this.growthComponent);
        this.addComponent(new RenderComponent(this.rowRenderer));
    }

    public void setFarmRow(List<FarmItem> row) {
        this.growthComponent.setFarmRow(row);
        this.rowRenderer.setFarmRow(row);
    }

}
