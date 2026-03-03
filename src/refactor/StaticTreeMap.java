package refactor;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Interface for your custom {@code TreeMap} implementation.
 * <p>
 * You should create a new class that implements this interface.
 *
 * @apiNote DO NOT MODIFY THIS FILE!
 * @implSpec
 * <p> Take your recently coded {@code AVL} class and refactor it
 * into a class that implements {@link StaticTreeMap}
 * @version 1.0
 * @author CS 1332 TAs
 * @param <K> the type of keys – must be {@link Comparable}
 * @param <V> the type of values
 */
public interface StaticTreeMap<K extends Comparable<? super K>, V> extends Iterable<V> {
    /**
     * Adds the given key-value pair to the map. If an entry in the map
     * already has this key, replace the entry's value with the new one
     * passed in.
     *
     * @param key the key to add
     * @param value the value to add
     * @return null if the key was not already in the map. If it was in the
     * map, return the old value associated with it
     * @throws IllegalArgumentException if key or value is null
     * @implSpec {@code O(log n)} runtime
     */
    V put(K key, V value);

    /**
     * Removes the entry with a matching key from map.
     *
     * Use the predecessor when removing a node with two children.
     * 
     * @param key the key to remove
     * @return the value previously associated with the key
     * @throws IllegalArgumentException if key is null
     * @throws java.util.NoSuchElementException   if the key is not in the map
     * @implSpec {@code O(log n)} runtime
     */
    V remove(K key);

    /**
     * Gets the value associated with the given key.
     *
     * @param key the key to search for in the map
     * @return the value associated with the given key
     * @throws IllegalArgumentException if key is {@code null}
     * @throws java.util.NoSuchElementException   if the key is not in the map
     * @implSpec {@code O(log n)} runtime
     */
    V get(K key);

    /**
     * Returns a list of all values within an inclusive key range.
     *
     * @param lower the lower key bound (inclusive)
     * @param upper the upper key bound (inclusive)
     * @return a list of all values within the range {@code [lower, upper]}.
     * @throws IllegalArgumentException if any argument is {@code null}
     * @implSpec
     * <p> {@code O(log(n) + k)} runtime
     * <p> {@code O(1)} auxiliary space (excluding recursive stack)
     * @implNote consider {@code k} to be the number of elements within the range,
     * and is practically less than {@code n}
     */
    List<V> getRange(K lower, K upper);

    /**
     * Returns whether or not the key is in the map.
     *
     * @param key the key to search for in the map
     * @return true if the key is contained within the map, false
     * otherwise
     * @throws IllegalArgumentException if key is null
     * @implSpec {@code O(log n)} runtime
     */
    boolean containsKey(K key);

    /**
     * Returns a Set view of the keys contained in this map.
     *
     * @return the set of keys in this map
     * @implSpec
     * <p> {@code O(n)} runtime
     * <p> Use {@link java.util.HashSet}
     */
    Set<K> keySet();

    /**
     * Returns a List view of the values contained in this map.
     *
     * @return list of values in this map
     * @implSpec
     * <p> {@code O(n)} runtime
     * <p> Use {@link java.util.ArrayList} or {@link java.util.LinkedList}
     */
    List<V> values();

    /**
     * Clears the map.
     * 
     * @implSpec {@code O(1)} runtime
     */
    void clear();

    /**
     * Returns the height of the root of the tree.
     *
     * @return the height of the root of the tree, -1 if the tree is empty
     * @implSpec {@code O(1)} runtime
     */
    int height();

    /**
     * Returns the number of entries in the map.
     *
     * @return the size of the map
     * @implSpec {@code O(1)} runtime
     */
    int size();

    /**
     * Returns an iterator over the values of the map.
     *
     * @return an iterator over the values of the map
     * @implSpec
     * <p> {@code O(log n)} auxiliary space. You cannot simply call
     * {@link #values()}.
     */
    Iterator<V> iterator();
}
