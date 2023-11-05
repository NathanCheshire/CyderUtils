package cyder.threads;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;

// todo

/**
 * Thread names to ignore when determining if Cyder should be classified as busy.
 */
public enum IgnoreThread {
    AwtEventQueue0("AWT-EventQueue-0", false),
    DestroyJavaVm("DestroyJavaVM", false);

    /**
     * The name associated with the thread to ignore.
     */
    private final String name;

    /**
     * Whether the thread is launched by Cyder internally or is a JVM process thread.
     */
    private final boolean isCyderThread;

    IgnoreThread(String name) {
        this(name, true);
    }

    IgnoreThread(String name, boolean isCyderThread) {
        this.name = name;
        this.isCyderThread = isCyderThread;
    }

    /**
     * Returns the name associated with this ignore thread.
     *
     * @return the name associated with this ignore thread
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether this thread is a cyder thread.
     *
     * @return whether this thread is a cyder thread
     */
    public boolean isCyderThread() {
        return isCyderThread;
    }

    /**
     * Returns a list of the names of all ignore threads.
     *
     * @return a list of the names of all ignore threads
     */
    public static ImmutableList<String> getNames() {
        ArrayList<String> ret = new ArrayList<>();
        Arrays.stream(values()).forEach(value -> ret.add(value.getName()));
        return ImmutableList.copyOf(ret);
    }
}
