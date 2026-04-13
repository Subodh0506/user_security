package com.security.security.cache;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LRUCache<K, V> extends LinkedHashMap<K, V>{

    private static final Integer cap = 7;

    LRUCache() {
        super(cap, 0.75f, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > cap;
    }

}
