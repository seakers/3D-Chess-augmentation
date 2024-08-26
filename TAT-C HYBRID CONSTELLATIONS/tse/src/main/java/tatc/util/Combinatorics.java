package tatc.util;

import java.math.BigInteger;
import java.util.*;

/**
 * Class containing useful static methods involving combinatorics
 */
public class Combinatorics {
    /**
     * Computes all possible combinations of a given size of a list of objects
     * Code source: https://stackoverflow.com/questions/5162254/all-possible-combinations-of-an-array/32507096
     * @param values list of objects
     * @param size size of the combinations
     * @param <T> object type
     * @return the list of lists containing all the possible combinations
     */
    public static <T> List<List<T>> combination(List<T> values, int size) {

        if (0 == size) {
            return Collections.singletonList(Collections.<T> emptyList());
        }

        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<T>> combination = new LinkedList<List<T>>();

        T actual = values.iterator().next();

        List<T> subSet = new LinkedList<T>(values);
        subSet.remove(actual);

        List<List<T>> subSetCombination = combination(subSet, size - 1);

        for (List<T> set : subSetCombination) {
            List<T> newSet = new LinkedList<T>(set);
            newSet.add(0, actual);
            combination.add(newSet);
        }

        combination.addAll(combination(subSet, size));

        return combination;
    }

    /**
     * Computes the power set (set of all the subsets of a set) of a given set of objects
     * @param originalSet the original set
     * @param <T> the type of the objects in the set
     * @return all the subsets of the given set
     */
    @SuppressWarnings("unchecked")
    public static <T> ArrayList<ArrayList<T>> powerSet(ArrayList<T> originalSet) {
        ArrayList<ArrayList<T>> sets = new ArrayList<>();
        if (originalSet.isEmpty()) {
            sets.add(new ArrayList<>());
            return sets;
        } else {
            ArrayList<T> list = new ArrayList<>(originalSet);
            T head = list.get(0);
            ArrayList<T> rest = new ArrayList<>(list.subList(1, list.size()));
            Iterator var5 = powerSet(rest).iterator();

            while(var5.hasNext()) {
                ArrayList<T> set = (ArrayList)var5.next();
                ArrayList<T> newSet = new ArrayList<>();
                newSet.add(head);
                newSet.addAll(set);
                sets.add(newSet);
                sets.add(set);
            }

            return sets;
        }
    }

    /**
     * Computes the cartesian product of the given lists
     * Code source: https://stackoverflow.com/questions/714108/cartesian-product-of-arbitrary-sets-in-java/20001337
     * @param lists the list containing the lists for which to compute the cartesian product
     * @return a list containing all the cartesian products
     */
    public static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> resultLists = new ArrayList<>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<T>());
            return resultLists;
        } else {
            List<T> firstList = lists.get(0);
            List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            for (T condition : firstList) {
                for (List<T> remainingList : remainingLists) {
                    ArrayList<T> resultList = new ArrayList<T>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }

    /**
     * Computes the union of two lists
     * Code source: https://stackoverflow.com/questions/5283047/intersection-and-union-of-arraylists-in-java
     * @param list1 the first list
     * @param list2 the second list
     * @param <T> type of objects in list1 and list2
     * @return the union of list1 and list2
     */
    public static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<>(set);
    }

    /**
     * Computes the intersection of two lists
     * Code source: https://stackoverflow.com/questions/5283047/intersection-and-union-of-arraylists-in-java
     * @param list1 the first list
     * @param list2 the second list
     * @param <T> type of objects in list1 and list2
     * @return the intersection of list1 and list2
     */
    public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

    /**
     *Binomial coefficient or all combinations. This is the number of combinations of N things taken K at a time.
     * @param N number of total elements
     * @param K size of the combinations
     * @return the binomial coefficient or total number of combinations of N elements taken K at a time.
     */
    public static BigInteger binomial(final int N, final int K) {
        BigInteger ret = BigInteger.ONE;
        for (int k = 0; k < K; k++) {
            ret = ret.multiply(BigInteger.valueOf(N-k))
                    .divide(BigInteger.valueOf(k+1));
        }
        return ret;
    }
}
