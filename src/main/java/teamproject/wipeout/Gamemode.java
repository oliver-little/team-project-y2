package teamproject.wipeout;

/**
 * An enum for each gamemode implemented in the game
 */
public enum Gamemode
{
	TIME_MODE("Time Mode"), 
	WEALTH_MODE("Wealth Mode");
	
	private String name;
	
	Gamemode(String name) {
		this.name=name;
	}
	
	public String toString() {
		return name;
	}
}
