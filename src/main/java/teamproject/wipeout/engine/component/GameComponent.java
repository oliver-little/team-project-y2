package teamproject.wipeout.engine.component;

/**
 * Interface representing any component that can be associated with a GameEntity
 */
public interface GameComponent {
    /**
     * Returns the type of the component as a string
     * @return A String, representing the type of the component
     */
    public String getType();
}