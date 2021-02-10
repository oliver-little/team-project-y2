package teamproject.wipeout.util;

import java.util.Comparator;
import java.util.List;

/**
 * Implements an insertion sort
 */
public class InsertionSort {

    // Adapted from: https://www.geeksforgeeks.org/insertion-sort/
    /**
     * Sorts an unsorted list using insertion sort
     * This is extremely quick for nearly sorted lists
     * 
     * @param toSort The unsorted list
     * @param comparator A comparator to compare elements of the list
     * @return The sorted list
     */
    public static <T> List<T> sort(List<T> toSort, Comparator<T> comparator) {
        int size = toSort.size();
        for (int i = 1; i < size; i++) {
            T current = toSort.get(i);
            int j = i - 1;
            while ((j >= 0) && comparator.compare(toSort.get(j), current) == 1) {
                toSort.set(j+1, toSort.get(j));
                j--;
            }
            toSort.set(j+1, current);
        }

        return toSort;
    }
}
