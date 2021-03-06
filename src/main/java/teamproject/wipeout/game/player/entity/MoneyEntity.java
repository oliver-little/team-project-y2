package teamproject.wipeout.game.player.entity;


import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.task.Task;

import java.util.ArrayList;

public class MoneyEntity extends GameEntity {
    private Point2D topLeft;

    private GameEntity textEntity;

    private TextRenderable textRenderable;

    public MoneyEntity(GameScene scene, double x, double y, Player player) {
        super(scene);

        this.addComponent(new Transform(x, y));
        this.addComponent(new RenderComponent(new RectRenderable(Color.BLACK, 80, 15)));

        this.topLeft = new Point2D(x, y);

        textEntity = scene.createEntity();
        textEntity.addComponent(new Transform (x + 10, y + 10));
        textRenderable = new TextRenderable("Money: " + player.money.toString());
        textEntity.addComponent(new RenderComponent(textRenderable));

        showMoney(player.money);
    }

    public void showMoney(Double money) {
        textRenderable.setText("Money: " + money.toString());
    }
}
