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
import teamproject.wipeout.networking.state.StateException;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Pickables {

    public static class Pickable implements Serializable {
        private Integer id;
        private Point2D startPosition;
        private Point2D velocity;

        private GameEntity entity;

        public Pickable(Integer id, Point2D startPosition, Point2D velocity) {
            this.id = id;
            this.startPosition = startPosition;
            this.velocity = velocity;
        }

        public Integer getID() {
            return this.id;
        }

        // Methods writeObject(), readObject() and readObjectNoData() are implemented
        // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeInt(this.id);
            out.writeDouble(this.startPosition.getX());
            out.writeDouble(this.startPosition.getY());
            out.writeDouble(this.velocity.getX());
            out.writeDouble(this.velocity.getY());
        }

        private void readObject(ObjectInputStream in) throws IOException {
            this.id = in.readInt();
            this.startPosition = new Point2D(in.readDouble(), in.readDouble());
            this.velocity = new Point2D(in.readDouble(), in.readDouble());
        }

        private void readObjectNoData() throws StateException {
            throw new StateException("WorldState is corrupted");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Pickable that = (Pickable) o;
            return this.id.equals(that.id) && this.startPosition.equals(that.startPosition) && this.velocity.equals(that.velocity);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result += startPosition.hashCode();
            result += velocity.hashCode();
            return result;
        }
    }

    private HashSet<Pickable> items;

    private final GameScene gameScene;
    private final ItemStore itemStore;
    private final SpriteManager spriteManager;

    private Runnable onUpdate;

    public Pickables(GameScene gameScene, ItemStore itemStore, SpriteManager spriteManager) {
        this.items = new HashSet<Pickable>();
        this.gameScene = gameScene;
        this.itemStore = itemStore;
        this.spriteManager = spriteManager;
    }

    public HashSet<Pickable> get() {
        return this.items;
    }

    public void updateFrom(Set<Pickable> updatedItems) {
        HashSet<Pickables.Pickable> removePickables = new HashSet<Pickables.Pickable>();

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

    public void setOnUpdate(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

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
     * @param item Harvested {@link Item}
     * @param x X scene coordinate
     * @param y Y scene coordinate
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

        for(int i = 0; i < numberOfPickables; i++) {
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
        Item item = this.itemStore.getItem(pickable.id);
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

    private GameEntity createPickableEntity(Pickable pickable, Image sprite) {
        GameEntity entity = this.gameScene.createEntity();
        SpriteRenderable spriteRenderable = new SpriteRenderable(sprite, 0.01);
        entity.addComponent(new RenderComponent(spriteRenderable));
        entity.addComponent(new Transform(pickable.startPosition, 0.0, 1));
        entity.addComponent(new HitboxComponent(new Rectangle(0, 0, sprite.getWidth() * 0.75, sprite.getHeight() * 0.75)));
        entity.addComponent(new MovementComponent(pickable.velocity, Point2D.ZERO));
        entity.addComponent(new PickableComponent(pickable));
        // Growing animation
        entity.addComponent(new ScriptComponent((timeStep) -> {
            if (spriteRenderable.spriteScale.getX() < 0.75) {
                double step = timeStep * 10;
                spriteRenderable.spriteScale = spriteRenderable.spriteScale.add(step, step);
            }
            else {
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
            randX = randomizer.nextDouble(x - (2 * FarmEntity.SQUARE_SIZE), x);
            randY = randomizer.nextDouble(y - (2 * FarmEntity.SQUARE_SIZE), y);
        } else {
            randX = randomizer.nextDouble(x, x + (FarmEntity.SQUARE_SIZE / 4.0));
            randY = randomizer.nextDouble(y, y + (FarmEntity.SQUARE_SIZE / 4.0));
        }
        return new Point2D(randX, randY);
    }

}
