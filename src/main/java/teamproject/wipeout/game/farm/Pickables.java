package teamproject.wipeout.game.farm;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import teamproject.wipeout.engine.component.PickableComponent;
import teamproject.wipeout.engine.component.ScriptComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.component.shape.Rectangle;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Creates a container for all {@link Pickable} instances in a game.
 */
public class Pickables {

    private final HashSet<Pickable> items;

    private final GameScene gameScene;
    private final SpriteManager spriteManager;
    private final ItemStore itemStore;

    private Runnable onUpdate;

    /**
     * Creates a new instance of {@code Pickables}
     *
     * @param scene         The {@link GameScene} this entity is part of
     * @param spriteManager Current {@link SpriteManager}
     * @param itemStore     Current {@link ItemStore}
     */
    public Pickables(GameScene scene, SpriteManager spriteManager, ItemStore itemStore) {
        this.items = new HashSet<Pickable>();
        this.gameScene = scene;
        this.spriteManager = spriteManager;
        this.itemStore = itemStore;
    }

    /**
     * @return {@code HashSet} of all available {@link Pickable} objects
     */
    public HashSet<Pickable> get() {
        return this.items;
    }

    /**
     * {@code onUpdate} setter
     *
     * @param onUpdate {@code Runnable} action executed after {@code Pickables} entity has been updated
     */
    public void setOnUpdate(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    /**
     * Update self(= {@code this}) based on the data in the given {@code Set<Pickable>}.
     *
     * @param updatedItems {@code Set} of {@link Pickable}s used for self-update
     */
    public void updateFrom(Set<Pickable> updatedItems) {
        HashSet<Pickable> removePickables = new HashSet<Pickable>();

        for (Pickable pickable : this.items) {
            if (updatedItems.contains(pickable)) {
                updatedItems.remove(pickable);
            } else {
                removePickables.add(pickable);
            }
        }

        for (Pickable pickable : updatedItems) {
            this.createPickable(pickable);
        }

        for (Pickable pickable : removePickables) {
            this.items.remove(pickable);
            pickable.entity.destroy();
        }
    }

    /**
     * Removes given {@code Set} of {@code Pickable}s from available objects
     *
     * @param pickedItems {@code Set} of {@link Pickable}s to be removed
     */
    public void picked(Set<Pickable> pickedItems) {
        for (Pickable pickable : pickedItems) {
            pickable.entity.destroy();
        }
        this.items.removeAll(pickedItems);

        if (this.onUpdate != null) {
            this.onUpdate.run();
        }
    }

    /**
     * Creates pickable entity(/entities) for a given item after it was harvested.
     * The entity(/entities) are rendered around a given X, Y scene coordinates.
     *
     * @param item              Harvested {@link Item}
     * @param x                 X scene coordinate
     * @param y                 Y scene coordinate
     * @param numberOfPickables The number of pickables to generate
     */
    public void createPickablesFor(Item item, double x, double y, int numberOfPickables) {
        Image sprite;
        try {
            InventoryComponent invComponent = item.getComponent(InventoryComponent.class);
            sprite = this.spriteManager.getSpriteSet(invComponent.spriteSheetName, invComponent.spriteSetName)[0];

        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            return;
        }

        Point2D centrePos = new Point2D(x, y);

        for (int i = 0; i < numberOfPickables; i++) {
            Point2D velocityVector = this.giveRandomPositionAround(x, y).subtract(centrePos).normalize().multiply(ThreadLocalRandom.current().nextDouble(40.0, 175.0));
            Pickable pickable = new Pickable(item.id, centrePos, velocityVector);
            pickable.entity = this.createPickableEntity(pickable, sprite);
            this.items.add(pickable);
        }

        if (this.onUpdate != null) {
            this.onUpdate.run();
        }
    }

    /**
     * Creates pickable entity(/entities) for a given item after it was harvested.
     * The entity(/entities) are rendered around a given X, Y scene coordinates.
     *
     * @param pickable {@link Pickable} to be spawned
     */
    private void createPickable(Pickable pickable) {
        Item item = this.itemStore.getItem(pickable.getID());
        Image sprite;
        try {
            InventoryComponent invComponent = item.getComponent(InventoryComponent.class);
            sprite = this.spriteManager.getSpriteSet(invComponent.spriteSheetName, invComponent.spriteSetName)[0];

        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            return;
        }

        pickable.entity = this.createPickableEntity(pickable, sprite);
        this.items.add(pickable);
    }

    /**
     * Creates a single {@code Pickable} game entity
     *
     * @param pickable {@link Pickable} for which the {@link GameEntity} is being created
     * @param sprite   {@code Image} for the given {@code Pickble}
     * @return Created {@code Pickable} {@code GameEntity}
     */
    private GameEntity createPickableEntity(Pickable pickable, Image sprite) {
        GameEntity entity = this.gameScene.createEntity();
        SpriteRenderable spriteRenderable = new SpriteRenderable(sprite, 0.01);

        entity.addComponent(new RenderComponent(spriteRenderable));
        entity.addComponent(new Transform(pickable.getStartPosition(), 0.0, 1));
        entity.addComponent(new HitboxComponent(new Rectangle(0, 0, sprite.getWidth() * 0.75, sprite.getHeight() * 0.75)));
        entity.addComponent(new MovementComponent(pickable.getVelocity(), Point2D.ZERO));
        entity.addComponent(new PickableComponent(pickable));

        // Growing animation
        entity.addComponent(new ScriptComponent((timeStep) -> {
            if (spriteRenderable.spriteScale.getX() < 0.75) {
                double step = timeStep * 10;
                spriteRenderable.spriteScale = spriteRenderable.spriteScale.add(step, step);
            } else {
                entity.getComponent(ScriptComponent.class).requestDeletion = true;
            }
        }));

        return entity;
    }

    /**
     * Gives random X, Y coordinates centered around a given X, Y scene coordinates
     *
     * @param x X scene coordinate
     * @param y Y scene coordinate
     * @return Randomised X, Y coordinates in form of a {@link Point2D}
     */
    private Point2D giveRandomPositionAround(double x, double y) {
        ThreadLocalRandom randomizer = ThreadLocalRandom.current();

        double randX;
        double randY;

        if (randomizer.nextBoolean()) {
            randX = randomizer.nextDouble(x - (2.0 * FarmEntity.SQUARE_SIZE), x);
            randY = randomizer.nextDouble(y - (2.0 * FarmEntity.SQUARE_SIZE), y);

        } else {
            randX = randomizer.nextDouble(x, x + (FarmEntity.SQUARE_SIZE / 4.0));
            randY = randomizer.nextDouble(y, y + (FarmEntity.SQUARE_SIZE / 4.0));
        }

        return new Point2D(randX, randY);
    }

}
