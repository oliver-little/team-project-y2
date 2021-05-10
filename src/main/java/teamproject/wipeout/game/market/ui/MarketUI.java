package teamproject.wipeout.game.market.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.util.Pair;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.game.item.components.SabotageComponent;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

/**
 * Creates a tabbed screen with items to buy and sell
 */
public class MarketUI extends AnchorPane {

    public Runnable onUIClose;

    private Pane parent;
    private List<ScrollableTileUI> pages;

    /**
     * Creates a new instance of MarketUI
     * @param items The list of items to render
     * @param market The Market instance to use
     * @param currentPlayer The current player
     * @param spriteManager A SpriteManager instance to get Market images from
     * @param purchasableTasks The list of purchasable tasks to display
     */
    public MarketUI(Collection<Item> items, Market market, CurrentPlayer currentPlayer, SpriteManager spriteManager, ArrayList<Task> purchasableTasks) {
        super();

        try {
            InputStream path = new FileInputStream(ResourceLoader.get(ResourceType.STYLESHEET, "fonts/Kalam-Regular.ttf"));
            Font.loadFont(path, 12);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "market-menu.css");

        // Tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        List<Pair<Node, String>> seedsList = new ArrayList<>();
        List<Pair<Node, String>> plantsList = new ArrayList<>();
        List<Pair<Node, String>> potionsList = new ArrayList<>();
        List<Pair<Node, String>> tasksList = new ArrayList<>();
        List<Pair<Node, String>> farmsList = new ArrayList<>();

        // Services
        for (Item item : items) {
            if (item.hasComponent(PlantComponent.class)) {
                seedsList.add(new Pair<>(new MarketItemUI(item, market, currentPlayer, spriteManager), item.name.toLowerCase()));
            }
            else if (item.hasComponent(SabotageComponent.class)) {
                potionsList.add(new Pair<>(new MarketItemUI(item, market, currentPlayer, spriteManager), item.name.toLowerCase()));
            }
            else {
                plantsList.add(new Pair<>(new MarketItemUI(item, market, currentPlayer, spriteManager), item.name.toLowerCase()));
            }
        }

        // Purchasable tasks
        for (Task purchasableTask : purchasableTasks) {
            if (currentPlayer.getCurrentAvailableTasks().containsKey(purchasableTask.id) || purchasableTask.completed) {
                continue;
            }
            Item relatedItem = purchasableTask.relatedItem;
            tasksList.add(new Pair<>(new MarketTaskUI(purchasableTask, relatedItem, market, currentPlayer, spriteManager), purchasableTask.descriptionWithoutMoney.toLowerCase()));
        }

        farmsList.add(new Pair<>(new FarmExpansionUI(currentPlayer, spriteManager), "Farm"));

        pages = new ArrayList<>();

        ScrollableTileUI page = new ScrollableTileUI(seedsList, true);
        pages.add(page);
        Tab seeds = new Tab("Seeds", page);

        page = new ScrollableTileUI(plantsList, true);
        pages.add(page);
        Tab plants = new Tab("Plants & Veg", page);

        page = new ScrollableTileUI(potionsList, true);
        pages.add(page);
        Tab potions = new Tab("Potions", page);

        page = new ScrollableTileUI(farmsList, false);
        pages.add(page);
        Tab farmExpansions = new Tab("Farm Expansions", page);

        page = new ScrollableTileUI(tasksList, true);
        pages.add(page);
        Tab tasks = new Tab("Tasks", page);
        tabPane.getTabs().addAll(seeds, plants, potions, farmExpansions, tasks);

        Button close = new Button("X");

        close.addEventFilter(MouseEvent.MOUSE_CLICKED, actionEvent -> {
            if (this.onUIClose != null) {
                this.onUIClose.run();
            }
            this.parent.getChildren().remove(this);
            actionEvent.consume();
        });

        AnchorPane.setBottomAnchor(tabPane, 120.0);
        AnchorPane.setTopAnchor(tabPane, 50.0);
        AnchorPane.setLeftAnchor(tabPane, 100.0);
        AnchorPane.setRightAnchor(tabPane, 100.0);
        AnchorPane.setTopAnchor(close, 60.0);
        AnchorPane.setRightAnchor(close, 100.0);

        this.getChildren().addAll(tabPane, close);
    }

    /**
     * Sets the parent this MarketUI is attached to
     * @param parent The Parent pane 
     */
    public void setParent(Pane parent) {
        this.parent = parent;
    }

    /**
     * Should be called when the market is shown on the screen
     */
    public void onDisplay() {
        this.pages.forEach((page) -> page.fixBlurryText());
    }

    public Parent getContent() {
        return this;
    }
}
