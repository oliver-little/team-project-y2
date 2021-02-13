package teamproject.wipeout.game.assetmanagement.spritesheet;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

public class SpritesheetTest {

    @Test
    void testParseValidSpritesheet() {
        SpritesheetDescriptor ss = null;
        try {
            ss = Spritesheet.getSpritesheetFromJSON(new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/spritesheet-descriptor.json").getAbsolutePath());
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assertNotEquals(null, ss);

        Map<String, Image[]> spritesheet = null;
        try {
            spritesheet = Spritesheet.parseSpriteSheet(ss, new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/spritesheet.png").getAbsolutePath());
        }
        catch (Exception e) {
            // Exceptions should not occur so fail this test
            assertEquals(true, false);
        }

        assertNotEquals(null, spritesheet);

        assertEquals(true, Set.of("idle", "potion", "test").containsAll(spritesheet.keySet()));

        assertEquals(10, spritesheet.get("idle").length);
        assertEquals(10, spritesheet.get("potion").length);
        assertEquals(1, spritesheet.get("test").length);
        Image test = spritesheet.get("test")[0];
        assertEquals(139, test.getWidth());
        assertEquals(235, test.getHeight());
    }

    @Test
    void testParseValidSpriteList() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/sprite.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertNotEquals(null, image);

        HashMap<String, Integer> parameters = new HashMap<>();
        parameters.put("x", 7);
        parameters.put("y", 15);
        parameters.put("width", 2);
        parameters.put("height", 4);
        parameters.put("length", 5);

        Image[] images = Spritesheet.parseSpriteList(image, parameters, true);

        assertEquals(2, images[0].getWidth());
        assertEquals(4, images[0].getHeight());

        PixelReader pr = images[0].getPixelReader();

        // First image should be all white pixels
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 4; y++) {
                assertEquals(0xffffffff, pr.getArgb(x, y));
            }
        }

        // Rest of the images should have no white pixels
        for (int i = 1; i < images.length; i++) {
            pr = images[i].getPixelReader();
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 4; y++) {
                    assertNotEquals(0xffffffff, pr.getArgb(x, y));
                }
            }
        }
    }

    @Test
    void testParseInvalidSpriteList() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/sprite.png"));
        } 
        catch (IOException e) {
            e.printStackTrace();
        }

        assertNotEquals(null, image);

        HashMap<String, Integer> parameters = new HashMap<>();
        parameters.put("x", 7);
        parameters.put("y", 15);
        parameters.put("width", 2);
        parameters.put("length", 4);

        Image[] images = Spritesheet.parseSpriteList(image, parameters, true);

        assertEquals(null, images);
    }

    @Test
    void testGetValidSubimage() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/sprite.png"));
        } 
        catch (IOException e) {
            e.printStackTrace();
        }

        assertNotEquals(null, image);

        final int X = 1;
        final int Y = 13;
        final int WIDTH = 8;
        final int HEIGHT = 8;

        Image subImage = Spritesheet.getSubImage(image, X, Y, WIDTH, HEIGHT);

        assertEquals(WIDTH, subImage.getWidth());
        assertEquals(HEIGHT, subImage.getHeight());
        PixelReader pr = subImage.getPixelReader();

        // The subimage grabbed was a section of the image with all white pixels, check
        // all pixels are set to white:
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                assertEquals(0xffffffff, pr.getArgb(x, y));
            }
        }
    }

    @Test
    void testGetInvalidSubimage() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/sprite.png"));
        } 
        catch (IOException e) {
            e.printStackTrace();
        }

        assertNotEquals(null, image);

        int x = -1;
        int y = 13;
        int width = 8;
        int height = 8;

        Image subImage = Spritesheet.getSubImage(image, x, y, width, height);
        assertEquals(null, subImage);
        x = 1;
        y = -1;
        subImage = Spritesheet.getSubImage(image, x, y, width, height);
        assertEquals(null, subImage);
        y = 1;
        x = image.getWidth() - 1;
        subImage = Spritesheet.getSubImage(image, x, y, width, height);
        assertEquals(null, subImage);
        x = 0;
        y = image.getHeight() - 1;
        subImage = Spritesheet.getSubImage(image, x, y, width, height);
        assertEquals(null, subImage);
    }

    @Test
    void testGetValidSpritesheet() {
        SpritesheetDescriptor s = null;
        try {
            File f = new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/valid-descriptor.json");
            s = Spritesheet.getSpritesheetFromJSON(f.getAbsolutePath());
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assertNotEquals(null, s);

        assertEquals("test", s.name);
        assertEquals("list-x", s.sprites[0].name);
        assertEquals("sprite-list-x", s.sprites[0].type);
        assertEquals(0, s.sprites[0].parameters.get("x"));
        assertEquals(0, s.sprites[0].parameters.get("y"));
        assertEquals(32, s.sprites[0].parameters.get("width"));
        assertEquals(32, s.sprites[0].parameters.get("height"));
        assertEquals(10, s.sprites[0].parameters.get("length"));

        assertEquals("list-y", s.sprites[1].name);

        assertEquals("single sprite", s.sprites[2].name);
        assertEquals(null, s.sprites[2].parameters.get("length"));
    }

    @Test
    void testGetInvalidSpritesheet() {
        final File f = new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/invalid-filepath.json");
        assertThrows(FileNotFoundException.class, () -> Spritesheet.getSpritesheetFromJSON(f.getAbsolutePath()));
        final File g = new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/invalid-descriptor.json");
        assertThrows(FileNotFoundException.class, () -> Spritesheet.getSpritesheetFromJSON(g.getAbsolutePath()));
    }
}
