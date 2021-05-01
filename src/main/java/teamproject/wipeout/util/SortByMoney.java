package teamproject.wipeout.util;

import teamproject.wipeout.game.player.Player;

import java.util.Comparator;

/**
 * Custom comparator which compares money balances of {@link Player} instances.
 */
public class SortByMoney implements Comparator<Player> {

    private final boolean isAscending;

    /**
     * Default initiliazer for {@code SortByMoney}.
     *
     * @param ascending Should sort in ascending order?
     */
    public SortByMoney(boolean ascending) {
        this.isAscending = ascending;
    }

    // Used for sorting of player's money value
    public int compare(Player a, Player b) {

        double moneyDiff = a.getMoney() - b.getMoney();

        if (moneyDiff > 0.0) {
            return this.isAscending ? -1 : 1;
        } else if (moneyDiff < 0.0) {
            return this.isAscending ? 1 : -1;
        }

        return 0;
    }

}