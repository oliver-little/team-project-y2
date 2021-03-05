package teamproject.wipeout.engine.component.render;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import teamproject.wipeout.engine.entity.InventoryEntity;

import java.io.IOException;

public class InventoryRenderable implements Renderable {
	
	protected InventoryEntity invEntity;
	
	public InventoryRenderable(InventoryEntity invEntity) {
		this.invEntity = invEntity;
	}
	
	public InventoryEntity getEntity() {
		return this.invEntity;
	}
	
	public double getWidth()
	{
		return invEntity.size.getX();
	}

	public double getHeight()
	{
		return invEntity.size.getY();
	}

	public void render(GraphicsContext gc, double x, double y, double scale)
	{
		try
		{
			for (int i = 0; i < 10; i ++) {
				gc.drawImage(this.invEntity.getSquare(), (x * scale) + (65*i), y * scale, 65, 65);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
}
