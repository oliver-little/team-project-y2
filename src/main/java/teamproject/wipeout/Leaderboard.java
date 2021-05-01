package teamproject.wipeout;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.util.SortByMoney;
import teamproject.wipeout.util.resources.ResourceType;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Leaderboard extends VBox {

    private static final String[] ORDINAL_STRINGS = new String[]{"1st", "2nd", "3rd", "4th"};

    private SimpleListProperty<Player> players;
    private Consumer<Long> gameModeValueAction;

    private final ListView<String> list;
    private final Comparator<Player> moneyComparator;

	public Leaderboard(SimpleListProperty<Player> players) {
        this();

        this.players = players;
        this.gameModeValueAction = null;

        players.addListener((ListChangeListener<? super Player>) (change) -> {
            if (!change.next()) {
                return;
            }

            if (change.wasAdded()) {
                this.newPlayersAdded((List<Player>) change.getAddedSubList());
            }

            this.update((List<Player>) change.getList());
        });

        this.update(this.players);
        this.newPlayersAdded(this.players);
    }

    public Leaderboard(List<Player> finalPlayers) {
        this();

        this.players = null;
        this.gameModeValueAction = null;

        this.update(finalPlayers);
    }

    public Leaderboard() {

        this.list = new ListView<String>();
        this.moneyComparator = new SortByMoney(true);

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "task-ui.css");

        this.list.setMaxWidth(180);
        this.list.setMaxHeight(100);
        this.list.setMouseTransparent( true );
        this.list.setFocusTraversable( false );
        this.list.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        this.getChildren().addAll(this.list);
    }

    public void setGameModeValueAction(Consumer<Long> gameModeValueAction) {
        this.gameModeValueAction = gameModeValueAction;
    }

	public void update(List<Player> unsortedPlayers) {
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
	}

    private void newPlayersAdded(List<Player> newPlayers) {
	    if (this.players == null) {
	        return;
        }

        // updates leaderboard when any player's money changes
        for (Player p : newPlayers) {
            p.moneyProperty().addListener((observable, oldVal, newVal) -> {
                this.update(this.players);

                if (this.gameModeValueAction != null) {
                    this.gameModeValueAction.accept(newVal.longValue());
                }
            });
        }
    }

}
