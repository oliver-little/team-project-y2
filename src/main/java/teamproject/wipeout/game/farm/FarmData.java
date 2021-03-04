package teamproject.wipeout.game.farm;

import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * Class used as a data container for a {@code FarmEntity}.
 */
public class FarmData {

    /** Number of rows on any farm */
    public static int FARM_ROWS = 3;
    /** Number of columns on any farm */
    public static int FARM_COLUMNS = 6;

    public Integer playerID;

    protected final ArrayList<ArrayList<FarmItem>> items;

    private Consumer<FarmItem> growthCustomDelegate;

    /**
     * Action called when any item changes its growth value.
     */
    private final Consumer<FarmItem> growthDelegate = (farmItem) -> {
        if (this.growthCustomDelegate != null) {
            this.growthCustomDelegate.accept(farmItem);
        }
    };

    /**
     * Creates a data container for an empty farm.
     * Data are tied to a player via playerID.
     *
     * @param playerID Player's ID
     */
    public FarmData(Integer playerID) {
        this.playerID = playerID;

        this.items = new ArrayList<ArrayList<FarmItem>>(FARM_ROWS);
        for (int i = 0; i < FARM_ROWS; i++) {
            ArrayList<FarmItem> newRow = new ArrayList<>(Collections.nCopies(FARM_COLUMNS, null));
            this.items.add(newRow);
        }
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
        if (rowMax > FARM_ROWS || columnMax > FARM_COLUMNS) {
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
     * @param row Farm row
     * @param column Farm column
     * @return {@code true} if the {@code Item} was placed, <br> otherwise {@code false}.
     */
    public boolean placeItem(Item item, int row, int column) {
        PlantComponent plant = item.getComponent(PlantComponent.class);
        int plantWidth = plant.width;
        int plantHeight= plant.height;
        if (!this.canBePlaced(row, column, plantWidth, plantHeight)) {
            return false;
        }
        FarmItem placingItem = new FarmItem(item);

        // Handles oversized items
        if (plantWidth > 1 || plantHeight > 1) {
            this.fillSquares(placingItem, row, column, plantWidth, plantHeight);
        } else {
            this.items.get(row).set(column, new FarmItem(item));
        }

        return true;
    }

    /**
     * Checks if a {@link FarmItem} at a given position can be picked.
     *
     * @param row Row of the {@code FarmItem}
     * @param column Column of the {@code FarmItem}
     * @return {@code true} if the {@code FarmItem} can be picked, <br> otherwise {@code false}.
     */
    public boolean canBePicked(int row, int column) {
        FarmItem checkingItem = this.itemAt(row, column);
        if (checkingItem == null) {
            return false;
        }
        return checkingItem.growth >= (RowGrowthComponent.GROWTH_STAGES * checkingItem.getGrowthRate());
    }

    /**
     * Picks(= removes) a {@code FarmItem} from a given position.
     *
     * @param row Row of the {@code FarmItem}
     * @param column Column of the {@code FarmItem}
     * @return Picked(= removed) {@link FarmItem} or {@code null} if nothing can be picked.
     */
    public Item pickItemAt(int row, int column) {
        if (!this.canBePicked(row, column)) {
            return null;
        }
        Item pickedItem = this.itemAt(row, column).get();

        // Handles oversized items
        PlantComponent plant = pickedItem.getComponent(PlantComponent.class);
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

    /**
     * Gets the growth delegate.
     *
     * @return {@link Consumer<FarmItem>}
     */
    public Consumer<FarmItem> getGrowthDelegate() {
        return this.growthDelegate;
    }

    /**
     * Sets a given action for the growth delegate.
     * The action is called when any item changes its growth value.
     *
     * @param customGrowthDelegate Action to be called when any item changes its growth value.
     */
    public void setGrowthDelegate(Consumer<FarmItem> customGrowthDelegate) {
        this.growthCustomDelegate = customGrowthDelegate;
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
        boolean lessThanBoundary = row < FARM_ROWS && column < FARM_COLUMNS;
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