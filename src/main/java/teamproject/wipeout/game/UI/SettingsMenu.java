package teamproject.wipeout.game.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;

import java.util.*;

/**
 * Class that constructs the settings/how to play screen 
 */
public class SettingsMenu {

    private static final List<String> DROPDOWN_ITEMS = List.of("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","UP","DOWN","LEFT","RIGHT");

    private final VBox menuBox;
    private final Runnable backAction;

    private final LinkedHashMap<String, KeyCode> keyBindings; //maps string describing action to a key
    private final ArrayList<ComboBox<String>> dropDowns;

    public SettingsMenu(VBox menuBox, Runnable backAction) {
        this.menuBox = menuBox;
        this.backAction = backAction;

        this.dropDowns = new ArrayList<ComboBox<String>>();
        this.keyBindings = new LinkedHashMap<String, KeyCode>();

        this.createDefaultBindings();
    }

    public Runnable getMenu() {
        return () -> this.createSettingsMenu();
    }

    public Map<String, KeyCode> getKeyBindings() {
        return this.keyBindings;
    }

    /**
     * Creates settings menu for changing key bindings
     */
    private void createSettingsMenu() {
        menuBox.getChildren().clear();

        menuBox.getChildren().add(UIUtil.createTitle("How to Play"));

        TilePane instructionsTilePane = new TilePane();
        instructionsTilePane.setMaxSize(600, 100);

        TextFlow goal = new TextFlow();
        Text goalTitle = new Text("Goal: ");
        goalTitle.setStyle("-fx-font-weight: bold");
        goal.getChildren().addAll(goalTitle, new Text("Make as much money as fast as possible!"));
        goal.getStyleClass().add("black-label");

        Label howToPlayLabel = new Label("How to Play:");
        howToPlayLabel.setStyle("-fx-font-weight: bold");
        Label moveAroundLabel = new Label("    1. Move around with the arrow keys to get to the market in the centre.");
        Label marketLabel = new Label("    2. Go close to the market and then click on it to buy seeds/tasks/potions.");
        Label selectItemLabel = new Label("    3. Rush back to your farm, select the seed you want in the hotbar with the number keys or the mouse.");
        Label plantLabel = new Label("    4. Now plant the seeds by clicking somewhere on your farm.");
        Label harvestLabel = new Label("    5. Wait for them to grow and then harvest them by pressing H and clicking on the crop.");
        Label pickUpLabel = new Label("    6. Pick up the harvested crop by standing on them and pressing X.");
        Label sellItemLabel = new Label("    7. Hurry back to the market and sell your crops to make money!");

        Label[] infoLabels = new Label[]{howToPlayLabel, moveAroundLabel, marketLabel, selectItemLabel,
                plantLabel, harvestLabel, pickUpLabel, sellItemLabel};

        for (Label infoLabel : infoLabels) {
            infoLabel.getStyleClass().add("black-label");
        }

        instructionsTilePane.getChildren().add(goal);
        instructionsTilePane.getChildren().addAll(infoLabels);

        instructionsTilePane.setTileAlignment(Pos.TOP_LEFT);
        instructionsTilePane.getStyleClass().add("tile-pane");

        menuBox.getChildren().add(instructionsTilePane);
        
        Text keyBindingTitle = UIUtil.createTitle("Key Bindings:");
        keyBindingTitle.setFont(Font.font("Kalam", 30));
        menuBox.getChildren().add(keyBindingTitle);

        TilePane tilePane = new TilePane();
        tilePane.setAlignment(Pos.TOP_CENTER);
        tilePane.setPadding(new Insets(10, 10, 10, 10));
        tilePane.setVgap(5);
        tilePane.setHgap(20);
        tilePane.setMaxSize(600, 300);

        menuBox.getChildren().add(tilePane);
        tilePane.getStyleClass().add("tile-pane");

        //create a HBox for each drop down menu (key-binding)
        for(Map.Entry<String, KeyCode> entry : keyBindings.entrySet()) {
            String action = entry.getKey();
            KeyCode code = entry.getValue();

            HBox box = new HBox();
            box.setMaxSize(180, 50);
            box.setAlignment(Pos.CENTER_RIGHT);
            box.setPrefWidth(200);

            Label label = new Label(action + ":  ");
            label.getStyleClass().add("black-label");

            ComboBox<String> dropDown = new ComboBox<String>();

            dropDown.getItems().addAll(DROPDOWN_ITEMS); //adds all possible key bindings
            dropDown.setValue(code.getName().toUpperCase()); //sets default value

            dropDowns.add(dropDown);

            dropDown.getSelectionModel().selectedItemProperty().addListener((ov, oldvalue, newvalue) -> {
                keyBindings.put(action, KeyCode.valueOf(newvalue));
                updateDisabledItems(); //greys out taken bindings
            });
            box.getChildren().addAll(label, dropDown);
            tilePane.getChildren().add(box);
            box.getStyleClass().add("keybinding-hbox");
        }
        updateDisabledItems();

        List<Pair<String, Runnable>> buttonData = Arrays.asList(new Pair<String, Runnable>("Back", backAction));

        VBox buttonBox = UIUtil.createMenu(buttonData);
        menuBox.getChildren().add(buttonBox);
    }

    /**
     * Creates default key bindings to be passed into game
     */
    private void createDefaultBindings() {
        keyBindings.put("Move left", KeyCode.LEFT);
        keyBindings.put("Move right", KeyCode.RIGHT);
        keyBindings.put("Move up", KeyCode.UP);
        keyBindings.put("Move down", KeyCode.DOWN);
        keyBindings.put("Drop", KeyCode.U);
        keyBindings.put("Pick-up", KeyCode.X);
        keyBindings.put("Destroy", KeyCode.D);
        keyBindings.put("Harvest", KeyCode.H);
    }

    /**
     * Gets key bindings which are taken
     * @return ArrayList of strings representing key bindings which are taken
     */
    private ArrayList<String> getTakenBindings() {
        ArrayList<String> result = new ArrayList<>();
        for(Map.Entry<String, KeyCode> entry : keyBindings.entrySet()){
            result.add(entry.getValue().getName().toUpperCase());
        }

        return result;
    }

    /**
     * Method to grey out items in all of the comboboxes which are already taken
     */
    private void updateDisabledItems() {
        ArrayList<String> takenBindings = getTakenBindings();
        for(ComboBox<String> d : dropDowns){
            d.setCellFactory(lv -> new ListCell<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item.toString());
                        setDisable(takenBindings.contains(item.toString()));
                    }

                }
            });
        }
    }

}
