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
        // Java does not allow generic array creation directly, so we cast
        table = (MapEntry<K, V>[]) new MapEntry[initialCapacity];

        // Map starts empty
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

        // Prevent null keys or values
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value cannot be null");
        }

        // Check if adding one more element exceeds load factor
        // If so, resize table before inserting
        if ((size + 1) / (double) table.length >= MAX_LOAD_FACTOR) {
            resizeBackingTable(2 * table.length + 1);
        }

        // Compute starting index using key's hash code
        // Math.abs prevents negative indices
        int index = Math.abs(key.hashCode() % table.length);

        // Track the first tombstone encountered during probing
        int firstDeleted = -1;

        // Probe through table
        for (int i = 0; i < table.length; i++) {

            // Linear probing index calculation
            int probe = ((index + i) % table.length);

            // Empty slot found
            if (table[probe] == null) {

                // Prefer inserting into earlier tombstone if found
                if (firstDeleted != -1) {
                    table[firstDeleted] = new MapEntry<>(key, value);
                } else {
                    table[probe] = new MapEntry<>(key, value);
                }

                // Increase active element count
                size++;

                // New key inserted
                return null;

            } else if (table[probe].isRemoved()) {

                // Record first tombstone index for reuse
                if (firstDeleted == -1) {
                    firstDeleted = probe;
                }

            } else if (table[probe].getKey().equals(key)) {

                // Key already exists — replace value
                V oldValue = table[probe].getValue();
                table[probe].setValue(value);

                return oldValue;
            }
        }

        // If table contained tombstone but no empty slot
        // insert at first tombstone location
        table[firstDeleted] = new MapEntry<>(key, value);

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

        // Prevent null key removal
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Compute starting probe index
        int index = Math.abs(key.hashCode() % table.length);

        // Probe through table
        for (int i = 0; i < table.length; i++) {

            int probe = (index + i) % table.length;

            // If null encountered, key does not exist
            if (table[probe] == null) {
                throw new NoSuchElementException("Key not found in the map.");

            // Skip tombstones
            } else if (table[probe].isRemoved()) {
                continue;

            // Active matching key found
            } else if (table[probe].getKey().equals(key)) {

                V oldValue = table[probe].getValue();

                // Mark entry as removed instead of deleting
                table[probe].setRemoved(true);

                // Decrease active size
                size--;

                return oldValue;
            }
        }

        // Entire probe sequence searched without match
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

        // Prevent null key lookup
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Compute starting index using key's hash code
        // Math.abs prevents negative indices
        int startIndex = Math.abs(key.hashCode() % table.length);

        // Current probing index
        int index = startIndex;

        // Probe until empty slot encountered
        while (table[index] != null) {

            MapEntry<K, V> cur = table[index];

            // Return value if active entry matches
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                return cur.getValue();
            }

            // Continue linear probing
            index = (index + 1) % table.length;

            // Stop if we've looped entire table
            if (index == startIndex) {
                break;
            }
        }

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

        // Prevent null arguments
        if (key == null || defaultValue == null) {
            throw new IllegalArgumentException("Key and defaultValue cannot be null");
        }

        // Compute starting index using key's hash code
        // Math.abs prevents negative indices
        int startIndex = Math.abs(key.hashCode() % table.length);

        int index = startIndex;

        // Probe table exactly like get()
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

        // If not found return default
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
        // Prevent null key search
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Compute starting index for probing
        int startIndex = Math.abs(key.hashCode() % table.length);
        int index = startIndex;

        // Probe until null encountered
        while (table[index] != null) {

            MapEntry<K, V> cur = table[index];

            // Found matching active key
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                return true;
            }

            index = (index + 1) % table.length;

            // Stop if full loop completed
            if (index == startIndex) {
                break;
            }
        }

        // Key not found
        return false;
    }

    /**
     * Returns a set of all keys in the map in index order of the backing array,
     * skipping null and removed entries.
     *
     * @return a set of all keys in the map
     */
    public Set<K> keySet() {

        // HashSet ensures no duplicate keys
        Set<K> set = new HashSet<>();

        // Iterate entire backing array
        for (MapEntry<K, V> entry : table) {

            // Only include active entries
            if (entry != null && !entry.isRemoved()) {
                set.add(entry.getKey());
            }
        }

        return set;
    }

    /**
     * Returns a list of all values in the map in index order of the backing array,
     * skipping null and removed entries. Duplicate values are allowed.
     *
     * @return a list of all values in the map
     */
    public List<V> values() {

        // ArrayList preserves traversal order
        List<V> list = new ArrayList<>();

        // Traverse backing table
        for (MapEntry<K, V> entry : table) {

            // Include only active entries
            if (entry != null && !entry.isRemoved()) {
                list.add(entry.getValue());
            }
        }

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

        // Prevent shrinking table below current number of items
        if (length < size) {
            throw new IllegalArgumentException("Length cannot be less than number of items in the map");
        }

        // Create new table
        MapEntry<K, V>[] newTable = new MapEntry[length];

        // Rehash every active entry
        for (int i = 0; i < table.length; i++) {

            if (table[i] != null && !table[i].isRemoved()) {

                // Compute new index based on new table size
                int index = Math.abs(table[i].getKey().hashCode() % length);

                // Linear probe into new table
                for (int j = 0; j < length; j++) {

                    int probeIndex = (index + j) % length;

                    if (newTable[probeIndex] == null) {
                        newTable[probeIndex] = table[i];
                        break;
                    }
                }
            }
        }

        // Replace old table
        table = newTable;
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

        // Prevent null arguments
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value cannot be null");
        }

        // Compute starting index using key's hash code
        // Math.abs prevents negative indices
        int startIndex = Math.abs(key.hashCode() % table.length);

        // Track the first tombstone encountered during probing
        int firstRemovedIndex = -1;
        int index = startIndex;

        // Probe through table
        for (int probes = 0; probes < table.length; probes++) {

            MapEntry<K, V> cur = table[index];

            // Empty slot found
            if (cur == null) {

                int insertIndex = (firstRemovedIndex != -1) ? firstRemovedIndex : index;

                table[insertIndex] = new MapEntry<>(key, value);

                return;
            }

            // Replace value if key already exists
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                cur.setValue(value);
                return;
            }

            // Track first tombstone
            if (cur.isRemoved() && firstRemovedIndex == -1) {
                firstRemovedIndex = index;
            }

            index = (index + 1) % table.length;
        }

        // Insert into tombstone if one exists
        if (firstRemovedIndex != -1) {
            table[firstRemovedIndex] = new MapEntry<>(key, value);
            return;
        }

        // Table full edge case
        throw new IllegalStateException("HashMap backing table is full during putNoResize.");
    }

    /**
     * Clears the map by resetting the backing array to initial capacity and size to 0.
     */
    public void clear() {

        // Allocate new empty table
        table = new MapEntry[INITIAL_CAPACITY];

        // Reset size
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

        // Return custom iterator
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

            // If we haven't returned size active elements yet
            return seen < size;
        }

        /**
         * Returns the next key in the iteration. If there are no more elements, throws a NoSuchElementException.
         * @return the next key in the iteration
         * @throws NoSuchElementException if there are no more elements to return
         */
        @Override
        public K next() {

            // If iterator exhausted
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements");
            }

            // Skip null entries and tombstones
            while (index < table.length
                    && (table[index] == null || table[index].isRemoved())) {
                index++;
            }

            // Defensive bounds check
            if (index >= table.length) {
                throw new NoSuchElementException("No more elements");
            }

            // Retrieve key at current index
            K key = table[index].getKey();

            // Advance iterator state
            index++;
            seen++;

            return key;
        }
    }
}