package teamproject.wipeout;

/**
 * An enum for each gamemode implemented in the game
 */
public enum GameMode {
	TIME_MODE("Time Mode"), 
	WEALTH_MODE("Wealth Mode");
	
	private final String name;

	public static GameMode fromName(String name) {
		for (GameMode mode : GameMode.values()) {
			if (mode.name.equals(name)) {
				return mode;
			}
		}
		return null;
	}

	GameMode(String name) {
		this.name=name;
	}
	
	public String toString() {
		return name;
	}
}
