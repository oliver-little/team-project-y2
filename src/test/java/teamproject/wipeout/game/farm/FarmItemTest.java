package teamproject.wipeout.game.farm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.PlantComponent;

import java.io.FileNotFoundException;

class FarmItemTest {

    private static final double GROWTH = 0.5;
    private static final double FINISHED_GROWTH = 100.01;

    private static Item item;
    private static Item finishedItem;
    private FarmItem farmItem;
    private FarmItem finishedFarmItem;

    @BeforeAll
    static void initialization() throws FileNotFoundException, ReflectiveOperationException {
        ItemStore itemStore = new ItemStore("items.json");
        item = itemStore.getItem(28);
        finishedItem = itemStore.getItem(43);
    }

    @BeforeEach
    void setUp() {
        farmItem = new FarmItem(item);
        farmItem.growth.set(GROWTH);

        finishedFarmItem = new FarmItem(finishedItem, FINISHED_GROWTH);
    }

    @Test
    void testGet() {
        Assertions.assertEquals(farmItem.get(), item);
        Assertions.assertEquals(farmItem.get().id, item.id);

        Assertions.assertEquals(finishedItem, finishedFarmItem.get());
        Assertions.assertEquals(finishedItem.id, finishedFarmItem.get().id);
    }

    @Test
    void testGrowthRate() {
        double expectedRate1 = item.getComponent(PlantComponent.class).growthRate;
        double expectedRate2 = finishedItem.getComponent(PlantComponent.class).growthRate;

        Assertions.assertEquals(expectedRate1, farmItem.getGrowthRate());
        Assertions.assertEquals(expectedRate2, finishedFarmItem.getGrowthRate());
    }

    @Test
    void testCurrentGrowthStage() {
        double growthRate1 = item.getComponent(PlantComponent.class).growthRate;
        int expectedStage1 = (int) (GROWTH / growthRate1);

        double growthRate2 = finishedItem.getComponent(PlantComponent.class).growthRate;
        int expectedStage2 = (int) (FINISHED_GROWTH / growthRate2);

        Assertions.assertEquals(expectedStage1, farmItem.getCurrentGrowthStage());
        Assertions.assertEquals(expectedStage2, finishedFarmItem.getCurrentGrowthStage());
    }

    @Test
    void testCurrentGrowthPercentage() {
        PlantComponent plant = item.getComponent(PlantComponent.class);
        double maxGrowth = plant.maxGrowthStage * plant.growthRate;
        int expectedPercentage = (int) ((GROWTH / maxGrowth) * 100);

        Assertions.assertEquals(expectedPercentage, farmItem.getCurrentGrowthPercentage());

        Assertions.assertEquals(100, finishedFarmItem.getCurrentGrowthPercentage());
    }

}