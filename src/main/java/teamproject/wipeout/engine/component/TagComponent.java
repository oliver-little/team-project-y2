package teamproject.wipeout.engine.component;

public class TagComponent implements GameComponent {
    public String tag;

    public TagComponent() {
        this.tag = "";
    }

    public TagComponent(String tag) {
        this.tag = tag;
    }

    public String getType() {
        return "tag";
    }
}
