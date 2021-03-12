package teamproject.wipeout.game.entity;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
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
import teamproject.wipeout.util.Networker;

import java.util.HashMap;
import java.util.Map;

public class WorldEntity extends GameEntity {

	public Networker networker;
	public final MarketPriceUpdater marketUpdater;
	public final MarketEntity market;
	public final Map<Integer, FarmEntity> farms;

	private Player myPlayer;
	private FarmEntity myFarm;

	private InputHandler inputHandler;

	public WorldEntity(GameScene gameScene, double width, double height, int numberOfPlayers, Player player, ItemStore itemStore, SpriteManager spriteManager, StackPane uiContainer, InputHandler input) {
		super(gameScene);

		this.farms = new HashMap<Integer, FarmEntity>();
		this.inputHandler = input;

		this.addComponent(new Transform(0,0));
		//grass background
		GameEntity grass = new GameEntity(gameScene);
		grass.setParent(this);
		grass.addComponent(new Transform(-width * 4, -height * 4));
		grass.addComponent(new RenderComponent(new RectRenderable(Color.rgb(47, 129, 54), width * 8, height * 8)));
		

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

		this.myPlayer = player;

		if (numberOfPlayers == 4) {
			FarmEntity farmEntity1 = new FarmEntity(gameScene, 1, new Point2D(50, 50), spriteManager, itemStore);
			FarmEntity farmEntity2 = new FarmEntity(gameScene, 2, new Point2D(500, 50), spriteManager, itemStore);
			FarmEntity farmEntity3 = new FarmEntity(gameScene, 3, new Point2D(50, 400), spriteManager, itemStore);
			FarmEntity farmEntity4 = new FarmEntity(gameScene, 4, new Point2D(500, 400), spriteManager, itemStore);

			this.farms.put(farmEntity1.farmID, farmEntity1);
			this.farms.put(farmEntity2.farmID, farmEntity2);
			this.farms.put(farmEntity3.farmID, farmEntity3);
			this.farms.put(farmEntity4.farmID, farmEntity4);

		} else if (numberOfPlayers == 2) {
			FarmEntity farmEntity1 = new FarmEntity(gameScene, 1, new Point2D(50, 50), spriteManager, itemStore);
			FarmEntity farmEntity2 = new FarmEntity(gameScene, 2, new Point2D(500, 400), spriteManager, itemStore);

			this.farms.put(farmEntity1.farmID, farmEntity1);
			this.farms.put(farmEntity2.farmID, farmEntity2);
		}

		this.market = new MarketEntity(gameScene, 260, 200, itemStore, player, spriteManager, uiContainer);
		this.market.setOnUIOpen(() -> input.setDisableInput(true));
		this.market.setOnUIClose(() -> input.setDisableInput(false));
		this.marketUpdater = new MarketPriceUpdater(this.market.getMarket(), true);

		this.setMyFarm(this.farms.get(1));
		this.setupFarmPickingKey();
	}

	public Player getMyPlayer() {
		return this.myPlayer;
	}

	public FarmEntity getMyFarm() {
		return this.myFarm;
	}

	public void setMyPlayer(Player myPlayer) {
		this.myPlayer = myPlayer;
	}

	public void setupFarmPickingKey() {
		this.inputHandler.onKeyRelease(KeyCode.H, () -> {
			this.myFarm.onKeyPickAction(this.inputHandler.mouseHoverSystem).performKeyAction();
		});
	}

	public void setMyFarm(FarmEntity farm) {
		this.myFarm = farm;
		if (farm != null) {
			this.myPlayer.getCurrentState().assignFarm(farm.farmID);
			farm.assignPlayer(this.myPlayer.playerID, true, () -> this.networker.getClient());
		} else {
			this.myPlayer.getCurrentState().assignFarm(null);
		}
	}

}

