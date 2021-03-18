package teamproject.wipeout.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ImageUtil {
    /**
     * Scales a given JavaFX Image to a new width and height
     * 
     * @param image The image to scale
     * @param scaleFactor The factor to scale the image by (must be greater than 1)
     * @return The rescaled image
     */
    public static Image scaleImage(Image image, double scaleFactor) {
        if (scaleFactor < 1) {
            throw new IllegalArgumentException("Invalid scale factor, scale factor must be greater than 1.");
        }
        
        int newWidth = (int) (image.getWidth() * scaleFactor);
        int newHeight = (int) (image.getHeight() * scaleFactor);

        WritableImage outImage = new WritableImage(newWidth, newHeight);
        PixelWriter writer = outImage.getPixelWriter();
        PixelReader reader = image.getPixelReader();

        for (int x = 0; x < outImage.getWidth(); x++) {
            for (int y = 0; y < outImage.getHeight(); y++) {
                writer.setArgb(x, y, reader.getArgb((int) (x/scaleFactor), (int) (y/scaleFactor)));
            }
        }

        return outImage;
    }
}
