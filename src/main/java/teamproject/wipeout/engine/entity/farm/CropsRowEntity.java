package teamproject.wipeout.engine.entity.farm;

import javafx.util.Pair;
import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.engine.component.render.CropsRowRenderer;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;

import java.util.ArrayList;

/**
 * Represents a row of crops.
 */
public class CropsRowEntity extends GameEntity {

    /**
     * Creates a new instance of {@code CropsRowEntity}.
     *
     * @param scene The GameScene this entity is part of
     * @param row ArrayList of crops in a certain row
     * @param spriteManager {@link SpriteManager} for the {@link CropsRowRenderer}
     * @param itemStore {@link ItemStore} for the {@link CropsRowRenderer}
     */
    public CropsRowEntity(GameScene scene, ArrayList<Pair<Item, Double>> row, SpriteManager spriteManager, ItemStore itemStore) {
        super(scene);
        this.addComponent(new RenderComponent(new CropsRowRenderer(row, spriteManager, itemStore)));
        this.addComponent(new RowGrowthComponent(row));
    }

}
