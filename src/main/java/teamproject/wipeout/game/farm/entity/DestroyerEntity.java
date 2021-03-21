package teamproject.wipeout.game.farm.entity;

import javafx.scene.paint.Color;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.item.components.PlantComponent;

/**
 * Represents a tool for picking plants.
 */
public class DestroyerEntity extends GameEntity {

    protected RectRenderable destroyerArea;

    private boolean destroyMode;

    /**
     * Creates a new instance of {@code DestroyerEntity}.
     *
     * @param scene The GameScene this entity is part of
     */
    public DestroyerEntity(GameScene scene) {
        super(scene);

        this.destroyerArea = new RectRenderable(Color.RED, FarmEntity.SQUARE_SIZE, FarmEntity.SQUARE_SIZE * FarmEntity.SKEW_FACTOR);
        this.destroyerArea.radius = FarmEntity.SQUARE_SIZE / 3.0;
        this.destroyerArea.alpha = 0.5;
        destroyMode = false;

        this.addComponent(new RenderComponent(this.destroyerArea));
    }

    /**
     * Creates a new instance of {@code DestroyerEntity}.
     *
     * @param scene The GameScene this entity is part of
     */
    public DestroyerEntity(GameScene scene, boolean destroyMode) {
        super(scene);

        this.destroyerArea = new RectRenderable(Color.RED, FarmEntity.SQUARE_SIZE, FarmEntity.SQUARE_SIZE * FarmEntity.SKEW_FACTOR);
        this.destroyerArea.radius = FarmEntity.SQUARE_SIZE / 3.0;
        this.destroyerArea.alpha = 0.5;
        this.destroyMode = destroyMode;

        this.addComponent(new RenderComponent(this.destroyerArea));
    }

    /**
     * Sets green or red color for the entity based on a given argument.
     *
     * @param isPickable {@code true} for green. {@code true} for red.
     */
    public void setColorForPickable(boolean isPickable) {
        this.destroyerArea.color = isPickable ? Color.GREEN : Color.RED;
    }

    /**
     * Adapts (= resizes and changes color) the {@code DestroyerEntity}
     * to the {@code FarmItem} it is currently pointing to.
     *
     * @param farmItemPair {@link Pair} of {@link FarmItem} and {@code Boolean}(= is grown?) that the entity
     * is currently pointing to. Pass {@code null} to reset the {@code DestroyerEntity} to default state.
     */
    public void adaptToFarmItem(Pair<FarmItem, Boolean> farmItemPair) {
        if (farmItemPair == null) {
            this.destroyerArea.color = Color.RED;
            this.resizeUsing(null);
        } else {
            this.setColorForPickable(farmItemPair.getValue());
            this.resizeUsing(farmItemPair.getKey().get().getComponent(PlantComponent.class));
        }
    }

    /**
     * Adapts (= resizes and changes color) the {@code DestroyerEntity}
     * to the {@code FarmItem} it is currently pointing to.
     *
     * @param farmItemPair {@link Pair} of {@link FarmItem} and {@code Boolean}(= is grown?) that the entity
     * is currently pointing to. Pass {@code null} to reset the {@code DestroyerEntity} to default state.
     */
    public void adaptToDestroyFarmItem(Pair<FarmItem, Boolean> farmItemPair) {
        if (farmItemPair == null) {
            this.destroyerArea.color = Color.RED;
            this.resizeUsing(null);
        } else {
            this.setColorForPickable(true);
            this.resizeUsing(farmItemPair.getKey().get().getComponent(PlantComponent.class));
        }
    }

    /**
     * Resizes the {@code DestroyerEntity} based on a given {@code PlantComponent}.
     *
     * @param plant {@link PlantComponent} used for resizing.
     * Pass {@code null} to reset the {@code DestroyerEntity} to default size.
     */
    protected void resizeUsing(PlantComponent plant) {
        if (plant == null || (plant.width == 1  && plant.height == 1)) {
            this.destroyerArea.width = FarmEntity.SQUARE_SIZE;
            this.destroyerArea.height = FarmEntity.SQUARE_SIZE * FarmEntity.SKEW_FACTOR;
            return;
        }
        this.destroyerArea.width = plant.width * FarmEntity.SQUARE_SIZE;
        this.destroyerArea.height = plant.height * FarmEntity.SQUARE_SIZE;
    }

    public boolean getDestroyMode() {
        return this.destroyMode;
    }

}
