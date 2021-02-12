package teamproject.wipeout.game.assetmanagement.spritesheet;

import java.util.Map;

public class SpriteSetDescriptor {
    public String name;
    public String type;
    public Map<String, Integer> parameters;

    public String toString() {
        return "SpriteSet: Name: " + name + ", type: " + type;
    }
}
