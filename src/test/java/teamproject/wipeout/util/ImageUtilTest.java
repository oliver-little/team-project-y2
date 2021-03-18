package teamproject.wipeout.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ImageUtilTest {

    @Test
    public void testScaleImage() {
        WritableImage inImage = new WritableImage(10, 10);
        PixelWriter writer = inImage.getPixelWriter();
        writer.setArgb(0, 0, 0xFFFFFFFF);
        writer.setArgb(9, 9, 0xFFFFFFFF);
        writer.setArgb(4, 5, 0xFFFFFFFF);
        writer.setArgb(5, 4, 0xFFFFFFFF);

        Image outImage = ImageUtil.scaleImage(inImage, 2);
        PixelReader reader = outImage.getPixelReader();

        assertEquals(20, outImage.getWidth());
        assertEquals(20, outImage.getHeight());
        // Top Left
        assertEquals(0xFFFFFFFF, reader.getArgb(0, 0));
        assertEquals(0xFFFFFFFF, reader.getArgb(0, 1));
        assertEquals(0xFFFFFFFF, reader.getArgb(1, 0));
        assertEquals(0xFFFFFFFF, reader.getArgb(1, 1));

        // Bottom right
        assertEquals(0xFFFFFFFF, reader.getArgb(19, 19));
        assertEquals(0xFFFFFFFF, reader.getArgb(19, 18));
        assertEquals(0xFFFFFFFF, reader.getArgb(18, 19));
        assertEquals(0xFFFFFFFF, reader.getArgb(18, 18));

        // Middle
        assertEquals(0xFFFFFFFF, reader.getArgb(8, 10));
        assertEquals(0xFFFFFFFF, reader.getArgb(8, 11));
        assertEquals(0xFFFFFFFF, reader.getArgb(9, 10));
        assertEquals(0xFFFFFFFF, reader.getArgb(9, 11));

        assertEquals(0xFFFFFFFF, reader.getArgb(10, 8));
        assertEquals(0xFFFFFFFF, reader.getArgb(10, 9));
        assertEquals(0xFFFFFFFF, reader.getArgb(11, 8));
        assertEquals(0xFFFFFFFF, reader.getArgb(11, 9));

        assertThrows(IllegalArgumentException.class, () -> ImageUtil.scaleImage(inImage, 0.5));
    }
}
