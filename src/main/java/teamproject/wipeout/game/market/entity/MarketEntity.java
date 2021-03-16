package teamproject.wipeout.game.market.entity;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
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
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.ui.MarketUI;
import teamproject.wipeout.game.player.Player;

public class MarketEntity extends GameEntity {

    public static final double PLAYER_INTERACTION_DISTANCE = 250;
    public static final double Y_OFFSET = -44;

    protected MarketUI marketUI;
    protected Market market;

    protected StackPane uiContainer;

    protected Runnable onUIOpen;

    // Hover and click variables
    protected Transform playerTransform;
    protected Point2D clickableTopLeft;
    protected Point2D clickableBottomRight;
    protected Point2D clickableCentre;
    protected RectRenderable hoverRect;
    protected boolean mouseIn = false;

    public MarketEntity(GameScene scene, double x, double y, ItemStore items, Player player, SpriteManager spriteManager, StackPane uiContainer) {
        super(scene);

        this.uiContainer = uiContainer;
        this.playerTransform = player.getComponent(Transform.class);

        this.addComponent(new Transform(x, y, 1));

        // Create child component for hoverable so it displays behind everything
        GameEntity child = new GameEntity(scene);
        child.setParent(this);
        Transform childTransform = new Transform(0, Y_OFFSET);
        child.addComponent(childTransform);
        child.addComponent(new Hoverable(this.onHover));
        child.addComponent(new Clickable(this.onClick));
        child.addComponent(new ScriptComponent(this.onStep));

        try {
            spriteManager.loadSpriteSheet("gameworld/market-descriptor.json", "gameworld/market.png");
            Image marketSprite = spriteManager.getSpriteSet("market", "market")[0];

            this.addComponent(new RenderComponent(new Point2D(0, Y_OFFSET), new SpriteRenderable(marketSprite), new RectRenderable(Color.BLACK, 1, 1)));

            RenderComponent marketRenderComponent = this.getComponent(RenderComponent.class);
            this.hoverRect = new RectRenderable(Color.DARKGRAY, marketRenderComponent.getWidth(), marketRenderComponent.getHeight());
            this.hoverRect.alpha = 0;
            this.hoverRect.radius = 50;

            child.addComponent(new RenderComponent(hoverRect));

            // Set up interaction areas
            clickableTopLeft = childTransform.getWorldPosition();
            clickableBottomRight = clickableTopLeft.add(marketRenderComponent.getWidth(), marketRenderComponent.getHeight());
            clickableCentre = clickableTopLeft.add(clickableBottomRight).multiply(0.5);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        // Physics
        Shape[] hitboxes = {
                // Main body right
        		new Rectangle(96, 44 + Y_OFFSET, 159, 95),
                // Main body left
                new Rectangle(49, 46 + Y_OFFSET, 47, 106),
                // Sign and wood pile left
                new Rectangle(22, 152 + Y_OFFSET, 65, 32),
                // Planters and half of spears
                new Rectangle(16, 78 + Y_OFFSET, 37, 74),
                // Rest of spears
                new Rectangle(5, 127 + Y_OFFSET, 18, 29),
                // Bows arrows and green plant
                new Rectangle(172, 138 + Y_OFFSET, 75, 29),
                // Wood pile bottom
                new Rectangle(225, 166 + Y_OFFSET, 22, 17),
                // Target and scarecrow
                new Rectangle(246, 68 + Y_OFFSET, 46, 84)
        };
        this.addComponent(new HitboxComponent(hitboxes));
        this.addComponent(new CollisionResolutionComponent(false));

        // Create logic market
        market = new Market(items, false);

        this.marketUI = new MarketUI(items.getData().values(), market, player, spriteManager);
        this.marketUI.setParent(uiContainer);
    }

    public Market getMarket(){
        return this.market;
    }

    public void setOnUIOpen(Runnable onOpen) {
        this.onUIOpen = onOpen;
    }

    public void setOnUIClose(Runnable onClose) {
        this.marketUI.onUIClose = onClose;
    }

    private EntityClickAction onClick = (x, y, button, entity) -> {
        if (this.getPlayerDistance() < PLAYER_INTERACTION_DISTANCE && marketUI.getParent() == null) {
            if (this.onUIOpen != null) {
                this.onUIOpen.run();
            }
            this.uiContainer.getChildren().add(this.marketUI);
        }
    };

    private InputHoverableAction onHover = (x, y) -> {
        if (clickableTopLeft.getX() < x && clickableTopLeft.getY() < y && clickableBottomRight.getX() > x && clickableBottomRight.getY() > y) {
            this.mouseIn = true;
        }
        else {
            this.mouseIn = false;
        }
    };

    private Runnable onStep = () -> {
        if (this.mouseIn && this.getPlayerDistance() < PLAYER_INTERACTION_DISTANCE) {
            this.hoverRect.alpha = 0.2;
        }
        else {
            this.hoverRect.alpha = 0;
        }
    };

    private double getPlayerDistance() {
        return clickableCentre.distance(playerTransform.getPosition());
    }
}
