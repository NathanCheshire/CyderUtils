package cyderutils.threads;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

import java.util.concurrent.ThreadFactory;

/**
 * A custom thread factory for Cyder.
 */
@Immutable
public final class CyderThreadFactory implements ThreadFactory {
    /**
     * The name of this thread factory.
     */
    private final String name;

    /**
     * Constructs a new thread factory using the provided name
     *
     * @param name the name of the thread factory
     * @throws NullPointerException if the provided name is null
     * @throws IllegalArgumentException if the provided name is empty
     */
    public CyderThreadFactory(String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.trim().isEmpty());

        this.name = name;
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
     * Returns a new thread using the provided runnable and name.
     *
     * @param runnable the runnable to use for the thread
     * @return a new thread using the provided runnable and name
     */
    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(Preconditions.checkNotNull(runnable), name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CyderThreadFactory{name=\"" + name + "\"}";
    }
}
