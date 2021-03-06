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
import teamproject.wipeout.game.market.entity.MarketEntity;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.ItemStore;

public class WorldEntity extends GameEntity
{

	public WorldEntity(GameScene gameScene, int numberOfPlayers, ItemStore itemStore, Player player, SpriteManager spriteManager, StackPane uiContainer)
	{
		super(gameScene);
		gameScene.addEntity(this);
		this.addComponent(new Transform(0,0));
		//grass background
		this.addComponent(new RenderComponent(new RectRenderable(Color.rgb(47, 129, 54), 800, 600)));
		
		//boundaries
		GameEntity worldBoundaries = gameScene.createEntity();
		worldBoundaries.addComponent(new Transform(0,0));
		Rectangle[] hitboxes = {new Rectangle(-5,-5,5,610),
								new Rectangle(-5,-5,810,5),
								new Rectangle(-5,600,810,5),
								new Rectangle(800,-5,5,610)
								};
		GameEntity leftBoundary = gameScene.createEntity();
		leftBoundary.addComponent(new Transform(0,0));
		leftBoundary.addComponent(new HitboxComponent(new Rectangle(-5,-5,5,610)));
		leftBoundary.addComponent(new CollisionResolutionComponent(false));
		
		GameEntity topBoundary = gameScene.createEntity();
		topBoundary.addComponent(new Transform(0,0));
		topBoundary.addComponent(new HitboxComponent(new Rectangle(-5,-5,810,5)));
		topBoundary.addComponent(new CollisionResolutionComponent(false));

		GameEntity rightBoundary = gameScene.createEntity();
		rightBoundary.addComponent(new Transform(0,0));
		rightBoundary.addComponent(new HitboxComponent(new Rectangle(800,-5,5,610)));
		rightBoundary.addComponent(new CollisionResolutionComponent(false));
		
		GameEntity bottomBoundary = gameScene.createEntity();
		bottomBoundary.addComponent(new Transform(0,0));
		bottomBoundary.addComponent(new HitboxComponent(new Rectangle(-5,600,810,5)));
		bottomBoundary.addComponent(new CollisionResolutionComponent(false));
		
		
		//TreeEntity tree = new TreeEntity(gameScene, new Point2D(40,40));
		//TreeEntity tree2 = new TreeEntity(gameScene, new Point2D(20,20));
		
		FarmEntity farmEntity = new FarmEntity(gameScene, new Point2D(50, 50), 1, spriteManager, itemStore);
		if(numberOfPlayers==4) {
			//FarmEntity farmEntity = new FarmEntity(gameScene, new Point2D(50, 50), 1, spriteManager, itemStore);
			FarmEntity farmEntity2 = new FarmEntity(gameScene, new Point2D(500, 50), 2, spriteManager, itemStore);
			FarmEntity farmEntity3 = new FarmEntity(gameScene, new Point2D(50, 400), 3, spriteManager, itemStore);
			FarmEntity farmEntity4 = new FarmEntity(gameScene, new Point2D(500, 400), 4, spriteManager, itemStore);
		}
		
		MarketEntity market = new MarketEntity(gameScene, 260, 200, itemStore, player, spriteManager, uiContainer);
        //market.setOnUIOpen(() -> input.setDisableInput(true));
        //market.setOnUIClose(() -> input.setDisableInput(false));
		


	}

}

