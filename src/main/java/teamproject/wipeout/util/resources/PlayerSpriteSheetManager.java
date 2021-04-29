package teamproject.wipeout.util.resources;

import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.player.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class PlayerSpriteSheetManager {

    private final Random random;
    private final ArrayList<String> availablePlayerSpriteSheets;

    public static void loadPlayerSpriteSheets(SpriteManager spriteManager) throws IOException {
        spriteManager.loadSpriteSheet("player/player-one-female-descriptor.json", "player/player-one-female.png");
        spriteManager.loadSpriteSheet("player/player-one-male-descriptor.json", "player/player-one-male.png");
        spriteManager.loadSpriteSheet("player/player-two-female-descriptor.json", "player/player-two-female.png");
        spriteManager.loadSpriteSheet("player/player-two-male-descriptor.json", "player/player-two-male.png");
        spriteManager.loadSpriteSheet("player/player-three-female-descriptor.json", "player/player-three-female.png");
        spriteManager.loadSpriteSheet("player/player-three-male-descriptor.json", "player/player-three-male.png");
        spriteManager.loadSpriteSheet("player/skeleton-descriptor.json", "player/skeleton.png");
    }

    public PlayerSpriteSheetManager() {
        this.random = ThreadLocalRandom.current();
        this.availablePlayerSpriteSheets = new ArrayList<String>(Arrays.asList(Player.PLAYER_SPRITESHEETS));
    }

    public String getPlayerSpriteSheet() {
        int availableCount = this.availablePlayerSpriteSheets.size();
        int randIndex = this.random.nextInt(availableCount);
        return this.availablePlayerSpriteSheets.remove(randIndex);
    }

    public Supplier<String> getPlayerSpriteSheetSupplier() {
        return () -> this.getPlayerSpriteSheet();
    }

}
