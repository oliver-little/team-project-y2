package teamproject.wipeout.game.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ItemStoreTest {

    @Test
    public void testJSONReadCorrect() throws FileNotFoundException, ReflectiveOperationException {
        Map<Integer, Item> itemsForSale = new ItemStore("items.json").getData();
        Item item = itemsForSale.get(1);
        assertEquals(1, item.id);
        assertEquals("Tomato", item.name);

        item = itemsForSale.get(2);
        assertEquals(2, item.id);
        assertEquals("Lettuce", item.name);
    }

    @Test
    public void testJSONReadIncorrect() {
        Assertions.assertThrows(FileNotFoundException.class, () -> new ItemStore("itemsss.json"));
    }

    @Test
    public void testItemStoreGet() throws FileNotFoundException, ReflectiveOperationException {
        ItemStore itemStore = new ItemStore("items.json");
        Item item = itemStore.getItem(1);
        assertEquals(1, item.id);
        assertEquals("Tomato", item.name);

        item = itemStore.getItem(2);
        assertEquals(2, item.id);
        assertEquals("Lettuce", item.name);

        Assertions.assertNull(itemStore.getItem(-1));
    }

}
