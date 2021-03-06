package teamproject.wipeout.game.player;

/**
 * Structure to hold an itemID and its respective quanitity for the inventory ArrayList
 * @author Kalam Billan
 *
 */
public class invPair{
	public Integer itemID;
	public Integer quantity;
	public invPair(Integer itemID2, int i)
	{
		this.itemID = itemID2;
		this.quantity = i;
	}
	
}