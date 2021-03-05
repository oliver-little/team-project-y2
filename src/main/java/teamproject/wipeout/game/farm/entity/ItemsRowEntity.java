package teamproject.wipeout.game.farm.entity;

import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.engine.component.render.ItemsRowRenderer;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.item.ItemStore;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Represents a row of items.
 */
public class ItemsRowEntity extends GameEntity {

    /**
     * Creates a new instance of {@code ItemsRowEntity}.
     *
     * @param scene The GameScene this entity is part of
     * @param row ArrayList of items in a certain row
     * @param growthUpdater Calls .accept() when growth is updated
     * @param spriteManager {@link SpriteManager} for the {@link ItemsRowRenderer}
     * @param itemStore {@link ItemStore} for the {@link ItemsRowRenderer}
     */
    public ItemsRowEntity(GameScene scene, ArrayList<FarmItem> row, Consumer<FarmItem> growthUpdater, SpriteManager spriteManager, ItemStore itemStore) {
        super(scene);
        this.addComponent(new RenderComponent(new ItemsRowRenderer(row, spriteManager, itemStore)));
        this.addComponent(new RowGrowthComponent(row, growthUpdater));
    }

}
