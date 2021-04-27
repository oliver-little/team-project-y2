package teamproject.wipeout.engine.system.farm;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.farm.FarmSpriteComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.farm.entity.SeedEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

/**
 * System that handles rendering of all plants with the {@link FarmSpriteComponent}.
 *
 * @see GameSystem
 */
public class FarmSpriteSystem implements GameSystem {

    private final SignatureEntityCollector plantCollector;
    private final SpriteManager spriteManager;

    /**
     * Creates an instance of a {@code FarmSpriteSystem}.
     *
     * @param scene         The {@link GameScene} this system is part of
     * @param spriteManager Current {@link SpriteManager} instance
     */
    public FarmSpriteSystem(GameScene scene, SpriteManager spriteManager) {
        this.plantCollector = new SignatureEntityCollector(scene, Set.of(Transform.class, FarmSpriteComponent.class));
        this.spriteManager = spriteManager;
    }

    /**
     * Cleans up the plant collector instance of type {@link SignatureEntityCollector}.
     */
    public void cleanup() {
        plantCollector.cleanup();
    }

    public void accept(Double timeStep) {
        List<GameEntity> entities = plantCollector.getEntities();

        for (GameEntity entity : entities) {
            FarmSpriteComponent fs = entity.getComponent(FarmSpriteComponent.class);
            FarmItem farmItem = fs.getItem();

            if (farmItem == null) {
                fs.spriteRenderer.sprite = null;
                continue;
            }

            Item currentItem = farmItem.get();
            if (currentItem == null) {
                fs.spriteRenderer.sprite = null;
                continue;
            }

            int growthStage = farmItem.getCurrentGrowthStage();

            if (growthStage == fs.getLastGrowthStage()) {
                continue;
            }

            fs.setLastGrowthStage(growthStage);

            PlantComponent plant = currentItem.getComponent(PlantComponent.class);
            try {
                Image[] sprites = this.spriteManager.getSpriteSet(plant.spriteSheetName, plant.spriteSetName);

                Image sprite = sprites[growthStage];
                fs.spriteRenderer.sprite = sprite;

                Point2D spriteScale = this.rescaleToFitWidth(plant.width, sprite.getWidth(), sprite.getHeight());
                fs.spriteRenderer.spriteScale = new Point2D(spriteScale.getX() / sprite.getWidth(), spriteScale.getY() / sprite.getHeight());


                double lastYOffset = fs.getLastYOffset();
                double yOffset = -spriteScale.getY() * (0.5 / plant.height);
                fs.setLastYOffset(yOffset);

                Transform entityTransform = entity.getComponent(Transform.class);
                entityTransform.setPosition(entityTransform.getPosition().add(0, -lastYOffset + yOffset));

            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Calculates {@code Point2D} scale factor to fit the width of farm's square space(s).
     *
     * @param squareWidth Plant's square width value of type {@code int}
     * @param width       {@code double} value of width to be rescaled
     * @param height      {@code double} value of height to be rescaled
     * @return Calculated new {@link Point2D} scale factor value for width (X) and height (Y)
     */
    private Point2D rescaleToFitWidth(int squareWidth, double width, double height) {
        double scaleFactor = SeedEntity.scaleFactorToFitWidth(squareWidth, width);
        return new Point2D(width * scaleFactor, height * scaleFactor);
    }

}
