package teamproject.wipeout.game.assetmanagement;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.image.Image;
import teamproject.wipeout.util.resources.ResourceLoader;

public class SpriteManagerTest {

    @BeforeAll
    static void setup() {
        ResourceLoader.setTargetClass(SpriteManagerTest.class);
    }

    @Test
    void testGetImage() {
        final SpriteManager sm = new SpriteManager();

        Image test = assertDoesNotThrow(() -> sm.getImage("t_sprite.png"));

        Image test2 = assertDoesNotThrow(() -> sm.getImage("t_sprite.png"));
    
        // Test that the exact same image is returned (i.e: the image is cached)
        assertEquals(test, test2);
    }

    @Test
    void testGetSpriteSet() {
        final SpriteManager sm = new SpriteManager();

        assertThrows(FileNotFoundException.class, () -> sm.getSpriteSet("player", "test"));

        assertDoesNotThrow(() -> sm.loadSpriteSheet("t_spritesheet-descriptor.json", "t_spritesheet.png"));

        assertThrows(FileNotFoundException.class, () -> sm.getSpriteSet("player", "nonexistentspriteset"));

        Image[] ss = assertDoesNotThrow(() -> sm.getSpriteSet("player", "test"));
        assertEquals(1, ss.length);
    }
}
