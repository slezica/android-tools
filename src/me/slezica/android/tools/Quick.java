package me.slezica.android.tools;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

@SuppressWarnings("unchecked")
public class Quick {
    
    public static <K, V> Map<K, V> map() { return new HashMap<K, V>(); }

    public static <K, V> Map<K, V> map(Map.Entry<K, V>[] entries) {
        Map<K, V> map = map();
        for (Map.Entry<K, V> t : entries) map.put(t.getKey(), t.getValue());
        return map;
    }

    public static <K, V> Map<K, V> map(K key0, V value0, Object... pairs) {
        if (pairs.length % 2 != 0)
        throw new IllegalArgumentException(missingValue(pairs[pairs.length - 1]));

        Map<K, V> map = map();
        map.put(key0, value0);

        for (int i = 0; i < pairs.length - 1; i += 2)
            map.put((K) pairs[i], (V) pairs[i + 1]);

        return map;
    }
    
    public static <T> List<T> list() { return new ArrayList<T>(); }
    
    public static <T> Set<T> set() { return new HashSet<T>(); }

    public static <T> Set<T> set(T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }
    
    static String missingValue(Object key) {
        return "Missing value for key <" + key + "> in map parameters";
    }
    
    public static String justHttpGetMe(String url) {
        try   { return readInputStream(new URL(url).openStream()); } 
        catch (Exception e) { throw new RuntimeException(e); }
    }
    
    public static String justHttpGetMe(String url, int retries) {
        if (retries == 0) return justHttpGetMe(url);
        else try {
            return justHttpGetMe(url);
        } catch (Exception ex) { return justHttpGetMe(url, retries - 1); }
    }
    
    public static String readInputStream(InputStream i) {
        return new Scanner(i).useDelimiter("\\A").next();
    }
}
