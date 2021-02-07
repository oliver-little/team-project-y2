package teamproject.wipeout.game.assets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class SpriteManager {

    public static final SpriteManager instance = new SpriteManager();

    protected Map<String, Image> imageCache;
    protected Map<String, Image[][]> spriteSheetCache;

    public SpriteManager() {

        imageCache = new HashMap<String, Image>();
        spriteSheetCache = new HashMap<String, Image[][]>();

    }

    public Image getImage(String path) throws FileNotFoundException {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }       
            FileInputStream file = new FileInputStream(path);

            Image image = new Image(file);
    
            imageCache.put(path,image);
    
            return image;
    } 


    public Image getSprite(String path, int x, int y) throws FileNotFoundException {

        if (!spriteSheetCache.containsKey(path)) {
            throw new FileNotFoundException("Sprite sheet not loaded.");
        }

        return spriteSheetCache.get(path)[x][y];


    }

    public void loadSpriteSheet(String path, int width, int height) throws FileNotFoundException, IOException {

        BufferedImage image = ImageIO.read(new File(path));

        Image[][] subImage = new Image[image.getWidth()/width][image.getHeight()/height];

        for (int x = 0; x < image.getWidth(); x += width) {
            for (int y = 0; y < image.getHeight(); y += height) {
                subImage[x/width][y/height] = SpriteManager.getSubImage(image, x, y, width, height);
            }
        }

        spriteSheetCache.put(path,subImage); 

            
    }

    private static Image getSubImage(BufferedImage image, int xStart, int yStart, int width, int height) {
        if (xStart < 0 || yStart < 0 || xStart + width > image.getWidth() || yStart + height > image.getHeight()) {
            return null;
        }
        
        WritableImage wr = new WritableImage(width, height);
        PixelWriter pixelWriter = wr.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixelWriter.setArgb(x, y, image.getRGB(x + xStart, y + yStart));
            }
        }

        return wr;
    }
}
