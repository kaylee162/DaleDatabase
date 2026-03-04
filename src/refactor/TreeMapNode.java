package refactor;

/**
 * A node in the AVL-backed TreeMap.
 *
 * @author Kaylee Henry
 * @version 1.0
 * @userid khenry61
 * @GTID 904065531
 *
 * @param <K> the type of key; must be {@link Comparable}
 * @param <V> the type of value stored in the map
 */
public class TreeMapNode<K extends Comparable<? super K>, V> {

    private K key;
    private V value;

    private TreeMapNode<K, V> left;
    private TreeMapNode<K, V> right;

    private int height;
    private int balanceFactor;

    /**
     * Constructs a node storing the given key-value pair.
     *
     * @param key the key
     * @param value the value
     */
    public TreeMapNode(K key, V value) {
        this.key = key;
        this.value = value;
        height = 0;
        balanceFactor = 0;
    }

    /**
     * Gets the key stored in the node.
     *
     * @return the key
     */
    public K getKey() {
        return key;
    }

    /**
     * Sets the key stored in the node.
     *
     * @param key the new key
     */
    public void setKey(K key) {
        this.key = key;
    }

    /**
     * Gets the value stored in the node.
     *
     * @return the value
     */
    public V getValue() {
        return value;
    }

    /**
     * Sets the value stored in the node.
     *
     * @param value the new value
     */
    public void setValue(V value) {
        this.value = value;
    }

    /**
     * Gets the left child.
     *
     * @return the left child
     */
    public TreeMapNode<K, V> getLeft() {
        return left;
    }

    /**
     * Sets the left child.
     *
     * @param left the new left child
     */
    public void setLeft(TreeMapNode<K, V> left) {
        this.left = left;
    }

    /**
     * Gets the right child.
     *
     * @return the right child
     */
    public TreeMapNode<K, V> getRight() {
        return right;
    }

    /**
     * Sets the right child.
     *
     * @param right the new right child
     */
    public void setRight(TreeMapNode<K, V> right) {
        this.right = right;
    }

    /**
     * Gets the height of this node.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of this node.
     *
     * @param height the new height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Gets the balance factor of this node.
     *
     * @return the balance factor
     */
    public int getBalanceFactor() {
        return balanceFactor;
    }

    /**
     * Sets the balance factor of this node.
     *
     * @param balanceFactor the new balance factor
     */
    public void setBalanceFactor(int balanceFactor) {
        this.balanceFactor = balanceFactor;
    }
}