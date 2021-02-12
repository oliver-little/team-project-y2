package teamproject.wipeout.game.assetmanagement.spritesheet;

public class SpritesheetDescriptor {
    public String fileType;
    public String version;
    public String name;
    public SpriteSetDescriptor[] sprites;

    public String toString() {
        return "Spritesheet: Name: " + name + ", sprites: " + sprites.toString();
    }
}
