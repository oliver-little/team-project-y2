package teamproject.wipeout.engine.system;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.SabotageComponent;
import teamproject.wipeout.game.item.components.SabotageComponent.SabotageType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * Tests that SabotageSystem correctly adds and removes modifiers based on the item ID
 */
public class SabotageSystemTest {
    @Test
    public void testSpeedPotion() {
        GameScene scene = new GameScene();
        SabotageSystem system = new SabotageSystem(scene);
        GameEntity entity = new GameEntity(scene);
        MovementComponent mc = new MovementComponent();
        entity.addComponent(mc);

        assertEquals(1, mc.getSpeedMultiplier());

        entity.addComponent(new SabotageComponent(SabotageType.SPEED, 0.5, 2));
        assertEquals(2, mc.getSpeedMultiplier());

        try {
            Thread.sleep(750);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, mc.getSpeedMultiplier());
        
        try {
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 2));
            Thread.sleep(50);
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 2));
            Thread.sleep(50);
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 2));
            assertEquals(8, mc.getSpeedMultiplier());

            Thread.sleep(1100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, mc.getSpeedMultiplier());

        try {
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 0.5));
            Thread.sleep(50);
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 0.5));
            Thread.sleep(50);
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 0.5));
            assertEquals(0.125, mc.getSpeedMultiplier());

            Thread.sleep(1100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, mc.getSpeedMultiplier());

        try {
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 0.5));
            Thread.sleep(50);
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 0.5));
            Thread.sleep(50);
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 4));
            assertEquals(1, mc.getSpeedMultiplier());

            Thread.sleep(1100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, mc.getSpeedMultiplier());

        system.cleanup();
    }

    @Test
    public void testSpeedPotionLimiter() {
        GameScene scene = new GameScene();
        SabotageSystem system = new SabotageSystem(scene);
        GameEntity entity = new GameEntity(scene);
        MovementComponent mc = new MovementComponent();
        entity.addComponent(mc);

        assertEquals(1, mc.getSpeedMultiplier());

        try {
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 7));
            Thread.sleep(50);
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 2));
            assertEquals(7, mc.getSpeedMultiplier());

            Thread.sleep(1100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, mc.getSpeedMultiplier());

        try {
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 8));
            Thread.sleep(50);
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 2));
            assertEquals(8, mc.getSpeedMultiplier());

            Thread.sleep(1100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, mc.getSpeedMultiplier());

        try {
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 0.25));
            Thread.sleep(50);
            entity.addComponent(new SabotageComponent(SabotageType.SPEED, 1, 0.25));
            assertEquals(0.25, mc.getSpeedMultiplier());

            Thread.sleep(1100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, mc.getSpeedMultiplier());
        
        system.cleanup();
    }

    @Test
    public void testFarmGrowthSpeed() {
        GameScene scene = new GameScene();
        SabotageSystem system = new SabotageSystem(scene);
        FarmEntity farm = null;

        try {
            farm = new FarmEntity(scene, 1, new Point2D(0, 0), null, new SpriteManager(), new ItemStore("items.json"));
        }
        catch (Exception e) {
            fail(e);
        }
        assertEquals(1, farm.getGrowthMultiplier());

        try {
            farm.addComponent(new SabotageComponent(SabotageType.GROWTHRATE, 1, 4));
            assertEquals(4, farm.getGrowthMultiplier());
            Thread.sleep(1100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, farm.getGrowthMultiplier());

        try {
            farm.addComponent(new SabotageComponent(SabotageType.GROWTHRATE, 1, 0.5));
            assertEquals(0.5, farm.getGrowthMultiplier());
            Thread.sleep(1100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, farm.getGrowthMultiplier());

        system.cleanup();
    }

    @Test
    public void testFarmAIMultiplier() {
        GameScene scene = new GameScene();
        SabotageSystem system = new SabotageSystem(scene);
        FarmEntity farm = null;

        try {
            farm = new FarmEntity(scene, 1, new Point2D(0, 0), null, new SpriteManager(), new ItemStore("items.json"));
        }
        catch (Exception e) {
            fail(e);
        }
        assertEquals(1, farm.getAIMultiplier());

        try {
            farm.addComponent(new SabotageComponent(SabotageType.AI, 1, 4));
            assertEquals(4, farm.getAIMultiplier());
            Thread.sleep(1100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, farm.getAIMultiplier());

        try {
            farm.addComponent(new SabotageComponent(SabotageType.AI, 1, 0.5));
            assertEquals(0.5, farm.getAIMultiplier());
            Thread.sleep(1100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, farm.getAIMultiplier());

        system.cleanup();
    }
}
