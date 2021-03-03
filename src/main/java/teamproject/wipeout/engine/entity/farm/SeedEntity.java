package teamproject.wipeout.engine.entity.farm;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.game.item.components.SeedComponent;

import java.io.FileNotFoundException;

/**
 * Represents a seed prepared to be planted.
 */
public class SeedEntity extends GameEntity {

    protected RenderComponent renderComponent;
    protected RectRenderable seedArea;

    /**
     * Creates a new instance of {@code SeedEntity}.
     *
     * @param scene The GameScene this entity is part of
     * @param item Item to be displayed as a seed
     * @param spriteManager {@link SpriteManager} used for getting the seed sprite
     */
    public SeedEntity(GameScene scene, Item item, SpriteManager spriteManager) throws FileNotFoundException {
        super(scene);
        scene.addEntity(this);

        SeedComponent seed = item.getComponent(SeedComponent.class);
        PlantComponent plant = item.getComponent(PlantComponent.class);
        int widthPixelSize = plant.width * FarmEntity.SQUARE_SIZE;
        int heightPixelSize = plant.height * FarmEntity.SQUARE_SIZE;
        Image seedImage = spriteManager.getSpriteSet(seed.spriteSheetName, seed.spriteSetName)[0];

        this.seedArea = new RectRenderable(Color.GREEN, widthPixelSize, heightPixelSize);
        this.seedArea.radius = Math.max(widthPixelSize, heightPixelSize) / 3.0;
        this.seedArea.alpha = 0.4;

        SpriteRenderable seedRenderable = new SpriteRenderable(seedImage);

        this.renderComponent = new RenderComponent(seedRenderable);
        this.addComponent(this.renderComponent);
    }

    public void showAreaOverlay() {
        if (!this.renderComponent.hasRenderable(seedArea)) {
            this.renderComponent.addRenderable(0, seedArea);
        }
    }

    public void hideAreaOverlay() {
        this.renderComponent.removeRenderable(seedArea);
    }

}