package teamproject.wipeout.game.market.entity;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.ScriptComponent;
import teamproject.wipeout.engine.component.Transform;
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

    // Hover and click variables
    protected Transform playerTransform;
    protected Transform marketTransform;
    protected Point2D marketEnd;
    protected Point2D marketCentre;
    protected RectRenderable hoverRect;
    protected boolean mouseIn = false;

    public MarketEntity(GameScene scene, double x, double y, ItemStore items, Player player, SpriteManager spriteManager, StackPane uiContainer) {
        super(scene);
        scene.addEntity(this);

        this.uiContainer = uiContainer;
        this.playerTransform = player.getComponent(Transform.class);

        this.marketTransform = new Transform(x, y);
        this.addComponent(this.marketTransform);
        try {
            spriteManager.loadSpriteSheet("gameworld/market-descriptor.json", "gameworld/market.png");
            Image marketSprite = spriteManager.getSpriteSet("market", "market")[0];

            this.hoverRect = new RectRenderable(Color.DARKGRAY, marketSprite.getWidth(), marketSprite.getHeight());
            this.hoverRect.alpha = 0;
            this.hoverRect.radius = 50;

            this.addComponent(new RenderComponent(this.hoverRect, new SpriteRenderable(marketSprite)));

            marketEnd = new Point2D(x + marketSprite.getWidth(), y + marketSprite.getHeight());
            marketCentre = this.playerTransform.getPosition().add(marketEnd).multiply(0.5);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        this.addComponent(new Clickable(this.onClick));
        this.addComponent(new Hoverable(this.onHover));

        this.addComponent(new ScriptComponent(this.onStep));

        market = new Market(items);

        this.marketUI = new MarketUI(items.getData().values(), market, player, spriteManager);
        this.marketUI.setParent(uiContainer);
    }

    public Market getMarket(){
        return this.market;
    }

    private EntityClickAction onClick = (x, y, button, entity) -> {
        if (this.getPlayerDistance() < PLAYER_INTERACTION_DISTANCE && marketUI.getParent() == null) {
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
