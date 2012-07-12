package me.slezica.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class _ {
    public static <K, V> Map<K, V> map() { return new HashMap<K, V>(); }

    public static <K, V> Map<K, V> map(Map.Entry<K, V>[] entries) {
        Map<K, V> map = map();
        for (Map.Entry<K, V> t : entries) map.put(t.getKey(), t.getValue());
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> map(K key0, V value0, Object... pairs) {
        if (pairs.length % 2 != 0)
        throw new IllegalArgumentException(missingValue(pairs[pairs.length - 1]));

        Map<K, V> map = map();
        map.put(key0, value0);

        for (int i = 0; i < pairs.length - 1; i += 2)
            map.put((K) pairs[i], (V) pairs[i + 1]);

        return map;
    }
    
    public static <T> Set<T> set() { return new HashSet<T>(); }

    public static <T> Set<T> set(T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }
    
    static String missingValue(Object key) {
        return "Missing value for key <" + key + "> in map parameters";
    }
}
