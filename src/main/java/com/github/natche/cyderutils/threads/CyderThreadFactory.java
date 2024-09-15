package com.github.natche.cyderutils.threads;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** A custom thread factory for Cyder. */
@Immutable
public final class CyderThreadFactory implements ThreadFactory {
    /** The name of this thread factory. */
    private final String name;

    /** The number of times {@link #newThread(Runnable)} has been invoked. */
    private final AtomicInteger newThreadInvocations;

    /**
     * Constructs a new thread factory using the provided name
     *
     * @param name the name of the thread factory
     * @throws NullPointerException     if the provided name is null
     * @throws IllegalArgumentException if the provided name is empty
     */
    public CyderThreadFactory(String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.trim().isEmpty());

        this.name = name;
        this.newThreadInvocations = new AtomicInteger();
    }

    /**
     * Returns the name of this thread factory.
     *
     * @return the name of this thread factory
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the number of times {@link #newThread(Runnable)} has been invoked.
     *
     * @return the number of times {@link #newThread(Runnable)} has been invoked
     */
    public int newThreadInvocationCount() {
        return newThreadInvocations.get();
    }

    /**
     * Returns a new thread using the provided runnable and name.
     *
     * @param runnable the runnable to use for the thread
     * @return a new thread using the provided runnable and name
     */
    @Override
    public Thread newThread(Runnable runnable) {
        Preconditions.checkNotNull(runnable);

        newThreadInvocations.incrementAndGet();
        return new Thread(runnable, name);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "CyderThreadFactory{"
                + "name=\"" + name + "\", "
                + "newThreadInvocations=\"" + newThreadInvocations.get() + "\""
                + "}";
    }

    /**
     * Returns a hashcode of this object.
     *
     * @return a hashcode of this object
     */
    @Override
    public int hashCode() {
        int ret = name.hashCode();
        ret = 31 * ret + Integer.hashCode(newThreadInvocations.get());
        return ret;
    }

    /**
     * Returns whether the provided object is equal to this.
     *
     * @param o the other object
     * @return whether the provided object is equal to this
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof CyderThreadFactory)) {
            return false;
        }

        CyderThreadFactory other = (CyderThreadFactory) o;
        return other.name.equals(name)
                && other.newThreadInvocations.get() == newThreadInvocations.get();
    }
}
