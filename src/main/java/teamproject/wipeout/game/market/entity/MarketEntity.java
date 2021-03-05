package teamproject.wipeout.game.market.entity;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.ui.UIComponent;
import teamproject.wipeout.engine.component.input.Clickable;
import teamproject.wipeout.engine.component.input.EntityClickAction;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.ui.MarketUI;
import teamproject.wipeout.game.player.Player;

public class MarketEntity extends GameEntity {

    protected MarketUI marketUI;
    protected Market market;

    protected StackPane uiContainer;

    public MarketEntity(GameScene scene, double x, double y, ItemStore items, Player player, SpriteManager spriteManager, StackPane uiContainer) {
        super(scene);
        scene.addEntity(this);
        this.uiContainer = uiContainer;

        this.addComponent(new Transform(x, y));
        try {
            spriteManager.loadSpriteSheet("market-descriptor.json", "market.png");
            this.addComponent(new RenderComponent(new SpriteRenderable(spriteManager.getSpriteSet("market", "market")[0])));
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        this.addComponent(new Clickable(this.onClick));

        market = new Market(items);

        this.marketUI = new MarketUI(items.getData().values(), market, player, spriteManager);
        this.marketUI.setParent(uiContainer);
    }

    private EntityClickAction onClick = (x, y, button, entity) -> {
        if (this.marketUI.getParent() == null) {
            this.uiContainer.getChildren().add(this.marketUI);
        }
    };
}
