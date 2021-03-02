package teamproject.wipeout.game.item;

/**
 * Wrapper for the list of items once the JSON file has been read by ItemStore.
 */
public class ItemFile {
    public String fileType;
    public String version;
    public RawItem[] items;
}
