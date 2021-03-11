package teamproject.wipeout.game.farm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.PlantComponent;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.function.Consumer;

// FarmData dimensions (FARM_ROWS and FARM_COLUMNS) must be at least:
// FARM_ROWS = 3, and
// FARM_COLUMNS = 5,
// for these tests to function properly
class FarmDataTest {

    private static final double GROWTH = 0.5;

    private static Item item;
    private static Item finishedItem;
    private static Consumer<FarmItem> customGrowthDelegate;
    private static FarmItem growthDelegateItem;

    private FarmItem farmItem;
    private FarmItem finishedFarmItem;
    private FarmItem dummyFarmItem;

    private FarmData farmData;

    @BeforeAll
    static void initialization() throws FileNotFoundException, ReflectiveOperationException {
        ItemStore itemStore = new ItemStore("items.json");
        item = itemStore.getItem(28);
        finishedItem = itemStore.getItem(43);

        Assertions.assertTrue(FarmData.FARM_ROWS >= 3);
        Assertions.assertTrue(FarmData.FARM_COLUMNS >= 5);

        customGrowthDelegate = (delegateItem) -> {
            growthDelegateItem = delegateItem;
        };
    }

    @BeforeEach
    void setUp() {
        farmItem = new FarmItem(item, GROWTH * item.getComponent(PlantComponent.class).maxGrowthStage);

        finishedFarmItem = new FarmItem(finishedItem);
        finishedFarmItem.growth = finishedFarmItem.getGrowthRate() * finishedFarmItem.getMaxGrowthStage();
        dummyFarmItem = new FarmItem(null, 0.1);

        farmData = new FarmData(1, 1, null);
        farmData.addGrowthDelegate(customGrowthDelegate);
        farmData.items.get(0).set(0, farmItem);
        farmData.items.get(0).set(1, finishedFarmItem);
        farmData.items.get(0).set(2, dummyFarmItem);
        farmData.items.get(1).set(1, dummyFarmItem);
        farmData.items.get(1).set(2, dummyFarmItem);

        Assertions.assertEquals(farmData.items.size(), FarmData.FARM_ROWS);
        Assertions.assertEquals(farmData.items.get(0).size(), FarmData.FARM_COLUMNS);

        growthDelegateItem = null;
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

        Assertions.assertFalse(farmData.areCoordinatesInvalid(FarmData.FARM_ROWS - 1, FarmData.FARM_COLUMNS - 1));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(FarmData.FARM_ROWS, FarmData.FARM_COLUMNS));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(FarmData.FARM_ROWS - 1, FarmData.FARM_COLUMNS));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(FarmData.FARM_ROWS, FarmData.FARM_COLUMNS - 1));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(FarmData.FARM_ROWS + 1, FarmData.FARM_COLUMNS));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(FarmData.FARM_ROWS, FarmData.FARM_COLUMNS + 1));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(FarmData.FARM_ROWS + 1, FarmData.FARM_COLUMNS + 1));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(FarmData.FARM_ROWS + 10, FarmData.FARM_COLUMNS - 1));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(FarmData.FARM_ROWS - 1, FarmData.FARM_COLUMNS + 10));
        Assertions.assertTrue(farmData.areCoordinatesInvalid(FarmData.FARM_ROWS + 10, FarmData.FARM_COLUMNS + 10));
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
        FarmItem nullFarmItemOutOfBounds4 = farmData.itemAt(FarmData.FARM_ROWS, FarmData.FARM_COLUMNS);

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
        boolean canBePlacedXW_2_0 = farmData.canBePlaced(2, 0, FarmData.FARM_COLUMNS, 1);
        boolean canBePlacedXXW_2_0 = farmData.canBePlaced(2, 0, FarmData.FARM_COLUMNS + 1, 1);

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
        boolean canBePlacedXH_0_3 = farmData.canBePlaced(0, 3, 1, FarmData.FARM_ROWS);
        boolean canBePlacedXXH_0_3 = farmData.canBePlaced(0, 3, 1, FarmData.FARM_ROWS + 1);

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
        boolean placed = farmData.placeItem(item, -1, 0);
        Assertions.assertFalse(placed);

        placed = farmData.placeItem(item, 0, 0);
        Assertions.assertFalse(placed);

        placed = farmData.placeItem(item, 1, 0);
        Assertions.assertTrue(placed);

        FarmItem newFarmItem = farmData.items.get(1).get(0);

        FarmItem placedItem = farmData.itemAt(1, 0);
        Assertions.assertEquals(newFarmItem, placedItem);
        Assertions.assertEquals(placedItem.get().id, item.id);
    }

    @Test
    void testPlacingOversizedItem() {
        boolean placed = farmData.placeItem(finishedItem, -2, -2);
        Assertions.assertFalse(placed);

        placed = farmData.placeItem(finishedItem, 0, 2);
        Assertions.assertFalse(placed);

        placed = farmData.placeItem(finishedItem, 1, 2);
        Assertions.assertFalse(placed);

        placed = farmData.placeItem(finishedItem, 0, 3);
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
        Assertions.assertNull(farmData.canBePicked(0, FarmData.FARM_COLUMNS)); // out of bounds
        Assertions.assertNull(farmData.canBePicked(FarmData.FARM_ROWS, 0)); // out of bounds
        Assertions.assertNull(farmData.canBePicked(FarmData.FARM_ROWS, FarmData.FARM_COLUMNS)); // out of bounds

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
    void pickItemAtStartCoordinates() {
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
        farmItem.growth = 1 + farmItem.getGrowthRate() * farmItem.getMaxGrowthStage();
        pickedItem1 = farmData.pickItemAt(0, 0);

        Assertions.assertEquals(item, pickedItem1);
        Assertions.assertEquals(item.id, pickedItem1.id);
        Assertions.assertEquals(item.name, pickedItem1.name);

        for (ArrayList<FarmItem> row : farmData.items) {
            for (FarmItem emptyItem : row) {
                Assertions.assertNull(emptyItem);
            }
        }
    }

    @Test
    void pickItemAtAnyCoordinates() {
        // Coordinates of dummy FarmItem pointing to the real oversized FarmItem...
        Item pickedItem = farmData.pickItemAt(1, 2);

        Assertions.assertEquals(finishedItem, pickedItem);
        Assertions.assertEquals(finishedItem.id, pickedItem.id);
        Assertions.assertEquals(finishedItem.name, pickedItem.name);

        // Grow the 1st item so that it can be picked...
        farmItem.growth = farmItem.getGrowthRate() * farmItem.getMaxGrowthStage();
        Assertions.assertNotNull(farmData.pickItemAt(0, 0));

        for (ArrayList<FarmItem> row : farmData.items) {
            for (FarmItem emptyItem : row) {
                Assertions.assertNull(emptyItem);
            }
        }
    }

    @Test
    void pickItem() {
        Item pickedItem0 = farmData.pickItem(null);
        Item pickedItem1 = farmData.pickItem(new FarmItem(finishedItem)); // FarmItem has not been placed onto the farm
        Item pickedItem2 = farmData.pickItem(farmItem);
        Item pickedItem3 = farmData.pickItem(finishedFarmItem);

        Assertions.assertNull(pickedItem0);
        Assertions.assertNull(pickedItem1);
        Assertions.assertNull(pickedItem2);

        Assertions.assertEquals(finishedItem, pickedItem3);
        Assertions.assertEquals(finishedItem.id, pickedItem3.id);
        Assertions.assertEquals(finishedItem.name, pickedItem3.name);

        // Grow the 1st item so that it can be picked...
        farmItem.growth = farmItem.getGrowthRate() * farmItem.getMaxGrowthStage();
        pickedItem2 = farmData.pickItem(farmItem);

        Assertions.assertEquals(item, pickedItem2);
        Assertions.assertEquals(item.id, pickedItem2.id);
        Assertions.assertEquals(item.name, pickedItem2.name);

        Assertions.assertTrue(farmData.placeItem(finishedItem, FarmData.FARM_ROWS - 2, FarmData.FARM_COLUMNS - 2));
        finishedFarmItem = farmData.items.get(FarmData.FARM_ROWS - 2).get(FarmData.FARM_COLUMNS - 2);
        finishedFarmItem.growth = finishedFarmItem.getGrowthRate() * finishedFarmItem.getMaxGrowthStage();
        Item pickedItem4 = farmData.pickItem(finishedFarmItem);

        Assertions.assertEquals(finishedItem, pickedItem4);
        Assertions.assertEquals(finishedItem.id, pickedItem4.id);
        Assertions.assertEquals(finishedItem.name, pickedItem4.name);

        for (ArrayList<FarmItem> row : farmData.items) {
            for (FarmItem emptyItem : row) {
                Assertions.assertNull(emptyItem);
            }
        }
    }

    @Test
    void testGettingGrowthDelegate() {
        Assertions.assertNotEquals(customGrowthDelegate, farmData.getGrowthDelegate());
    }

    @Test
    void testGrowthDelegateManipulation() {
        Assertions.assertNotNull(farmData.getGrowthDelegate());

        farmData.addGrowthDelegate(customGrowthDelegate);
        Assertions.assertNotNull(farmData.getGrowthDelegate());

        farmData.removeGrowthDelegate(customGrowthDelegate);
        Assertions.assertNotNull(farmData.getGrowthDelegate());
        Assertions.assertNotEquals(customGrowthDelegate, farmData.getGrowthDelegate());
    }

    @Test
    void testGrowthDelegate() {
        customGrowthDelegate.accept(farmItem);
        Assertions.assertEquals(farmItem, growthDelegateItem);

        farmData.removeGrowthDelegate(customGrowthDelegate);
        farmData.getGrowthDelegate().accept(finishedFarmItem);
        Assertions.assertEquals(farmItem, growthDelegateItem);
        Assertions.assertNotEquals(finishedFarmItem, growthDelegateItem);

        farmData.addGrowthDelegate(customGrowthDelegate);
        farmData.getGrowthDelegate().accept(finishedFarmItem);
        Assertions.assertEquals(finishedFarmItem, growthDelegateItem);
    }

}