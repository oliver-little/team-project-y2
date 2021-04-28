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
 * Represents a tool for picking/destroying plants.
 *
 * @see GameEntity
 */
public class DestroyerEntity extends GameEntity {

    private final RectRenderable destroyerArea;
    private final boolean destroyMode;

    /**
     * Creates a new instance of {@code DestroyerEntity}.
     *
     * @param scene       The {@link GameScene} this entity is part of
     * @param destroyMode Specifies whether the entity is a destroyer or picker;
     *                    {@code true} for destroyer, {@code false} for picker.
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
     * {@code destroyMode} getter
     *
     * @return {@code true} if the player is destroying an item or {@code false} if the player is harvesting an item.
     */
    public boolean getDestroyMode() {
        return this.destroyMode;
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
     * Adapts(= resizes and changes color) the {@code DestroyerEntity}
     * to the {@code FarmItem} it is currently pointing to.
     *
     * @param farmItemPair {@link Pair} of {@link FarmItem} and {@code Boolean}(= is fully grown?) that the entity
     *                     is currently pointing to. Pass {@code null} to reset the {@code DestroyerEntity} to default state.
     */
    public void adaptToFarmItem(Pair<FarmItem, Boolean> farmItemPair) {
        if (farmItemPair == null) {
            this.destroyerArea.color = Color.RED;
            this.resizeUsing(null);

        } else {
            this.setColorForPickable(this.destroyMode || farmItemPair.getValue());
            this.resizeUsing(farmItemPair.getKey().get().getComponent(PlantComponent.class));
        }
    }

    /**
     * Resizes the {@code DestroyerEntity} based on the given {@code PlantComponent}.
     *
     * @param plantComponent {@link PlantComponent} used for resizing.
     *                       Pass {@code null} to reset the {@code DestroyerEntity} to default size.
     */
    private void resizeUsing(PlantComponent plantComponent) {
        if (plantComponent == null) {
            this.destroyerArea.width = FarmEntity.SQUARE_SIZE;
            this.destroyerArea.height = FarmEntity.SQUARE_SIZE;
            return;
        }
        this.destroyerArea.width = plantComponent.width * FarmEntity.SQUARE_SIZE;
        this.destroyerArea.height = plantComponent.height * FarmEntity.SQUARE_SIZE * FarmEntity.SKEW_FACTOR;
    }

}
