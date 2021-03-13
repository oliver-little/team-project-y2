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
import teamproject.wipeout.engine.component.physics.Rectangle;
import teamproject.wipeout.engine.component.physics.Shape;
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

    protected MarketUI marketUI;
    protected Market market;

    protected StackPane uiContainer;

    protected Runnable onUIOpen;

    // Hover and click variables
    protected Transform playerTransform;
    protected Transform marketTransform;
    protected Point2D marketEnd;
    protected Point2D marketCentre;
    protected RectRenderable hoverRect;
    protected boolean mouseIn = false;

    public MarketEntity(GameScene scene, double x, double y, ItemStore items, Player player, SpriteManager spriteManager, StackPane uiContainer) {
        super(scene);

        this.uiContainer = uiContainer;
        this.playerTransform = player.getComponent(Transform.class);

        this.marketTransform = new Transform(x, y, 1);
        this.addComponent(this.marketTransform);
        
        double yOffset = 44;

        try {
            spriteManager.loadSpriteSheet("gameworld/market-descriptor.json", "gameworld/market.png");
            Image marketSprite = spriteManager.getSpriteSet("market", "market")[0];

            

            this.addComponent(new RenderComponent(new Point2D(0, -yOffset), new SpriteRenderable(marketSprite)));

            marketEnd = new Point2D(x + marketSprite.getWidth(), y + marketSprite.getHeight());
            marketCentre = this.playerTransform.getPosition().add(marketEnd).multiply(0.5);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        this.addComponent(new Clickable(this.onClick));

        Shape[] hitboxes = {
                // Main body right
        		new Rectangle(96, 44 - yOffset, 159, 95),
                // Main body left
                new Rectangle(49, 46 - yOffset, 47, 106),
                // Sign and wood pile left
                new Rectangle(22, 152 - yOffset, 65, 32),
                // Planters and half of spears
                new Rectangle(16, 78 - yOffset, 37, 74),
                // Rest of spears
                new Rectangle(5, 127 - yOffset, 18, 29),
                // Bows arrows and green plant
                new Rectangle(172, 138 - yOffset, 75, 29),
                // Wood pile bottom
                new Rectangle(225, 166 - yOffset, 22, 17),
                // Target and scarecrow
                new Rectangle(246, 68 - yOffset, 46, 84)
        };
        this.addComponent(new HitboxComponent(hitboxes));
        this.addComponent(new CollisionResolutionComponent(false));

        // Create child component for hoverable so it displays behind everything
        GameEntity child = new GameEntity(scene);
        child.setParent(this);
        child.addComponent(new Transform(0, 0));
        child.addComponent(new Hoverable(this.onHover));
        child.addComponent(new ScriptComponent(this.onStep));

        RenderComponent marketRenderComponent = this.getComponent(RenderComponent.class);
        this.hoverRect = new RectRenderable(Color.DARKGRAY, marketRenderComponent.getWidth(), marketRenderComponent.getHeight());
        this.hoverRect.alpha = 0;
        this.hoverRect.radius = 50;

        child.addComponent(new RenderComponent(new Point2D(0, -yOffset), hoverRect));

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
        Point2D position = this.marketTransform.getPosition();
        if (position.getX() < x && position.getY() < y && this.marketEnd.getX() > x && this.marketEnd.getY() > y) {
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
        return marketCentre.distance(playerTransform.getPosition());
    }
}
