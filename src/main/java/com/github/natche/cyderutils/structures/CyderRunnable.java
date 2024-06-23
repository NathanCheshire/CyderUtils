package com.github.natche.cyderutils.structures;

/**
 * A runnable which allows for an easy to read toString representation.
 */
public interface CyderRunnable extends Runnable {
    /**
     * Runs this runnable.
     */
    void run();

    /**
     * Returns a String representation of this runnable.
     *
     * @return a String representation of this runnable
     */
    String toString();

    /**
     * Returns a hash code representation of this runnable.
     *
     * @return a hash code representation of this runnable
     */
    int hashCode();

    /**
     * Returns whether the provided object is equal to this object.
     *
     * @param o the object to compare against this
     * @return whether the provided object is equal to this object
     */
    boolean equals(Object o);
}
