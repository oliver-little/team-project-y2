package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Renderable to display a sequence of sprite frames at a certain framerate
 */
public class AnimatedSpriteRenderable implements Renderable {

    public Point2D offset;
    public Point2D spriteScale;
    
    protected Image[] frames;
    protected int framesPerSecond;

    protected double secondsPerFrame;
    protected double lastFrameTime;
    protected int currentFrame;

    /**
     * Creates a new instance of AnimatedSpriteRenderable
     * 
     * @param frames The images to display, in order
     * @param framesPerSecond The number of images to display per second
     */
    public AnimatedSpriteRenderable(Image[] frames, int framesPerSecond) {
        this.setFrames(frames);
        this.setFPS(framesPerSecond);

        this.offset = Point2D.ZERO;
        this.spriteScale = new Point2D(1, 1);

        this.lastFrameTime = System.nanoTime() / 1000000000.0;
    }

    /**
     * Creates a new instance of AnimatedSpriteRenderable
     * 
     * @param frames The images to display, in order
     * @param framesPerSecond The number of images to display per second
     * @param offset An offset position to display at, from the top left of this object
     */
    public AnimatedSpriteRenderable(Image[] frames, int framesPerSecond, Point2D offset) {
        this(frames, framesPerSecond);
        this.offset = offset;
        this.spriteScale = new Point2D(1, 1);
    }

    /**
     * Creates a new instance of AnimatedSpriteRenderable
     * 
     * @param frames The images to display, in order
     * @param framesPerSecond The number of images to display per second
     * @param scale The scale to display the images at
     */
    public AnimatedSpriteRenderable(Image[] frames, int framesPerSecond, double scale) {
        this(frames, framesPerSecond);

        this.spriteScale = new Point2D(scale, scale);
    }

    /**
     * Creates a new instance of AnimatedSpriteRenderable
     * 
     * @param frames The images to display, in order
     * @param framesPerSecond The number of images to display per second
     * @param scaleX The scale to display the image width at
     * @param scaleY The scale to display the image height at
     */
    public AnimatedSpriteRenderable(Image[] frames, int framesPerSecond, double scaleX, double scaleY) {
        this(frames, framesPerSecond);

        this.spriteScale = new Point2D(scaleX, scaleY);
    }

    /**
     * Changes the frames this object is displaying
     * 
     * @param newFrames The new images to display
     */
    public void setFrames(Image[] newFrames) {
        this.frames = newFrames;
        this.currentFrame = 0;
    }

    /**
     * Changes the current frame being displayed
     * 
     * @param newFrame The new frame position to begin animating at
     */
    public void setCurrentFrame(int newFrame) {
        this.currentFrame = newFrame % this.frames.length;
    }

    /**
     * Changes the framerate this animation is displayed at
     * 
     * @param framesPerSecond
     */
    public void setFPS(int framesPerSecond) {
        this.framesPerSecond = framesPerSecond;
        this.secondsPerFrame = 1.0/framesPerSecond;
    }

    public double getWidth() {
        return this.frames[this.currentFrame].getWidth() * this.spriteScale.getX();
    }

    public double getHeight() {
        return this.frames[this.currentFrame].getHeight() * this.spriteScale.getY();
    }

    public void render(GraphicsContext gc, double x, double y, double scale) {
        double currentTime = System.nanoTime() / 1000000000.0;
        double timeSinceLastFrameChange = currentTime - this.lastFrameTime;

        if (timeSinceLastFrameChange > this.secondsPerFrame) {
            // Skip to current frame
            this.currentFrame = (this.currentFrame + (int) (timeSinceLastFrameChange / this.secondsPerFrame)) % this.frames.length;
            this.lastFrameTime = currentTime;
        }

        Image sprite = this.frames[this.currentFrame];

        gc.drawImage(sprite, (x + offset.getX()) * scale, (y + offset.getY()) * scale, sprite.getWidth() * scale * this.spriteScale.getX(), sprite.getHeight() * scale * this.spriteScale.getY());
    }
}
