package dev.builder.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentMultiValuedHashMap<K,V> {

    private final ConcurrentMap<K, Set<V>> map = new ConcurrentHashMap<>();

    public ConcurrentMultiValuedHashMap() {

    }

    public ConcurrentMap<K, Set<V>> asMap() {
        return map;
    }

    public Set<Map.Entry<K,Set<V>>> entrySet() {
        return map.entrySet();
    }

    public Set<V> get(final K key) {
        return Collections.unmodifiableSet(
                map.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
        );
    }

    public List<V> values(){
        return map.values().stream().flatMap(Set::stream).toList();
    }

    public Set<K> keySet(){
        return map.keySet();
    }

    public V put(final K key, final V value) {
        map.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                .add(value);
        return value;
    }

    public void remove(final K key, final V value) {
        map.computeIfPresent(key, (k, set) -> {
            set.remove(value);
            return set.isEmpty() ? null : set;
        });
    }

    public boolean containsMapping(final Object key, final Object value) {
        Set<V> valueSet = map.get(key);
        return valueSet != null && !valueSet.isEmpty() && valueSet.contains(value);
    }

    public boolean containsKey(K type) {
        return map.containsKey(type);
    }

    public void clear(){
        map.clear();
    }
}
