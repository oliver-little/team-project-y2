package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.awt.*;

//TODO: add multiple contructors for different fonts, sizes and colors etc
public class TextRenderable implements Renderable {

	public Font font;
	public Color color;
	public Text text;
	
	public TextRenderable(String text) {
		this.font = new Font("Arial", 10);
		this.setFont(font);
		this.text = new Text(text);
	}

	public TextRenderable(String text, int size){
		this.font = new Font("Arial", size);
		this.setFont(font);
		this.text = new Text(text);
	}
	
	public void setText(String text) {
		this.text = new Text(text);;
	}
	
	public String getText() {
		return this.text.getText();
	}
	
	public double getWidth()
	{
		double width = text.getLayoutBounds().getWidth()*font.getSize()/17.5f;
		//System.out.println("textWidth "+ width);
		return width;
	}

	public double getHeight()
	{
		double height = text.getLayoutBounds().getHeight()*font.getSize()/17.5f;
		//System.out.println("textHeight "+ height);
		return height;
	}

	public void setFont(Font font){
		this.font = font;
	}

	@Override
	public void render(GraphicsContext gc, double x, double y, double scale)
	{
		gc.setFill(Color.WHITE);
		gc.setFont(font);
		gc.fillText(text.getText(), scale * x, scale * y);
	}
	
}