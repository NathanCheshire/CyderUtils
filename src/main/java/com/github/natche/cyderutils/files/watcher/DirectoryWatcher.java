package com.github.natche.cyderutils.files.watcher;

import com.github.natche.cyderutils.files.FileUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.github.natche.cyderutils.exceptions.FatalException;
import com.github.natche.cyderutils.threads.CyderThreadRunner;
import com.github.natche.cyderutils.threads.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An observer of events and broker to subscribers of events which happen in a directory.
 * Instances of this class are immutable not thread safe. To achieve thread-safety,
 * clients should surround object method invocations using external synchronization
 * techniques.
 */
public class DirectoryWatcher {
    /** The file type a file path represents. */
    private enum FileType {
        FILE,
        DIRECTORY;

        public boolean isFile() {
            return this == FILE;
        }

        public boolean isDirectory() {
            return this == DIRECTORY;
        }
    }

    /**
     * A record to associate a file type with its size.
     *
     * @param type     the type of file
     * @param size     the size of the file/directory
     * @param numFiles the number of files, 1 for a file, 0 or more for a directory
     */
    private record FileTypeSize(FileType type, long size, int numFiles) {}

    /** The directory this watcher watches. */
    private final File watchDirectory;

    /** The default poll timeout. */
    private static final Duration DEFAULT_POLL_TIMEOUT = Duration.ofMillis(100);

    /** The timeout between checking the watch directory. */
    private long pollTimeout;

    /** The map of file paths to byte sizes last cached by this directory watcher. */
    private ImmutableMap<String, FileTypeSize> oldDirectoryContents = ImmutableMap.of();

    /** The subscribers of {@link WatchDirectoryEvent}s this watcher produces. */
    private final ArrayList<WatchDirectorySubscriber> subscribers = new ArrayList<>();

    /**
     * Constructs a new directory watcher.
     *
     * @param watchDirectory the directory to watch
     */
    public DirectoryWatcher(File watchDirectory) {
        this(watchDirectory, DEFAULT_POLL_TIMEOUT.toMillis());
    }

    /**
     * Constructs a new directory watcher.
     * Note this does not invoke {@link #startWatching()}.
     *
     * @param watchDirectory the directory to watch
     * @param pollTimeout    the timeout between checking the directory
     */
    public DirectoryWatcher(File watchDirectory, long pollTimeout) {
        Preconditions.checkNotNull(watchDirectory);
        Preconditions.checkArgument(watchDirectory.exists());
        Preconditions.checkArgument(watchDirectory.isDirectory());
        Preconditions.checkArgument(pollTimeout > 0);

        this.watchDirectory = watchDirectory;
        this.pollTimeout = pollTimeout;
    }

    /**
     * Returns the directory this watcher watches.
     *
     * @return the directory this watcher watches
     */
    public File getWatchDirectory() {
        return watchDirectory;
    }

    /**
     * Returns the timeout between directory content polls.
     *
     * @return the timeout between directory content polls
     */
    public long getPollTimeout() {
        return pollTimeout;
    }

    /**
     * Sets the timeout between directory content polls.
     *
     * @param pollTimeout the timeout between directory content polls
     */
    public void setPollTimeout(int pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    /** Whether this directory watcher is/should be active. */
    private final AtomicBoolean isWatching = new AtomicBoolean();

    /** Stops watching the watch directory if this watcher is active. */
    public void stopWatching() {
        isWatching.set(false);
    }

    /**
     * Returns whether this directory watcher is active.
     *
     * @return whether this directory watcher is active
     */
    public boolean isWatching() {
        return isWatching.get();
    }

    /**
     * Starts watching the watch directory for {@link WatchDirectoryEvent}s.
     *
     * @throws IllegalStateException if the watch directory DNE
     * @throws IllegalStateException if the directory is already being watched
     * @throws FatalException        if the watch directory is deleted while the watch subroutine is active
     */
    @SuppressWarnings("ConstantConditions") /* Unboxing of Long to long */
    public void startWatching() {
        Preconditions.checkState(watchDirectory.exists());
        Preconditions.checkState(!isWatching.get());

        isWatching.set(true);

        oldDirectoryContents = getUpdatedDirectoryContents();

        String threadName = "Directory Watcher, directory: " + watchDirectory.getAbsolutePath();
        CyderThreadRunner.submit(() -> {
            while (isWatching.get()) {
                if (!watchDirectory.exists()) {
                    throw new FatalException("Watch directory no longer exists: " + watchDirectory.getAbsolutePath());
                }

                ImmutableMap<String, FileTypeSize> newDirectoryContents = getUpdatedDirectoryContents();
                HashMap<String, FileTypeSize> unionContents = new HashMap<>(newDirectoryContents);
                unionContents.putAll(oldDirectoryContents);

                unionContents.keySet().forEach(path -> {
                    boolean inOldContents = oldDirectoryContents.containsKey(path);
                    boolean inNewContents = newDirectoryContents.containsKey(path);

                    File currentFilePointer = new File(path);

                    if (inOldContents && inNewContents) {
                        FileTypeSize oldTypeSize = oldDirectoryContents.get(path);
                        FileTypeSize newTypeSize = newDirectoryContents.get(path);

                        boolean sizesDifferent = oldTypeSize.size() != newTypeSize.size();
                        boolean numFilesDifferent = oldTypeSize.numFiles() != newTypeSize.numFiles();

                        if (sizesDifferent || numFilesDifferent) {
                            if (oldTypeSize.type() == FileType.DIRECTORY) {
                                notifySubscribers(WatchDirectoryEvent.DIRECTORY_MODIFIED, currentFilePointer);
                            } else {
                                notifySubscribers(WatchDirectoryEvent.FILE_MODIFIED, currentFilePointer);
                            }
                        }
                    } else if (inOldContents) {
                        if (oldDirectoryContents.get(path).type().isDirectory()) {
                            notifySubscribers(WatchDirectoryEvent.DIRECTORY_DELETED, currentFilePointer);
                        } else {
                            notifySubscribers(WatchDirectoryEvent.FILE_DELETED, currentFilePointer);
                        }
                    } else if (inNewContents) {
                        if (newDirectoryContents.get(path).type().isDirectory()) {
                            notifySubscribers(WatchDirectoryEvent.DIRECTORY_ADDED, currentFilePointer);
                        } else {
                            notifySubscribers(WatchDirectoryEvent.FILE_ADDED, currentFilePointer);
                        }
                    }
                });

                oldDirectoryContents = newDirectoryContents;

                ThreadUtil.sleep(pollTimeout);
            }

            cleanUpFromWatching();
        }, threadName);
    }

    /** Performs cleaning calls after the directory watching loop exits. */
    private void cleanUpFromWatching() {
        stopWatching();
    }

    /**
     * Polls the watch directory for its contents and returns the file paths and sizes.
     *
     * @return the list of directories/files and sizes contained in the watch directory
     */
    private ImmutableMap<String, FileTypeSize> getUpdatedDirectoryContents() {
        HashMap<String, FileTypeSize> ret = new HashMap<>();

        File[] files = watchDirectory.listFiles();
        if (files != null && files.length > 0) {
            Arrays.stream(files).forEach(file -> {
                FileType type = file.isFile() ? FileType.FILE : FileType.DIRECTORY;

                int numFiles = 1;
                if (file.isDirectory()) {
                    File[] length = file.listFiles();
                    if (length != null) numFiles = length.length;
                }

                long childSize = 0L;
                try {
                    childSize = FileUtil.getFileSize(file);
                } catch (IOException ignored) {}

                FileTypeSize add = new FileTypeSize(type, childSize, numFiles);
                ret.put(file.getAbsolutePath(), add);
            });
        }

        return ImmutableMap.copyOf(ret);
    }

    /**
     * Adds the provided subscriber to the list of subscribers to be notified when events
     * the subscriber is subscribed to occur.
     *
     * @param subscriber the subscriber to add
     */
    public void addSubscriber(WatchDirectorySubscriber subscriber) {
        Preconditions.checkNotNull(subscriber);
        Preconditions.checkState(!subscribers.contains(subscriber));
        subscribers.add(subscriber);
    }

    /**
     * Removes the provided subscriber from the list.
     *
     * @param subscriber the subscriber to remove
     */
    public void removeSubscriber(WatchDirectorySubscriber subscriber) {
        Preconditions.checkNotNull(subscriber);
        Preconditions.checkState(subscribers.contains(subscriber));
        subscribers.remove(subscriber);
    }

    /**
     * Publishes the event to all subscribers of the event.
     *
     * @param event     the event
     * @param eventFile a file pointer which caused the event
     */
    private void notifySubscribers(WatchDirectoryEvent event, File eventFile) {
        subscribers.stream()
                .filter(subscriber -> subscriber.getSubscriptions().contains(event))
                .filter(subscriber -> subscriber.patternsMatch(eventFile))
                .forEach(subscriber -> subscriber.onEvent(this, event, eventFile));
    }
}
