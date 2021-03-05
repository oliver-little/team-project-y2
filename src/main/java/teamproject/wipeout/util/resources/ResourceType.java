package teamproject.wipeout.util.resources;

public enum ResourceType {
    UI("ui/"), ASSET("sprites/"), AUDIO("audio/"), ITEM("items/"), STYLESHEET("stylesheets/");

    public final String path;

    private ResourceType(String path) {
        this.path = path;
    }

}
