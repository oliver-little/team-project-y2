package teamproject.wipeout.game.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.item.components.TradableComponent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ItemTest {

    private static ItemStore itemStore;

    @BeforeAll
    static void initialization() {
        itemStore = assertDoesNotThrow(() -> new ItemStore("items.json"));
    }

    @Test
    void testHasComponent() {
        Item item = itemStore.getItem(1);
        Assertions.assertTrue(item.hasComponent(InventoryComponent.class));

        item = itemStore.getItem(2);
        Assertions.assertTrue(item.hasComponent(TradableComponent.class));
    }

    @Test
    void getComponent() {
        Item item = itemStore.getItem(1);
        Assertions.assertNotNull(item.getComponent(InventoryComponent.class));

        item = itemStore.getItem(2);
        Assertions.assertNotNull(item.getComponent(TradableComponent.class));
    }

}