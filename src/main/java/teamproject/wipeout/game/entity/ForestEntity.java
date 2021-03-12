package teamproject.wipeout.game.entity;

import java.io.FileNotFoundException;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

public class ForestEntity extends GameEntity
{

	public ForestEntity(GameScene scene, Point2D coords, Point2D dimensions, SpriteManager spriteManager) throws FileNotFoundException
	{
		super(scene);

		Image treeImages[] = spriteManager.getSpriteSet("fruit-tree", "apple-tree");
		
		int numTrees = 10;
		TreeEntity[] trees = new TreeEntity[10];
		double xChange = dimensions.getX()/(float)numTrees;
		for(int i=0; i<numTrees;i++) {
			//add small random offset so trees are not placed uniformly
			double yOffset = Math.random()*20;
			double xOffset = Math.random()*20;
			int randomIndex = (int) (Math.random()*treeImages.length);
			trees[i] = new TreeEntity(scene, coords.getX()+xChange*i+xOffset, coords.getY()-yOffset, treeImages[randomIndex]);
		}
		

		
	}

}
