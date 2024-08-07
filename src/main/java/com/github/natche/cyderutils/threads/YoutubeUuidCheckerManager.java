package com.github.natche.cyderutils.threads;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.time.TimeUtil;
import com.github.natche.cyderutils.ui.pane.CyderOutputPane;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/** A manager for {@link YoutubeUuidChecker}s. */
public enum YoutubeUuidCheckerManager {
    /** The YouTube UUID checker manager instance. */
    INSTANCE;

    /** The linked {@link CyderOutputPane}. */
    private CyderOutputPane outputPane;

    /** Whether any instances of helper YouTube threads are running. */
    private boolean isActive;

    /** The number of urls checked during the current instance of the YouTube thread(s). */
    private final AtomicInteger urlsChecked = new AtomicInteger();

    /** The last time the user was notified of the current rate. */
    private long lastNotifyTime;

    /** The frequency in seconds to notify the user of the time remaining until all YouTube uuids have been checked. */
    private static final Duration notifyUserOfRateFrequency = Duration.ofSeconds(1);

    /** The list of active YouTube uuid checkers. */
    private final ArrayList<YoutubeUuidChecker> youTubeUuidCheckers = new ArrayList<>();

    /** The time this YouTube thread session started. */
    private final AtomicLong startTime = new AtomicLong();

    /**
     * Attempts to acquire the output pane's lock.
     *
     * @return whether the lock was acquired
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean acquireLock() {
        return outputPane.acquireLock();
    }

    /** Releases the output pane's lock. */
    public void releaseLock() {
        outputPane.releaseLock();
    }

    /**
     * Returns the number of active uuid checkers.
     *
     * @return the number of active uuid checkers
     */
    public int getActiveUuidCheckersLength() {
        return youTubeUuidCheckers.size();
    }

    /** Whether the Youtube UUID checker manager has been initialized. */
    private final AtomicBoolean initialized = new AtomicBoolean();

    /**
     * Sets the master YouTube JTextPane, and its linked semaphore.
     *
     * @param outputPane the output pane to use for appending text to and to acquire the locking semaphore
     */
    public void initialize(CyderOutputPane outputPane) {
        Preconditions.checkNotNull(outputPane);
        Preconditions.checkArgument(!initialized.get());

        initialized.set(true);

        this.outputPane = outputPane;
    }

    /** Reverts this manager to the non-initialized state. */
    public void deactivate() {
        Preconditions.checkArgument(initialized.get());

        killAll();

        initialized.set(false);

        this.outputPane = null;
    }

    /** Kills any instances of helper YouTube threads that are currently running. */
    public void killAll() {
        youTubeUuidCheckers.forEach(youtubeUuidChecker -> {
            String lastCheckedUuid = youtubeUuidChecker.kill();
            // todo do something with last checked?
        });
        youTubeUuidCheckers.clear();

        isActive = false;
    }

    /**
     * Starts the provided number of YouTube helper threads to check UUIDs.
     *
     * @param number the number of threads to start
     */
    public void start(int number) {
        Preconditions.checkArgument(number > 0);
        Preconditions.checkState(initialized.get());

        checkIfStartingFirstThreads();

        IntStream.range(0, number).forEach(i -> {
            YoutubeUuidChecker checker = new YoutubeUuidChecker(outputPane);
            checker.startChecking();
            youTubeUuidCheckers.add(checker);
        });

        isActive = true;
    }

    /**
     * Checks the size of the YouTube uuid checkers list of active checkers. If empty,
     * resets the variables used to notify the user of the estimated time to completion.
     */
    private void checkIfStartingFirstThreads() {
        if (youTubeUuidCheckers.isEmpty()) {
            urlsChecked.set(0);
            startTime.set(System.currentTimeMillis());
            lastNotifyTime = System.currentTimeMillis();
        }
    }

    /** Increments the urls checked counter. */
    public void incrementUrlsChecked() {
        Preconditions.checkState(initialized.get());

        urlsChecked.getAndIncrement();
        checkIfShouldNotifyOfRate();
    }

    /**
     * Returns whether one or more {@link YoutubeUuidChecker} are active.
     *
     * @return whether one or more {@link YoutubeUuidChecker} are active
     */
    public boolean hasActiveCheckers() {
        return isActive;
    }

    /** Checks for whether the user should be notified of the current uuid check rate. */
    private void checkIfShouldNotifyOfRate() {
        if (System.currentTimeMillis() - lastNotifyTime > notifyUserOfRateFrequency.toMillis()) {
            notifyOfRate();
            lastNotifyTime = System.currentTimeMillis();
        }
    }

    /** Notifies the user of the current calculated rate of urls checked each minute. */
    private void notifyOfRate() {
        long timeTaken = System.currentTimeMillis() - startTime.get();
        float urlsPerMs = urlsChecked.get() / (float) timeTaken;
        double urlsPerSecond = urlsPerMs * TimeUtil.millisInSecond;
        double urlsPerMinute = urlsPerSecond * TimeUtil.secondsInMinute;

        // todo store these off and allow accessor for current rate and accept a unit such as urls per sec, min, etc.
    }
}
