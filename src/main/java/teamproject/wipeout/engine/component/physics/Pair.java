package teamproject.wipeout.engine.component.physics;


//https://stackoverflow.com/a/6271781
/**
 * Generic function that can hold a pair variables of any type
 *
 * @param <T> first variable
 * @param <U> second variable
 */
public class Pair<T, U>
{
	public final T first;
	public final U second;
	
    public Pair(T first, U second) {         
        this.first= first;
        this.second= second;
     }
}
