package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

//TODO: add multiple contructors for different fonts, sizes and colors etc
public class TextRenderable implements Renderable {

	public Font font;
	public Color color;
	public float width;
	public float height;
	public String text;
	
	public TextRenderable(String text) {
		this.font = new Font("Arial", 10);
		this.height = 10;
		this.width = 10;
		this.text = text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return this.text;
	}
	
	public double getWidth()
	{
		return this.width;
	}

	public double getHeight()
	{
		return this.height;
	}

	@Override
	public void render(GraphicsContext gc, double x, double y, double scale)
	{
		gc.setFill(Color.WHITE);
		gc.setFont(font);
		gc.fillText(text, scale * x, scale * y);
	}
	
}