package teamproject.wipeout.game.UI;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.util.SortByMoney;
import teamproject.wipeout.util.resources.ResourceType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Leaderboard VBox that displays the players and their money in descending order
 */
public class Leaderboard extends VBox {

    private static final String[] ORDINAL_STRINGS = new String[]{"1st", "2nd", "3rd", "4th"};

    private final ListView<String> list;
    private final Comparator<Player> moneyComparator;

    private SimpleListProperty<Player> players;
    private Function<Long, Boolean> gameModeValueAction;

    private boolean gameEnded;
    private ListChangeListener<? super Player> newPlayerListener;
    private HashMap<DoubleProperty, ChangeListener<? super Number>> moneyListeners;

    /**
     * Initializes {@code Leaderboard} for the gameplay purposes.
     * The leaderboard will be updated with the changes to players' money balances.
     *
     * @param players {@code List} of (unsorted) {@link Player}s to be shown and updated in the leaderboard
     */
    public Leaderboard(SimpleListProperty<Player> players) {
        this();

        this.players = players;
        this.gameModeValueAction = null;

        this.gameEnded = false;
        this.newPlayerListener = (ListChangeListener<? super Player>) (change) -> {
            if (!change.next()) {
                return;
            }

            if (change.wasAdded()) {
                this.newPlayersAdded(change.getAddedSubList());
            }

            this.update(change.getList());
        };
        players.addListener(this.newPlayerListener);

        this.moneyListeners = new HashMap<DoubleProperty, ChangeListener<? super Number>>();

        this.update(this.players);
        this.newPlayersAdded(this.players);
    }

    /**
     * Initializes {@code Leaderboard} for the GameOverUI purposes.
     * The leaderboard will not be updated.
     *
     * @param finalPlayers {@code List} of (unsorted) {@link Player}s to be shown in the leaderboard
     */
    public Leaderboard(List<Player> finalPlayers) {
        this();

        this.players = null;
        this.gameModeValueAction = null;

        this.gameEnded = true;
        this.newPlayerListener = null;
        this.moneyListeners = null;

        this.update(finalPlayers);
    }

    /**
     * Private initializer that is used only as a part of public initializers.
     */
    private Leaderboard() {
        this.list = new ListView<String>();
        this.moneyComparator = new SortByMoney(true);

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "task-ui.css");

        this.list.setMaxWidth(180);
        this.list.setMaxHeight(100);
        this.list.setMouseTransparent(true);
        this.list.setFocusTraversable(false);
        this.list.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        this.getChildren().addAll(this.list);
    }

    /**
     * {@code gameModeValueAction} setter
     *
     * @param gameModeValueAction New {@code gameModeValueAction} value of type {@code Function<Long, Boolean>}
     */
    public void setGameModeValueAction(Function<Long, Boolean> gameModeValueAction) {
        this.gameModeValueAction = gameModeValueAction;
    }

    /**
     * Updates the leaderboard based on the given (unsorted) {@code List} of {@link Player}s.
     * Sorting is done inside this method.
     *
     * @param unsortedPlayers {@code List} of (unsorted) {@link Player}s
     * @return {@code List} of sorted {@link Player}s
     */
    public List<Player> update(List<? extends Player> unsortedPlayers) {
        List<Player> sortedPlayers = unsortedPlayers.stream().sorted(this.moneyComparator).collect(Collectors.toList());

        Platform.runLater(() -> {
            this.list.getItems().clear();

            int place = 0;
            for (Player p : sortedPlayers) {
                String playerName = p.playerName;
                Double playerMoney = p.moneyProperty().getValue();
                String playerEntry = ORDINAL_STRINGS[place++] + ": " + playerName + " " + "$" + String.format("%.2f", playerMoney);
                this.list.getItems().add(playerEntry);
            }
        });

        return sortedPlayers;
    }

    /**
     * Processes new players that has been added to the {@code players SimpleListProperty}.
     * Adds money listeners to the added players.
     *
     * @param newPlayers {@code List} of added {@link Player}s
     */
    private void newPlayersAdded(List<? extends Player> newPlayers) {
        if (this.players == null) {
            return;
        }

        // updates leaderboard when any player's money changes
        for (Player p : newPlayers) {
            ChangeListener<? super Number> moneyListener = (observable, oldVal, newVal) -> {
                this.update(this.players);

                if (!this.gameEnded && this.gameModeValueAction != null) {
                    this.gameEnded = this.gameModeValueAction.apply(newVal.longValue());
                }

                if (this.gameEnded) {
                    this.removeListeners();
                }
            };

            p.moneyProperty().addListener(moneyListener);
            this.moneyListeners.put(p.moneyProperty(), moneyListener);
        }
    }

    /**
     * Removes new player and money listeners.
     */
    private void removeListeners() {
        for (Map.Entry<DoubleProperty, ChangeListener<? super Number>> entry : this.moneyListeners.entrySet()) {
            entry.getKey().removeListener(entry.getValue());
        }
        this.players.removeListener(this.newPlayerListener);

        this.moneyListeners.clear();
    }

}
