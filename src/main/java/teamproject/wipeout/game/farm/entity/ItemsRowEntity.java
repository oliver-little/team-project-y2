package teamproject.wipeout.game.farm.entity;

import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.farm.FarmSpriteComponent;
import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.FarmItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * Represents a row of items.
 */
public class ItemsRowEntity extends GameEntity {

    private final RowGrowthComponent growthComponent;

    private List<Pair<GameEntity, FarmSpriteComponent>> rowRenderers;

    /**
     * Creates a new instance of {@code ItemsRowEntity}.
     *
     * @param scene The GameScene this entity is part of
     * @param row ArrayList of items in a certain row
     * @param growthUpdater Calls .accept() when growth is updated
     * @param spriteManager {@link SpriteManager} for the {@link ItemsRowRenderer}
     */
    public ItemsRowEntity(GameScene scene, List<FarmItem> row, Supplier<Double> growthMultiplier, Consumer<FarmItem> growthUpdater) {
        super(scene);

        this.growthComponent = new RowGrowthComponent(row, growthMultiplier, growthUpdater);
        this.rowRenderers = new ArrayList<>();
        
        this.addComponent(this.growthComponent);

        for (int i = 0; i < row.size(); i++) {
            this.rowRenderers.add(createRenderer(row, i));
        }
    }

    public void setFarmRow(List<FarmItem> row) {
        this.growthComponent.setFarmRow(row);

        // Remove elements if size has decreased
        while (this.rowRenderers.size() > row.size()) {
            rowRenderers.remove(this.rowRenderers.size() - 1).getKey().destroy();
        }

        this.rowRenderers.forEach((pair) -> pair.getValue().setFarmRow(row));

        // Add elements if size has increased
        while(this.rowRenderers.size() < row.size()) {
            int index = this.rowRenderers.size();
            this.rowRenderers.add(createRenderer(row, index));
        }
    }

    private Pair<GameEntity, FarmSpriteComponent> createRenderer(List<FarmItem> row, int index) {
        GameEntity child = new GameEntity(this.getScene());
        child.setParent(this);
        child.addComponent(new Transform(FarmEntity.SQUARE_SIZE + FarmEntity.SQUARE_SIZE * index, 0, 1));

        SpriteRenderable sprite = new SpriteRenderable(null);
        child.addComponent(new RenderComponent(sprite, new RectRenderable(Color.PINK, 1, 1)));
        FarmSpriteComponent spriteController = new FarmSpriteComponent(sprite, row, index);
        child.addComponent(spriteController);

        return new Pair<GameEntity, FarmSpriteComponent>(child, spriteController);
    }
}
