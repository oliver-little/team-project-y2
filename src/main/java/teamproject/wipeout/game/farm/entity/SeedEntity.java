package teamproject.wipeout.game.farm.entity;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.ItemsRowRenderer;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.item.components.PlantComponent;

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
     * @throws FileNotFoundException Thrown when {@link SpriteManager} cannot find a sprite for the item's seeds.
     */
    public SeedEntity(GameScene scene, Item item, SpriteManager spriteManager) throws FileNotFoundException {
        super(scene);

        PlantComponent plant = item.getComponent(PlantComponent.class);
        InventoryComponent inventory = item.getComponent(InventoryComponent.class);

        int widthPixelSize = plant.width * FarmEntity.SQUARE_SIZE;
        int heightPixelSize = plant.height * FarmEntity.SQUARE_SIZE;
        Image seedImage = spriteManager.getSpriteSet(inventory.spriteSheetName, inventory.spriteSetName)[0];

        this.seedArea = new RectRenderable(Color.GREEN, widthPixelSize, heightPixelSize * FarmEntity.SKEW_FACTOR);
        this.seedArea.radius = Math.max(widthPixelSize, heightPixelSize) / 3.0;
        this.seedArea.alpha = 0.4;


        // Show seed as separate entity to handle offset
        GameEntity seedRenderEntity = new GameEntity(scene);
        seedRenderEntity.setParent(this);

        seedRenderEntity.addComponent(new Transform(0, 0, 2));

        SpriteRenderable seedRenderable = new SpriteRenderable(seedImage);
        double scaleFactor = ItemsRowRenderer.scaleFactorToFitWidth(plant.width, seedImage.getWidth(), seedImage.getHeight());
        seedRenderable.spriteScale = new Point2D(scaleFactor, scaleFactor);
        RenderComponent seedRenderComponent = new RenderComponent(seedRenderable);
        seedRenderEntity.addComponent(seedRenderComponent);
        if (plant.height != 1) {
            seedRenderComponent.offset = new Point2D(0, -1 * (seedRenderable.getHeight() / 2.5));
        }
        this.renderComponent = new RenderComponent();
        this.addComponent(this.renderComponent);
    }

    /**
     * Shows green overlay behind seeds.
     */
    public void showAreaOverlay() {
        if (!this.renderComponent.hasRenderable(seedArea)) {
            this.renderComponent.addRenderable(0, seedArea);
        }
    }

    /**
     * Hides green overlay behind seeds.
     */
    public void hideAreaOverlay() {
        this.renderComponent.removeRenderable(seedArea);
    }

}