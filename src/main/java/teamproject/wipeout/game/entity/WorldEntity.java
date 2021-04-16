package teamproject.wipeout.game.entity;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.component.shape.Rectangle;
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
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketPriceUpdater;
import teamproject.wipeout.game.market.entity.MarketEntity;
import teamproject.wipeout.game.player.AIPlayer;
import teamproject.wipeout.game.player.AIPlayerHelper;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.potion.PotionEntity;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.state.StateUpdatable;
import teamproject.wipeout.networking.state.WorldState;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public class WorldEntity extends GameEntity implements StateUpdatable<WorldState> {

	public static final int[] WORLD_SIZE = new int[]{1461, 1184};
	public static final Point2D MARKET_START = new Point2D(576, 544);
	public static final String[] AI_NAMES = new String[]{"Siri", "Alexa", "Cortana"};

	public final ItemStore itemStore;
	public final SpriteManager spriteManager;

	public final CurrentPlayer myCurrentPlayer;
	public final AnimalEntity myAnimal;
	public final MarketPriceUpdater marketUpdater;
	public final Map<Integer, FarmEntity> farms;
	public final Pickables pickables;

	public final AIPlayerHelper aiPlayerHelper;

	private Supplier<GameClient> clientSupplier;
	private Supplier<ClockSystem> clockSupplier;

	private final ArrayList<Player> players;
	private final InputHandler inputHandler;
	private final NavigationMesh navMesh;

	private final MarketEntity marketEntity;
	private final HashMap<Integer, Point2D[]> potions;

	public WorldEntity(Map<String, Object> worldPack) {
		super((GameScene) worldPack.get("gameScene"));

		this.spriteManager = (SpriteManager) worldPack.get("spriteManager");
		this.itemStore = (ItemStore) worldPack.get("itemStore");

		this.farms = new HashMap<Integer, FarmEntity>();
		this.pickables = new Pickables(this.scene, this.itemStore, this.spriteManager);
		this.pickables.setOnUpdate(() -> this.sendStateUpdate());

		this.potions = new HashMap<Integer, Point2D[]>();

		this.inputHandler = (InputHandler) worldPack.get("inputHandler");

		this.addComponent(new Transform(0,0));

		int width = WORLD_SIZE[0];
		int height = WORLD_SIZE[1];

		// Grass Background
		GameEntity grass = this.scene.createEntity();
		grass.setParent(this);
		grass.addComponent(new Transform(-width * 4, -height * 4, -100));
		grass.addComponent(new RenderComponent(new RectRenderable(Color.rgb(47, 129, 54), width * 8, height * 8)));
		
		// World Boundary
		this.createEndOfTheWorld(width, height);

		// Current Player
		this.myCurrentPlayer = (CurrentPlayer) worldPack.get("currentPlayer");
		this.players = new ArrayList<Player>();
		this.players.add(this.myCurrentPlayer);

		// Market
		this.marketEntity = (MarketEntity) worldPack.get("marketEntity");
		this.marketUpdater = new MarketPriceUpdater(this.marketEntity.getMarket(), true);
		this.aiPlayerHelper = new AIPlayerHelper(this.marketEntity.getMarket());

		// Farms
		Integer numberOfPlayers = (Integer) worldPack.get("players");
		this.createFarmsFor(numberOfPlayers);
		this.setFarmFor(this.myCurrentPlayer, true, this.farms.get(1));

		// AI
        this.navMesh = NavigationMesh.generateMesh(
        		Point2D.ZERO,
				new Point2D(width - this.myCurrentPlayer.size.getX(), height - this.myCurrentPlayer.size.getY()),
				this.getNavMeshAvoidRectangles()
		);

		this.myAnimal = this.createAnimalAt(new Point2D(10.0, 10.0));

		this.clockSupplier = null;
		this.clientSupplier = null;

		Boolean singleplayer = (Boolean) worldPack.get("singleplayer");
		if (singleplayer) {
			this.createAIPlayers();
		}
	}

	public Market getMarket() {
		return this.marketEntity.getMarket();
	}

	public NavigationMesh getNavMesh() {
		return this.navMesh;
	}

	public void setClientSupplier(Supplier<GameClient> supplier) {
		this.clientSupplier = supplier;
		this.myCurrentPlayer.setClientSupplier(supplier);
		this.marketEntity.getMarket().setClientSupplier(supplier);
		this.myAnimal.setClientSupplier(supplier);
	}

	public void setFarmFor(Player player, boolean activePlayer, FarmEntity farm) {
		if (farm != null) {
			this.setPositionForPlayer(player, farm);
			player.assignFarm(farm);
			farm.assignPlayer(player.playerID, activePlayer, this.clientSupplier);
		} else {
			player.getCurrentState().assignFarm(null);
		}
	}

	public void setFarmFor(AIPlayer aiPlayer, FarmEntity farm) {
		if (farm != null) {
			Point2D[] corners = this.setPositionForPlayer(aiPlayer, farm);
			aiPlayer.setDesignatedMarketPoint(corners[0]);
			aiPlayer.assignFarm(farm, corners[1]);
			farm.assignPlayer(aiPlayer.playerID, false, this.clientSupplier);

			aiPlayer.start();
		} else {
			aiPlayer.getCurrentState().assignFarm(null);
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

				ArrayList<GameEntity> possibleEffectEntities = new ArrayList<GameEntity>();

				if (sc.type == SabotageComponent.SabotageType.SPEED) {
					possibleEffectEntities.addAll(this.players);
					possibleEffectEntities.add(this.myAnimal);
				}
				else if (sc.type == SabotageComponent.SabotageType.GROWTHRATE || sc.type == SabotageComponent.SabotageType.AI) {
					possibleEffectEntities.addAll(this.farms.values());
				}
				new PotionEntity(this.getScene(), this.spriteManager, potion, possibleEffectEntities, this.myCurrentPlayer, false, entry.getValue()[0], entry.getValue()[1]);
			}
		}
	}

	public void addPotion(PotionEntity potionEntity) {
		Point2D[] pointPoints = new Point2D[]{potionEntity.getStartPosition(), potionEntity.getEndPosition()};
		potionEntity.setPotionRemover(() -> this.potions.remove(potionEntity.getPotionID(), pointPoints));
		this.potions.put(potionEntity.getPotionID(), pointPoints);
		this.sendStateUpdate();
	}

	public void setupFarmPickingKey(KeyCode code) {
		this.inputHandler.onKeyRelease(code, () -> {
			this.myCurrentPlayer.getMyFarm().onKeyPickAction(this.inputHandler.mouseHoverSystem).performKeyAction();
		});
	}

	public void setupFarmDestroyingKey(KeyCode code) {
		this.inputHandler.onKeyRelease(code, () -> {
			this.myCurrentPlayer.getMyFarm().onKeyPickActionDestroy(this.inputHandler.mouseHoverSystem).performKeyAction();
		});
	}

	public ArrayList<Player> getPlayers() {
		return this.players;
	}

	public void addPlayer(Player newPlayer) {
		this.players.add(newPlayer);
	}

	public void removePlayer(Player newPlayer) {
		this.players.remove(newPlayer);
	}

	public Supplier<ClockSystem> getClockSupplier() {
		return this.clockSupplier;
	}

	public void setClockSupplier(Supplier<ClockSystem> clock) {
		this.clockSupplier = clock;
	}

	protected Point2D[] setPositionForPlayer(Player player, FarmEntity farm) {
		Point2D marketCorner = this.marketEntity.corners[farm.farmID - 1].subtract(player.size.getX() / 2, player.size.getY() / 2);

		Point2D playerPosition;
		Point2D farmCorner;
		switch (farm.farmID) {
			case 1:
				playerPosition = farm.getWorldPosition().add(farm.getSize());
				farmCorner = playerPosition.subtract(player.size);
				break;
			case 2:
				playerPosition = farm.getWorldPosition().add(-player.size.getX(), farm.getSize().getY());
				farmCorner = playerPosition.add(player.size.getX(), -player.size.getY());
				break;
			case 3:
				playerPosition = farm.getWorldPosition().add(farm.getSize().getX(), -player.size.getY());
				farmCorner = playerPosition.add(-player.size.getX(), player.size.getY());
				break;
			case 4:
				playerPosition = farm.getWorldPosition().subtract(player.size.getX(), player.size.getY());
				farmCorner = farm.getWorldPosition();
				break;
			default:
				playerPosition = Point2D.ZERO;
				farmCorner = Point2D.ZERO;
				break;
		}

		player.setWorldPosition(playerPosition);
		return new Point2D[]{marketCorner, farmCorner};
	}

	private void createEndOfTheWorld(int width, int height) {
		GameEntity worldBoundaries = this.scene.createEntity();
		worldBoundaries.addComponent(new Transform(0,0));
		Rectangle[] hitboxes = {
				new Rectangle(-5,-5,5,height+10),
				new Rectangle(-5,-5,width+10,5),
				new Rectangle(-5,height,width+10,5),
				new Rectangle(width,-5,5,height+10)
		};
		this.addComponent(new HitboxComponent(hitboxes));
		this.addComponent(new CollisionResolutionComponent(false, null));

		try {
			new ForestEntity(this.scene, new Point2D(-50,-100), new Point2D(1461,0), this.spriteManager);
			new ForestEntity(this.scene, new Point2D(-100,-100), new Point2D(0,1411), this.spriteManager);
			new ForestEntity(this.scene, new Point2D(0,1184), new Point2D(1461,0), this.spriteManager);
			new ForestEntity(this.scene, new Point2D(1461,-100), new Point2D(0,1411), this.spriteManager);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void createFarmsFor(int numberOfPlayers) {
		if (numberOfPlayers == 4) {
			FarmEntity farmEntity1 = new FarmEntity(this.scene, 1, new Point2D(220, 240), this.pickables, this.spriteManager, this.itemStore);
			FarmEntity farmEntity2 = new FarmEntity(this.scene, 2, new Point2D(981, 240), this.pickables, this.spriteManager, this.itemStore);
			FarmEntity farmEntity3 = new FarmEntity(this.scene, 3, new Point2D(220, 800), this.pickables, this.spriteManager, this.itemStore);
			FarmEntity farmEntity4 = new FarmEntity(this.scene, 4, new Point2D(981, 800), this.pickables, this.spriteManager, this.itemStore);

			this.farms.put(farmEntity1.farmID, farmEntity1);
			this.farms.put(farmEntity2.farmID, farmEntity2);
			this.farms.put(farmEntity3.farmID, farmEntity3);
			this.farms.put(farmEntity4.farmID, farmEntity4);

		} else if (numberOfPlayers == 2) {
			FarmEntity farmEntity1 = new FarmEntity(this.scene, 1, new Point2D(220, 240), this.pickables, this.spriteManager, this.itemStore);
			FarmEntity farmEntity2 = new FarmEntity(this.scene, 2, new Point2D(981, 800), this.pickables, this.spriteManager, this.itemStore);

			this.farms.put(farmEntity1.farmID, farmEntity1);
			this.farms.put(farmEntity2.farmID, farmEntity2);
		}
	}

	private AnimalEntity createAnimalAt(Point2D startPoint) {
		AnimalEntity animal = new AnimalEntity(this.scene, startPoint, this.navMesh, this.spriteManager, List.copyOf(this.farms.values()));

		TextRenderable tag = new TextRenderable("Remy", 20);
		GameEntity nameTag = this.scene.createEntity();
		nameTag.addComponent(new RenderComponent(tag));

		RenderComponent ratRender = animal.getComponent(RenderComponent.class);
		nameTag.addComponent(new Transform(ratRender.getWidth()/2f - tag.getWidth(), -tag.getHeight()*1f, 10));
		nameTag.setParent(this.myAnimal);

		return animal;
	}

	private void createAIPlayers() {
		for (int i = 2; i < 5; i++) {
			Pair<Integer, String> playerInfo = new Pair<Integer, String>(i, AI_NAMES[i - 2]);
			AIPlayer aiPlayer = new AIPlayer(this.scene, playerInfo, this);

			aiPlayer.setThrownPotion((potion) ->  this.addPotion(potion));
			this.setFarmFor(aiPlayer, this.farms.get(i));

			this.players.add(aiPlayer);
		}
	}

	private List<Rectangle> getNavMeshAvoidRectangles() {
		Point2D marketTopLeftCorner = this.marketEntity.corners[0];
		Point2D marketBottomRightCorner = this.marketEntity.corners[3];

		double marketWidth = marketBottomRightCorner.getX() - marketTopLeftCorner.getX();
		double marketHeight = marketBottomRightCorner.getY() - marketTopLeftCorner.getY();

		Rectangle marketRect = new Rectangle(marketTopLeftCorner, marketWidth, marketHeight);
		return List.of(marketRect);
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

