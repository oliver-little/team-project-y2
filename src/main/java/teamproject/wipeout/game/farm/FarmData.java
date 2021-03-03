package teamproject.wipeout.game.farm;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Class used as a data container for {@code FarmEntity}
 */
public class FarmData {

    public static int FARM_ROWS = 3;
    public static int FARM_COLUMNS = 6;

    public String playerID;

    protected final ArrayList<ArrayList<Pair<Item, Double>>> items;

    /**
     * Creates an "empty farm" which is tied to a player's ID.
     *
     * @param playerID Owner's ID
     */
    public FarmData(String playerID) {
        this.playerID = playerID;

        this.items = new ArrayList<ArrayList<Pair<Item, Double>>>(FARM_ROWS);
        for (int i = 0; i < FARM_ROWS; i++) {
            ArrayList<Pair<Item, Double>> newRow = new ArrayList<>(Collections.nCopies(FARM_COLUMNS, null));
            this.items.add(newRow);
        }
    }

    /**
     * Retrieves 2D ArrayList of the Items at the farm with their growth values.
     *
     * @return 2D ArrayList of the Items at the farm
     */
    public ArrayList<ArrayList<Pair<Item, Double>>> getItems() {
        return this.items;
    }

    /**
     * Retrieves ArrayList of the Items with their growth values in a given row.
     *
     * @param row Row with the Items
     * @return ArrayList of the Items in the given row
     */
    public ArrayList<Pair<Item, Double>> getItemsInRow(int row) {
        return this.items.get(row);
    }

    /**
     * Retrieves the Item with its growth values in a given row and column.
     *
     * @param row Row of the Item
     * @param column Column of the Item
     * @return Item in the given row and column
     */
    public Pair<Item, Double> itemAt(int row, int column) {
        Pair<Item, Double> item = this.items.get(row).get(column);
        if (item != null && item.getKey() == null) {
            String[] coordinates = item.getValue().toString().split("[.]");
            int actualRow = Integer.parseInt(coordinates[0]);
            int actualColumn = Integer.parseInt(coordinates[1]);
            return this.itemAt(actualRow, actualColumn);
        }
        return item;
    }

    /**
     * Checks if a given "square" at the farm is empty.
     *
     * @param row Row of the "square"
     * @param column Column of the "square"
     * @param w Item width
     * @param h Item height
     * @return {@code true} if the "square" is empty, <br> otherwise {@code false}
     */
    public boolean canBePlaced(int row, int column, int w, int h) {
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
     * Puts a given Item on a given "square" defined by the row and column.
     *
     * @param item {@link Item} to be stored
     * @param row Row of the "square"
     * @param column Column of the "square"
     *
     */
    public boolean placeItem(Item item, int row, int column) {
        PlantComponent plant = item.getComponent(PlantComponent.class);
        int plantWidth = plant.width;
        int plantHeight= plant.height;
        if (!this.canBePlaced(row, column, plantWidth, plantHeight)) {
            return false;
        }
        this.items.get(row).set(column, new Pair<Item, Double>(item, 0.0));

        if (plantWidth > 1 || plantHeight > 1) {
            this.fillSquares(true, row, column, plantWidth, plantHeight);
        }

        return true;
    }

    /**
     * Checks if an Item at the farm can be picked.
     *
     * @param row Row of the Item
     * @param column Column of the Item
     * @return {@code true} if the Item can be picked, <br> otherwise {@code false}
     */
    public boolean canBePicked(int row, int column) {
        Pair<Item, Double> checkingItem = this.itemAt(row, column);
        if (checkingItem == null) {
            return false;
        }

        double growthRate = checkingItem.getKey().getComponent(PlantComponent.class).growthRate;
        double growth = checkingItem.getValue();

        return growth >= (RowGrowthComponent.GROWTH_STAGES * growthRate);
    }

    /**
     * Picks(= removes) an Item from a given "square" defined by the row and column.
     *
     * @param row Row of the Item
     * @param column Column of the Item
     * @return Picked(= removed) item or {@code null} if the item cannot be picked (or it is an empty square)
     */
    public Item pickItemAt(int row, int column) {
        if (!this.canBePicked(row, column)) {
            return null;
        }
        Item pickedItem = this.items.get(row).get(column).getKey();
        this.items.get(row).set(column, null);

        PlantComponent plant = pickedItem.getComponent(PlantComponent.class);
        int plantWidth = plant.width;
        int plantHeight= plant.height;
        if (plantWidth > 1 || plantHeight > 1) {
            this.fillSquares(false, row, column, plantWidth, plantHeight);
        }

        return pickedItem;
    }

    /**
     * Picks(= removes) an Item with a given name.
     *
     * @param itemName Name of the Item
     * @return Picked(= removed) item or {@code null} if the item cannot be picked (or it is an empty square)
     */
    public Item pickItem(String itemName) {
        int rowIndex = 0;
        for (ArrayList<Pair<Item, Double>> row : this.items) {
            int columnIndex = 0;
            for (Pair<Item, Double> pair : row) {
                if (pair != null && pair.getKey() != null) {
                    if (pair.getKey().name.equals(itemName)) {
                        return this.pickItemAt(rowIndex, columnIndex);
                    }
                }
                columnIndex += 1;
            }
            rowIndex += 1;
        }
        return null;
    }

    /**
     * Fills squares with dummy values pointing to the square with the oversized item.
     *
     * @param flag {@code true} if you want to use dummy values. {@code false} to use {@code null}.
     * @param row Row of the Item
     * @param column Column of the Item
     * @param w Item width
     * @param h Item height
     */
    protected void fillSquares(boolean flag, int row, int column, int w, int h) {
        Pair<Item, Double> fillPair = null;
        if (flag) {
            String doubleCoordinates = row + "." + column;
            fillPair = new Pair<Item, Double>(null, Double.valueOf(doubleCoordinates));
        }

        for (int r = row; r < row + h; r++) {
            for (int c = column; c < column + w; c++) {
                if (r == row && c == column) {
                    continue;
                }
                this.items.get(r).set(c, fillPair);
            }
        }
    }

}
