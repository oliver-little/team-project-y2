package teamproject.wipeout.engine.system.farm;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

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
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;

public class FarmSpriteSystem implements GameSystem {

    public SpriteManager spriteManager;

    protected SignatureEntityCollector entityCollector;

    public FarmSpriteSystem(GameScene scene, SpriteManager spriteManager) {
        entityCollector = new SignatureEntityCollector(scene, Set.of(Transform.class, FarmSpriteComponent.class));

        this.spriteManager = spriteManager;
    }

    public void cleanup() {
        entityCollector.cleanup();
    }

    public void accept(Double timeStep) {
        List<GameEntity> entities = entityCollector.getEntities();

        for (GameEntity entity : entities) {
            FarmSpriteComponent fs = entity.getComponent(FarmSpriteComponent.class);
            FarmItem farmItem = fs.getFarmRow().get(fs.getIndex());

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
                entityTransform.setPosition(entityTransform.getPosition().add(0, -lastYOffset+yOffset));
            } 
            catch (FileNotFoundException exception) {
                exception.printStackTrace();
            }
        }
    }

    

    private Point2D rescaleToFitWidth(int squareScale, double w, double h) {
        double scaleFactor = FarmEntity.scaleFactorToFitWidth(squareScale, w, h);
        return new Point2D(w * scaleFactor, h * scaleFactor);
    }
}
