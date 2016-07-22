package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Counter<T> {
    final Map<T, Integer> counts = new HashMap<>();

    public void add(T t) {
        counts.merge(t, 1, Integer::sum);
    }

    public Integer remove(T t) {
        return counts.remove(t);
    }

    public Set<T> keySet() {
        return counts.keySet();
    }

    public int count(T t) {
        return counts.getOrDefault(t, 0);
    }

    public List<T> mostCommon(int n) {
        /*
          Returns the n most frequently occurring keys
         */
        return counts.entrySet().stream()
                // Sort by value in descending order.
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                // Top n.
                .limit(n)
                // Keys only.
                .map(e -> e.getKey())
                // As a list.
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        Counter<String> c = new Counter<>();
        System.out.println(c.mostCommon(1));
        String[] numbers = {"0", "1", "2", "3", "4", "5", "6", "0", "7", "0", "1", "1", "0", "7"};
        for (int i = 0; i < numbers.length; i++) {
            c.add(numbers[i]);
        }
        System.out.println(c.mostCommon(3));
    }
}