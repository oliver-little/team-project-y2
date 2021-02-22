package teamproject.wipeout.util.resources;

public enum ResourceType {
    UI("ui/"), ASSET("assets/"), AUDIO("audio/");

    public final String path;

    private ResourceType(String path) {
        this.path = path;
    }

}
