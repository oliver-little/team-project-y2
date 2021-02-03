package teamproject.wipeout.engine.component;

public class GravityComponent implements GameComponent {
    
	//acceleration due to gravity
    public float g;
    
    // acceleration due to gravity on earth
    public GravityComponent() {
    	this.g = 9.81f;
    }

    // custom gravity
    public GravityComponent(float a) {
    	this.g = a;
    }

    public String getType() {
        return "gravity";
    }
}
