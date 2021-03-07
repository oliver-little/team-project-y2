package teamproject.wipeout.game.entity;

import javafx.geometry.Point2D;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.Rectangle;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.game.market.MarketPriceUpdater;
import teamproject.wipeout.game.market.entity.MarketEntity;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.ItemStore;

public class WorldEntity extends GameEntity
{

	public WorldEntity(GameScene gameScene, double width, double height, int numberOfPlayers, ItemStore itemStore, Player player, SpriteManager spriteManager, StackPane uiContainer, InputHandler input)
	{
		super(gameScene);
		gameScene.addEntity(this);
		this.addComponent(new Transform(0,0));
		//grass background
		this.addComponent(new RenderComponent(new RectRenderable(Color.rgb(47, 129, 54), width, height)));
		
		//boundaries
		GameEntity worldBoundaries = gameScene.createEntity();
		worldBoundaries.addComponent(new Transform(0,0));
		Rectangle[] hitboxes = {new Rectangle(-5,-5,5,height+10),
								new Rectangle(-5,-5,width+10,5),
								new Rectangle(-5,height,width+10,5),
								new Rectangle(width,-5,5,height+10)
								};
		this.addComponent(new HitboxComponent(hitboxes));
		this.addComponent(new CollisionResolutionComponent(false));
		
		
		//TreeEntity tree = new TreeEntity(gameScene, new Point2D(40,40));
		//TreeEntity tree2 = new TreeEntity(gameScene, new Point2D(20,20));
		
		FarmEntity farmEntity = new FarmEntity(gameScene, new Point2D(50, 50), 1, spriteManager, itemStore);
		
		
		if(numberOfPlayers==4) {
			//FarmEntity farmEntity = new FarmEntity(gameScene, new Point2D(50, 50), 1, spriteManager, itemStore);
			FarmEntity farmEntity2 = new FarmEntity(gameScene, new Point2D(500, 50), 2, spriteManager, itemStore);
			FarmEntity farmEntity3 = new FarmEntity(gameScene, new Point2D(50, 400), 3, spriteManager, itemStore);
			FarmEntity farmEntity4 = new FarmEntity(gameScene, new Point2D(500, 400), 4, spriteManager, itemStore);
		}
		else if(numberOfPlayers==2) {
			FarmEntity farmEntity4 = new FarmEntity(gameScene, new Point2D(500, 400), 4, spriteManager, itemStore);
		}
		MarketEntity market = new MarketEntity(gameScene, 260, 200, itemStore, player, spriteManager, uiContainer);
        market.setOnUIOpen(() -> input.setDisableInput(true));
        market.setOnUIClose(() -> input.setDisableInput(false));

	}

}

