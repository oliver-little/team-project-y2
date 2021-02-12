package teamproject.wipeout.game.assetmanagement.spritesheet;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

public class SpritesheetTest {
    @Test
    void testGetValidSpritesheet() {
        SpritesheetDescriptor s = null;
        try {
            s = Spritesheet.getSpritesheetFromJSON("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/valid-descriptor.json");
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
        SpritesheetDescriptor s = null;
        try {
            s = Spritesheet.getSpritesheetFromJSON("./src/test/java/teamproject/wipeout/game/assetmanagement/spritesheet/resources/invalid-descriptor.json");   
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assertEquals(null, s);
    }
}
