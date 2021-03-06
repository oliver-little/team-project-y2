package teamproject.wipeout.game.player;

/**
 * Structure to hold an itemID and its respective quanitity for the inventory ArrayList
 *
 */
public class invPair{
	public Integer itemID;
	public Integer quantity;
	public invPair(Integer itemID, Integer i)
	{
		this.itemID = itemID;
		this.quantity = i;
	}
	
}