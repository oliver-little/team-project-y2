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

/**
 * Provides utilities for parsing a spritesheet into its contained images using a descriptor JSON file.
 */
public class Spritesheet {

    public static final String SPRITESHEET_VERSION = "1.0.0";

    /**
     * Parses a spritesheet using a given JSON file and image
     * 
     * @param spriteSheetDescriptor A SpritesheetDescriptor object describing the sprites in the image
     * @param imagePath The path to the image to parse
     * @return A map of spriteSet names (as described in the JSON file) to a list of images.
     */
    public static Map<String, Image[]> parseSpriteSheet(SpritesheetDescriptor spritesheetDescriptor, String imagePath) throws FileNotFoundException, IOException {

        BufferedImage image = ImageIO.read(new File(imagePath));

        Map<String, Image[]> spriteSets = new HashMap<String, Image[]>();
        
        for (SpriteSetDescriptor spriteSet : spritesheetDescriptor.sprites) {
            Image[] sprites = null;
            switch (spriteSet.type) {
                case "sprite":
                    int xStart = spriteSet.parameters.get("x");
                    int yStart = spriteSet.parameters.get("y");
                    int width = spriteSet.parameters.get("width");
                    int height = spriteSet.parameters.get("height");
                    sprites = new Image[] {Spritesheet.getSubImage(image, xStart, yStart, width, height)};
                case "sprite-list-x":
                    sprites = Spritesheet.parseSpriteList(image, spriteSet.parameters, true);
                    break;
                case "sprite-list-y":
                    sprites = Spritesheet.parseSpriteList(image, spriteSet.parameters, false);
                    break;
                default:
                    System.out.println("Invalid sprite set type: " + spriteSet.type);
                    continue;
            }   

            if (sprites != null) {
                spriteSets.put(spriteSet.name, sprites);
            }
        }

        return spriteSets;
    }

    /**
     * Parses a list of sprites from a spritesheet into a list of images
     * 
     * @param image The image to parse
     * @param parameters A Map of string to integer containing x, y, width, height and length (number of images)
     * @param incrementX Boolean determining whether to get images along the x axis (true) or the y axis (false)
     * @return An array of subimages
     */
    public static Image[] parseSpriteList(BufferedImage image, Map<String, Integer> parameters, boolean incrementX) {
        int xStart = parameters.get("x");
        int yStart = parameters.get("y");
        int width = parameters.get("width");
        int height = parameters.get("height");
        int length = parameters.get("length");

        Image[] images = new Image[length];

        if (incrementX) {
            for (int i = 0; i < length; i++) {
                images[i] = Spritesheet.getSubImage(image, xStart + width * i, yStart, width, height);
                if (images[i] == null) {
                    System.out.println("Invalid subimage.");
                }
            }
        }
        else {
            for (int i = 0; i < length; i++) {
                images[i] = Spritesheet.getSubImage(image, xStart, yStart + height * i, width, height);
                if (images[i] == null) {
                    System.out.println("Invalid subimage.");
                }
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
     */
    public static Image getSubImage(BufferedImage image, int xStart, int yStart, int width, int height) {
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

    /**
     * Creates a SpritesheetDescriptor from a JSON file
     * 
     * @param filePath The path to the JSON file
     * @return The SpritesheetDescriptor object the JSON file describes
     */
    public static SpritesheetDescriptor getSpritesheetFromJSON(String filePath) throws FileNotFoundException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));

        Gson gson = new Gson();
        SpritesheetDescriptor spritesheetDescriptor = gson.fromJson(bufferedReader, SpritesheetDescriptor.class);

        if (spritesheetDescriptor.fileType == null || spritesheetDescriptor.version == null || !spritesheetDescriptor.fileType.equals("SpritesheetDescriptor")) {
            return null;
        }

        if (!spritesheetDescriptor.version.equals(Spritesheet.SPRITESHEET_VERSION)) {
            System.out.println("Version of spritesheet descriptor is different to current version, unexpected behaviour may occur.");
        }

        return spritesheetDescriptor;
    }
}
