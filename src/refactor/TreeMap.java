package refactor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An AVL-backed TreeMap implementation.
 *
 * Keys are kept in sorted order and each key maps to a value.
 * Removal uses the predecessor in the two-child case.
 *
 * @author Kaylee Henry
 * @version 1.0
 * @userid khenry61
 * @GTID 904065531
 *
 * @param <K> the type of key; must be {@link Comparable}
 * @param <V> the type of value
 */
@SuppressWarnings("DuplicatedCode")
public class TreeMap<K extends Comparable<? super K>, V> implements StaticTreeMap<K, V> {

    private TreeMapNode<K, V> root;
    private int size;

    /**
     * Puts a key-value pair into the map. If the key already exists, replaces
     * the existing value and returns the old value.
     *
     * @param key the key to put
     * @param value the value to associate with the key
     * @return the old value if replaced, or {@code null} if no prior mapping existed
     * @throws IllegalArgumentException if key or value is null
     */
    public V put(K key, V value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key and value must be non-null.");
        }
        ValueBox<V> old = new ValueBox<>();
        root = putH(root, key, value, old);
        return old.value;
    }

    /**
     * Recursive helper method for inserting a key-value pair into the AVL tree.
     * Performs standard BST insertion followed by AVL rebalancing.
     *
     * @param curr the current node being examined
     * @param key the key to insert
     * @param value the value to associate with the key
     * @param old box used to return the previous value if the key already exists
     * @return the updated subtree root after insertion and balancing
     */
    private TreeMapNode<K, V> putH(TreeMapNode<K, V> curr, K key, V value, ValueBox<V> old) {
        if (curr == null) {
            size++;
            return new TreeMapNode<>(key, value);
        }

        int comp = key.compareTo(curr.getKey());
        if (comp < 0) {
            curr.setLeft(putH(curr.getLeft(), key, value, old));
        } else if (comp > 0) {
            curr.setRight(putH(curr.getRight(), key, value, old));
        } else {
            old.value = curr.getValue();
            curr.setValue(value);
            return curr;
        }

        update(curr);
        return balance(curr);
    }

    /**
     * Gets the value associated with the key.
     *
     * @param key the key to search
     * @return the associated value
     * @throws IllegalArgumentException if key is null
     * @throws NoSuchElementException if key is not in the map
     */
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key must be non-null.");
        }

        TreeMapNode<K, V> curr = root;
        while (curr != null) {
            int comp = key.compareTo(curr.getKey());
            if (comp < 0) {
                curr = curr.getLeft();
            } else if (comp > 0) {
                curr = curr.getRight();
            } else {
                return curr.getValue();
            }
        }
        throw new NoSuchElementException("key not found.");
    }

    /**
     * Returns true if the key exists in the map.
     *
     * @param key the key to check
     * @return true if present; false otherwise
     * @throws IllegalArgumentException if key is null
     */
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key must be non-null.");
        }

        TreeMapNode<K, V> curr = root;
        while (curr != null) {
            int comp = key.compareTo(curr.getKey());
            if (comp < 0) {
                curr = curr.getLeft();
            } else if (comp > 0) {
                curr = curr.getRight();
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the key-value mapping for the key and returns the removed value.
     *
     * @param key the key to remove
     * @return the removed value
     * @throws IllegalArgumentException if key is null
     * @throws NoSuchElementException if key not present
     */
    public V remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key must be non-null.");
        }

        ValueBox<V> removed = new ValueBox<>();
        root = removeH(root, key, removed);
        return removed.value;
    }

    /**
     * Recursive helper method for removing a key from the AVL tree.
     *
     * @param curr the current node being examined
     * @param key the key to remove
     * @param removed box used to store the removed value
     * @return the updated subtree root after removal and balancing
     */
    private TreeMapNode<K, V> removeH(TreeMapNode<K, V> curr, K key, ValueBox<V> removed) {
        if (curr == null) {
            throw new NoSuchElementException("key not found.");
        }

        int comp = key.compareTo(curr.getKey());
        if (comp < 0) {
            curr.setLeft(removeH(curr.getLeft(), key, removed));
        } else if (comp > 0) {
            curr.setRight(removeH(curr.getRight(), key, removed));
        } else {
            removed.value = curr.getValue();
            size--;

            if (curr.getLeft() == null) {
                return curr.getRight();
            } else if (curr.getRight() == null) {
                return curr.getLeft();
            } else {
                NodeBox<K, V> pred = new NodeBox<>();
                curr.setLeft(removePredecessor(curr.getLeft(), pred));
                curr.setKey(pred.key);
                curr.setValue(pred.value);
            }
        }

        update(curr);
        return balance(curr);
    }

    /**
     * Removes the predecessor node (maximum node of the left subtree)
     * during a two-child deletion case.
     *
     * @param curr the current subtree root
     * @param pred box used to store the predecessor key and value
     * @return the updated subtree root after predecessor removal
     */
    private TreeMapNode<K, V> removePredecessor(TreeMapNode<K, V> curr, NodeBox<K, V> pred) {
        if (curr.getRight() == null) {
            pred.key = curr.getKey();
            pred.value = curr.getValue();
            return curr.getLeft();
        }

        curr.setRight(removePredecessor(curr.getRight(), pred));
        update(curr);
        return balance(curr);
    }

    /**
     * Returns an in-order list of values whose keys lie within [lower, upper], inclusive.
     *
     * Time complexity must be O(log n + k) where k is the number of returned values.
     *
     * @param lower the lower bound (inclusive)
     * @param upper the upper bound (inclusive)
     * @return list of values in ascending key order within the bounds
     * @throws IllegalArgumentException if lower or upper is null
     */
    public List<V> getRange(K lower, K upper) {
        if (lower == null || upper == null) {
            throw new IllegalArgumentException("bounds must be non-null.");
        }

        List<V> out = new ArrayList<>();
        if (root == null || lower.compareTo(upper) > 0) {
            return out;
        }

        getRangeH(root, lower, upper, out);
        return out;
    }

    /**
     * Recursive helper method that performs a bounded in-order traversal
     * to collect values whose keys fall within the specified range.
     *
     * @param curr the current node being visited
     * @param lower the lower bound
     * @param upper the upper bound
     * @param out the list collecting qualifying values
     */
    private void getRangeH(TreeMapNode<K, V> curr, K lower, K upper, List<V> out) {
        if (curr == null) {
            return;
        }

        int cmpLower = curr.getKey().compareTo(lower);
        int cmpUpper = curr.getKey().compareTo(upper);

        // Only go left if current key is greater than lower bound —
        // there may be qualifying keys in the left subtree
        if (cmpLower > 0) {
            getRangeH(curr.getLeft(), lower, upper, out);
        }

        // Visit current node if key is within [lower, upper]
        if (cmpLower >= 0 && cmpUpper <= 0) {
            out.add(curr.getValue());
        }

        // Only go right if current key is less than upper bound —
        // there may be qualifying keys in the right subtree
        if (cmpUpper < 0) {
            getRangeH(curr.getRight(), lower, upper, out);
        }
    }

    /**
     * Returns all keys in ascending key order.
     *
     * @return a set of keys in sorted order
     */
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        keySetH(root, keys);
        return keys;
    }

    /**
     * Recursive helper method that performs an in-order traversal
     * to collect all keys in sorted order.
     *
     * @param curr the current node being visited
     * @param keys the set collecting keys
     */
    private void keySetH(TreeMapNode<K, V> curr, Set<K> keys) {
        if (curr == null) {
            return;
        }
        keySetH(curr.getLeft(), keys);
        keys.add(curr.getKey());
        keySetH(curr.getRight(), keys);
    }

    /**
     * Returns all values in ascending key order.
     *
     * @return a list of values in key-sorted order
     */
    public List<V> values() {
        List<V> vals = new ArrayList<>();
        valuesH(root, vals);
        return vals;
    }

    /**
     * Recursive helper method that performs an in-order traversal
     * to collect values in sorted key order.
     *
     * @param curr the current node
     * @param vals the list collecting values
     */
    private void valuesH(TreeMapNode<K, V> curr, List<V> vals) {
        if (curr == null) {
            return;
        }
        valuesH(curr.getLeft(), vals);
        vals.add(curr.getValue());
        valuesH(curr.getRight(), vals);
    }

    /**
     * Clears the map.
     */
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * Returns the height of the tree.
     *
     * @return -1 if empty; otherwise root height
     */
    public int height() {
        return (root == null) ? -1 : root.getHeight();
    }

    /**
     * Returns the number of key-value pairs in the map.
     *
     * @return the size
     */
    public int size() {
        return size;
    }

    /**
     * Returns the root node (useful for testing/visualization).
     *
     * @return root node of the AVL tree
     */
    public TreeMapNode<K, V> getRoot() {
        return root;
    }

    /**
     * Returns an iterator over the values of the map in ascending key order.
     *
     * @return an in-order iterator over the values
     */
    @Override
    public Iterator<V> iterator() {
        return new TreeMapIterator();
    }

    /**
     * Updates the height and balance factor of a node based on its children.
     *
     * @param node the node to update
     */
    private void update(TreeMapNode<K, V> node) {
        int lh = (node.getLeft() == null) ? -1 : node.getLeft().getHeight();
        int rh = (node.getRight() == null) ? -1 : node.getRight().getHeight();
        node.setHeight(1 + Math.max(lh, rh));
        node.setBalanceFactor(lh - rh);
    }

    /**
     * Performs a left rotation around the given node.
     *
     * @param node the root of the rotation
     * @return the new subtree root after rotation
     */
    private TreeMapNode<K, V> leftRotate(TreeMapNode<K, V> node) {
        TreeMapNode<K, V> newRoot = node.getRight();
        TreeMapNode<K, V> transfer = newRoot.getLeft();

        newRoot.setLeft(node);
        node.setRight(transfer);

        update(node);
        update(newRoot);
        return newRoot;
    }

    /**
     * Performs a right rotation around the given node.
     *
     * @param node the root of the rotation
     * @return the new subtree root after rotation
     */
    private TreeMapNode<K, V> rightRotate(TreeMapNode<K, V> node) {
        TreeMapNode<K, V> newRoot = node.getLeft();
        TreeMapNode<K, V> transfer = newRoot.getRight();

        newRoot.setRight(node);
        node.setLeft(transfer);

        update(node);
        update(newRoot);
        return newRoot;
    }

    /**
     * Restores AVL balance for the given node if its balance factor
     * indicates that it is unbalanced.
     *
     * @param node the node to rebalance
     * @return the new subtree root after balancing
     */
    private TreeMapNode<K, V> balance(TreeMapNode<K, V> node) {
        int bf = node.getBalanceFactor();

        if (bf > 1) {
            if (node.getLeft() != null && node.getLeft().getBalanceFactor() < 0) {
                node.setLeft(leftRotate(node.getLeft()));
            }
            return rightRotate(node);
        } else if (bf < -1) {
            if (node.getRight() != null && node.getRight().getBalanceFactor() > 0) {
                node.setRight(rightRotate(node.getRight()));
            }
            return leftRotate(node);
        }

        return node;
    }

    /**
     * Simple wrapper class used to pass a mutable reference
     * when returning old values during insertion or removal.
     *
     * @param <T> the boxed value type
     */
    private static class ValueBox<T> {
        private T value;
    }

    /**
     * Helper container used to return predecessor key-value pairs
     * during deletion operations.
     *
     * @param <K> key type
     * @param <V> value type
     */
    private static class NodeBox<K, V> {
        private K key;
        private V value;
    }

    /**
     * Iterator implementation that performs an in-order traversal
     * over the AVL tree values.
     */
    private class TreeMapIterator implements Iterator<V> {

        private final Deque<TreeMapNode<K, V>> stack = new ArrayDeque<>();

        /**
         * Constructs the iterator and initializes the stack
         * with the leftmost path of the tree.
         */
        private TreeMapIterator() {
            pushLeft(root);
        }

        /**
         * Pushes all nodes along the left path of a subtree onto the stack.
         *
         * @param node the subtree root
         */
        private void pushLeft(TreeMapNode<K, V> node) {
            TreeMapNode<K, V> curr = node;
            while (curr != null) {
                stack.push(curr);
                curr = curr.getLeft();
            }
        }

        /**
         * Returns whether the iterator has more elements.
         *
         * @return true if another value exists
         */
        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        /**
         * Returns the next value in ascending key order.
         *
         * @return the next value
         * @throws NoSuchElementException if no elements remain
         */
        @Override
        public V next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            TreeMapNode<K, V> node = stack.pop();
            if (node.getRight() != null) {
                pushLeft(node.getRight());
            }
            return node.getValue();
        }
    }
}