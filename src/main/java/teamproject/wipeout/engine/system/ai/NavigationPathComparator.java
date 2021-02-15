package teamproject.wipeout.engine.system.ai;

import java.util.Comparator;

/**
 * Implementation of a comparator for NavigationPath
 */
public class NavigationPathComparator implements Comparator<NavigationPath> {
    public int compare(NavigationPath a, NavigationPath b) {
        return Double.compare(a.getCost(), b.getCost());
    }
}
