package teamproject.wipeout.game.farm;

import org.junit.jupiter.api.*;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.util.resources.ResourceLoader;

import java.io.FileNotFoundException;
import java.util.ArrayList;

// FarmData dimensions (FARM_ROWS and FARM_COLUMNS) must be at least:
// FARM_ROWS = 3, and
// FARM_COLUMNS = 5,
// for these tests to function properly
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FarmDataTest {
    private static final double GROWTH = 0.5;

    private Item item;
    private Item finishedItem;

    private FarmItem farmItem;
    private FarmItem finishedFarmItem;
    private FarmItem dummyFarmItem;

    private FarmData farmData;

    @BeforeAll
    void initialization() throws FileNotFoundException, ReflectiveOperationException {
        ResourceLoader.setTargetClass(ResourceLoader.class);

        ItemStore itemStore = new ItemStore("items.json");
        item = itemStore.getItem(28);
        finishedItem = itemStore.getItem(43);
    }

    @BeforeEach
    void setUp() {
        farmItem = new FarmItem(item, GROWTH * item.getComponent(PlantComponent.class).maxGrowthStage);

        finishedFarmItem = new FarmItem(finishedItem);
        finishedFarmItem.growth.set(finishedFarmItem.getGrowthRate() * getMaxGrowthStageFor(finishedFarmItem));
        dummyFarmItem = new FarmItem(null, 0.1);

        farmData = new FarmData(1, 1, (value) -> {}, null);

        Assertions.assertTrue(farmData.farmRows >= 3);
        Assertions.assertTrue(farmData.farmColumns >= 5);

        farmData.items.get(0).set(0, farmItem);
        farmData.items.get(0).set(1, finishedFarmItem);
        farmData.items.get(0).set(2, dummyFarmItem);
        farmData.items.get(1).set(1, dummyFarmItem);
        farmData.items.get(1).set(2, dummyFarmItem);
    }

    @Test
    void testCoordinatesChecking() {
        Assertions.assertTrue(farmData.areCoordinatesInvalid(-20, -20));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(-1, -1));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(-10, 0));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(0, -10));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(-1, 0));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(0, -1));
        Assertions.assertFalse(farmData.areCoordinatesInvalid(0, 0));
        Assertions.assertFalse(farmData.areCoordinatesInvalid(0, 1));
        Assertions.assertFalse(farmData.areCoordinatesInvalid(1, 0));
        Assertions.assertFalse(farmData.areCoordinatesInvalid(1, 1));

        Assertions.assertFalse(farmData.areCoordinatesInvalid(farmData.farmRows - 1, farmData.farmColumns - 1));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(farmData.farmRows, farmData.farmColumns));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(farmData.farmRows - 1, farmData.farmColumns));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(farmData.farmRows, farmData.farmColumns - 1));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(farmData.farmRows + 1, farmData.farmColumns));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(farmData.farmRows, farmData.farmColumns + 1));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(farmData.farmRows + 1, farmData.farmColumns + 1));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(farmData.farmRows + 10, farmData.farmColumns - 1));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(farmData.farmRows - 1, farmData.farmColumns + 10));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(farmData.farmRows + 10, farmData.farmColumns + 10));
    }

    @Test
    void testGetItems() {
        ArrayList<ArrayList<FarmItem>> allItems = farmData.getItems();

        Assertions.assertEquals(farmData.items.size(), allItems.size());
        Assertions.assertEquals(farmData.items.get(0).size(), allItems.get(0).size());

        Assertions.assertEquals(farmItem, allItems.get(0).get(0));
        Assertions.assertEquals(finishedFarmItem, allItems.get(0).get(1));
        Assertions.assertEquals(dummyFarmItem, allItems.get(0).get(2));
        Assertions.assertEquals(dummyFarmItem, allItems.get(1).get(1));
        Assertions.assertEquals(dummyFarmItem, allItems.get(1).get(2));
    }

    @Test
    void testGetItemsInRow() {
        ArrayList<FarmItem> row0Items = farmData.getItemsInRow(0);
        ArrayList<FarmItem> row1Items = farmData.getItemsInRow(1);

        Assertions.assertEquals(farmData.items.get(0).size(), row0Items.size());
        Assertions.assertEquals(farmData.items.get(1).size(), row1Items.size());

        Assertions.assertEquals(farmItem, row0Items.get(0));
        Assertions.assertEquals(finishedFarmItem, row0Items.get(1));
        Assertions.assertEquals(dummyFarmItem, row0Items.get(2));
        Assertions.assertEquals(dummyFarmItem, row1Items.get(1));
        Assertions.assertEquals(dummyFarmItem, row1Items.get(2));
    }

    @Test
    void testItemAt() {
        FarmItem farmItem1 = farmData.itemAt(0, 0);
        FarmItem farmItem2 = farmData.itemAt(0, 1);
        FarmItem farmItem2_0 = farmData.itemAt(0, 2);
        FarmItem farmItem2_1 = farmData.itemAt(1, 1);
        FarmItem farmItem2_2 = farmData.itemAt(1, 2);
        FarmItem nullFarmItem1_0 = farmData.itemAt(1, 0);
        FarmItem nullFarmItemOutOfBounds1 = farmData.itemAt(-1, -1);
        FarmItem nullFarmItemOutOfBounds2 = farmData.itemAt(-1, 0);
        FarmItem nullFarmItemOutOfBounds3 = farmData.itemAt(1, -1);
        FarmItem nullFarmItemOutOfBounds4 = farmData.itemAt(farmData.farmRows, farmData.farmColumns);

        Assertions.assertEquals(farmItem, farmItem1);
        Assertions.assertEquals(finishedFarmItem, farmItem2);
        Assertions.assertEquals(finishedFarmItem, farmItem2_0);
        Assertions.assertEquals(finishedFarmItem, farmItem2_1);
        Assertions.assertEquals(finishedFarmItem, farmItem2_2);
        Assertions.assertNull(nullFarmItem1_0);
        Assertions.assertNull(nullFarmItemOutOfBounds1);
        Assertions.assertNull(nullFarmItemOutOfBounds2);
        Assertions.assertNull(nullFarmItemOutOfBounds3);
        Assertions.assertNull(nullFarmItemOutOfBounds4);
    }

    @Test
    void testCanBePlaced() {
        boolean canBePlaced_Invalid1 = farmData.canBePlaced(0, -1, 1, 1);
        boolean canBePlaced_Invalid2 = farmData.canBePlaced(-1, 0, 2, 2);
        boolean canBePlaced_0_0 = farmData.canBePlaced(0, 0, 1, 1);
        boolean canBePlaced_1_0 = farmData.canBePlaced(1, 0, 1, 1);
        boolean canBePlacedXW_1_0 = farmData.canBePlaced(1, 0, 2, 1);
        boolean canBePlacedXW_2_0 = farmData.canBePlaced(2, 0, farmData.farmColumns, 1);
        boolean canBePlacedXXW_2_0 = farmData.canBePlaced(2, 0, farmData.farmColumns + 1, 1);

        Assertions.assertTrue(canBePlaced_1_0);
        Assertions.assertFalse(canBePlaced_Invalid1);
        Assertions.assertFalse(canBePlaced_Invalid2);
        Assertions.assertFalse(canBePlaced_0_0);
        Assertions.assertFalse(canBePlacedXW_1_0);
        Assertions.assertTrue(canBePlacedXW_2_0);
        Assertions.assertFalse(canBePlacedXXW_2_0);

        boolean canBePlaced_0_1 = farmData.canBePlaced(0, 1, 1, 1);
        boolean canBePlaced_0_2 = farmData.canBePlaced(0, 2, 1, 1);
        boolean canBePlaced_1_1 = farmData.canBePlaced(1, 1, 1, 1);
        boolean canBePlaced_1_2 = farmData.canBePlaced(1, 2, 1, 1);
        boolean canBePlacedXL_1_2 = farmData.canBePlaced(1, 2, 2, 2);
        boolean canBePlacedXH_1_0 = farmData.canBePlaced(1, 0, 1, 2);
        boolean canBePlacedXL_0_3 = farmData.canBePlaced(0, 3, 2, 2);
        boolean canBePlacedXH_0_3 = farmData.canBePlaced(0, 3, 1, farmData.farmRows);
        boolean canBePlacedXXH_0_3 = farmData.canBePlaced(0, 3, 1, farmData.farmRows + 1);

        Assertions.assertFalse(canBePlaced_0_1);
        Assertions.assertFalse(canBePlaced_0_2);
        Assertions.assertFalse(canBePlaced_1_1);
        Assertions.assertFalse(canBePlaced_1_2);
        Assertions.assertFalse(canBePlacedXL_1_2);
        Assertions.assertTrue(canBePlacedXH_1_0);
        Assertions.assertTrue(canBePlacedXL_0_3);
        Assertions.assertTrue(canBePlacedXH_0_3);
        Assertions.assertFalse(canBePlacedXXH_0_3);
    }

    @Test
    void testPlacingRegularItem() {
        boolean placed = farmData.placeItem(item, 0.0, -1, 0);
        Assertions.assertFalse(placed);

        placed = farmData.placeItem(item, 0.0, 0, 0);
        Assertions.assertFalse(placed);

        placed = farmData.placeItem(item, 0.0, 1, 0);
        Assertions.assertTrue(placed);

        FarmItem newFarmItem = farmData.items.get(1).get(0);

        FarmItem placedItem = farmData.itemAt(1, 0);
        Assertions.assertEquals(newFarmItem, placedItem);
        Assertions.assertEquals(placedItem.get().id, item.id);
    }

    @Test
    void testPlacingOversizedItem() {
        boolean placed = farmData.placeItem(finishedItem, 0.0, -2, -2);
        Assertions.assertFalse(placed);

        placed = farmData.placeItem(finishedItem, 0.0, 0, 2);
        Assertions.assertFalse(placed);

        placed = farmData.placeItem(finishedItem, 0.0, 1, 2);
        Assertions.assertFalse(placed);

        placed = farmData.placeItem(finishedItem, 0.0, 0, 3);
        Assertions.assertTrue(placed);

        FarmItem newFarmItem = farmData.items.get(0).get(3);

        FarmItem placedItem1 = farmData.itemAt(0, 3);
        FarmItem placedItem2 = farmData.itemAt(1, 4);
        Assertions.assertEquals(newFarmItem, placedItem1);
        Assertions.assertEquals(placedItem1, placedItem2);
        Assertions.assertEquals(placedItem1.get().id, finishedItem.id);
        Assertions.assertEquals(placedItem2.get().id, finishedItem.id);
    }

    @Test
    void testCanBePicked() {
        Assertions.assertNotNull(farmData.canBePicked(0, 0)); // unripe
        Assertions.assertFalse(farmData.canBePicked(0, 0).getValue()); // unripe
        Assertions.assertNull(farmData.canBePicked(1, 0)); // empty
        Assertions.assertNull(farmData.canBePicked(-1, -1)); // out of bounds
        Assertions.assertNull(farmData.canBePicked(0, -1)); // out of bounds
        Assertions.assertNull(farmData.canBePicked(-1, 0)); // out of bounds
        Assertions.assertNull(farmData.canBePicked(0, farmData.farmColumns)); // out of bounds
        Assertions.assertNull(farmData.canBePicked(farmData.farmRows, 0)); // out of bounds
        Assertions.assertNull(farmData.canBePicked(farmData.farmRows, farmData.farmColumns)); // out of bounds

        Assertions.assertEquals(finishedFarmItem, farmData.canBePicked(0, 1).getKey());
        Assertions.assertEquals(finishedFarmItem, farmData.canBePicked(0, 2).getKey());
        Assertions.assertEquals(finishedFarmItem, farmData.canBePicked(1, 1).getKey());
        Assertions.assertEquals(finishedFarmItem, farmData.canBePicked(1, 2).getKey());

        Assertions.assertTrue(farmData.canBePicked(0, 1).getValue());
        Assertions.assertTrue(farmData.canBePicked(0, 2).getValue());
        Assertions.assertTrue(farmData.canBePicked(1, 1).getValue());
        Assertions.assertTrue(farmData.canBePicked(1, 2).getValue());
    }

    @Test
    void testPickingItemAtStartCoordinates() {
        Item pickedItemMinus1 = farmData.pickItemAt(-1, -1);
        Item pickedItem1 = farmData.pickItemAt(0, 0);
        Item pickedItem2 = farmData.pickItemAt(1, 0);
        Item pickedItem3 = farmData.pickItemAt(0, 1);

        Assertions.assertNull(pickedItemMinus1);
        Assertions.assertNull(pickedItem1);
        Assertions.assertNull(pickedItem2);

        Assertions.assertEquals(finishedItem, pickedItem3);
        Assertions.assertEquals(finishedItem.id, pickedItem3.id);
        Assertions.assertEquals(finishedItem.name, pickedItem3.name);

        // Grow the 1st item so that it can be picked...
        farmItem.growth.set(1 + farmItem.getGrowthRate() * getMaxGrowthStageFor(farmItem));
        pickedItem1 = farmData.pickItemAt(0, 0);

        Assertions.assertEquals(item, pickedItem1);
        Assertions.assertEquals(item.id, pickedItem1.id);
        Assertions.assertEquals(item.name, pickedItem1.name);

        int[] treePosition = farmData.positionForItem(finishedFarmItem);
        farmData.destroyItemAt(treePosition[0], treePosition[1]);

        for (ArrayList<FarmItem> row : farmData.items) {
            for (FarmItem emptyItem : row) {
                Assertions.assertNull(emptyItem);
            }
        }
    }

    @Test
    void testPickingItemAtAnyCoordinates() {
        // Coordinates of dummy FarmItem pointing to the real oversized FarmItem...
        Item pickedItem = farmData.pickItemAt(1, 2);

        Assertions.assertEquals(finishedItem, pickedItem);
        Assertions.assertEquals(finishedItem.id, pickedItem.id);
        Assertions.assertEquals(finishedItem.name, pickedItem.name);

        // Grow the 1st item so that it can be picked...
        farmItem.growth.set(farmItem.getGrowthRate() * getMaxGrowthStageFor(farmItem));
        Assertions.assertNotNull(farmData.pickItemAt(0, 0));

        int[] treePosition = farmData.positionForItem(finishedFarmItem);
        farmData.destroyItemAt(treePosition[0], treePosition[1]);

        for (ArrayList<FarmItem> row : farmData.items) {
            for (FarmItem emptyItem : row) {
                Assertions.assertNull(emptyItem);
            }
        }
    }

    @Test
    void testPickingItem() {
        Item pickedItem0 = farmData.pickItem(null);
        Item pickedItem1 = farmData.pickItem(new FarmItem(finishedItem)); // FarmItem has not been placed onto the farm
        Item pickedItem2 = farmData.pickItem(farmItem); // FarmItem isn't fully grown
        Item pickedItem3 = farmData.pickItem(finishedFarmItem);

        Assertions.assertNull(pickedItem0);
        Assertions.assertNull(pickedItem1);
        Assertions.assertNull(pickedItem2);
        Assertions.assertNotNull(pickedItem3);

        Assertions.assertEquals(finishedItem, pickedItem3);
        Assertions.assertEquals(finishedItem.id, pickedItem3.id);
        Assertions.assertEquals(finishedItem.name, pickedItem3.name);

        int[] treePosition = farmData.positionForItem(finishedFarmItem);
        farmData.destroyItemAt(treePosition[0], treePosition[1]);

        int[] itemPosition = farmData.positionForItem(farmItem);
        farmData.destroyItemAt(itemPosition[0], itemPosition[1]);

        for (ArrayList<FarmItem> row : farmData.items) {
            for (FarmItem emptyItem : row) {
                Assertions.assertNull(emptyItem);
            }
        }

        Assertions.assertTrue(farmData.placeItem(finishedItem, 0.1, farmData.farmRows - 2, farmData.farmColumns - 2));
        finishedFarmItem = farmData.items.get(farmData.farmRows - 2).get(farmData.farmColumns - 2);
        treePosition = farmData.positionForItem(finishedFarmItem);
        farmData.destroyItemAt(treePosition[0], treePosition[1]);

        for (ArrayList<FarmItem> row : farmData.items) {
            for (FarmItem emptyItem : row) {
                Assertions.assertNull(emptyItem);
            }
        }
    }

    @Test
    void testExpandingFarm() {
        int expandBy = 2;

        int oldRows = farmData.getNumberOfRows();
        int expansionLevel = farmData.getExpansionLevel();

        Assertions.assertEquals(0, expansionLevel);

        farmData.expandFarm(expandBy);

        int newRows = farmData.getNumberOfRows();
        expansionLevel = farmData.getExpansionLevel();

        Assertions.assertEquals(expandBy, expansionLevel);
        Assertions.assertNotEquals(oldRows, newRows);
        Assertions.assertEquals(newRows, oldRows + expandBy);
        Assertions.assertEquals(farmData.farmRows, oldRows + expandBy);
    }

    /**
     * Gets the max growth stage of the {@link FarmItem} from the {@link PlantComponent}.
     *
     * @return Max growth stage in the form of an {@code int}.
     */
    private int getMaxGrowthStageFor(FarmItem farmItem) {
        return farmItem.get().getComponent(PlantComponent.class).maxGrowthStage;
    }

}