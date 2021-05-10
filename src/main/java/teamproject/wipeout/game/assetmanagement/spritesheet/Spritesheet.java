package teamproject.wipeout.game.assetmanagement.spritesheet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import com.google.gson.Gson;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

/**
 * Provides utilities for parsing a spritesheet into its contained images using a descriptor JSON file.
 */
public class Spritesheet {

    public static final String SPRITESHEET_VERSION = "1.0.0";

    /**
     * Parses a spritesheet using a given JSON file and image
     * 
     * @param spritesheetDescriptor A SpritesheetDescriptor object describing the sprites in the image
     * @param imagePath The relative path to the image to be parsed inside /resources/assets/
     * @return A map of spriteSet names (as described in the JSON file) to a list of images
     * @throws FileNotFoundException if the file path provided is invalid
     * @throws IOException if loading the image fails
     * @throws IllegalArgumentException if the spritesheetDescriptor is invalid
     */
    public static Map<String, Image[]> parseSpriteSheet(SpritesheetDescriptor spritesheetDescriptor, String imagePath) throws FileNotFoundException, IOException {
        File imageFile = ResourceLoader.get(ResourceType.ASSET, imagePath);
        BufferedImage image = ImageIO.read(imageFile);

        Map<String, Image[]> spriteSets = new HashMap<String, Image[]>();
        
        for (SpriteSetDescriptor spriteSet : spritesheetDescriptor.sprites) {
            Image[] sprites = null;
            int xStart = spriteSet.parameters.get("x");
            int yStart = spriteSet.parameters.get("y");
            int width = spriteSet.parameters.get("width");
            int height = spriteSet.parameters.get("height");

            switch (spriteSet.type) {
                case "sprite":
                    sprites = new Image[] {Spritesheet.getSubImage(image, xStart, yStart, width, height)};
                    break;
                case "sprite-list-x":
                    sprites = Spritesheet.parseSpriteList(image, xStart, yStart, width, height, spriteSet.parameters.get("length"), true);
                    break;
                case "sprite-list-y":
                    sprites = Spritesheet.parseSpriteList(image, xStart, yStart, width, height, spriteSet.parameters.get("length"), false);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid sprite set type: " + spriteSet.type);
            }   
            
            spriteSets.put(spriteSet.name, sprites);
        }

        return spriteSets;
    }

    /**
     * Parses a list of sprites from a spritesheet into a list of images
     * 
     * @param image The image to parse
     * @param xStart The x coordinate the first subimage starts at
     * @param yStart The y coordinate the first subimage starts at
     * @param width The width of each subimage
     * @param height The height of each subimage
     * @param length The number of subimages to get
     * @param incrementX Boolean determining whether to get images along the x axis (true) or the y axis (false)
     * @return An array of subimages
     */
    public static Image[] parseSpriteList(BufferedImage image, int xStart, int yStart, int width, int height, int length, boolean incrementX) {
        Image[] images = new Image[length];

        if (incrementX) {
            for (int i = 0; i < length; i++) {
                images[i] = Spritesheet.getSubImage(image, xStart + width * i, yStart, width, height);
            }
        }
        else {
            for (int i = 0; i < length; i++) {
                images[i] = Spritesheet.getSubImage(image, xStart, yStart + height * i, width, height);
            }
        }

        return images;
    }

    /**
     * Creates a single subimage from a larger image
     * @param image The image to read from
     * @param xStart The x coordinate the subimage starts at
     * @param yStart The y coordinate the subimage starts at
     * @param width The width of the subimage
     * @param height The height of the subimage
     * @return The subimage
     * @throws IllegalArgumentException if the subimage requested is invalid.
     */
    public static Image getSubImage(BufferedImage image, int xStart, int yStart, int width, int height) {
        if (xStart < 0 || yStart < 0 || xStart + width > image.getWidth() || yStart + height > image.getHeight()) {
            throw new IllegalArgumentException("Invalid subimage parameters - Image width:" + image.getWidth() + ", image height:" + image.getHeight() + ", requested x, y, w, h: " + xStart + ", " + yStart + ", " + width + ", " + height);
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

    /**
     * Creates a SpritesheetDescriptor from a JSON file
     * 
     * @param JSONPath The relative path to the JSON file inside /resources/assets/
     * @return The SpritesheetDescriptor object the JSON file describes
     * @throws FileNotFoundException if the filepath is invalid or the contents of the file were invalid.
     */
    public static SpritesheetDescriptor getSpritesheetFromJSON(String JSONPath) throws FileNotFoundException {
        File JSONFile = ResourceLoader.get(ResourceType.ASSET, JSONPath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(JSONFile));
        Gson gson = new Gson();
        SpritesheetDescriptor spritesheetDescriptor = gson.fromJson(bufferedReader, SpritesheetDescriptor.class);

        if (spritesheetDescriptor.fileType == null || spritesheetDescriptor.version == null || !spritesheetDescriptor.fileType.equals("SpritesheetDescriptor")) {
            throw new FileNotFoundException("Invalid file format.");
        }

        if (!spritesheetDescriptor.version.equals(Spritesheet.SPRITESHEET_VERSION)) {
            throw new FileNotFoundException("Version of spritesheet descriptor is different to current version, unexpected behaviour may occur.");
        }

        return spritesheetDescriptor;
    }
}
