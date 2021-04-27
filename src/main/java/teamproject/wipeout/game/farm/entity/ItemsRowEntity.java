package teamproject.wipeout.game.farm.entity;

import javafx.util.Pair;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.farm.FarmSpriteComponent;
import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.farm.FarmItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Represents a row of items.
 *
 * @see GameEntity
 */
public class ItemsRowEntity extends GameEntity {

    private final RowGrowthComponent growthComponent;
    private final List<Pair<GameEntity, FarmSpriteComponent>> rowRenderers;

    /**
     * Creates a new instance of {@code ItemsRowEntity}.
     *
     * @param scene            The {@link GameScene} this entity is part of
     * @param row              {@code List} of items in a certain row
     * @param growthMultiplier {@code Supplier} og growth multiplier {@code double} value
     */
    public ItemsRowEntity(GameScene scene, List<FarmItem> row, Supplier<Double> growthMultiplier) {
        super(scene);

        this.growthComponent = new RowGrowthComponent(row, growthMultiplier);
        this.rowRenderers = new ArrayList<Pair<GameEntity, FarmSpriteComponent>>();

        this.addComponent(this.growthComponent);

        for (int i = 0; i < row.size(); i++) {
            this.rowRenderers.add(this.createRenderer(row, i));
        }
    }

    /**
     * Sets an updated {@code List} of {@code FarmItem}s in a row.
     *
     * @param newRow Updated {@code List} of {@code FarmItem}s in a row
     */
    public void setFarmRow(List<FarmItem> newRow) {
        this.growthComponent.setFarmRow(newRow);

        // Remove elements if size has decreased
        while (this.rowRenderers.size() > newRow.size()) {
            rowRenderers.remove(this.rowRenderers.size() - 1).getKey().destroy();
        }

        // Update farm row for each renderer, also update last growth stage to force update the sprite being shown
        this.rowRenderers.forEach((pair) -> {
            FarmSpriteComponent fs = pair.getValue();
            fs.setFarmRow(newRow);
            fs.setLastGrowthStage(-1);
            fs.spriteRenderer.sprite = null;
        });

        // Add elements if size has increased
        while (this.rowRenderers.size() < newRow.size()) {
            int index = this.rowRenderers.size();
            this.rowRenderers.add(createRenderer(newRow, index));
        }
    }

    /**
     * Creates a renderer for an individual item in a row.
     *
     * @param row   Row of the item
     * @param index Index of the item's position
     * @return {@code Pair} of created {@link GameEntity} and {@link FarmSpriteComponent}
     */
    private Pair<GameEntity, FarmSpriteComponent> createRenderer(List<FarmItem> row, int index) {
        GameEntity child = this.getScene().createEntity();
        child.setParent(this);
        child.addComponent(new Transform(FarmEntity.SQUARE_SIZE + FarmEntity.SQUARE_SIZE * index, 0, 1));

        SpriteRenderable sprite = new SpriteRenderable(null);
        child.addComponent(new RenderComponent(sprite));

        FarmSpriteComponent spriteController = new FarmSpriteComponent(sprite, row, index);
        child.addComponent(spriteController);

        return new Pair<GameEntity, FarmSpriteComponent>(child, spriteController);
    }

}
