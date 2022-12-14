package teamproject.wipeout.game.market.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.geometry.Point2D;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.ScriptComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.input.Clickable;
import teamproject.wipeout.engine.component.input.EntityClickAction;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.shape.Rectangle;
import teamproject.wipeout.engine.component.shape.Shape;
import teamproject.wipeout.engine.component.input.*;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputHoverableAction;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.assetmanagement.spritesheet.SpriteSetDescriptor;
import teamproject.wipeout.game.assetmanagement.spritesheet.Spritesheet;
import teamproject.wipeout.game.assetmanagement.spritesheet.SpritesheetDescriptor;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketPriceUpdater;
import teamproject.wipeout.game.market.ui.MarketUI;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.game.task.Task;

/**
 * Entity representing the Market in the game world.
 */
public class MarketEntity extends GameEntity {

    public static final double PLAYER_INTERACTION_DISTANCE = 250;

    public final Point2D[] corners;

    protected MarketUI marketUI;
    protected Market market;

    protected StackPane uiContainer;

    protected Runnable onUIOpen;

    // Hover and click variables
    protected Supplier<Point2D> playerWorldPosition;
    protected Point2D clickableTopLeft;
    protected Point2D clickableBottomRight;
    protected Point2D clickableCentre;
    protected RectRenderable hoverRect;
    protected boolean mouseIn = false;

    private MarketPriceUpdater marketPriceUpdater;

    /**
     * Creates a new instance of MarketEntity
     * @param marketPack An information pack for MarketEntity.
     */
    public MarketEntity(Map<String, Object> marketPack) {
        super((GameScene) marketPack.get("gameScene"));

        ItemStore itemStore = (ItemStore) marketPack.get("itemStore");
        SpriteManager spriteManager = (SpriteManager) marketPack.get("spriteManager");
        CurrentPlayer currentPlayer = (CurrentPlayer) marketPack.get("currentPlayer");

        this.uiContainer = (StackPane) marketPack.get("uiContainer");
        this.playerWorldPosition = () -> currentPlayer.getWorldPosition();

        double x = WorldEntity.MARKET_START.getX();
        double y = WorldEntity.MARKET_START.getY();
        this.addComponent(new Transform(x, y, 1));

        // Create child component for hoverable so it displays behind everything
        GameEntity hoverEntity = this.scene.createEntity();
        hoverEntity.setParent(this);
        Transform childTransform = new Transform(0, 0);
        hoverEntity.addComponent(childTransform);
        hoverEntity.addComponent(new Hoverable(this.onHover));
        hoverEntity.addComponent(new Clickable(this.onClick));
        hoverEntity.addComponent(new ScriptComponent(this.onStep));

        int width = 306;
        int height = 203;
        this.addComponent(new RenderComponent(new RectRenderable(Color.TRANSPARENT, width, height)));
        this.corners = new Point2D[]{
                new Point2D(x, y),
                new Point2D(x + width, y),
                new Point2D(x, y + height),
                new Point2D(x + width, y + height),
        };

        try {
            spriteManager.loadSpriteSheet("gameworld/market-descriptor.json", "gameworld/market.png");

            RenderComponent marketRenderComponent = this.getComponent(RenderComponent.class);
            this.hoverRect = new RectRenderable(Color.DARKGRAY, marketRenderComponent.getWidth(), marketRenderComponent.getHeight());
            this.hoverRect.alpha = 0;
            this.hoverRect.radius = 50;

            // Set up interaction areas
            clickableTopLeft = childTransform.getWorldPosition();
            clickableBottomRight = clickableTopLeft.add(marketRenderComponent.getWidth(), marketRenderComponent.getHeight());
            clickableCentre = clickableTopLeft.add(clickableBottomRight).multiply(0.5);

            hoverEntity.addComponent(new RenderComponent(hoverRect));

            // Add individual parts of the market using descriptor
            SpritesheetDescriptor marketDescriptor = Spritesheet.getSpritesheetFromJSON("gameworld/market-descriptor.json");

            for (SpriteSetDescriptor spriteSet : marketDescriptor.sprites) {
                GameEntity childRenderer = new GameEntity(scene);
                childRenderer.setParent(this);
                childRenderer.addComponent(new Transform(spriteSet.parameters.get("x"), spriteSet.parameters.get("y"), 1));
                childRenderer.addComponent(new RenderComponent(new SpriteRenderable(spriteManager.getSpriteSet("market", spriteSet.name)[0])));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        // Physics
        Shape[] hitboxes = {
                // Main body right
                new Rectangle(96, 44, 159, 95),
                // Main body left
                new Rectangle(49, 44, 47, 108),
                // Sign and wood pile left
                new Rectangle(22, 152, 65, 32),
                // Planters and half of spears
                new Rectangle(16, 78, 37, 74),
                // Rest of spears
                new Rectangle(5, 127, 18, 29),
                // Red plant
                new Rectangle(88, 130, 19, 20),
                // Bows arrows and green plant
                new Rectangle(172, 138, 75, 29),
                // Wood pile bottom
                new Rectangle(225, 166, 22, 17),
                // Target and scarecrow
                new Rectangle(246, 118, 37, 34)
        };
        this.addComponent(new HitboxComponent(hitboxes));
        this.addComponent(new CollisionResolutionComponent(false, null));

        // Create logic market
        boolean isNetworked = (boolean) marketPack.get("networked");
        this.market = new Market(itemStore, isNetworked);
        if (!isNetworked) {
            this.marketPriceUpdater = new MarketPriceUpdater(this.market, true);
        }

        ArrayList<Task> purchasableTasks = (ArrayList<Task>) marketPack.get("tasks");
        Collection<Item> items = itemStore.getData().values();
        this.marketUI = new MarketUI(items, this.market, currentPlayer, spriteManager, purchasableTasks);
        this.marketUI.setParent(uiContainer);
    }

    /**
     * Getter for the Market instance controlled by this MarketEntity
     * @return The Market instance
     */
    public Market getMarket() {
        return this.market;
    }

    /**
     * Disables the MarketPriceUpdater, if it is active.
     */
    public void disableUpdater() {
        if (marketPriceUpdater != null) {
            this.marketPriceUpdater.stop();
        }
    }

    /**
     * Setter for the runnable to call when the MarketUI is opened
     * @param onOpen The runnable to call when the UI is opened.
     */
    public void setOnUIOpen(Runnable onOpen) {
        this.onUIOpen = onOpen;
    }

    /**
     * Setter for the runnable to call  when the MarketUI is closed.
     * @param onClose The runnable to call when the UI is closed.
     */
    public void setOnUIClose(Runnable onClose) {
        this.marketUI.onUIClose = onClose;
    }

    /**
     * Runnable called when this MarketEntity is clicked.
     */
    private final EntityClickAction onClick = (x, y, button) -> {
        if (this.getPlayerDistance() < PLAYER_INTERACTION_DISTANCE && marketUI.getParent() == null) {
            if (this.onUIOpen != null) {
                this.onUIOpen.run();
            }
            this.uiContainer.getChildren().add(this.marketUI);
            this.marketUI.onDisplay();
        }
    };

    /**
     * Runnable called when this MarketEntity is hovered.
     */
    private final InputHoverableAction onHover = (x, y) -> {
        if (clickableTopLeft.getX() < x && clickableTopLeft.getY() < y && clickableBottomRight.getX() > x && clickableBottomRight.getY() > y) {
            this.mouseIn = true;
        }
        else {
            this.mouseIn = false;
        }
    };

    /**
     * Runnable callede on every frame of the game.
     */
    private final Consumer<Double> onStep = (step) -> {
        if (this.mouseIn && this.getPlayerDistance() < PLAYER_INTERACTION_DISTANCE) {
            this.hoverRect.alpha = 0.2;
        }
        else {
            this.hoverRect.alpha = 0;
        }
    };

    /**
     * Gets the distance of the player from the market
     * @return The distance between the player and the market
     */
    private double getPlayerDistance() {
        return clickableCentre.distance(playerWorldPosition.get());
    }
}
