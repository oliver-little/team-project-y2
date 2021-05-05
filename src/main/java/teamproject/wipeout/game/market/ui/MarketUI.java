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

    public MarketUI(
            Collection<Item> items,
            Market market,
            CurrentPlayer currentPlayer,
            SpriteManager spriteManager,
            ArrayList<Task> purchasableTasks
    ) {
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
                seedsList.add(new Pair<>(new MarketItemUI(item, market, currentPlayer, spriteManager), item.name));
            }
            else if (item.hasComponent(SabotageComponent.class)) {
                potionsList.add(new Pair<>(new MarketItemUI(item, market, currentPlayer, spriteManager), item.name));
            }
            else {
                plantsList.add(new Pair<>(new MarketItemUI(item, market, currentPlayer, spriteManager), item.name));
            }
        }

        // Purchasable tasks
        for (Task purchasableTask : purchasableTasks) {
            if (currentPlayer.getCurrentAvailableTasks().containsKey(purchasableTask.id) || purchasableTask.completed) {
                continue;
            }
            Item relatedItem = purchasableTask.relatedItem;
            tasksList.add(new Pair<>(new MarketTaskUI(purchasableTask, relatedItem, market, currentPlayer, spriteManager), purchasableTask.description));
        }

        farmsList.add(new Pair<>(new FarmExpansionUI(currentPlayer, spriteManager), "Farm"));

        Tab seeds = new Tab("Seeds", new ScrollableTileUI(seedsList, true));
        Tab plants = new Tab("Plants & Veg", new ScrollableTileUI(plantsList, true));
        Tab potions = new Tab("Potions", new ScrollableTileUI(potionsList, true));
        Tab farmExpansions = new Tab("Farm Expansions", new ScrollableTileUI(farmsList, false));
        Tab tasks = new Tab("Tasks", new ScrollableTileUI(tasksList, true));
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

    public void setParent(Pane parent) {
        this.parent = parent;
    }

    public Parent getContent() {
        return this;
    }
}
