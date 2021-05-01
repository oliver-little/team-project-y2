package teamproject.wipeout.game.assetmanagement.spritesheet;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SpritesheetTest {

    @BeforeAll
    static void setup() {
        ResourceLoader.setTargetClass(SpritesheetTest.class);
    }

    @Test
    void testParseValidSpritesheet() {
        final SpritesheetDescriptor ss = assertDoesNotThrow(() -> Spritesheet.getSpritesheetFromJSON("t_spritesheet-descriptor.json"));

        Map<String, Image[]> spritesheet = assertDoesNotThrow(() -> Spritesheet.parseSpriteSheet(ss, "t_spritesheet.png"));


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
        BufferedImage image = assertDoesNotThrow(()-> ImageIO.read(ResourceLoader.get(ResourceType.ASSET, "t_sprite.png")));

        Image[] images = Spritesheet.parseSpriteList(image, 7, 15, 2, 4, 5, true);

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
        BufferedImage image = assertDoesNotThrow(() -> ImageIO.read(ResourceLoader.get(ResourceType.ASSET, "t_sprite.png")));

        assertThrows(IllegalArgumentException.class, () -> Spritesheet.parseSpriteList(image, image.getHeight() - 20, 0, 20, 20, 4, true));
    }

    @Test
    void testGetValidSubimage() {
        BufferedImage image = assertDoesNotThrow(() -> ImageIO.read(ResourceLoader.get(ResourceType.ASSET, "t_sprite.png")));

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
        final BufferedImage image = assertDoesNotThrow(() -> ImageIO.read(ResourceLoader.get(ResourceType.ASSET, "t_sprite.png")));
        final int width = 8;
        final int height = 8;

        assertThrows(IllegalArgumentException.class, () -> Spritesheet.getSubImage(image, -1, 1, width, height));
        assertThrows(IllegalArgumentException.class, () -> Spritesheet.getSubImage(image, 1, -1, width, height));
        assertThrows(IllegalArgumentException.class, () -> Spritesheet.getSubImage(image, image.getWidth() - 1, 1, width, height));
        assertThrows(IllegalArgumentException.class, () -> Spritesheet.getSubImage(image, 1, image.getHeight() - 1, width, height));
    }

    @Test
    void testGetValidSpritesheet() {
        SpritesheetDescriptor s = null;
        try {
            s = Spritesheet.getSpritesheetFromJSON("t_valid-descriptor.json");
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
        assertThrows(FileNotFoundException.class, () -> Spritesheet.getSpritesheetFromJSON("t_invalid-filepath.json"));
        assertThrows(FileNotFoundException.class, () -> Spritesheet.getSpritesheetFromJSON("t_invalid-descriptor.json"));
    }
}
