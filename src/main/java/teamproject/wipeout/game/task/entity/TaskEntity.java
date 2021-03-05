package teamproject.wipeout.game.task.entity;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.input.Clickable;
import teamproject.wipeout.engine.component.input.EntityClickAction;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.component.ui.UIComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.market.MarketUI;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.game.task.ui.TaskUI;

import java.util.ArrayList;

public class TaskEntity extends GameEntity {

    protected TaskUI taskUI;
    public Transform transform;
    public Point2D size;
    public GameScene gameScene;

    private GameEntity textEntity;

    private Integer MAX_TASKS = 5;

    private Point2D topLeft;

    private TextRenderable textRenderable;
    private TextRenderable[] textRenderables = new TextRenderable[MAX_TASKS];

    public TaskEntity(GameScene scene, double x, double y, Player player) {
//    public TaskEntity(GameScene scene, double x, double y) {
        super(scene);

        this.gameScene = scene;
        this.addComponent(new Transform(x, y));
        this.addComponent(new RenderComponent(new RectRenderable(Color.BLACK, 100, 100)));

        this.topLeft = new Point2D(x, y);


        this.addComponent(new Clickable(this.onClick));

//        this.taskUI = new TaskUI("Test");
        createTextRenderables();
        showTasks(player.tasks);
    }

    public void showTasks(ArrayList<Task> tasks) {
        int i = 0;
        for(Task task: tasks) {
            if (task.completed) {
                continue;
            }
            textRenderables[i].setText(task.description);
            i += 1;
        }
        while(i < MAX_TASKS) {
            textRenderables[i].setText("");
            i += 1;
        }
    }

    private void createTextRenderables() {
        double posTask = topLeft.getY();
        for(int i = 0; i < MAX_TASKS; i++) {
            textEntity = gameScene.createEntity();
            textEntity.addComponent(new Transform (topLeft.getX() + 10, posTask + 10));
            textRenderable = new TextRenderable("");
            textEntity.addComponent(new RenderComponent(textRenderable));
            textRenderables[i] = textRenderable;
            posTask += 10;
        }
    }

    private EntityClickAction onClick = (x, y, button, entity) -> {
        if (this.taskUI.getParent() == null) {
            entity.addComponent(new UIComponent(this.taskUI));
        }
    };

}
