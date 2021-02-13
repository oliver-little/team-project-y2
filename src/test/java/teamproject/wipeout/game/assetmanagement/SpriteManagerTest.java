package teamproject.wipeout.game.assetmanagement;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

import javafx.scene.image.Image;

public class SpriteManagerTest {
    @Test
    void testGetImage() {
        final SpriteManager sm = new SpriteManager();

        Image test = assertDoesNotThrow(() -> sm.getImage(new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/sprite.png").getAbsolutePath()));

        Image test2 = assertDoesNotThrow(() -> sm.getImage(new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/sprite.png").getAbsolutePath()));
    
        // Test that the exact same image is returned (i.e: the image is cached)
        assertEquals(test, test2);
    }

    @Test
    void testGetSpriteSet() {
        final SpriteManager sm = new SpriteManager();

        String json = new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/spritesheet-descriptor.json").getAbsolutePath();
        String imagePath = new File("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/spritesheet.png").getAbsolutePath();
        
        assertThrows(FileNotFoundException.class, () -> sm.getSpriteSet("player", "test"));

        assertDoesNotThrow(() -> sm.loadSpriteSheet(json, imagePath));

        assertThrows(FileNotFoundException.class, () -> sm.getSpriteSet("player", "nonexistentspriteset"));

        Image[] ss = assertDoesNotThrow(() -> sm.getSpriteSet("player", "test"));
        assertEquals(1, ss.length);
    }
}
