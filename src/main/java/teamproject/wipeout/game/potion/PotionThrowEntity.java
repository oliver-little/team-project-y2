package teamproject.wipeout.game.potion;


import java.io.FileNotFoundException;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.input.Clickable;
import teamproject.wipeout.engine.component.input.EntityClickAction;
import teamproject.wipeout.engine.component.input.Hoverable;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.player.Player;

/**
 * Generates an icon following the mouse
 */
public class PotionThrowEntity extends GameEntity {

    public static final double MAX_THROW_DISTANCE = 500.0;

    public Player throwingPlayer;
    public Item potion;

    private Transform potionTransform;

    private SpriteManager spriteManager;

    private Runnable onComplete;
    private Runnable onAbort;

    public PotionThrowEntity(GameScene scene, SpriteManager sm, Player throwingPlayer, Item potion, Runnable onComplete, Runnable onAbort) {
        super(scene);

        this.throwingPlayer = throwingPlayer;
        this.potion = potion;
        this.onComplete = onComplete;
        this.onAbort = onAbort;
        this.spriteManager = sm;

        potionTransform = new Transform(0, 0, 10);
        this.addComponent(potionTransform);
        Image potionSprite = null;
        InventoryComponent itemInventory = potion.getComponent(InventoryComponent.class);
        try {
            potionSprite = sm.getSpriteSet(itemInventory.spriteSheetName, itemInventory.spriteSetName)[0];
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.addComponent(new Hoverable((x, y) -> potionTransform.setPosition(new Point2D(x, y))));
        this.addComponent(new RenderComponent(new SpriteRenderable(potionSprite)));
        this.addComponent(new Clickable(this.onClick));
    }

    public void abortThrowing() {
        this.onAbort.run();
        this.destroy();
    }

    public EntityClickAction onClick = (x, y, button) -> {
        if (button == MouseButton.SECONDARY) {
            this.abortThrowing();
        }
        else {
            // Throw potion
            Point2D startPos = throwingPlayer.getComponent(Transform.class).getWorldPosition();
            Point2D clickPos = new Point2D(x, y);
            Point2D endPos = startPos.add(clickPos.subtract(startPos).normalize().multiply(MAX_THROW_DISTANCE));

            new PotionEntity(this.getScene(), spriteManager, potion, startPos, endPos);
            this.onComplete.run();
            this.destroy();
        }
    };
}
