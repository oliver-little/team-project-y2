package teamproject.wipeout.game.farm.ui;

import teamproject.wipeout.game.farm.FarmItem;

@FunctionalInterface
public interface FarmItemCellDelegate {
    public void harvest(FarmItem farmItem);
}
