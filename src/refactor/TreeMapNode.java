package refactor;

/**
 * A node in the AVL-backed TreeMap.
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
 * @param <V> the type of value stored in the map
 */
public class TreeMapNode<K extends Comparable<? super K>, V> {

    // The key is what determines this node's position in the tree.
    // Because this is a BST/AVL structure, keys must be comparable so
    // the tree can decide whether to go left or right during operations.
    private K key;

    // The value is the payload associated with the key.
    // Separating key and value like this makes the node act like a map entry.
    private V value;

    // Reference to the left child.
    // By BST ordering, everything in the left subtree should have a smaller key.
    private TreeMapNode<K, V> left;

    // Reference to the right child.
    // By BST ordering, everything in the right subtree should have a larger key.
    private TreeMapNode<K, V> right;

    // Height of the node in the AVL tree.
    // Storing height directly makes balancing operations efficient,
    // because we do not need to recompute subtree height from scratch every time.
    private int height;

    // Balance factor = left subtree height - right subtree height.
    // AVL trees use this value to detect whether rotations are needed.
    // Storing it explicitly makes rebalance checks O(1) at each node.
    private int balanceFactor;

    /**
     * Constructs a node storing the given key-value pair.
     *
     * @param key the key
     * @param value the value
     */
    public TreeMapNode(K key, V value) {
        // Store the key for ordering and lookup.
        this.key = key;
        // Store the value associated with that key.
        this.value = value;

        // A newly created node starts as a leaf.
        // A leaf node has height 0 by convention in this implementation.
        height = 0;

        // A leaf has no children on either side, so its balance factor is 0.
        // This is the most balanced possible starting state.
        balanceFactor = 0;
    }

    /**
     * Gets the key stored in the node.
     *
     * @return the key
     */
    public K getKey() {
        // Simple O(1) accessor for the node's key.
        // This is useful because tree operations often need to compare keys
        // without exposing the field directly.
        return key;
    }

    /**
     * Sets the key stored in the node.
     *
     * @param key the new key
     */
    public void setKey(K key) {
        // Replace the key stored in this node.
        // This is O(1), though in practice changing keys in a BST node must be done carefully
        // because key order determines the structure of the tree.
        this.key = key;
    }

    /**
     * Gets the value stored in the node.
     *
     * @return the value
     */
    public V getValue() {
        // Simple O(1) accessor for the node's value.
        // This supports map-style retrieval once the correct key has been found.
        return value;
    }

    /**
     * Sets the value stored in the node.
     *
     * @param value the new value
     */
    public void setValue(V value) {
        // Replace the value stored at this key.
        // This is O(1) and is common in map updates where the key stays the same
        // but the associated value changes.
        this.value = value;
    }

    /**
     * Gets the left child.
     *
     * @return the left child
     */
    public TreeMapNode<K, V> getLeft() {
        // O(1) accessor for the left child pointer.
        // Tree traversal logic uses this to move into the left subtree.
        return left;
    }

    /**
     * Sets the left child.
     *
     * @param left the new left child
     */
    public void setLeft(TreeMapNode<K, V> left) {
        // Update the left child reference in O(1).
        // This is used during insertion, deletion, and AVL rotations when subtree links change.
        this.left = left;
    }

    /**
     * Gets the right child.
     *
     * @return the right child
     */
    public TreeMapNode<K, V> getRight() {
        // O(1) accessor for the right child pointer.
        // Tree traversal logic uses this to move into the right subtree.
        return right;
    }

    /**
     * Sets the right child.
     *
     * @param right the new right child
     */
    public void setRight(TreeMapNode<K, V> right) {
        // Update the right child reference in O(1).
        // This is especially important during restructuring operations like rotations.
        this.right = right;
    }

    /**
     * Gets the height of this node.
     *
     * @return the height
     */
    public int getHeight() {
        // O(1) accessor for height.
        // Height is stored rather than recomputed so AVL balancing remains efficient.
        return height;
    }

    /**
     * Sets the height of this node.
     *
     * @param height the new height
     */
    public void setHeight(int height) {
        // Update the cached height in O(1).
        // AVL implementations do this after structural changes so future operations
        // can use the value immediately.
        this.height = height;
    }

    /**
     * Gets the balance factor of this node.
     *
     * @return the balance factor
     */
    public int getBalanceFactor() {
        // O(1) accessor for balance factor.
        // This value is central to AVL trees because it tells whether a node is
        // balanced, left-heavy, or right-heavy.
        return balanceFactor;
    }

    /**
     * Sets the balance factor of this node.
     *
     * @param balanceFactor the new balance factor
     */
    public void setBalanceFactor(int balanceFactor) {
        // Update the cached balance factor in O(1).
        // This is typically recalculated after insertions, deletions, or rotations
        // so the tree can maintain AVL balance guarantees.
        this.balanceFactor = balanceFactor;
    }
}