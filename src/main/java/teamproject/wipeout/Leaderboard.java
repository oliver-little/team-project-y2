package teamproject.wipeout;

import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import teamproject.wipeout.engine.entity.gameover.GameOverUI.SortByMoney;
import teamproject.wipeout.game.player.Player;

public class Leaderboard extends VBox
{
    private final ListView<String> list;
    private static final String[] ORDINAL_STRINGS = new String[]{"1st", "2nd", "3rd", "4th", "5th", "6th"};
    
	public Leaderboard(List<Player> players) {
			
		this.list = new ListView<>();
        list.setMaxWidth(180);
        list.setMaxHeight(100);
        list.setMouseTransparent( true );
        list.setFocusTraversable( false );
        list.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
        
        
		update(players);
		this.getChildren().addAll(list);
	}
	
	public void update(List<Player> players) {
        this.list.getItems().clear();
        players.sort(new SortByMoney().reversed());


        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            this.list.getItems().add(ORDINAL_STRINGS[i] + ": " + player.playerName + " " + "$" + String.format("%.2f",  player.moneyProperty().getValue()));
        }
	}

}
