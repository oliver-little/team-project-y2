package teamproject.wipeout.game.entity;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.component.shape.Rectangle;
import teamproject.wipeout.engine.component.shape.Shape;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.gameclock.ClockSystem;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.Pickables;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.SabotageComponent;
import teamproject.wipeout.game.market.MarketPriceUpdater;
import teamproject.wipeout.game.market.entity.MarketEntity;
import teamproject.wipeout.game.player.AIPlayerComponent;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.potion.PotionEntity;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.state.StateUpdatable;
import teamproject.wipeout.networking.state.WorldState;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WorldEntity extends GameEntity implements StateUpdatable<WorldState> {

	public final MarketPriceUpdater marketUpdater;
	public final MarketEntity market;
	public final Map<Integer, FarmEntity> farms;
	public final Player myPlayer;
	public final AnimalEntity myAnimal;
	public final Pickables pickables;

	private HashMap<Integer, Point2D[]> potions;

	private FarmEntity myFarm;
	private NavigationMesh navMesh;

	private InputHandler inputHandler;
	private Supplier<GameClient> clientSupplier;
	private ArrayList<Player> players;

	public SpriteManager spriteManager;
	public ItemStore itemStore;

	public WorldEntity(GameScene gameScene, int numberOfPlayers, Player player, ItemStore itemStore, SpriteManager spriteManager, StackPane uiContainer, InputHandler input, ArrayList<Task> purchasableTasks) {
		super(gameScene);
		this.spriteManager = spriteManager;
		this.itemStore = itemStore;

		this.farms = new HashMap<Integer, FarmEntity>();
		this.pickables = new Pickables(gameScene, itemStore, spriteManager);
		this.pickables.setOnUpdate(() -> this.sendStateUpdate());

		this.potions = new HashMap<Integer, Point2D[]>();

		this.inputHandler = input;

		this.addComponent(new Transform(0,0));

		int width = 1461;
		int height = 1184;

		//grass background
		GameEntity grass = new GameEntity(gameScene);
		grass.setParent(this);
		grass.addComponent(new Transform(-width * 4, -height * 4, -100));
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
		
		try {
			ForestEntity forestTop = new ForestEntity(gameScene, new Point2D(-50,-100), new Point2D(1461,0), spriteManager);
			ForestEntity forestLeft = new ForestEntity(gameScene, new Point2D(-100,-100), new Point2D(0,1411), spriteManager);
			ForestEntity forestBottom = new ForestEntity(gameScene, new Point2D(0,1184), new Point2D(1461,0), spriteManager);
			ForestEntity forestRight = new ForestEntity(gameScene, new Point2D(1461,-100), new Point2D(0,1411), spriteManager);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.myPlayer = player;
		this.players = new ArrayList<Player>();
		this.players.add(player);

		if (numberOfPlayers == 4) {
			FarmEntity farmEntity1 = new FarmEntity(gameScene, 1, new Point2D(220, 240), this.pickables, spriteManager, itemStore);
			FarmEntity farmEntity2 = new FarmEntity(gameScene, 2, new Point2D(981, 240), this.pickables, spriteManager, itemStore);
			FarmEntity farmEntity3 = new FarmEntity(gameScene, 3, new Point2D(220, 800), this.pickables, spriteManager, itemStore);
			FarmEntity farmEntity4 = new FarmEntity(gameScene, 4, new Point2D(981, 800), this.pickables, spriteManager, itemStore);

			this.farms.put(farmEntity1.farmID, farmEntity1);
			this.farms.put(farmEntity2.farmID, farmEntity2);
			this.farms.put(farmEntity3.farmID, farmEntity3);
			this.farms.put(farmEntity4.farmID, farmEntity4);

		} else if (numberOfPlayers == 2) {
			FarmEntity farmEntity1 = new FarmEntity(gameScene, 1, new Point2D(220, 240), this.pickables, spriteManager, itemStore);
			FarmEntity farmEntity2 = new FarmEntity(gameScene, 2, new Point2D(981, 800), this.pickables, spriteManager, itemStore);

			this.farms.put(farmEntity1.farmID, farmEntity1);
			this.farms.put(farmEntity2.farmID, farmEntity2);
		}

		this.market = new MarketEntity(gameScene, 576, 544, itemStore, player, spriteManager, uiContainer, this,  purchasableTasks);
		this.market.setOnUIOpen(() -> input.setDisableInput(true));
		this.market.setOnUIClose(() -> input.setDisableInput(false));
		this.marketUpdater = new MarketPriceUpdater(this.market.getMarket(), true);

		List<Rectangle> rectangles = new ArrayList<>();

		Point2D marketPos = this.market.getComponent(Transform.class).getWorldPosition();

		for (Shape shape : this.market.getComponent(HitboxComponent.class).getHitboxes()) {
			if (shape instanceof Rectangle) {
				Rectangle rect = (Rectangle) shape;
				Rectangle newRect = new Rectangle(marketPos.add(rect.getX(), rect.getY()), rect.getWidth(), rect.getHeight());
				rectangles.add(newRect);
			}
		}

        this.navMesh = NavigationMesh.generateMesh(Point2D.ZERO, new Point2D(width, height), rectangles);

		this.myAnimal = new AnimalEntity(gameScene, new Point2D(10, 10), this.navMesh, spriteManager, new ArrayList<>(farms.values()));
		TextRenderable tag= new TextRenderable("Remy", 20);
		GameEntity nameTag = new GameEntity(gameScene);
		nameTag.addComponent(new RenderComponent(tag));
		RenderComponent ratRender = this.myAnimal.getComponent(RenderComponent.class);
		nameTag.addComponent(new Transform(ratRender.getWidth()/2f -tag.getWidth()/1f, -tag.getHeight()*1f, 10));
		nameTag.setParent(this.myAnimal);

		this.setFarmFor(this.myPlayer, true, this.farms.get(1));

		this.createAIPlayers();
	}

	public void addPotion(PotionEntity potionEntity) {
		Point2D[] pointPoints = new Point2D[]{potionEntity.getStartPosition(), potionEntity.getEndPosition()};
		potionEntity.setPotionRemover(() -> this.potions.remove(potionEntity.getPotionID(), pointPoints));
		this.potions.put(potionEntity.getPotionID(), pointPoints);
		this.sendStateUpdate();
	}

	public FarmEntity getMyFarm() {
		return this.myFarm;
	}

	public NavigationMesh getNavMesh() {
		return this.navMesh;
	}

	public void setClientSupplier(Supplier<GameClient> supplier) {
		this.clientSupplier = supplier;
		this.myPlayer.setClientSupplier(supplier);
		this.market.getMarket().setClientSupplier(supplier);
		this.myAnimal.setClientSupplier(supplier);
	}

	public void setFarmFor(Player player, boolean activePlayer, FarmEntity farm) {
		if (activePlayer) {
			this.myFarm = farm;
		}
		if (farm != null) {
			player.getCurrentState().assignFarm(farm.farmID);
			this.setPositionForPlayer(player, farm);
			farm.assignPlayer(player.playerID, activePlayer, this.clientSupplier);
		} else {
			player.getCurrentState().assignFarm(null);
		}
	}

	public WorldState getCurrentState() {
		return new WorldState(this.pickables.get(), this.potions);
	}

	public void updateFromState(WorldState newState) {
		this.pickables.updateFrom(newState.getPickables());

		Set<Map.Entry<Integer, Point2D[]>> currentEntrySet = this.potions.entrySet();
		for (Map.Entry<Integer, Point2D[]> entry : newState.getPotions().entrySet()) {
			if (!currentEntrySet.contains(entry)) {
				Item potion = this.itemStore.getItem(entry.getKey());
				SabotageComponent sc = potion.getComponent(SabotageComponent.class);

				List<GameEntity> possibleEffectEntities = null;

				if (sc.type == SabotageComponent.SabotageType.SPEED) {
					possibleEffectEntities = List.of(this.myPlayer, this.myAnimal);
				}
				else if (sc.type == SabotageComponent.SabotageType.GROWTHRATE || sc.type == SabotageComponent.SabotageType.AI) {
					possibleEffectEntities = List.of(this.myFarm);
				}
				new PotionEntity(this.getScene(), this.spriteManager, potion, possibleEffectEntities, entry.getValue()[0], entry.getValue()[1]);
			}
		}
	}

	protected void setPositionForPlayer(Player player, FarmEntity farm) {
		Point2D playerPosition;
		switch (farm.farmID) {
			case 1:
				playerPosition = farm.getWorldPosition().add(farm.getSize());
				break;
			case 2:
				playerPosition = farm.getWorldPosition().add(-45, farm.getSize().getY());
				break;
			case 3:
				playerPosition = farm.getWorldPosition().add(farm.getSize().getX(), 0);
				break;
			case 4:
				playerPosition = farm.getWorldPosition().subtract(45, 0);
				break;
			default:
				playerPosition = Point2D.ZERO;
				break;
		}
		player.setWorldPosition(playerPosition);
	}

	public void setupFarmPickingKey(KeyCode code) {
		this.inputHandler.onKeyRelease(code, () -> {
			this.myFarm.onKeyPickAction(this.inputHandler.mouseHoverSystem).performKeyAction();
		});
	}

	public void setupFarmDestroyingKey(KeyCode code) {
		this.inputHandler.onKeyRelease(code, () -> {
			this.myFarm.onKeyPickActionDestroy(this.inputHandler.mouseHoverSystem).performKeyAction();
		});
	}

	private void createAIPlayers() {
		ArrayList<Player> aiPlayers = new ArrayList<Player>();
		for (int i = 2; i < 5; i++) {
			Player aiPlayer = new Player(this.scene, i + 1, "Farmer", new Point2D(10, 10), this.spriteManager, this.itemStore, null);
			aiPlayer.setThrownPotion((potion) ->  this.addPotion(potion));
			this.setFarmFor(aiPlayer, false, this.farms.get(i));
			aiPlayers.add(aiPlayer);
		}
		this.players.addAll(aiPlayers);

		for (Player aiPlayer : aiPlayers) {
			int aiFarmID = aiPlayer.getCurrentState().getFarmID();
			AIPlayerComponent aiComp = new AIPlayerComponent(aiPlayer, this.market.getMarket(), this.navMesh, this.farms.get(aiFarmID));
			aiComp.allPlayers = this.players.stream().filter((player) -> !player.playerID.equals(aiPlayer.playerID)).collect(Collectors.toList());
			aiComp.otherFarms = this.farms.values().stream().filter((farm) -> !farm.farmID.equals(aiFarmID)).collect(Collectors.toList());
			aiComp.theAnimal = this.myAnimal;
			aiPlayer.addComponent(aiComp);
		}
	}

	public void setClock(Supplier<ClockSystem> clock) {
		for (Player player : this.players) {
			if (player.hasComponent(AIPlayerComponent.class)) {
				player.getComponent(AIPlayerComponent.class).clock = clock;
			}
		}
	}

	/**
	 * If possible, sends an updated state of the world to the server.
	 */
	private void sendStateUpdate() {
		if (this.clientSupplier == null) {
			return;
		}

		GameClient client = this.clientSupplier.get();
		if (client != null) {
			try {
				client.send(new GameUpdate(GameUpdateType.WORLD_STATE, client.getID(), this.getCurrentState()));

			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

}

