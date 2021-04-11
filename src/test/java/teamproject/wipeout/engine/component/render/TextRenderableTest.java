package teamproject.wipeout.engine.component.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TextRenderableTest {

    @Test
    void testGetWidth(){
        System.out.println("a");
        TextRenderable tag1 = new TextRenderable("a", 10);
        System.out.println("tag1: "+tag1.getWidth());
        TextRenderable tag2 = new TextRenderable("a", 20);
        System.out.println("tag2: "+tag2.getWidth());
        assertTrue(tag1.getWidth()!=tag2.getWidth());

    }

    @Test
    void testGetWidth2() {
        System.out.println("a and i");
        TextRenderable tag1 = new TextRenderable("a", 10);
        System.out.println("tag1: " + tag1.getWidth());
        TextRenderable tag2 = new TextRenderable("i", 10);
        System.out.println("tag2: " + tag2.getWidth());
        assertTrue(tag1.getWidth() != tag2.getWidth());
    }

    @Test
    void testGetWidth3() {
        System.out.println("abc and x");
        TextRenderable tag1 = new TextRenderable("abc", 10);
        System.out.println("tag1: " + tag1.getWidth());
        TextRenderable tag2 = new TextRenderable("x", 10);
        System.out.println("tag2: " + tag2.getWidth());
        assertTrue(tag1.getWidth() != tag2.getWidth());
    }

}
