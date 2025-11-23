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
public class HashMap<K, V> implements Map<K, V> {

    private class Entry {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Lista<Entry> lista;

    public HashMap() {
        lista = new Lista<>();
    }

    @Override
    public V get(K key) {
        for (int i = 0; i < lista.size(); i++) {
            Entry e = lista.get(i);
            if ((key == null && e.key == null) || (key != null && key.equals(e.key))) {
                return e.value;
            }
        }
        return null;
    }

    @Override
    public void put(K key, V value) {
        for (int i = 0; i < lista.size(); i++) {
            Entry e = lista.get(i);
            if ((key == null && e.key == null) || (key != null && key.equals(e.key))) {
                e.value = value;
                lista.set(i, e);
                return;
            }
        }
        lista.add(new Entry(key, value));
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        V val = get(key);
        return (val != null) ? val : defaultValue;
    }

    @Override
    public boolean containsKey(K key) {
        for (int i = 0; i < lista.size(); i++) {
            Entry e = lista.get(i);
            if ((key == null && e.key == null) || (key != null && key.equals(e.key))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V remove(K key) {
        for (int i = 0; i < lista.size(); i++) {
            Entry e = lista.get(i);
            if ((key == null && e.key == null) || (key != null && key.equals(e.key))) {
                lista.remove(e);
                return e.value;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        lista.clear();
    }
}
