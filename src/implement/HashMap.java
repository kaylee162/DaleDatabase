package implement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Your implementation of a Linear Probing HashMap. Must implement {@link Iterable}.
 * 
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
    // Linear probing works directly on an array because collisions are resolved
    // by searching forward through neighboring slots rather than chaining.
    private MapEntry<K, V>[] table;

    // The number of ACTIVE (non-removed) elements in the map
    // This does NOT count tombstones
    // Tracking active size separately makes size() O(1) and is also important
    // for load factor checks and iterator correctness.
    private int size;

    /**
     * Constructs a new Linear Probing HashMap.
     *
     * The backing array should have an initial capacity of {@code INITIAL_CAPACITY}.
     *
     * Use constructor chaining.
     */
    public HashMap() {
        // Constructor chaining avoids duplicating initialization logic
        // and guarantees both constructors stay consistent.
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
        // Create a new array of MapEntry references.
        // Java does not allow direct generic array creation, so a cast is required here.
        table = (MapEntry<K, V>[]) new MapEntry[initialCapacity];

        // Map starts empty, so active element count is zero.
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

        // Null keys or values are disallowed because hashing and equality checks
        // assume valid, real objects and the spec requires rejecting null input.
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value cannot be null");
        }

        // Resize before inserting if adding one more active element would exceed
        // the maximum allowed load factor.
        // This preserves expected O(1) performance by preventing the table from
        // getting too crowded.
        if ((size + 1) / (double) table.length >= MAX_LOAD_FACTOR) {
            resizeBackingTable(2 * table.length + 1);
        }

        // Compute the initial hashed index from the key.
        // Mod by table length maps the hash code into the array bounds.
        // Math.abs is used so negative hash codes do not create negative indices.
        int index = Math.abs(key.hashCode() % table.length);

        // Track the first tombstone encountered during probing.
        // We do this so we can still continue searching for an existing copy of the key,
        // but reuse the earliest removed slot if the key is not found.
        int firstDeleted = -1;

        // Probe through the table linearly until we find where the key belongs.
        // In the worst case this is O(n), though expected time is O(1) when load stays bounded.
        for (int i = 0; i < table.length; i++) {

            // Linear probing checks consecutive positions, wrapping around with modulo.
            int probe = ((index + i) % table.length);

            // A null slot means the probe chain ends here.
            // Since nothing was ever inserted past this point in this cluster,
            // the key does not already exist later in the sequence.
            if (table[probe] == null) {

                // If we previously found a tombstone, reuse that earlier slot instead of
                // the later null slot. This helps keep clusters shorter.
                if (firstDeleted != -1) {
                    table[firstDeleted] = new MapEntry<>(key, value);
                } else {
                    table[probe] = new MapEntry<>(key, value);
                }

                // Count only active entries, so insertion increases size by one.
                size++;

                // Returning null signals this was a brand-new key rather than a replacement.
                return null;

            } else if (table[probe].isRemoved()) {

                // Tombstones cannot stop the search because the actual key might appear later
                // in the same probe chain.
                // We only remember the first tombstone because linear probing should reuse
                // the earliest available removed slot.
                if (firstDeleted == -1) {
                    firstDeleted = probe;
                }

            } else if (table[probe].getKey().equals(key)) {

                // If we find an active entry with the same key, this is an update.
                // Replace the value in place instead of inserting a duplicate key.
                V oldValue = table[probe].getValue();
                table[probe].setValue(value);

                // Return the old value as required by the map contract.
                return oldValue;
            }
        }

        // If we never found a null slot but did find a tombstone,
        // we can still insert into that tombstone location.
        // This handles the case where the table has no true nulls in the probe sequence
        // but still has removed entries available for reuse.
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

        // Null keys are invalid because probing depends on hashing a real key.
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Start probing from the key's hashed home position.
        int index = Math.abs(key.hashCode() % table.length);

        // Probe exactly as insertion/search would, because linear probing requires
        // following the same cluster path to find the key.
        for (int i = 0; i < table.length; i++) {

            int probe = (index + i) % table.length;

            // Hitting a null means the key is definitely not present.
            // In linear probing, a null ends the cluster.
            if (table[probe] == null) {
                throw new NoSuchElementException("Key not found in the map.");

            // Tombstones must be skipped, not treated as the end of the search,
            // because valid entries may lie further along the probe chain.
            } else if (table[probe].isRemoved()) {
                continue;

            // Found the active matching entry.
            } else if (table[probe].getKey().equals(key)) {

                V oldValue = table[probe].getValue();

                // Mark as removed instead of setting to null.
                // This preserves the probe chain for other keys that may have collided
                // and been placed later in the cluster.
                table[probe].setRemoved(true);

                // Removing an active entry decreases active size.
                size--;

                return oldValue;
            }
        }

        // If we probed the full table and never found the key, it is not present.
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

        // Reject null keys immediately to match the expected contract.
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Compute the starting slot from the hash code.
        int startIndex = Math.abs(key.hashCode() % table.length);

        // Track the current slot while probing.
        int index = startIndex;

        // Continue probing until we hit a null, which ends the cluster.
        // Expected time is O(1), worst case O(n).
        while (table[index] != null) {

            MapEntry<K, V> cur = table[index];

            // Return the value only if the entry is active and the key matches.
            // Removed entries cannot count as present.
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                return cur.getValue();
            }

            // Advance linearly to the next slot, wrapping around if needed.
            index = (index + 1) % table.length;

            // Defensive full-loop stop condition in case the table has no null slots
            // in the probed cycle.
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

        // Null key is invalid, and this implementation also rejects null default values
        // to keep behavior explicit and avoid ambiguity.
        if (key == null || defaultValue == null) {
            throw new IllegalArgumentException("Key and defaultValue cannot be null");
        }

        // Start probing at the key's home index.
        int startIndex = Math.abs(key.hashCode() % table.length);

        int index = startIndex;

        // Probe exactly like get(), since the search rules are identical.
        while (table[index] != null) {

            MapEntry<K, V> cur = table[index];

            // Return stored value if the active matching key is found.
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                return cur.getValue();
            }

            // Otherwise keep probing through the cluster.
            index = (index + 1) % table.length;

            // Defensive loop-termination if we circle back to the start.
            if (index == startIndex) {
                break;
            }
        }

        // If the key never appeared, return the provided default instead of throwing.
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
        // Null keys are not valid search input.
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Begin probing from the key's hashed location.
        int startIndex = Math.abs(key.hashCode() % table.length);
        int index = startIndex;

        // Search through the cluster until null is encountered.
        while (table[index] != null) {

            MapEntry<K, V> cur = table[index];

            // Presence means an active entry with the exact key exists.
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                return true;
            }

            // Keep following the probe chain.
            index = (index + 1) % table.length;

            // Stop if we've wrapped all the way around.
            if (index == startIndex) {
                break;
            }
        }

        // No active matching key found.
        return false;
    }

    /**
     * Returns a set of all keys in the map in index order of the backing array,
     * skipping null and removed entries.
     *
     * @return a set of all keys in the map
     */
    public Set<K> keySet() {

        // Use a set because keys in a map are unique by definition.
        Set<K> set = new HashSet<>();

        // Scan the whole backing array in index order.
        // This is O(n) in table length, which is expected for full traversal.
        for (MapEntry<K, V> entry : table) {

            // Only active entries represent real map contents.
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

        // Use an ArrayList because values are allowed to repeat and we want to preserve
        // traversal order based on the backing array.
        List<V> list = new ArrayList<>();

        // Full-table scan in index order.
        for (MapEntry<K, V> entry : table) {

            // Only include active entries currently in the map.
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

        // The table cannot be resized below the number of active entries,
        // because then some elements would have no place to go.
        if (length < size) {
            throw new IllegalArgumentException("Length cannot be less than number of items in the map");
        }

        // Allocate the new backing table.
        MapEntry<K, V>[] newTable = new MapEntry[length];

        // Rehash every active entry from the old table into the new one.
        // This is necessary because changing table length changes every key's
        // ideal index.
        for (int i = 0; i < table.length; i++) {

            if (table[i] != null && !table[i].isRemoved()) {

                // Recompute the hashed home index using the new table size.
                int index = Math.abs(table[i].getKey().hashCode() % length);

                // Linear-probe in the new table until an open slot is found.
                // This preserves the map contents while rebuilding the clustering pattern
                // appropriate for the new capacity.
                for (int j = 0; j < length; j++) {

                    int probeIndex = (index + j) % length;

                    if (newTable[probeIndex] == null) {
                        newTable[probeIndex] = table[i];
                        break;
                    }
                }
            }
        }

        // Swap in the resized table.
        // Size does not change because we only reinserted existing active entries.
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

        // Same null validation as ordinary put().
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value cannot be null");
        }

        // Compute the starting index in the current table.
        int startIndex = Math.abs(key.hashCode() % table.length);

        // Track the first removed slot so it can be reused if needed.
        int firstRemovedIndex = -1;
        int index = startIndex;

        // Probe the table without triggering any resize logic.
        // This helper is meant for internal use when we know the table capacity
        // is already appropriate.
        for (int probes = 0; probes < table.length; probes++) {

            MapEntry<K, V> cur = table[index];

            // Empty slot means insertion can happen here, or earlier at a remembered tombstone.
            if (cur == null) {

                int insertIndex = (firstRemovedIndex != -1) ? firstRemovedIndex : index;

                table[insertIndex] = new MapEntry<>(key, value);

                return;
            }

            // If the active key already exists, update its value in place.
            if (!cur.isRemoved() && cur.getKey().equals(key)) {
                cur.setValue(value);
                return;
            }

            // Remember the first tombstone but keep searching in case the key already exists later.
            if (cur.isRemoved() && firstRemovedIndex == -1) {
                firstRemovedIndex = index;
            }

            index = (index + 1) % table.length;
        }

        // If there was no null slot but there was a tombstone, reuse the tombstone.
        if (firstRemovedIndex != -1) {
            table[firstRemovedIndex] = new MapEntry<>(key, value);
            return;
        }

        // If neither a null slot nor a tombstone was found, the table is effectively full.
        throw new IllegalStateException("HashMap backing table is full during putNoResize.");
    }

    /**
     * Clears the map by resetting the backing array to initial capacity and size to 0.
     */
    public void clear() {

        // Allocate a brand-new empty backing array at the original default capacity.
        // This fully resets the map, including clearing out any tombstones.
        table = new MapEntry[INITIAL_CAPACITY];

        // Reset active size to reflect an empty map.
        size = 0;
    }

    /**
     * Returns the backing array of the map. This is for grading purposes only.
     * @return the backing array of the map
     */
    public MapEntry<K, V>[] getTable() {
        // Exposes internal array for testing/grading.
        return table;
    }

    /**
     * @return the number of active (non-removed) elements in the map
     */
    public int size() {
        // O(1) size retrieval because active count is tracked directly.
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

        // Return a custom iterator that walks the backing array directly.
        return new HashMapIterator();
    }

    /**
     * Private inner class that implements the Iterator interface for the HashMap.
     * It iterates over the keys in the backing array, skipping over null and removed entries.
     */
    private class HashMapIterator implements Iterator<K> {

        // Current index in backing array
        // This tracks where the iterator is scanning next.
        private int index = 0;

        // Number of valid elements returned so far
        // This lets hasNext() work in O(1) time without rescanning the table.
        private int seen = 0;

        /**
         * Returns true if there are more elements to iterate over, false otherwise.
         * @return true if there are more elements to iterate over, false otherwise
         */
        @Override
        public boolean hasNext() {

            // If fewer than size active keys have been returned, iteration is not finished.
            // This is efficient because size ignores tombstones.
            return seen < size;
        }

        /**
         * Returns the next key in the iteration. If there are no more elements, throws a NoSuchElementException.
         * @return the next key in the iteration
         * @throws NoSuchElementException if there are no more elements to return
         */
        @Override
        public K next() {

            // Standard iterator contract: next() cannot be called if nothing remains.
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements");
            }

            // Advance past all null slots and tombstones until reaching the next active entry.
            while (index < table.length
                    && (table[index] == null || table[index].isRemoved())) {
                index++;
            }

            // Defensive safety check in case the iterator state somehow runs past the table.
            if (index >= table.length) {
                throw new NoSuchElementException("No more elements");
            }

            // Read the key from the current active entry.
            K key = table[index].getKey();

            // Advance both scan position and count of returned active elements.
            index++;
            seen++;

            return key;
        }
    }
}