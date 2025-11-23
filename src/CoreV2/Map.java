/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

/**
 *
 * @author verol
 */
public interface Map<K, V> {
    V get(K key);
    void put(K key, V value);
    V getOrDefault(K key, V defaultValue);
    boolean containsKey(K key);
    V remove(K key);
    void clear();
}