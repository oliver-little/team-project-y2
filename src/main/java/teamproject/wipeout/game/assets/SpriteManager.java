package teamproject.wipeout.game.assets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;

public class SpriteManager {

    public static final SpriteManager instance = new SpriteManager();

    protected Map<String, Image> cache;

    public SpriteManager() {

        cache = new HashMap<String, Image>();

    }

    public Image getImage(String path) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }
        try {
        
            FileInputStream file = new FileInputStream(path);

            Image image = new Image(file);
    
            cache.put(path,image);
    
            return image;
        } 
        catch (FileNotFoundException e) {
            System.out.println("Exception - File not found: " + path);
            return null;
        }

    }

}
