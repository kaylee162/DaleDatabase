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

    // The backing array that stores all MapEntry objects
    // This is the actual hash table
    private MapEntry<K, V>[] table;

    // The number of ACTIVE (non-removed) elements in the map
    // This does NOT count tombstones
    private int size;

    /**
     * Constructs a new Linear Probing HashMap.
     *
     * The backing array should have an initial capacity of {@code INITIAL_CAPACITY}.
     *
     * Use constructor chaining.
     */
    public HashMap() {
        // Calls the other constructor with default capacity
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
        // Create a new array of MapEntry references
        // We must cast because Java does not allow generic array creation
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

        // Disallow null keys or values
        // This prevents ambiguity and keeps behavior consistent
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value cannot be null");
        }

        // Check if inserting one more element would exceed max load factor
        // (size + 1) because we are ABOUT to insert
        if ((size + 1.0) / table.length > MAX_LOAD_FACTOR) {
            // Resize to 2n + 1 (standard strategy)
            resizeBackingTable(2 * table.length + 1);
        }

        // Compute initial index using hashCode
        // & 0x7fffffff ensures non-negative
        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;

        // Tracks first tombstone found during probing
        int firstRemovedIndex = -1;

        // Begin probing from computed index
        int index = startIndex;

        // Continue probing until we hit a null slot
        // Null means end of cluster
        while (table[index] != null) {
            MapEntry<K, V> cur = table[index];

            // If this entry is active and keys match → update
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                V oldValue = cur.getValue();
                cur.setValue(value);
                return oldValue;
            }

            // If entry is a tombstone and we haven't recorded one yet,
            // remember this index for possible insertion
            if (cur.isRemoved() && firstRemovedIndex == -1) {
                firstRemovedIndex = index;
            }

            // Linear probing: move forward by 1 (wrap around)
            index = (index + 1) % table.length;

            // Safety guard: stop if we looped entire table
            if (index == startIndex) {
                break;
            }
        }

        // Prefer inserting into first tombstone found
        // Otherwise insert into first null slot encountered
        int insertIndex = (firstRemovedIndex != -1) ? firstRemovedIndex : index;

        table[insertIndex] = new MapEntry<>(key, value);

        // Increase active element count
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

        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;
        int index = startIndex;

        // Probe until we hit null (key not present)
        while (table[index] != null) {
            MapEntry<K, V> cur = table[index];

            // Found active matching key
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                V oldValue = cur.getValue();

                // Mark as tombstone instead of deleting
                // This preserves probe chains
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
     */
    public V get(K key) {

        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;
        int index = startIndex;

        // Probe exactly like put/remove
        while (table[index] != null) {
            MapEntry<K, V> cur = table[index];

            // Only return if entry is active and matches
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

    public V getOrDefault(K key, V defaultValue) {

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

        // Instead of throwing exception, return provided default
        return defaultValue;
    }

    public boolean containsKey(K key) {

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

    public Set<K> keySet() {
        // HashSet automatically prevents duplicates
        Set<K> set = new HashSet<>();

        // Iterate through entire backing array
        for (MapEntry<K, V> entry : table) {

            // Only add active entries
            if (entry != null && !entry.isRemoved()) {
                set.add(entry.getKey());
            }
        }
        return set;
    }

    public List<V> values() {
        // ArrayList maintains traversal order
        List<V> list = new ArrayList<>();

        // Traverse in increasing index order
        for (MapEntry<K, V> entry : table) {

            // Add only active entries
            if (entry != null && !entry.isRemoved()) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    public void resizeBackingTable(int length) {

        if (length < size) {
            throw new IllegalArgumentException("Length cannot be less than number of items in the map");
        }

        // Keep reference to old table
        MapEntry<K, V>[] oldTable = table;

        @SuppressWarnings("unchecked")
        MapEntry<K, V>[] newTable = (MapEntry<K, V>[]) new MapEntry[length];

        // Replace backing array
        table = newTable;

        // Save size because we will rebuild
        int oldSize = size;

        // Temporarily reset size to reinsert correctly
        size = 0;

        // Reinsert all active entries into new table
        // This recomputes indices using new capacity
        for (MapEntry<K, V> entry : oldTable) {
            if (entry != null && !entry.isRemoved()) {
                putNoResize(entry.getKey(), entry.getValue());
            }
        }

        // Restore correct size
        size = oldSize;
    }

    // Helper method used ONLY during resizing
    // Skips load factor check to avoid recursive resize
    private void putNoResize(K key, V value) {

        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;
        int index = startIndex;

        // Simple linear probing until null slot found
        while (table[index] != null) {
            index = (index + 1) % table.length;
        }

        table[index] = new MapEntry<>(key, value);
    }

    public void clear() {

        // Allocate brand new array
        // This makes clear O(1) because we do not iterate
        @SuppressWarnings("unchecked")
        MapEntry<K, V>[] newTable = (MapEntry<K, V>[]) new MapEntry[INITIAL_CAPACITY];

        table = newTable;
        size = 0;
    }

    public MapEntry<K, V>[] getTable() {
        return table;
    }

    public int size() {
        return size;
    }

    public Iterator<K> iterator() {
        return new HashMapIterator();
    }

    private class HashMapIterator implements Iterator<K> {

        // Current index in backing array
        private int index = 0;

        // Number of valid elements returned so far
        private int seen = 0;

        @Override
        public boolean hasNext() {
            // If we have returned fewer than size active elements,
            // then more exist
            return seen < size;
        }

        @Override
        public K next() {

            if (!hasNext()) {
                throw new NoSuchElementException("No more elements");
            }

            // Advance index until we hit active entry
            while (index < table.length &&
                  (table[index] == null || table[index].isRemoved())) {
                index++;
            }

            // Extract key at current index
            K key = table[index].getKey();

            // Move forward for next call
            index++;
            seen++;

            return key;
        }
    }
}