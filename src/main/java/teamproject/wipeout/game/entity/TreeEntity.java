package teamproject.wipeout.game.entity;

import java.io.FileNotFoundException;

import javafx.scene.image.Image;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

public class TreeEntity extends GameEntity
{

	public TreeEntity(GameScene scene, double x, double y, SpriteManager spriteManager)
	{
		super(scene);
		
		this.addComponent(new Transform(x,y));

		Image treeImage;
		try
		{
			treeImage = spriteManager.getSpriteSet("fruit-tree", "apple-tree")[3];
	        SpriteRenderable treeRenderable = new SpriteRenderable(treeImage);

	        RenderComponent renderComponent = new RenderComponent(treeRenderable);
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
		
		this.addComponent(new Transform(x,y));
        SpriteRenderable treeRenderable = new SpriteRenderable(treeImage);
        RenderComponent renderComponent = new RenderComponent(treeRenderable);
        this.addComponent(renderComponent);


	}

}
