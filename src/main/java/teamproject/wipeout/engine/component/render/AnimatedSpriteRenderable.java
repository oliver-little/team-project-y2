package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class AnimatedSpriteRenderable implements Renderable {

    public Point2D offset;
    
    protected Image[] _frames;
    protected int _framesPerSecond;

    protected double _secondsPerFrame;
    protected double _lastFrameTime;
    protected int _currentFrame;

    public AnimatedSpriteRenderable(Image[] frames, int framesPerSecond) {
        this.setFrames(frames);
        this.setFPS(framesPerSecond);

        this.offset = Point2D.ZERO;

        this._currentFrame = 0;
        this._lastFrameTime = System.nanoTime() / 1000000000.0;
    }

    public AnimatedSpriteRenderable(Image[] frames, int framesPerSecond, Point2D offset) {
        this.setFrames(frames);
        this.setFPS(framesPerSecond);

        this.offset = offset;

        this._currentFrame = 0;
        this._lastFrameTime = System.nanoTime();
    }

    public void setFrames(Image[] newFrames) {
        this._frames = newFrames;
    }

    public void setFPS(int framesPerSecond) {
        this._framesPerSecond = framesPerSecond;
        this._secondsPerFrame = 1.0/framesPerSecond;
    }

    public void render(GraphicsContext gc, double x, double y) {
        double currentTime = System.nanoTime() / 1000000000.0;
        double timeSinceLastFrameChange = currentTime - this._lastFrameTime;

        if (timeSinceLastFrameChange > this._secondsPerFrame) {
            // Skip to current frame
            this._currentFrame = (this._currentFrame + (int) (timeSinceLastFrameChange / this._secondsPerFrame)) % this._frames.length;
            this._lastFrameTime = currentTime;
        }

        gc.drawImage(this._frames[this._currentFrame], x + offset.getX(), y + offset.getY());
    }
}
