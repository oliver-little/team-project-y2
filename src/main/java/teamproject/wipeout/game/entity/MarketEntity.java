package teamproject.wipeout.game.entity;

import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.ui.UIComponent;
import teamproject.wipeout.engine.component.input.Clickable;
import teamproject.wipeout.engine.component.input.EntityClickAction;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.market.ui.MarketUI;
import teamproject.wipeout.util.resources.ResourceType;

public class MarketEntity extends GameEntity {

    protected MarketUI marketUI;

    public MarketEntity(GameScene scene, double x, double y) {
        super(scene);

        this.addComponent(new Transform(x, y));
        this.addComponent(new RenderComponent(new RectRenderable(Color.BLUE, 50, 50)));
        this.addComponent(new Clickable(this.onClick));

        this.marketUI = new MarketUI();
    }

    private EntityClickAction onClick = (x, y, button, entity) -> {
        if (this.marketUI.getParent() == null) {
            this.marketUI.getStylesheets().clear();

            this.marketUI.getStylesheets().add(ResourceType.STYLESHEET.path + "market-menu.css");
            
            entity.addComponent(new UIComponent(this.marketUI));
        }
    };
}
