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
 * <br>
 * <p>
 * Collaborators: NONE
 * <p>
 * Resources: NONE
 * <p>
 * <br>
 * By typing 'I agree' below, you are agreeing that this is your
 * own work and that you are responsible for the contents of all
 * submitted files. If this is left blank, this project will lose
 * points.
 *<p>
 *<br>
 * Agree Here: I agree
 *
 * @param <K> the type of key; must be {@link Comparable}
 * @param <V> the type of value
 */
@SuppressWarnings("DuplicatedCode")
public class TreeMap<K extends Comparable<? super K>, V> implements StaticTreeMap<K, V> {

    // Root node of the AVL tree.
    // Keeping a direct root reference allows all top-level operations
    // like put, get, and remove to begin in O(1) time.
    private TreeMapNode<K, V> root;

    // Number of key-value pairs currently stored in the tree.
    // This is tracked explicitly so size() can run in O(1) time.
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
        // Null keys or values are rejected immediately so the tree never stores
        // invalid entries and comparison logic never has to deal with null keys.
        if (key == null || value == null) {
            throw new IllegalArgumentException("key and value must be non-null.");
        }

        // This small box is used so the recursive helper can communicate back
        // the old value if an existing key gets replaced.
        ValueBox<V> old = new ValueBox<>();

        // Reassign root because AVL insertions may rotate the subtree,
        // which can change the root of the entire tree.
        root = putH(root, key, value, old);

        // If the key already existed, old.value holds the replaced value.
        // Otherwise it remains null, which matches the required return behavior.
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
        // Base case: once we reach a null spot, this is where the new key belongs.
        // Insertion into an AVL tree still starts as ordinary BST insertion.
        if (curr == null) {
            size++;
            return new TreeMapNode<>(key, value);
        }

        // Compare the incoming key to the current node's key to decide which direction
        // to continue searching. This is the core BST ordering rule.
        int comp = key.compareTo(curr.getKey());
        if (comp < 0) {
            // Smaller keys go into the left subtree.
            curr.setLeft(putH(curr.getLeft(), key, value, old));
        } else if (comp > 0) {
            // Larger keys go into the right subtree.
            curr.setRight(putH(curr.getRight(), key, value, old));
        } else {
            // Matching key means this is an update, not a structural insertion.
            // Save the old value so the public put() can return it.
            old.value = curr.getValue();

            // Replace only the value, because the key is already in the correct location.
            curr.setValue(value);

            // No structure changed, so no further balancing is needed here.
            return curr;
        }

        // After inserting into a subtree, this node's cached height and balance factor
        // may have changed, so update them before checking balance.
        update(curr);

        // AVL trees rebalance on the way back up recursion so height stays O(log n).
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
        // Null keys are invalid because comparison-based search cannot operate on them.
        if (key == null) {
            throw new IllegalArgumentException("key must be non-null.");
        }

        // Start at the root and walk downward like a standard BST search.
        TreeMapNode<K, V> curr = root;
        while (curr != null) {
            int comp = key.compareTo(curr.getKey());
            if (comp < 0) {
                // If the target key is smaller, it can only be in the left subtree.
                curr = curr.getLeft();
            } else if (comp > 0) {
                // If the target key is larger, it can only be in the right subtree.
                curr = curr.getRight();
            } else {
                // Found exact key match, so return the associated value.
                return curr.getValue();
            }
        }

        // Reaching null means the key is not in the tree.
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
        // Same validation reasoning as get(): a null key is not searchable.
        if (key == null) {
            throw new IllegalArgumentException("key must be non-null.");
        }

        // This uses the exact same BST walk pattern as get(),
        // but only answers presence/absence instead of returning a value.
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

        // If the search falls off the tree, the key is not present.
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
        // Null keys are invalid for the same reasons as other lookup operations.
        if (key == null) {
            throw new IllegalArgumentException("key must be non-null.");
        }

        // This box lets the recursive helper communicate the removed value back
        // through the recursive calls.
        ValueBox<V> removed = new ValueBox<>();

        // Reassign root because deletion can rebalance the tree and change
        // the subtree root, including the overall root.
        root = removeH(root, key, removed);

        // Return the value that belonged to the deleted key.
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
        // Falling off the tree means the key was never found.
        if (curr == null) {
            throw new NoSuchElementException("key not found.");
        }

        // Use BST ordering to locate the node to delete.
        int comp = key.compareTo(curr.getKey());
        if (comp < 0) {
            // Target key is smaller, so deletion must occur in the left subtree.
            curr.setLeft(removeH(curr.getLeft(), key, removed));
        } else if (comp > 0) {
            // Target key is larger, so deletion must occur in the right subtree.
            curr.setRight(removeH(curr.getRight(), key, removed));
        } else {
            // We found the node to delete, so record its value for the public return.
            removed.value = curr.getValue();
            size--;

            // Case 1: no left child.
            // The right child can simply move up and replace this node.
            if (curr.getLeft() == null) {
                return curr.getRight();

            // Case 2: no right child.
            // The left child can simply move up and replace this node.
            } else if (curr.getRight() == null) {
                return curr.getLeft();

            } else {
                // Case 3: two children.
                // This implementation uses the predecessor, meaning the maximum node
                // in the left subtree, to preserve BST ordering after deletion.
                NodeBox<K, V> pred = new NodeBox<>();

                // Remove the predecessor node from the left subtree and capture its data.
                curr.setLeft(removePredecessor(curr.getLeft(), pred));

                // Copy the predecessor's key and value into the current node.
                // This avoids deleting the current node object itself and only changes its contents.
                curr.setKey(pred.key);
                curr.setValue(pred.value);
            }
        }

        // After a successful deletion in a subtree, this node's height and balance factor
        // may have changed, so they must be recomputed.
        update(curr);

        // Restore AVL balance before returning the subtree upward.
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
        // The predecessor is the rightmost node in a subtree.
        // So once there is no more right child, this node is the predecessor.
        if (curr.getRight() == null) {
            // Save the predecessor's data so the caller can copy it upward.
            pred.key = curr.getKey();
            pred.value = curr.getValue();

            // Remove this predecessor node by returning its left subtree,
            // which is the only subtree it can possibly have.
            return curr.getLeft();
        }

        // Keep moving right until the predecessor is found.
        curr.setRight(removePredecessor(curr.getRight(), pred));

        // After removal from the right subtree, update this node's metadata.
        update(curr);

        // Rebalance because predecessor removal may have changed subtree heights.
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
        // Bounds must exist because range comparisons depend on valid comparable values.
        if (lower == null || upper == null) {
            throw new IllegalArgumentException("bounds must be non-null.");
        }

        // This list collects results in sorted order as the traversal progresses.
        List<V> out = new ArrayList<>();

        // Fast-return cases:
        // 1) empty tree means no results
        // 2) invalid range lower > upper can never match anything
        if (root == null || lower.compareTo(upper) > 0) {
            return out;
        }

        // Delegate to the bounded traversal helper.
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
        // Standard recursive base case for tree traversal.
        if (curr == null) {
            return;
        }

        // Compare current key to the range bounds once and reuse the results.
        // This avoids repeating compareTo calls unnecessarily.
        int cmpLower = curr.getKey().compareTo(lower);
        int cmpUpper = curr.getKey().compareTo(upper);

        // Only go left if current key is greater than lower bound.
        // If current key is already <= lower, then everything in the left subtree
        // is even smaller and cannot qualify, so pruning that side preserves the
        // desired O(log n + k) behavior.
        if (cmpLower > 0) {
            getRangeH(curr.getLeft(), lower, upper, out);
        }

        // Visit the current node only if its key lies inside the inclusive range.
        if (cmpLower >= 0 && cmpUpper <= 0) {
            out.add(curr.getValue());
        }

        // Only go right if current key is less than upper bound.
        // If current key is already >= upper, then everything in the right subtree
        // is even larger and cannot qualify.
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
        // Allocate a collection to gather every key in the tree.
        Set<K> keys = new HashSet<>();

        // Traverse the whole tree and collect the keys.
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
        // Base case: nothing to add from an empty subtree.
        if (curr == null) {
            return;
        }

        // In-order traversal visits left subtree, current node, then right subtree.
        // That is the natural way to process BST keys in ascending order.
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
        // This list collects values in the same order as their sorted keys.
        List<V> vals = new ArrayList<>();

        // In-order traversal of a BST naturally yields values in ascending key order.
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
        // Standard recursive base case.
        if (curr == null) {
            return;
        }

        // In-order traversal preserves ascending key order,
        // which is why TreeMap values come out sorted by key.
        valuesH(curr.getLeft(), vals);
        vals.add(curr.getValue());
        valuesH(curr.getRight(), vals);
    }

    /**
     * Clears the map.
     */
    public void clear() {
        // Dropping the root reference makes the entire tree unreachable,
        // which effectively clears all contents at once.
        root = null;

        // Reset the cached size so the map reports empty immediately in O(1).
        size = 0;
    }

    /**
     * Returns the height of the tree.
     *
     * @return -1 if empty; otherwise root height
     */
    public int height() {
        // By convention, an empty tree has height -1.
        // Otherwise the tree height is exactly the root's cached height.
        return (root == null) ? -1 : root.getHeight();
    }

    /**
     * Returns the number of key-value pairs in the map.
     *
     * @return the size
     */
    public int size() {
        // Return the cached size in O(1) time.
        return size;
    }

    /**
     * Returns the root node (useful for testing/visualization).
     *
     * @return root node of the AVL tree
     */
    public TreeMapNode<K, V> getRoot() {
        // Exposes the root reference for debugging, testing, or visualization.
        return root;
    }

    /**
     * Returns an iterator over the values of the map in ascending key order.
     *
     * @return an in-order iterator over the values
     */
    @Override
    public Iterator<V> iterator() {
        // Return a dedicated iterator object that performs lazy in-order traversal.
        // This is more efficient than building a full values list first if the caller
        // only consumes part of the iteration.
        return new TreeMapIterator();
    }

    /**
     * Updates the height and balance factor of a node based on its children.
     *
     * @param node the node to update
     */
    private void update(TreeMapNode<K, V> node) {
        // Missing children are treated as height -1 so leaf nodes end up with height 0.
        int lh = (node.getLeft() == null) ? -1 : node.getLeft().getHeight();
        int rh = (node.getRight() == null) ? -1 : node.getRight().getHeight();

        // Height is one plus the height of the taller child.
        node.setHeight(1 + Math.max(lh, rh));

        // AVL balance factor is left height minus right height.
        // Positive means left-heavy, negative means right-heavy.
        node.setBalanceFactor(lh - rh);
    }

    /**
     * Performs a left rotation around the given node.
     *
     * @param node the root of the rotation
     * @return the new subtree root after rotation
     */
    private TreeMapNode<K, V> leftRotate(TreeMapNode<K, V> node) {
        // In a left rotation, the node's right child becomes the new root of this subtree.
        TreeMapNode<K, V> newRoot = node.getRight();

        // The new root's left subtree gets transferred to become the old node's right subtree.
        // This preserves BST ordering during the rotation.
        TreeMapNode<K, V> transfer = newRoot.getLeft();

        // Perform the structural pointer changes for the rotation.
        newRoot.setLeft(node);
        node.setRight(transfer);

        // Update bottom-up: first the old root, then the new root.
        // This order matters because newRoot's metadata depends on node already being correct.
        update(node);
        update(newRoot);

        // Return the new subtree root so the caller can reconnect it upward.
        return newRoot;
    }

    /**
     * Performs a right rotation around the given node.
     *
     * @param node the root of the rotation
     * @return the new subtree root after rotation
     */
    private TreeMapNode<K, V> rightRotate(TreeMapNode<K, V> node) {
        // In a right rotation, the node's left child becomes the new root of this subtree.
        TreeMapNode<K, V> newRoot = node.getLeft();

        // The new root's right subtree gets transferred to become the old node's left subtree.
        // This is the mirror image of left rotation.
        TreeMapNode<K, V> transfer = newRoot.getRight();

        // Perform the structural pointer updates for the rotation.
        newRoot.setRight(node);
        node.setLeft(transfer);

        // Recompute cached metadata after the structure changes.
        update(node);
        update(newRoot);

        // Return the new root of the rotated subtree.
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
        // Read the balance factor once since it determines which case applies.
        int bf = node.getBalanceFactor();

        // Left-heavy subtree.
        if (bf > 1) {
            // If the left child is right-heavy, this is a left-right case.
            // First rotate the child left to convert it into a simple left-left case.
            if (node.getLeft() != null && node.getLeft().getBalanceFactor() < 0) {
                node.setLeft(leftRotate(node.getLeft()));
            }

            // Then rotate the current node right to restore AVL balance.
            return rightRotate(node);

        // Right-heavy subtree.
        } else if (bf < -1) {
            // If the right child is left-heavy, this is a right-left case.
            // First rotate the child right to convert it into a simple right-right case.
            if (node.getRight() != null && node.getRight().getBalanceFactor() > 0) {
                node.setRight(rightRotate(node.getRight()));
            }

            // Then rotate the current node left to restore AVL balance.
            return leftRotate(node);
        }

        // If balance factor is between -1 and 1, this node is already AVL-valid.
        return node;
    }

    /**
     * Simple wrapper class used to pass a mutable reference
     * when returning old values during insertion or removal.
     *
     * @param <T> the boxed value type
     */
    private static class ValueBox<T> {
        // Java passes object references by value, so recursion cannot directly "return"
        // an extra piece of data alongside the subtree root.
        // This wrapper provides a mutable container for that purpose.
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
        // Stores the predecessor's key during two-child deletion.
        private K key;

        // Stores the predecessor's value during two-child deletion.
        private V value;
    }

    /**
     * Iterator implementation that performs an in-order traversal
     * over the AVL tree values.
     */
    private class TreeMapIterator implements Iterator<V> {

        // This stack simulates the recursion of an in-order traversal.
        // Using an explicit stack avoids having to precompute all values in a list.
        private final Deque<TreeMapNode<K, V>> stack = new ArrayDeque<>();

        /**
         * Constructs the iterator and initializes the stack
         * with the leftmost path of the tree.
         */
        private TreeMapIterator() {
            // Start by pushing the root-to-leftmost path.
            // That ensures the smallest key is on top first, which matches in-order traversal.
            pushLeft(root);
        }

        /**
         * Pushes all nodes along the left path of a subtree onto the stack.
         *
         * @param node the subtree root
         */
        private void pushLeft(TreeMapNode<K, V> node) {
            TreeMapNode<K, V> curr = node;

            // Walk left as far as possible, pushing each node.
            // This makes the stack's top always hold the next in-order node to visit.
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
            // If the stack still has nodes, there is still an in-order value remaining.
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
            // Standard iterator contract: calling next() with no remaining elements is an error.
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            // Pop the next in-order node from the stack.
            TreeMapNode<K, V> node = stack.pop();

            // After visiting a node, the next in-order values come from its right subtree.
            // Specifically, the leftmost path of that right subtree must be pushed.
            if (node.getRight() != null) {
                pushLeft(node.getRight());
            }

            // Return the value associated with the visited node.
            return node.getValue();
        }
    }
}