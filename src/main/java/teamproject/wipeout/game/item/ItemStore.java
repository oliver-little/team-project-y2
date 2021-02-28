package teamproject.wipeout.game.item;

import com.google.gson.Gson;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Loader and container for all existing items in the game.
 */
public class ItemStore {

    private static final String VERSION = "2.0.0";

    protected Map<Integer, Item> data;

    /**
     * Initializes an {@code ItemStore} instance container by loading items from a given file.
     *
     * @param JSONPath The relative path to the JSON file inside /resources/items/
     * @throws FileNotFoundException If the filepath is invalid or the contents of the file are invalid.
     * @throws ReflectiveOperationException If the contents of the file are invalid.
     */
    public ItemStore(String JSONPath) throws FileNotFoundException, ReflectiveOperationException {
        this.data = this.getItemFileFromJSON(JSONPath);
    }

    /**
     * Gets all items in the game.
     *
     * @return {@code Map} of all items with their IDs
     */
    public Map<Integer, Item> getData() {
        return this.data;
    }

    /**
     * Gets an {@code Item} with a given ID.
     *
     * @param id Item's ID
     * @return {@link Item} with the given ID
     */
    public Item getItem(int id) {
        return this.data.get(id);
    }

    /**
     * Fills the {@code ItemsStore} with items from a given JSON file.
     * 
     * @param JSONPath The relative path to the JSON file inside /resources/items/
     * @return A map containing the item id mapped to the item.
     * @throws FileNotFoundException If the filepath is invalid or the contents of the file are invalid.
     * @throws ReflectiveOperationException If the contents of the file are invalid.
     */
    private Map<Integer, Item> getItemFileFromJSON(String JSONPath) throws FileNotFoundException, ReflectiveOperationException {
        File JSONFile = ResourceLoader.get(ResourceType.ITEM, JSONPath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(JSONFile));
        Gson gson = new Gson();
        ItemFile itemFile = gson.fromJson(bufferedReader, ItemFile.class);

        if (itemFile.fileType == null || itemFile.version == null || !itemFile.fileType.equals("ItemFile")) {
            throw new FileNotFoundException("Invalid file format.");
        }

        if (!itemFile.version.equals(VERSION)) {
            System.out.println("Version of item file is different to current version, unexpected behaviour may occur.");
        }

        Map<Integer, Item> itemMap = new HashMap<>();

        for (RawItem rawItem : itemFile.items) {
            if (rawItem.id == null) {
                throw new IllegalArgumentException("A rawItem has an invalid/non-existent ID.");
            }
            else if (itemMap.containsKey(rawItem.id)) {
                throw new IllegalArgumentException("Duplicate rawItem ID found in JSON file.");
            }

            itemMap.put(rawItem.id, rawItem.initializeRealItem());
        }
        return itemMap;
    }

}
