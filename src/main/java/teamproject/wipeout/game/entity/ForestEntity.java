package teamproject.wipeout.game.entity;

import java.io.FileNotFoundException;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

/**
 * Creates a 2D Array (of a given size) of TreeEntities with some randomness of image and position
 * @author Admin
 *
 */
public class ForestEntity extends GameEntity
{

	public ForestEntity(GameScene scene, Point2D coords, Point2D dimensions, SpriteManager spriteManager) throws FileNotFoundException
	{
		super(scene);

		Image treeImages[] = spriteManager.getSpriteSet("fruit-tree", "apple-tree");
		
		int xTrees = 1+ (int) (dimensions.getX()/treeImages[treeImages.length-1].getWidth());
		int yTrees = 1+ (int) (dimensions.getY()/treeImages[treeImages.length-1].getHeight());
		TreeEntity[][] trees = new TreeEntity[yTrees][xTrees];
		double xChange = dimensions.getX()/(float)xTrees;
		double yChange = dimensions.getY()/(float)yTrees;
		for(int y=0; y<yTrees;y++) {
			for(int x=0; x<xTrees;x++) {
				//add small random offset so trees are not placed uniformly
				double xOffset = Math.random()*20;
				double yOffset = Math.random()*20;
				int randomIndex = (int) (Math.random()*treeImages.length);
				trees[y][x] = new TreeEntity(scene, coords.getX()+xChange*x+xOffset, coords.getY()-yOffset+yChange*y, treeImages[randomIndex]);
			}
		}
		

		
	}

}
