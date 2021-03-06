package teamproject.wipeout.engine.component;

import javafx.scene.image.Image;

/**
 * Component storing animation frames for each direction a player is moving in
 */
public class PlayerAnimatorComponent implements GameComponent {
    
    private Image[] upFrames;
    private Image[] rightFrames;
    private Image[] downFrames;
    private Image[] leftFrames;
    private Image[] idleFrames;

    /**
     * Creates a new instance of PlayerAnimatorComponent
     * 
     * @param upFrames
     * @param rightFrames
     * @param downFrames
     * @param leftFrames
     * @param idleFrames
     */
    public PlayerAnimatorComponent(Image[] upFrames, Image[] rightFrames, Image[] downFrames, Image[] leftFrames, Image[] idleFrames) {
        this.upFrames = upFrames;
        this.rightFrames = rightFrames;
        this.downFrames = downFrames;
        this.leftFrames = leftFrames;
        this.idleFrames = idleFrames;
    }

    /**
     * Getter for the animation frames when this player is moving upwards
     * @return The animation frames
     */
    public Image[] getUpFrames() {
        return this.upFrames;
    }

    /**
     * Getter for the animation frames when this player is moving right
     * @return The animation frames
     */
    public Image[] getRightFrames() {
        return this.rightFrames;
    }

    /**
     * Getter for the animation frames when this player is moving down
     * @return The animation frames
     */
    public Image[] getDownFrames() {
        return this.downFrames;
    }

    /**
     * Getter for the animation frames when this player is moving left
     * @return The animation frames
     */
    public Image[] getLeftFrames() {
        return this.leftFrames;
    }

    /**
     * Getter for the animation frames when this player is idling
     * @return The animation frames
     */
    public Image[] getIdleFrames() {
        return this.idleFrames;
    }

    public String getType() {
        return "player-animator";
    }
}
