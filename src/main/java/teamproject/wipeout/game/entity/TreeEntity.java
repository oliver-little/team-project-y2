package teamproject.wipeout.game.entity;

import java.io.FileNotFoundException;

import javafx.scene.image.Image;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

/**
 * Creates a tree sprite in the game world.
 */
public class TreeEntity extends GameEntity {
	
	/**
	 * Creates a new instance of TreeEntity
	 * @param scene The GameScene this entity is part of
	 * @param x The x coordinate to spawn the tree at
	 * @param y The y coordinate to spawn the tree at
	 * @param spriteManager A SpriteManager instance to get the tree sprite from
	 */
	public TreeEntity(GameScene scene, double x, double y, SpriteManager spriteManager) {
		super(scene);
		
		this.addComponent(new Transform(x, y, 1));

		Image treeImage;
		try {
			treeImage = spriteManager.getSpriteSet("fruit-tree", "apple-tree")[3];
	        SpriteRenderable treeRenderable = new SpriteRenderable(treeImage);

	        RenderComponent renderComponent = new RenderComponent(treeRenderable);
	        this.addComponent(renderComponent);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new instance of TreeEntity
	 * @param scene The GameScene this entity is part of
	 * @param x The x coordinate to spawn the tree at
	 * @param y The y coordinate to spawn the tree at
	 * @param treeImage The image to use for this tree
	 */
	public TreeEntity(GameScene scene, double x, double y, Image treeImage) {
		super(scene);
		
		this.addComponent(new Transform(x,y, 1));
        SpriteRenderable treeRenderable = new SpriteRenderable(treeImage);
        RenderComponent renderComponent = new RenderComponent(treeRenderable);
        this.addComponent(renderComponent);
	}
}
