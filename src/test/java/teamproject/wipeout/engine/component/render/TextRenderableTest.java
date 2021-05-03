package teamproject.wipeout.engine.component.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextRenderableTest {

    @Test
    void testGetWidth(){
        TextRenderable tag1 = new TextRenderable("a", 10);
        TextRenderable tag2 = new TextRenderable("a", 20);
        assertTrue(tag1.getWidth()!=tag2.getWidth());

    }

    @Test
    void testGetWidth2() {
        TextRenderable tag1 = new TextRenderable("a", 10);
        TextRenderable tag2 = new TextRenderable("i", 10);
        assertTrue(tag1.getWidth() != tag2.getWidth());
    }

    @Test
    void testGetWidth3() {
        TextRenderable tag1 = new TextRenderable("abc", 10);
        TextRenderable tag2 = new TextRenderable("x", 10);
        assertTrue(tag1.getWidth() != tag2.getWidth());
    }

}
