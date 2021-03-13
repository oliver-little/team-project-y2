package teamproject.wipeout.game.entity;

import java.io.FileNotFoundException;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

public class TreeEntity extends GameEntity
{
	public static final int TREE_Y_OFFSET = 85;

	public TreeEntity(GameScene scene, double x, double y, SpriteManager spriteManager)
	{
		super(scene);
		
		this.addComponent(new Transform(x,y + TREE_Y_OFFSET, 1));

		Image treeImage;
		try
		{
			treeImage = spriteManager.getSpriteSet("fruit-tree", "apple-tree")[3];
	        SpriteRenderable treeRenderable = new SpriteRenderable(treeImage);

	        RenderComponent renderComponent = new RenderComponent(new Point2D(0, -TREE_Y_OFFSET), treeRenderable);
	        this.addComponent(renderComponent);
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public TreeEntity(GameScene scene, double x, double y, Image treeImage)
	{
		super(scene);
		
		this.addComponent(new Transform(x,y + TREE_Y_OFFSET, 1));
        SpriteRenderable treeRenderable = new SpriteRenderable(treeImage);
        RenderComponent renderComponent = new RenderComponent(new Point2D(0, -TREE_Y_OFFSET), treeRenderable);
        this.addComponent(renderComponent);


	}

}
