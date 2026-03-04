package refactor;

// ________________________________________________________________________________
// ********************************************************************************
//
//  THIS FILE WILL NOT BE TESTED!
//
//  To have a functioning AVL tree, you must complete the four methods at the
//  bottom of this file: update(), leftRotate(), rightRotate(), AND balance().
//
//  Read the PDF for more information on how to use this file.
// ________________________________________________________________________________
// ********************************************************************************

import java.util.NoSuchElementException;

/**
 * A partially completed implementation of an AVL tree.
 * <p>
 * You should implement the placeholder helper methods to
 * complete the functionality of the AVL.
 *
 * @version 1.0
 * @author CS 1332 TAs
 * @param <T> the type of data – must be {@link Comparable}
 */
@SuppressWarnings("DuplicatedCode")
public class AVL<T extends Comparable<? super T>> {

    private AVLNode<T> root;
    private int size;

    /**
     * Adds the element to the tree.
     * <p>
     * Start by adding it as a leaf like in a regular BST and then rotate the
     * tree as necessary.
     * <p>
     * If the data is already in the tree, then nothing should be done (the
     * duplicate shouldn't get added, and size should not be incremented).
     * <p>
     * Remember to recalculate heights and balance factors while going back
     * up the tree after adding the element, making sure to rebalance if
     * necessary.
     *
     * @param data the data to add
     * @throws IllegalArgumentException if data is null
     */
    public void add(T data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }

        root = addH(root, data);
    }

    /**
     * Recursive helper method for add method.
     *
     * @param curr the root of the current subtree
     * @param data the data to add to the tree
     * @return the new root of the current subtree
     */
    private AVLNode<T> addH(AVLNode<T> curr, T data) {
        if (curr == null) {
            size++;
            return new AVLNode<>(data);
        }
        int comp = data.compareTo(curr.getData());
        if (comp > 0) {
            curr.setRight(addH(curr.getRight(), data));
        } else if (comp < 0) {
            curr.setLeft(addH(curr.getLeft(), data));
        }

        update(curr);
        return balance(curr);
    }

    /**
     * Removes and returns the element from the tree matching the given
     * parameter.
     * <p>
     * There are 3 cases to consider:
     * 1: The node containing the data is a leaf (no children). In this case,
     * simply remove it.
     * 2: The node containing the data has one child. In this case, simply
     * replace it with its child.
     * 3: The node containing the data has 2 children. Use the predecessor to
     * replace the data, NOT successor. As a reminder, rotations can occur
     * after removing the predecessor node.
     * <p>
     * Remember to recalculate heights and balance factors while going back
     * up the tree after removing the element, making sure to rebalance if
     * necessary.
     * <p>
     * Do not return the same data that was passed in. Return the data that
     * was stored in the tree.
     *
     * @param data the data to remove
     * @return the data that was removed
     * @throws IllegalArgumentException if data is null
     * @throws NoSuchElementException if the data is not found
     */
    public T remove(T data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }

        AVLNode<T> dummy = new AVLNode<>(null);
        root = removeH(root, dummy, data);
        return dummy.getData();
    }

    /**
     * Recursive helper method for remove method.
     *
     * @param curr the root of the current subtree
     * @param dummy extra node to store removed data
     * @param data the data to remove
     * @return the new root of the subtree
     */
    private AVLNode<T> removeH(AVLNode<T> curr, AVLNode<T> dummy, T data) {
        if (curr == null) {
            throw new NoSuchElementException();
        }
        int comp = data.compareTo(curr.getData());
        if (comp > 0) {
            curr.setRight(removeH(curr.getRight(), dummy, data));
        } else if (comp < 0) {
            curr.setLeft(removeH(curr.getLeft(), dummy, data));
        } else {
            dummy.setData(curr.getData());
            --size;
            if (curr.getRight() == null) {
                return curr.getLeft();
            } else if (curr.getLeft() == null) {
                return curr.getRight();
            } else {
                AVLNode<T> predecessor = new AVLNode<>(null);
                curr.setLeft(removePredecessor(curr.getLeft(), predecessor));
                curr.setData(predecessor.getData());
            }
        }

        update(curr);
        return balance(curr);
    }

    /**
     * Recursive helper method for removing the predecessor.
     *
     * @param curr the root of the current subtree
     * @param dummy extra node to store the predecessor
     * @return the new root of the subtree
     */
    private AVLNode<T> removePredecessor(AVLNode<T> curr, AVLNode<T> dummy) {
        if (curr.getRight() == null) {
            dummy.setData(curr.getData());
            return curr.getLeft();
        } else {
            curr.setRight(removePredecessor(curr.getRight(), dummy));
            update(curr);
            return balance(curr);
        }

    }

    /**
     * Returns the element from the tree matching the given parameter.
     * <p>
     * Do not return the same data that was passed in. Return the data that
     * was stored in the tree.
     *
     * @param data the data to search for in the tree
     * @return the data in the tree equal to the parameter
     * @throws IllegalArgumentException if data is null
     * @throws NoSuchElementException   if the data is not in the tree
     */
    public T get(T data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }

        AVLNode<T> res = getH(root, data);
        if (res == null) {
            throw new NoSuchElementException();
        }

        return res.getData();
    }

    /**
     * Recursive helper method for retrieving the data.
     *
     * @param curr the root of the current subtree
     * @param data the data to find in the tree
     * @return the data found in the tree
     */
    private AVLNode<T> getH(AVLNode<T> curr, T data) {
        if (curr == null) {
            return null;
        }
        int comp = data.compareTo(curr.getData());
        if (comp > 0) {
            return getH(curr.getRight(), data);
        } else if (comp < 0) {
            return getH(curr.getLeft(), data);
        } else {
            return curr;
        }
    }

    /**
     * Returns whether or not data matching the given parameter is contained
     * within the tree.
     *
     * @param data the data to search for in the tree.
     * @return true if the parameter is contained within the tree, false
     * otherwise
     * @throws IllegalArgumentException if data is null
     */
    public boolean contains(T data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        return getH(root, data) != null;
    }  

    /**
     * Returns the height of the root of the tree.
     *
     * @return the height of the root of the tree, -1 if the tree is empty
     */
    public int height() {
        return root == null ? -1 : root.getHeight();
    }

    /**
     * Clears the tree, including all data.
     */
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * Returns the root of the tree.
     *
     * @return the root of the tree
     */
    public AVLNode<T> getRoot() {
        return root;
    }

    /**
     * Returns the size of the tree.
     *
     * @return the size of the tree
     */
    public int size() {
        return size;
    }

    // *** FILL IN THE BELOW HELPER METHODS ***

    /**
     * Updates a node's height and balance factor, assuming that the node's
     * children were both already properly updated.
     *
     * @param node the node to update (assume non-null)
     * @implNote <b>Fill in this method with your code.</b>
     */
    private void update(AVLNode<T> node) {

        // Height of left child
        // If left child is null, its height is defined as -1 (AVL convention)
        int leftHeight = (node.getLeft() == null) ? -1 : node.getLeft().getHeight();

        // Height of right child
        // Same null convention applies here
        int rightHeight = (node.getRight() == null) ? -1 : node.getRight().getHeight();

        // Node's height is 1 + the maximum height of its children
        // The +1 accounts for the current node itself
        node.setHeight(1 + Math.max(leftHeight, rightHeight));

        // Balance factor = left subtree height - right subtree height
        // Positive  → left heavy
        // Negative  → right heavy
        // 0         → perfectly balanced
        node.setBalanceFactor(leftHeight - rightHeight);
    }

    /**
     * @param node the root of the current subtree (assumed to be unbalanced)
     * @return the new root of the balanced subtree
     * @implNote <b>Fill in this method with your code.</b>
     */
    private AVLNode<T> leftRotate(AVLNode<T> node) {

        // The right child will become the new root after rotation
        AVLNode<T> newRoot = node.getRight();

        // This subtree will be "transferred"
        // It becomes the right child of the old root after rotation
        AVLNode<T> transferSubtree = newRoot.getLeft();

        // Perform rotation:
        // Step 1: Move original root down to become left child of newRoot
        newRoot.setLeft(node);

        // Step 2: Attach transferred subtree as right child of old root
        node.setRight(transferSubtree);

        // IMPORTANT:
        // Update heights and balance factors bottom-up
        // First update the old root (now lower in tree)
        update(node);

        // Then update the new root
        update(newRoot);

        // Return new root of this subtree
        return newRoot;
    }

    /**
     * @param node the root of the current subtree (assumed to be unbalanced)
     * @return the new root of the balanced subtree
     * @implNote <b>Fill in this method with your code.</b>
     */
    private AVLNode<T> rightRotate(AVLNode<T> node) {

        // The left child will become the new root after rotation
        AVLNode<T> newRoot = node.getLeft();

        // This subtree will be transferred
        // It becomes the left child of the old root
        AVLNode<T> transferSubtree = newRoot.getRight();

        // Perform rotation:
        // Step 1: Move original root down to become right child of newRoot
        newRoot.setRight(node);

        // Step 2: Attach transferred subtree as left child of old root
        node.setLeft(transferSubtree);

        // Update bottom-up:
        // First update the old root (now lower)
        update(node);

        // Then update the new root
        update(newRoot);

        // Return new root of this subtree
        return newRoot;
    }

    /**
     * Checks if the passed in node needs to be balanced and calls
     * the relevant rotation operations, if necessary.
     *
     * @param node the root of the current subtree
     * @return the new root of the balanced subtree
     * @implNote <b>Fill in this method with your code.</b>
     */
    private AVLNode<T> balance(AVLNode<T> node) {

        // Base case: empty subtree
        if (node == null) {
            return null;
        }

        // Get balance factor of current node
        int bf = node.getBalanceFactor();

        // Case 1: Left heavy (balance factor > 1)
        if (bf > 1) {

            // Check for Left-Right (LR) case:
            // Left child is right heavy (negative balance factor)
            if (node.getLeft() != null 
                && node.getLeft().getBalanceFactor() < 0) {

                // First rotate left on left child
                node.setLeft(leftRotate(node.getLeft()));
            }

            // Then perform right rotation on current node
            return rightRotate(node);
        }

        // Case 2: Right heavy (balance factor < -1)
        if (bf < -1) {

            // Check for Right-Left (RL) case:
            // Right child is left heavy (positive balance factor)
            if (node.getRight() != null 
                && node.getRight().getBalanceFactor() > 0) {

                // First rotate right on right child
                node.setRight(rightRotate(node.getRight()));
            }

            // Then perform left rotation on current node
            return leftRotate(node);
        }

        // If balance factor is -1, 0, or 1, subtree is already balanced
        return node;
    }
}