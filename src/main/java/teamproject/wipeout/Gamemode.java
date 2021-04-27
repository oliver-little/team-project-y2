package teamproject.wipeout;

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
