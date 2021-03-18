package teamproject.wipeout.game.player;

/**
 * Structure to hold an itemID and its respective quantity for the inventory ArrayList.
 */
public class InventoryItem {
	public Integer itemID;
	public Integer quantity;

	public InventoryItem(Integer itemID, Integer i) {
		this.itemID = itemID;
		this.quantity = i;
	}
}