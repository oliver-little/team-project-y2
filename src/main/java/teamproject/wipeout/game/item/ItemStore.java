package teamproject.wipeout.game.item;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

public class ItemStore {

    private static final String VERSION = "1.0.0";

    /**
     * Creates an ItemFile from an Item JSON file
     * 
     * @param JSONPath The relative path to the JSON file inside /resources/items/
     * @return A map containing the item id mapped to the item.
     * @throws FileNotFoundException if the filepath is invalid or the contents of the file were invalid.
     */
    public static Map<Integer, Item> getItemFileFromJSON(String JSONPath) throws FileNotFoundException {
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

        for (Item item : itemFile.items) {
            if (item.id == null) {
                throw new IllegalArgumentException("An item has an invalid/non-existent ID.");
            }
            else if (itemMap.containsKey(item.id)) {
                throw new IllegalArgumentException("Duplicate item ID found in JSON file.");
            }

            itemMap.put(item.id, item);
        }

        return itemMap;
    }
}
