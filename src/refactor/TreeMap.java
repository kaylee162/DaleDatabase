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
     * @return root
     */
    public TreeMapNode<K, V> getRoot() {
        return root;
    }

    @Override
    public Iterator<V> iterator() {
        return new TreeMapIterator();
    }

    private void update(TreeMapNode<K, V> node) {
        int lh = (node.getLeft() == null) ? -1 : node.getLeft().getHeight();
        int rh = (node.getRight() == null) ? -1 : node.getRight().getHeight();
        node.setHeight(1 + Math.max(lh, rh));
        node.setBalanceFactor(lh - rh);
    }

    private TreeMapNode<K, V> leftRotate(TreeMapNode<K, V> node) {
        TreeMapNode<K, V> newRoot = node.getRight();
        TreeMapNode<K, V> transfer = newRoot.getLeft();

        newRoot.setLeft(node);
        node.setRight(transfer);

        update(node);
        update(newRoot);
        return newRoot;
    }

    private TreeMapNode<K, V> rightRotate(TreeMapNode<K, V> node) {
        TreeMapNode<K, V> newRoot = node.getLeft();
        TreeMapNode<K, V> transfer = newRoot.getRight();

        newRoot.setRight(node);
        node.setLeft(transfer);

        update(node);
        update(newRoot);
        return newRoot;
    }

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

    private static class ValueBox<T> {
        private T value;
    }

    private static class NodeBox<K, V> {
        private K key;
        private V value;
    }

    private class TreeMapIterator implements Iterator<V> {

        private final Deque<TreeMapNode<K, V>> stack = new ArrayDeque<>();

        private TreeMapIterator() {
            pushLeft(root);
        }

        private void pushLeft(TreeMapNode<K, V> node) {
            TreeMapNode<K, V> curr = node;
            while (curr != null) {
                stack.push(curr);
                curr = curr.getLeft();
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

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