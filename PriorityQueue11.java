/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sachin
 */
public interface PriorityQueue11 {
    /**
     * The Position interface represents a type that can
     * be used for the decreaseKey operation.
     */
    public interface Position {
        /**
         * Returns the value stored at this position.
         * @return the value stored at this position.
         */
        Comparable getValue( );
    }
    
    /**
     * Insert into the priority queue, maintaining heap order.
     * Duplicates are allowed.
     * @param x the item to insert.
     * @return may return a Position useful for decreaseKey.
     */
    Position insert( Comparable x );
    
    /**
     * Find the smallest item in the priority queue.
     * @return the smallest item.
     * @throws UnderflowException if empty.
     */
    Comparable findMin( );
    
    /**
     * Remove the smallest item from the priority queue.
     * @return the smallest item.
     * @throws UnderflowException if empty.
     */
    Comparable deleteMin( );
    
    /**
     * Test if the priority queue is logically empty.
     * @return true if empty, false otherwise.
     */
    boolean isEmpty( );
    
    /**
     * Make the priority queue logically empty.
     */
    void makeEmpty( );
    
    /**
     * Returns the size.
     * @return current size.
     */
    int size( );
    
    /**
     * Change the value of the item stored in the pairing heap.
     * This is considered an advanced operation and might not
     * be supported by all priority queues. A priority queue
     * will signal its intention to not support decreaseKey by
     * having insert return null consistently.
     * @param p any non-null Position returned by insert.
     * @param newVal the new value, which must be smaller
     *    than the currently stored value.
     * @throws IllegalArgumentException if p invalid.
     * @throws UnsupportedOperationException if appropriate.
     */
    void decreaseKey( Position p, Comparable newVal );
}