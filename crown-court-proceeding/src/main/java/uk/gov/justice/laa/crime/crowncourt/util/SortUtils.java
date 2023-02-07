package uk.gov.justice.laa.crime.crowncourt.util;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class SortUtils {

    private SortUtils() {}

    public static <T, U extends Comparable> void sortListWithComparing(List<T> t, Function<T, U> compFunction, Function<T, U> thenCompFunc, Comparator<U> comparator) {
        if (t != null ) {
            t.sort(Comparator.comparing(compFunction, comparator).thenComparing(thenCompFunc, comparator));
        }
    }

    public static <U extends Comparable> Comparator<U> getComparator() {
        return Comparator.naturalOrder();
    }
}
