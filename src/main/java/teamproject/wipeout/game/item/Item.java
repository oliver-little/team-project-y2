package teamproject.wipeout.game.item;


/**
 * Defines an obtainable item in game.
 */
public class Item {
    
    public enum ItemType {
        PLANTABLE, //Seeds - items that can be placed and grown in the ground.
        USABLE, //For future implementation of tools which can be used.
        CONSTRUCTABLE, //For future implementation of utilities which can be placed and used.
        NONE //For any other items, such as fully grown vegetables which can only be bought/sold.
    }

    public Integer id;
    public ItemType itemType;
    public String name;
    public String spriteSheetName;
    public String spriteSetName;
    public Integer maxStackSize;
    public double defaultBuy;
    public double defaultSell;
    
}
