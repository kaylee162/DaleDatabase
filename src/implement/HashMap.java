package implement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Your implementation of a Linear Probing HashMap. Must implement {@link Iterable}.
 */
public class HashMap<K, V> implements Iterable<K> {


    /**
     * The initial capacity of the LinearProbingHashMap when created with the
     * default constructor.
     *
     * DO NOT MODIFY THIS VARIABLE!
     */
    public static final int INITIAL_CAPACITY = 13;

    /**
     * The max load factor of the LinearProbingHashMap
     *
     * DO NOT MODIFY THIS VARIABLE!
     */
    public static final double MAX_LOAD_FACTOR = 0.67;

    private MapEntry<K, V>[] table;
    private int size;

    /**
     * Constructs a new Linear Probing HashMap.
     *
     * The backing array should have an initial capacity of {@code INITIAL_CAPACITY}.
     *
     * Use constructor chaining.
     */
    public HashMap() {
        this(INITIAL_CAPACITY);
    }

    /**
     * Constructs a new LinearProbingHashMap.
     *
     * The backing array should have an initial capacity of initialCapacity.
     *
     * You may assume initialCapacity will always be positive.
     *
     * @param initialCapacity the initial capacity of the backing array
     */
    public HashMap(int initialCapacity) {
        table = (MapEntry<K, V>[]) new MapEntry[initialCapacity];
        size = 0;
    }

    /**
     * Adds the given key-value pair to the map. If an entry in the map
     * already has this key, replace the entry's value with the new one
     * passed in.
     *
     * In the case of a collision, use linear probing as your resolution
     * strategy. See the PDF for more instructions on edge cases and resizing.
     *
     * If a value was updated, return the old value; otherwise, return null.
     *
     * @param key   the key to add
     * @param value the value to add
     * @return null if the key was not already in the map. If it was in the
     * map, return the old value associated with it
     * @throws IllegalArgumentException if key or value is null
     */
    public V put(K key, V value) {
        // error handling for null key or value
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value cannot be null");
        }

        // resize before inserting (based on size + 1)
        if ((size + 1.0) / table.length > MAX_LOAD_FACTOR) {
            resizeBackingTable(2 * table.length + 1);
        }

        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;

        int firstRemovedIndex = -1;
        int index = startIndex;

        // Probe until we hit a null (end of cluster)
        while (table[index] != null) {
            MapEntry<K, V> cur = table[index];

            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                // Key exists: update value, return old
                V oldValue = cur.getValue();
                cur.setValue(value);
                return oldValue;
            }

            if (cur.isRemoved() && firstRemovedIndex == -1) {
                // Remember the first tombstone spot, but keep probing
                firstRemovedIndex = index;
            }

            index = (index + 1) % table.length;

            // Safety: full loop (shouldn't happen if resizing is correct, but prevents infinite loop)
            if (index == startIndex) {
                break;
            }
        }

        // Insert into first tombstone if it exists; otherwise into the null slot
        int insertIndex = (firstRemovedIndex != -1) ? firstRemovedIndex : index;
        table[insertIndex] = new MapEntry<>(key, value);
        size++;
        return null;
    }

    /**
     * Removes the entry with a matching key from map by marking the entry as
     * removed.
     *
     * @param key the key to remove
     * @return the value previously associated with the key
     * @throws IllegalArgumentException if key is null
     * @throws NoSuchElementException   if the key is not in the map
     */
    public V remove(K key) {
        // error handling for null key
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;
        int index = startIndex;

        while (table[index] != null) {
            MapEntry<K, V> cur = table[index];

            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                V oldValue = cur.getValue();
                cur.setRemoved(true);
                size--;
                return oldValue;
            }

            index = (index + 1) % table.length;
            if (index == startIndex) {
                break;
            }
        }

        throw new NoSuchElementException("Key not found in the map");
    }

    /**
     * Gets the value associated with the given key.
     *
     * @param key the key to search for in the map
     * @return the value associated with the given key
     * @throws IllegalArgumentException if key is null
     * @throws NoSuchElementException   if the key is not in the map
     */
    public V get(K key) {
        // error handling for null key
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;
        int index = startIndex;

        while (table[index] != null) {
            MapEntry<K, V> cur = table[index];

            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                return cur.getValue();
            }

            index = (index + 1) % table.length;
            if (index == startIndex) {
                break;
            }
        }

        throw new NoSuchElementException("Key not found in the map");
    }

    /**
     * Gets the value associated with the given key, or returns a provided default value.
     * 
     * @param key the key to search for in the map
     * @param defaultValue the value to return if key not found
     * @return the value associated with the given key if found,
     * {@code defaultValue} otherwise
     * @throws IllegalArgumentException if key is null
     */
    public V getOrDefault(K key, V defaultValue) {
        // error handling for null key 
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;
        int index = startIndex;

        while (table[index] != null) {
            MapEntry<K, V> cur = table[index];

            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                return cur.getValue();
            }

            index = (index + 1) % table.length;
            if (index == startIndex) {
                break;
            }
        }

        return defaultValue;
    }

    /**
     * Returns whether or not the key is in the map.
     *
     * @param key the key to search for in the map
     * @return true if the key is contained within the map, false
     * otherwise
     * @throws IllegalArgumentException if key is null
     */
    public boolean containsKey(K key) {
        // error handling for null key  
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;
        int index = startIndex;

        while (table[index] != null) {
            MapEntry<K, V> cur = table[index];

            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                return true;
            }

            index = (index + 1) % table.length;
            if (index == startIndex) {
                break;
            }
        }

        return false;
    }

    /**
     * Returns a Set view of the keys contained in this map.
     *
     * Use java.util.HashSet.
     *
     * @return the set of keys in this map
     */
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (MapEntry<K, V> entry : table) {
            if (entry != null && !entry.isRemoved()) {
                set.add(entry.getKey());
            }
        }
        return set;
    }

    /**
     * Returns a List view of the values contained in this map.
     *
     * Use java.util.ArrayList or java.util.LinkedList.
     *
     * You should iterate over the table in order of increasing index and add
     * entries to the List in the order in which they are traversed.
     *
     * @return list of values in this map
     */
    public List<V> values() {
        List<V> list = new ArrayList<>();
        for (MapEntry<K, V> entry : table) {
            if (entry != null && !entry.isRemoved()) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    /**
     * Resize the backing table to length.
     *
     * Disregard the load factor for this method. So, if the passed in length is
     * smaller than the current capacity, and this new length causes the table's
     * load factor to exceed MAX_LOAD_FACTOR, you should still resize the table
     * to the specified length.
     *
     * See the PDF for more details.
     *
     * @param length new length of the backing table
     * @throws IllegalArgumentException if length is less than the
     *                                            number of items in the hash
     *                                            map
     */
    public void resizeBackingTable(int length) {
        if (length < size) {
            throw new IllegalArgumentException("Length cannot be less than number of items in the map");
        }

        MapEntry<K, V>[] oldTable = table;

        @SuppressWarnings("unchecked")
        MapEntry<K, V>[] newTable = (MapEntry<K, V>[]) new MapEntry[length];

        table = newTable;
        int oldSize = size;
        size = 0;

        // Rehash without triggering further resizes
        for (MapEntry<K, V> entry : oldTable) {
            if (entry != null && !entry.isRemoved()) {
                putNoResize(entry.getKey(), entry.getValue());
            }
        }

        size = oldSize;
    }
    /**
     * Helper method to put a key-value pair into the map without checking for resizing.
     * @param key
     * @param value
     */
    private void putNoResize(K key, V value) {
        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;
        int index = startIndex;

        while (table[index] != null) {
            index = (index + 1) % table.length;
        }

        table[index] = new MapEntry<>(key, value);
    }

    /**
     * Clears the map.
     *
     * Resets the table to a new array of the INITIAL_CAPACITY and resets the
     * size.
     *
     * Must be O(1).
     */
    public void clear() {
        //clears the map by resetting the table to a new array of the INITIAL_CAPACITY and resets the size
        @SuppressWarnings("unchecked")
        MapEntry<K, V>[] newTable = (MapEntry<K, V>[]) new MapEntry[INITIAL_CAPACITY];
        table = newTable;
        size = 0;
    }

    /**
     * Returns the table of the map.
     *
     * For grading purposes only. You shouldn't need to use this method since
     * you have direct access to the variable.
     *
     * @return the table of the map
     */
    public MapEntry<K, V>[] getTable() {
        // DO NOT MODIFY THIS METHOD!
        return table;
    }

    /**
     * Returns the size of the map.
     *
     * For grading purposes only. You shouldn't need to use this method since
     * you have direct access to the variable.
     *
     * @return the size of the map
     */
    public int size() {
        // DO NOT MODIFY THIS METHOD!
        return size;
    }

    /**
     * Returns an iterator of all keys in the HashMap. The order should be the
     * order they appear in the array from left to right.
     * <p>
     * Must be done in O(1) auxiliary space.
     *
     * @return an iterator of all keys in the HashMap
     * @implNote you may create a private inner class to implement {@link Iterator}
     */
    public Iterator<K> iterator() {
        return new HashMapIterator();
    }
   
    /**
     * Iterator over the keys in the HashMap.
     *
     * Iterates through the backing table from left to right (in increasing
     * index order) and returns only keys that correspond to non-null and
     * non-removed entries.
     *
     * This iterator uses O(1) auxiliary space and does not modify the map.
     *
     * The iteration order reflects the physical layout of the backing array,
     * not insertion order.
     */
    private class HashMapIterator implements Iterator<K> {
        private int index = 0;
        private int seen = 0;

        @Override
        public boolean hasNext() {
            return seen < size;
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements");
            }

            while (index < table.length && (table[index] == null || table[index].isRemoved())) {
                index++;
            }

            // index should now be at a valid entry
            K key = table[index].getKey();
            index++;
            seen++;
            return key;
        }
    }
}
