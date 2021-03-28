package teamproject.wipeout.game.farm;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.networking.state.FarmState;
import teamproject.wipeout.networking.state.StateUpdatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Class used as a data container for a {@code FarmEntity}.
 */
public class FarmData implements StateUpdatable<FarmState> {

    /** Number of rows on any farm */
    public int farmRows = 3;
    /** Number of columns on any farm */
    public int farmColumns = 6;

    public final Integer farmID;
    public final Integer playerID;
    public final double TREE_GROWTH_HARVEST_PERCENTAGE = 0.75; //Sets the growth percentage to return a tree to when harvested.

    private double growthMultiplier;
    private double AIMultiplier;

    protected final ArrayList<ArrayList<FarmItem>> items;

    private final ItemStore itemStore;

    private final HashSet<Consumer<FarmItem>> customGrowthDelegates;
    private final Consumer<FarmItem> growthDelegate;

    /**
     * Creates a data container for an empty farm.
     * Data are tied to a player via playerID.
     *
     * @param playerID Player's ID
     */
    public FarmData(Integer farmID, Integer playerID, ItemStore itemStore) {
        this.farmID = farmID;
        this.playerID = playerID;

        this.growthMultiplier = 1.0;
        this.AIMultiplier = 1.0;

        this.items = new ArrayList<ArrayList<FarmItem>>(this.farmRows);
        for (int i = 0; i < this.farmRows; i++) {
            ArrayList<FarmItem> newRow = new ArrayList<>(Collections.nCopies(this.farmColumns, null));
            this.items.add(newRow);
        }

        this.itemStore = itemStore;

        this.customGrowthDelegates = new HashSet<Consumer<FarmItem>>();
        this.growthDelegate = (farmItem) -> {
            for (Consumer<FarmItem> customDelegate : this.customGrowthDelegates) {
                customDelegate.accept(farmItem);
            }
        };
    }

    public double getGrowthMultiplier() {
        return this.growthMultiplier;
    }

    public double getAIMultiplier() {
        return this.AIMultiplier;
    }

    public void setGrowthMultiplier(double growthMultiplier) {
        this.growthMultiplier = growthMultiplier;
    }

    public void setAIMultiplier(double AIMultiplier) {
        this.AIMultiplier = AIMultiplier;
    }

    /**
     * Gets the current state of the farm.
     *
     * @return Current {@link FarmState}
     */
    public FarmState getCurrentState() {
        return new FarmState(this.farmID, this.items, this.growthMultiplier, this.AIMultiplier);
    }

    /**
     * Updates the farm based on a given {@link FarmState}.
     *
     * @param farmState New state of the farm
     */
    public void updateFromState(FarmState farmState) {
        this.growthMultiplier = farmState.getGrowthMultiplier();
        this.AIMultiplier = farmState.getAIMultiplier();

        List<List<Pair<Integer, Double>>> newItems = farmState.getItems();
        for (int r = 0; r < this.items.size(); r++) {
            for (int c = 0; c < this.items.get(r).size(); c++) {

                FarmItem currentFarmItem = this.items.get(r).get(c);
                Pair<Integer, Double> newItemPair = newItems.get(r).get(c);

                if (currentFarmItem != null) {
                    Item currentItem = currentFarmItem.get();

                    if (currentItem != null && newItemPair != null && currentItem.id == newItemPair.getKey()) {
                        currentFarmItem.growth = newItemPair.getValue();
                        continue;
                    } else if (currentItem != null) {
                        this.destroyItemAt(r, c);
                    }
                }

                if (newItemPair != null) {
                    this.placeItem(this.itemStore.getItem(newItemPair.getKey()), newItemPair.getValue(), r, c);
                }
            }
        }
    }

    /**
     * @return {@code int} number of rows of the farm.
     */
    public int getNumberOfRows() {
        return this.items.size();
    }

    /**
     * Retrieves 2D {@code ArrayList} of all {@code FarmItem}s at the farm.
     *
     * @return 2D {@code ArrayList} of all {@code FarmItem}s
     */
    public ArrayList<ArrayList<FarmItem>> getItems() {
        return this.items;
    }

    /**
     * Retrieves {@code ArrayList} of all {@code FarmItem}s in a given row.
     *
     * @param row Row with the Items
     * @return {@code ArrayList} of all {@code FarmItem}s in the given row
     */
    public ArrayList<FarmItem> getItemsInRow(int row) {
        return this.items.get(row);
    }

    /**
     * Retrieves the {@code FarmItem} from a given row and column.
     *
     * @param row Row of the {@code FarmItem}
     * @param column Column of the {@code FarmItem}
     * @return {@link FarmItem} in the given row and column
     */
    public FarmItem itemAt(int row, int column) {
        if (this.areCoordinatesInvalid(row, column)) {
            return null;
        }
        FarmItem item = this.items.get(row).get(column);

        // Case when a FarmItem point to the FarmItem which contains the "real item"
        // -> used only for oversized items
        int[] actualPosition = this.getFarmPosition(item);
        if (actualPosition != null) {
            return this.itemAt(actualPosition[0], actualPosition[1]);
        }

        return item;
    }

    /**
     * Gives a row and column of a given item.
     *
     * @param item {@link FarmItem} whose position we want to know.
     * @return {@code int[]} with the item's position (x at int[0], y at int[1]).
     * If the item is not on the farm, {@code null} is returned.
     */
    public int[] positionForItem(FarmItem item) {
        int rowIndex = 0;
        for (ArrayList<FarmItem> row : this.items) {
            int columnIndex = row.indexOf(item);
            if (columnIndex < 0) {
                rowIndex += 1;
                continue;
            }
            return new int[]{rowIndex, columnIndex};
        }
        return null;
    }

    /**
     * Checks if a given position at the farm is empty / an item can be placed on it.
     *
     * @param row Farm row
     * @param column Farm column
     * @param w Item width
     * @param h Item height
     * @return {@code true} if the position is empty, <br> otherwise {@code false}.
     */
    public boolean canBePlaced(int row, int column, int w, int h) {
        if (this.areCoordinatesInvalid(row, column)) {
            return false;
        }
        int rowMax = row + h;
        int columnMax = column + w;
        if (rowMax > this.farmRows || columnMax > this.farmColumns) {
            return false;
        }

        for (int r = row; r < rowMax; r++) {
            for (int c = column; c < columnMax; c++) {
                if (this.itemAt(r, c) != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Places a given {@code Item} into a given position.
     *
     * @param item {@link Item} to be placed
     * @param growth Current growth of the item
     * @param row Farm row
     * @param column Farm column
     * @return {@code true} if the {@code Item} was placed, <br> otherwise {@code false}.
     */
    public boolean placeItem(Item item, double growth, int row, int column) {
        PlantComponent plant = item.getComponent(PlantComponent.class);
        int plantWidth = plant.width;
        int plantHeight= plant.height;
        if (!this.canBePlaced(row, column, plantWidth, plantHeight)) {
            return false;
        }
        FarmItem placingItem = new FarmItem(item);
        placingItem.growth = growth;

        // Handles oversized items
        if (plantWidth > 1 || plantHeight > 1) {
            this.fillSquares(placingItem, row, column, plantWidth, plantHeight);
        } else {
            this.items.get(row).set(column, new FarmItem(item));
        }

        return true;
    }

    /**
     * Destroys item at a given farm row and column.
     *
     * @param row Farm row
     * @param column Farm column
     */
    public void destroyItemAt(int row, int column) {
        FarmItem farmItem = this.itemAt(row, column);
        if (farmItem == null) {
            return;
        }

        Item pickedItem = farmItem.get();

        PlantComponent plant = pickedItem.getComponent(PlantComponent.class);

        // Handles oversized items
        int plantWidth = plant.width;
        int plantHeight= plant.height;
        if (plantWidth > 1 || plantHeight > 1) {
            int[] actualPosition = this.getFarmPosition(this.items.get(row).get(column));
            if (actualPosition == null) {
                this.fillSquares(null, row, column, plantWidth, plantHeight);
            } else {
                this.fillSquares(null, actualPosition[0], actualPosition[1], plantWidth, plantHeight);
            }
        } else {
            this.items.get(row).set(column, null);
        }
    }

    /**
     * Checks if a {@link FarmItem} at a given position can be picked.
     *
     * @param row Row of the {@code FarmItem}
     * @param column Column of the {@code FarmItem}
     * @return {@link Pair} of {@link FarmItem} and {@code Boolean} -
     * ({@code true} if fully grown, otherwise {@code false}).
     * {@code null} if the given position is empty.
     */
    public Pair<FarmItem, Boolean> canBePicked(int row, int column) {
        FarmItem checkingItem = this.itemAt(row, column);
        if (checkingItem == null) {
            return null;
        }
        return new Pair<FarmItem, Boolean>(checkingItem, checkingItem.isFullyGrown());
    }

    /**
     * Picks(= removes) a {@code FarmItem} from a given position.
     *
     * @param row Row of the {@code FarmItem}
     * @param column Column of the {@code FarmItem}
     * @return Picked(= removed) {@link FarmItem} or {@code null} if nothing can be picked.
     */
    public Item pickItemAt(int row, int column) {
        Pair<FarmItem, Boolean> farmItem = this.canBePicked(row, column);
        if (farmItem == null || farmItem.getValue() == false) {
            return null;
        }
        Item pickedItem = farmItem.getKey().get();

        PlantComponent plant = pickedItem.getComponent(PlantComponent.class);

        if (plant.isTree) {
            // Handles trees being harvested
            int plantWidth = plant.width;
            int plantHeight= plant.height;
            if (plantWidth > 1 || plantHeight > 1) {
                double maxGrowth = plant.maxGrowthStage * plant.growthRate;
                double newGrowth = TREE_GROWTH_HARVEST_PERCENTAGE * maxGrowth;
                FarmItem newFarmTree = new FarmItem(pickedItem, newGrowth);
                int[] actualPosition = this.getFarmPosition(this.items.get(row).get(column));
                if (actualPosition == null) {
                    this.fillSquares(newFarmTree, row, column, plantWidth, plantHeight);
                } else {
                    this.fillSquares(newFarmTree, actualPosition[0], actualPosition[1], plantWidth, plantHeight);
                }
            } else {
                throw new NoSuchElementException("Harvesting failed: A tree was attempted to be harvested, but had incorrect height and width parameters.");
            }
        }
        else {
            // Handles oversized items
            this.destroyItemAt(row, column);
        }

        return pickedItem;
    }

    /**
     * Picks(= removes) a given {@code FarmItem}.
     *
     * @param pickItem {@link FarmItem} to be picked
     * @return Picked(= removed) {@link Item} or {@code null} if nothing can be picked.
     */
    public Item pickItem(FarmItem pickItem) {
        if (pickItem == null) {
            return null;
        }

        int rowIndex = 0;
        for (ArrayList<FarmItem> row : this.items) {
            int columnIndex = row.indexOf(pickItem);
            if (columnIndex < 0) {
                rowIndex += 1;
                continue;
            }
            return this.pickItemAt(rowIndex, columnIndex);
        }

        return null;
    }

    public void expandFarm(int expandBy) {
        this.farmRows += expandBy;
        this.farmColumns += expandBy;

        switch (this.farmID) {
            case 1:
            case 3:
                for (ArrayList<FarmItem> row : this.items) {
                    row.add(0, null);
                }
                break;
            default:
                for (ArrayList<FarmItem> row : this.items) {
                    row.add(null);
                }
                break;
        }

        ArrayList<FarmItem> newRow = new ArrayList<>(Collections.nCopies(this.farmColumns, null));
        switch (this.farmID) {
            case 1:
            case 2:
                this.items.add(0, newRow);
                break;
            default:
                this.items.add(newRow);
                break;
        }
    }

    /**
     * Gets the growth delegate.
     *
     * @return {@link Consumer<FarmItem>}
     */
    public Consumer<FarmItem> getGrowthDelegate() {
        return this.growthDelegate;
    }

    /**
     * Adds a given action to the growth delegates.
     * The action will be called when any item changes its growth value.
     *
     * @param customGrowthDelegate Action to be called when any item changes its growth value.
     */
    public void addGrowthDelegate(Consumer<FarmItem> customGrowthDelegate) {
        this.customGrowthDelegates.add(customGrowthDelegate);
    }

    /**
     * Removes a given action from the growth delegates.
     *
     * @param customGrowthDelegate Action to be removed
     */
    public void removeGrowthDelegate(Consumer<FarmItem> customGrowthDelegate) {
        this.customGrowthDelegates.remove(customGrowthDelegate);
    }

    /**
     * Validates whether a given coordinates are within the farm's range.
     *
     * @param row Row to be validated
     * @param column Column To be validated
     * @return {@code true} if the coordinates are valid, <br> otherwise {@code false}.
     */
    protected boolean areCoordinatesInvalid(int row, int column) {
        boolean lessThanZero = row < 0 || column < 0;
        boolean lessThanBoundary = row < this.farmRows && column < this.farmColumns;
        return lessThanZero || !lessThanBoundary;
    }

    /**
     * Fills farm positions with a given oversized {@code FarmItem}
     * and dummy {@code FarmItem} values pointing to the position with the oversized {@code FarmItem}.
     * You can give {@code FarmItem null} argument to clear positions from an oversized {@code FarmItem}.
     *
     * @param fillItem Oversized {@link FarmItem} to be placed. Or {@code null} to clear positions.
     * @param row Start row of the oversized {@code FarmItem}
     * @param column Start column of the oversized {@code FarmItem}
     * @param w Oversized {@code FarmItem}'s width
     * @param h Oversized {@code FarmItem}'s height
     */
    private void fillSquares(FarmItem fillItem, int row, int column, int w, int h) {
        FarmItem dummyItem = null;
        if (fillItem != null) {
            // Encodes the oversized FarmItem coordinates as a double value ("x.y")
            // which is then stored as a growth value.
            String doubleCoordinates = row + "." + column;
            dummyItem = new FarmItem(null, Double.valueOf(doubleCoordinates));
        }

        for (int r = row; r < row + h; r++) {
            for (int c = column; c < column + w; c++) {
                if (r == row && c == column) {
                    this.items.get(r).set(c, fillItem);
                    continue;
                }
                this.items.get(r).set(c, dummyItem);
            }
        }
    }

    /**
     * Gets the true position for an oversized {@code FarmItem} from a given dummy value.
     * If the given (dummy) {@code FarmItem} does not point to the (true) oversized {@code FarmItem},
     * {@code null} is returned.
     *
     * @param item (dummy) {@link FarmItem}
     * @return The oversized {@code FarmItem} or {@code null}.
     */
    private int[] getFarmPosition(FarmItem item) {
        if (item != null && item.get() == null) {
            String[] coordinates = Double.toString(item.growth).split("[.]");
            int actualRow = Integer.parseInt(coordinates[0]);
            int actualColumn = Integer.parseInt(coordinates[1]);
            return new int[]{actualRow, actualColumn};
        }
        return null;
    }

}
