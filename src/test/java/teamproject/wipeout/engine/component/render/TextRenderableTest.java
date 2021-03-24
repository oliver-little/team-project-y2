package teamproject.wipeout.engine.component.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TextRenderableTest {

    @Test
    void testGetWidth(){
        TextRenderable tag1 = new TextRenderable("a", 10);
        TextRenderable tag2 = new TextRenderable("a", 20);
        assertTrue(tag1.getWidth()!=tag2.getWidth());

    }

}
