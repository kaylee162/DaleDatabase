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
        // This method adds a key-value pair to the hash map, handling collisions with linear probing.

        // error handling for null key or value
        // This is important to prevent null keys/values in the map
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

        // FIX: bounded probing so we never "break and overwrite" at startIndex
        for (int probes = 0; probes < table.length; probes++) {
            MapEntry<K, V> cur = table[index];

            // Null means end of cluster, safe place to insert
            if (cur == null) {
                int insertIndex = (firstRemovedIndex != -1) ? firstRemovedIndex : index;
                table[insertIndex] = new MapEntry<>(key, value);
                size++;
                return null;
            }

            // If this entry is active and keys match → update
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                V oldValue = cur.getValue();
                cur.setValue(value);
                return oldValue;
            }

            // If entry is a tombstone or del marker and we haven't recorded one yet,
            // remember this index for possible insertion
            if (cur.isRemoved() && firstRemovedIndex == -1) {
                firstRemovedIndex = index;
            }

            // Linear probing: move forward by 1 (wrap around)
            index = (index + 1) % table.length;
        }

        // FIX: if we scanned the whole table and found a tombstone, insert there
        if (firstRemovedIndex != -1) {
            table[firstRemovedIndex] = new MapEntry<>(key, value);
            size++;
            return null;
        }

        // FIX: extremely rare physical-full case; resize and retry safely
        resizeBackingTable(2 * table.length + 1);
        return put(key, value);
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
        // This method removes the entry with the specified key by marking it as removed (tombstone).

        // error handling for null key
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Compute initial index using hashCode
        // & 0x7fffffff ensures non-negative
        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;

        // Begin probing from computed index
        int index = startIndex;

        // Probe until we hit null (key not present)
        while (table[index] != null) {
            // Get the current entry at this index
            MapEntry<K, V> cur = table[index];

            // Found active matching key
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                // Store old value to return
                V oldValue = cur.getValue();

                // Mark as tombstone instead of deleting
                // This preserves probe chains
                cur.setRemoved(true);

                // Decrease active element count and return the old value
                size--;
                return oldValue;
            }

            // Linear probing: move forward by 1 (wrap around)
            index = (index + 1) % table.length;

            // Safety guard: stop if we looped entire table
            if (index == startIndex) {
                break;
            }
        }

        // If we reach here, the key was not found in the map
        throw new NoSuchElementException("Key not found in the map");
    }

    /**
     * Gets the value associated with the given key.
     * @param key the key to search for
     * @return the value associated with the key
     * @throws IllegalArgumentException if key is null
     * @throws NoSuchElementException if the key is not in the map
     */
    public V get(K key) {
        // This method retrieves the value associated with the specified key.

        // error handling for null key
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Compute initial index using hashCode
        // & 0x7fffffff ensures non-negative
        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;
        // Begin probing from computed index
        int index = startIndex;

        // Probe exactly like put/remove
        while (table[index] != null) {
            // Set current entry for easier access
            MapEntry<K, V> cur = table[index];

            // Only return if entry is active and matches
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                return cur.getValue();
            }

            // Linear probing: move forward by 1 (wrap around)
            index = (index + 1) % table.length;

            // Safety guard: stop if we looped entire table
            if (index == startIndex) {
                break;
            }
        }

        // If we reach here, the key was not found in the map
        throw new NoSuchElementException("Key not found in the map");
    }

    /**
     * Returns the value associated with the given key, or defaultValue if the key is not present.
     *
     * @param key the key to search for
     * @param defaultValue the value to return if the key is not present
     * @return the value mapped to key, or defaultValue if absent
     * @throws IllegalArgumentException if key is null
     */
    public V getOrDefault(K key, V defaultValue) {
        // This method retrieves the value associated with the specified key, or returns a default value if the key is not present.

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

        // Instead of throwing exception, return provided default
        return defaultValue;
    }

    /**
     * Returns true if the map contains the given key.
     *
     * @param key the key to check for
     * @return true if the key exists in the map, false otherwise
     * @throws IllegalArgumentException if key is null
     */
    public boolean containsKey(K key) {
        // This method checks if the map contains the specified key.

        // error handling for null key
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Compute initial index using hashCode
        // & 0x7fffffff ensures non-negative
        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;
        // Begin probing from computed index
        int index = startIndex;

        // Probe until we hit null (key not present)
        while (table[index] != null) {
            // Set current entry for easier access
            MapEntry<K, V> cur = table[index];

            // Only return true if entry is active and matches
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                return true; // Key found in the map
            }

            // Linear probing: move forward by 1 (wrap around)
            index = (index + 1) % table.length;

            // Safety guard: stop if we looped entire table
            if (index == startIndex) {
                break;
            }
        }

        // If we reach here, the key was not found in the map
        return false;
    }

    /**
     * Returns a set of all keys in the map in index order of the backing array,
     * skipping null and removed entries.
     *
     * @return a set of all keys in the map
     */
    public Set<K> keySet() {
        // HashSet automatically prevents duplicates
        Set<K> set = new HashSet<>();

        // Iterate through entire backing array
        for (MapEntry<K, V> entry : table) {

            // Only add active entries
            if (entry != null && !entry.isRemoved()) {
                // Add the key to the set
                set.add(entry.getKey());
            }
        }
        // Return the set of keys
        return set;
    }

    /**
     * Returns a list of all values in the map in index order of the backing array,
     * skipping null and removed entries. Duplicate values are allowed.
     *
     * @return a list of all values in the map
     */
    public List<V> values() {
        // ArrayList maintains traversal order
        List<V> list = new ArrayList<>();

        // Traverse in increasing index order
        for (MapEntry<K, V> entry : table) {

            // Add only active entries
            if (entry != null && !entry.isRemoved()) {
                // Add the value to the list
                list.add(entry.getValue());
            }
        }
        // Return the list of values
        return list;
    }

    /**
     * Resizes the backing table to the specified length. All entries in the
     * map must remain in the map and all links must be rehashed. The size of
     * the map should not change.
     *
     * @param length new length of the backing table
     * @throws IllegalArgumentException if length is less than the number of
     *                                  items in the map
     */
    public void resizeBackingTable(int length) {
        // This method resizes the backing array to the specified length and rehashes all active entries.

        // error handling for length less than current size
        if (length < size) {
            throw new IllegalArgumentException("Length cannot be less than number of items in the map");
        }

        // Keep reference to old table
        MapEntry<K, V>[] oldTable = table;

        // Create new table with specified length
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
                // Use helper method to insert without checking load factor
                putNoResize(entry.getKey(), entry.getValue());
            }
        }

        // Restore correct size
        size = oldSize;
    }

    /**
     * Inserts the given key-value pair into the backing table without resizing.
     * Used only during resizing to avoid triggering another resize.
     *
     * @param key the key to insert
     * @param value the value to insert
     * @throws IllegalArgumentException if key or value is null
     * @throws IllegalStateException if the table is full
     */
    private void putNoResize(K key, V value) {
        // error handling for null key or value
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value cannot be null");
        }

        // Compute initial index using hashCode
        // & 0x7fffffff ensures non-negative
        int startIndex = (key.hashCode() & 0x7fffffff) % table.length;

        int firstRemovedIndex = -1;
        int index = startIndex;

        // FIX: bounded probing + tombstone reuse + no infinite loop
        for (int probes = 0; probes < table.length; probes++) {
            MapEntry<K, V> cur = table[index];

            if (cur == null) {
                int insertIndex = (firstRemovedIndex != -1) ? firstRemovedIndex : index;
                table[insertIndex] = new MapEntry<>(key, value);
                return;
            }

            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                cur.setValue(value);
                return;
            }

            if (cur.isRemoved() && firstRemovedIndex == -1) {
                firstRemovedIndex = index;
            }

            index = (index + 1) % table.length;
        }

        if (firstRemovedIndex != -1) {
            table[firstRemovedIndex] = new MapEntry<>(key, value);
            return;
        }

        throw new IllegalStateException("HashMap backing table is full during putNoResize.");
    }

    /**
     * Clears the map by resetting the backing array to initial capacity and size to 0.
     */
    public void clear() {
        // Allocate brand new array
        // This makes clear O(1) because we do not iterate
        MapEntry<K, V>[] newTable = (MapEntry<K, V>[]) new MapEntry[INITIAL_CAPACITY];

        // Reset the backing array and size
        table = newTable;
        size = 0;
    }

    /**
     * Returns the backing array of the map. This is for grading purposes only.
     * @return the backing array of the map
     */
    public MapEntry<K, V>[] getTable() {
        return table;
    }

    /**
     * @return the number of active (non-removed) elements in the map
     */
    public int size() {
        return size;
    }

    /**
     * Returns an iterator over the keys in the map.
     * The iterator should return the keys in the order they appear in the backing array,
     * skipping over null and removed entries.
     *
     * @return an iterator over the keys in the map
     */
    public Iterator<K> iterator() {
        return new HashMapIterator();
    }

    /**
     * Private inner class that implements the Iterator interface for the HashMap.
     * It iterates over the keys in the backing array, skipping over null and removed entries.
     */
    private class HashMapIterator implements Iterator<K> {

        // Current index in backing array
        private int index = 0;

        // Number of valid elements returned so far
        private int seen = 0;

        /**
         * Returns true if there are more elements to iterate over, false otherwise.
         * @return true if there are more elements to iterate over, false otherwise
         */
        @Override
        public boolean hasNext() {
            // If we have returned fewer than size active elements,
            // then more exist
            return seen < size;
        }

        /**
         * Returns the next key in the iteration. If there are no more elements, throws a NoSuchElementException.
         * @return the next key in the iteration
         * @throws NoSuchElementException if there are no more elements to return
         */
        @Override
        public K next() {
            // If there are no more elements, throw exception
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements");
            }

            // Advance index until we hit active entry
            // This skips over nulls and tombstones or del markers
            while (index < table.length
                    && (table[index] == null || table[index].isRemoved())) {
                index++;
            }

            // FIX: defensive guard against out-of-bounds
            if (index >= table.length) {
                throw new NoSuchElementException("No more elements");
            }

            // Extract key at current index
            K key = table[index].getKey();

            // Move forward for next call
            index++;
            seen++;

            // Return the found key
            return key;
        }
    }
}