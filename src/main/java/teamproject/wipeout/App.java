package teamproject.wipeout;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.component.TagComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.physics.Rectangle;
import teamproject.wipeout.engine.component.render.AnimatedSpriteRenderable;
import teamproject.wipeout.engine.component.render.CameraComponent;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameLoop;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.core.SystemUpdater;
import teamproject.wipeout.engine.entity.FarmEntity;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputHandler;
import teamproject.wipeout.engine.system.AudioSystem;
import teamproject.wipeout.engine.system.CollisionSystem;
import teamproject.wipeout.engine.system.EventSystem;
import teamproject.wipeout.engine.system.GrowthSystem;
import teamproject.wipeout.engine.system.MouseClickSystem;
import teamproject.wipeout.engine.system.MovementSystem;
import teamproject.wipeout.engine.system.UISystem;
import teamproject.wipeout.engine.system.render.RenderSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.MarketEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.PlantableComponent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


/**
 * App is a class for containing the components for game play.
 * It implements the Controller interface.
 *
 */
public class App implements Controller {

    private StackPane root;
    private Canvas dynamicCanvas;
    private Canvas staticCanvas;
    private StackPane interfaceOverlay;
    private double windowWidth = 800;
    private double windowHeight = 600;

    // Temporarily placed variables
    ItemStore itemStore;
    Item item;
    FarmEntity farmEntity;

    // Store systems for cleanup
    RenderSystem renderer;
    SystemUpdater systemUpdater;
    List<EventSystem> eventSystems;

    /**
     * Creates the content to be rendered onto the canvas.
     */
    public void createContent() {
        GameScene gameScene = new GameScene();
        RenderSystem renderer = new RenderSystem(gameScene, dynamicCanvas, staticCanvas);
        InputHandler input = new InputHandler(root.getScene());
        SystemUpdater systemUpdater = new SystemUpdater();
        systemUpdater.addSystem(new MovementSystem(gameScene));
        systemUpdater.addSystem(new CollisionSystem(gameScene));
        systemUpdater.addSystem(new AudioSystem(gameScene));
        systemUpdater.addSystem(new GrowthSystem(gameScene));

        eventSystems = List.of(new UISystem(gameScene, interfaceOverlay), new MouseClickSystem(gameScene, input));

        GameLoop gl = new GameLoop(systemUpdater, renderer);

        GameEntity camera = gameScene.createEntity();
        camera.addComponent(new Transform(0, 0));
        camera.addComponent(new CameraComponent(1));
        camera.addComponent(new TagComponent("MainCamera"));

        // Animated Sprite
        SpriteManager spriteManager = new SpriteManager();

        GameEntity nge = gameScene.createEntity();
        nge.addComponent(new Transform(20, 20, 0.0,1));

        MovementComponent ngePhysics = new MovementComponent(0f, 0f, 0f, 0f);
        nge.addComponent(ngePhysics);
        nge.addComponent(new HitboxComponent(new Rectangle(5, 0, 24, 33)));
        nge.addComponent(new CollisionResolutionComponent());

        try {
            spriteManager.loadSpriteSheet("player/player-descriptor.json", "player/player-spritesheet.png");
            Image[] frames = spriteManager.getSpriteSet("player", "walk");
            nge.addComponent(new RenderComponent(new AnimatedSpriteRenderable(frames, 10)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        AudioComponent ngeSound = new AudioComponent("glassSmashing2.wav");
        nge.addComponent(ngeSound);

        input.onKeyRelease(KeyCode.D, ngeSound::play); //example - pressing the D key will trigger the sound

        GameAudio ga = new GameAudio("backingTrack2.wav");
        input.onKeyRelease(KeyCode.P, ga::stopStart); //example - pressing the P key will switch between stop and start

        
        input.addKeyAction(KeyCode.LEFT,
                () -> ngePhysics.acceleration = ngePhysics.acceleration.subtract(500f, 0f),
                () -> ngePhysics.acceleration = ngePhysics.acceleration.add(500f, 0f));

        input.addKeyAction(KeyCode.RIGHT,
                () -> ngePhysics.acceleration = ngePhysics.acceleration.add(500f, 0f),
                () -> ngePhysics.acceleration = ngePhysics.acceleration.subtract(500f, 0f));

        input.addKeyAction(KeyCode.UP,
                () -> ngePhysics.acceleration = ngePhysics.acceleration.subtract(0f, 500f),
                () -> ngePhysics.acceleration = ngePhysics.acceleration.add(0f, 500f));

        input.addKeyAction(KeyCode.DOWN,
                () -> ngePhysics.acceleration = ngePhysics.acceleration.add(0f, 500f),
                () -> ngePhysics.acceleration = ngePhysics.acceleration.subtract(0f, 500f));

        try {
            itemStore = new ItemStore("items.json");
            spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
            spriteManager.loadSpriteSheet("crops/fruit-tree-descriptor.json", "crops/FruitTrees.png");
            spriteManager.loadSpriteSheet("inventory/inventory-fruit-and-vegetable-descriptor.json", "inventory/FruitsAndVeg.png");
        } catch (IOException | ReflectiveOperationException exception) {
            exception.printStackTrace();
        }

        farmEntity = new FarmEntity(gameScene, new Point2D(100, 100), "123", spriteManager, itemStore);

        item = itemStore.getItem(14);

        input.onMouseClick(MouseButton.SECONDARY, (x, y) -> {
            item = itemStore.getItem(item.id + 1);
            System.out.println(item.id);
        });

        input.onMouseClick(MouseButton.PRIMARY, (x, y) -> {
            if (farmEntity.isWithinFarm(x, y) != null) {
                try {
                    if (input.mouseHovering == null) {
                        farmEntity.pickItemAt(x, y);
                    } else {
                        farmEntity.putItem(item, x, y);
                    }
                } catch (FileNotFoundException exception) {
                    exception.printStackTrace();
                }
            }
        });

        GameEntity shadow = gameScene.createEntity();
        try {
            PlantableComponent crop = item.getComponent(PlantableComponent.class);
            Image sprite = spriteManager.getSpriteSet(crop.seedSpriteSheetName, crop.seedSpriteSetName)[0];
            shadow.addComponent(new RenderComponent(new SpriteRenderable(sprite)));

            input.onKeyRelease(KeyCode.A, () -> {
                if (input.mouseHovering == null) {
                    input.onMouseHover((x, y) -> {
                        Point2D point = farmEntity.isWithinFarm(x, y);
                        Transform transform = shadow.getComponent(Transform.class);
                        if (transform == null) {
                            shadow.addComponent(new Transform(x, y, 0.0, 2));
                            transform = shadow.getComponent(Transform.class);
                        }
                        if (point == null || !farmEntity.isEmpty(x, y)) {
                            transform.setPosition(new Point2D(x - sprite.getWidth()/3, y - sprite.getHeight()/3));
                        } else {
                            transform.setPosition(point);
                        }
                    });
                } else {
                    shadow.removeComponent(Transform.class);
                    input.removeMouseHover();
                }
            });

        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        }

        gl.start();
    }

    /**
     * Gets the root node of this class.
     * @return StackPane which contains the canvas.
     */
	@Override
	public Parent getContent() {
		dynamicCanvas = new Canvas(windowWidth, windowHeight);
        staticCanvas = new Canvas(windowWidth, windowHeight);
        interfaceOverlay = new StackPane();
        interfaceOverlay.setPrefWidth(windowWidth);
        interfaceOverlay.setPrefHeight(windowHeight);
        root = new StackPane(staticCanvas, dynamicCanvas, interfaceOverlay);
		return root;
	}

    public void cleanup() {
        if (renderer != null) {
            renderer.cleanup();
        }
        if (systemUpdater != null) {
            systemUpdater.cleanup();
        }
        if (eventSystems != null) {
            for (EventSystem eventSystem : eventSystems) {
                eventSystem.cleanup();
            }
        }
    }
}