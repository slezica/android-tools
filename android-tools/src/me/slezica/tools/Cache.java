package me.slezica.tools;

import java.util.Map;

public class Cache<T> {
    
    private int idCounter = 0;
    
    private Map<Integer, T> cache = _.map();
    
    public synchronized int put(T object) {
        cache.put(++idCounter, object);
        return idCounter;
    }
    
    public synchronized T get(int id)    { return cache.get(id);    }
    public synchronized T remove(int id) { return cache.remove(id); }
}
