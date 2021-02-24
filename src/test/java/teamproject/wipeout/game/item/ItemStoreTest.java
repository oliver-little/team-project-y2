package teamproject.wipeout.game.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class ItemStoreTest {
    @Test
    public void testJSONReadCorrect() {
        
        try {
            Map<Integer, Item> itemsForSale = ItemStore.getItemFileFromJSON("items.JSON");
            Item item = itemsForSale.get(1);
            assertEquals(1, item.id);
            assertEquals("Tomato", item.name);

            item = itemsForSale.get(2);
            assertEquals(2, item.id);
            assertEquals("Lettuce", item.name);
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }  
}
