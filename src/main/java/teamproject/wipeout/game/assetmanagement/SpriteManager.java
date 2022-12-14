package teamproject.wipeout.game.assetmanagement;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;
import teamproject.wipeout.game.assetmanagement.spritesheet.Spritesheet;
import teamproject.wipeout.game.assetmanagement.spritesheet.SpritesheetDescriptor;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

/**
 * Provides a cache for storing a single instance of images and spritesheets
 */
public class SpriteManager {

    public static final SpriteManager instance = new SpriteManager();

    protected Map<String, Image> imageCache;
    protected Map<String, Map<String, Image[]>> spriteSheetCache;

    /**
     * Creates a new instance of SpriteManager
     */
    public SpriteManager() {
        imageCache = new HashMap<String, Image>();
        spriteSheetCache = new HashMap<String, Map<String, Image[]>>();
    }

    /**
     * Gets an image from a relative file path -
     * if the image already exists in the cache, it will not be loaded again.
     * 
     * @param imagePath The relative path to the image file inside /resources/assets/
     * @return The image
     * @throws FileNotFoundException Thrown if the path does not point to a valid file
     */
    public Image getImage(String imagePath) throws FileNotFoundException {
        if (imageCache.containsKey(imagePath)) {
            return imageCache.get(imagePath);
        }
        FileInputStream fileStream = new FileInputStream(ResourceLoader.get(ResourceType.ASSET, imagePath));

        Image image = new Image(fileStream);

        imageCache.put(imagePath, image);

        return image;
    } 

    /**
     * Gets a given SpriteSet using the spritesheet name, and the SpriteSet name
     * The SpriteSet must already be loaded using loadSpriteSheet
     * 
     * @param spriteSheetName The name of the spriteSheet as written in its descriptor file
     * @param spriteSetName The name of the spriteSet as written in its descriptor file
     * @return The images in the sprite set
     * @throws FileNotFoundException Thrown when a given spritesheet or SpriteSet name is not found
     */
    public Image[] getSpriteSet(String spriteSheetName, String spriteSetName) throws FileNotFoundException {
        if (!spriteSheetCache.containsKey(spriteSheetName)) {
            throw new FileNotFoundException("Sprite sheet not loaded: " + spriteSheetName + " " + spriteSetName);
        }

        Map<String, Image[]> spriteSheet = spriteSheetCache.get(spriteSheetName);

        if (!spriteSheet.containsKey(spriteSetName)) {
            throw new FileNotFoundException("Sprite set (" + spriteSetName + ") does not exist in the following spritesheet:" + spriteSheetName);
        }

        return spriteSheet.get(spriteSetName);
    }

    /**
     * Loads a given spritesheet into the cache using its JSON descriptor file and the image itself
     * 
     * @param JSONPath The relative path inside /resources/assets/ to the JSON descriptor file for this spritesheet
     * @param imagePath The relative path inside /resources/assets/ to the image containing this spritesheet
     * @throws FileNotFoundException Thrown if one or more of the file paths is invalid
     * @throws IOException Thrown when there is an error loading the image
     */
    public void loadSpriteSheet(String JSONPath, String imagePath) throws FileNotFoundException, IOException {
        SpritesheetDescriptor descriptor = Spritesheet.getSpritesheetFromJSON(JSONPath);
        if (!spriteSheetCache.containsKey(descriptor.name)) {
            Map<String, Image[]> spriteSets = Spritesheet.parseSpriteSheet(descriptor, imagePath);
            spriteSheetCache.put(descriptor.name, spriteSets);  
        }   
    }
}
