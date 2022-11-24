package cyder.time;

import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.utils.JvmUtil;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A watchdog timer for Cyder to detect a freeze on the GUI and kill the application.
 */
public final class CyderWatchdog {
    /**
     * The time in ms to wait between checking for the first appearance of AWT-EventQueue-0.
     */
    public static final int INITIALIZE_TIMEOUT_MS = 3000;

    /**
     * The time in ms to wait between checking the AWT-EventQueue-0 thread for its status.
     */
    public static final int POLL_TIMEOUT = 100; // todo make prop configurable

    /**
     * The standard name of the AWT-EventQueue-0 thread.
     */
    public static final String AWT_EVENT_QUEUE_0_NAME = "AWT-EventQueue-0";

    /**
     * The actual watchdog timer to detect a halt if it is not reset by the time a certain
     * value is reached.
     */
    private static final AtomicInteger watchdogCounter = new AtomicInteger();

    /**
     * The maximum number the watchdog counter can achieve before triggering a fatal reset.
     */
    public static final int MAX_WATCHDOG_FREEZE_MS = 5000;

    /**
     * The key to get whether the watchdog should be active from the props.
     */
    private static final String ACTIVATE_WATCHDOG = "activate_watchdog";

    /**
     * Whether the watchdog has been initialized and started.
     */
    private static final AtomicBoolean watchdogInitialized = new AtomicBoolean();

    /**
     * The name of the windows shell executable.
     */
    private static final String CMD_EXE = "cmd.exe";

    /**
     * The /C command line argument.
     */
    private static final String SLASH_C = "/C";

    /**
     * The previous state of the awt event queue thread.
     */
    private static Thread.State currentAwtEventQueueThreadState;

    /**
     * Suppress default constructor.
     */
    private CyderWatchdog() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the current state of the awt event queue thread.
     *
     * @return the current state of the awt event queue thread
     */
    public static Thread.State getCurrentAwtEventQueueThreadState() {
        return currentAwtEventQueueThreadState;
    }

    /**
     * Waits for the AWT-EventQueue-0 thread to spawn and then polls the thread's state
     * every {@link CyderWatchdog#POLL_TIMEOUT} checking to ensure the thread is not frozen.
     * Upon a possible freeze event, the system will exit and attempt to bootstrap if possible.
     * Note: the Watchdog will only start if the prop value <b>activate_watchdog</b> exists and is set to true.
     */
    public static void initializeWatchDog() {
        Preconditions.checkState(!watchdogInitialized.get());

        if (PropLoader.propExists(ACTIVATE_WATCHDOG) && !PropLoader.getBoolean(ACTIVATE_WATCHDOG)) {
            Logger.log(LogTag.WATCHDOG, "Watchdog skipped as prop is not set");
            return;
        } else if (JvmUtil.currentInstanceLaunchedWithDebug()) {
            Logger.log(LogTag.WATCHDOG, "Watchdog skipped as current JVM session was launched using debug");
            return;
        }

        watchdogInitialized.set(true);

        CyderThreadRunner.submit(() -> {
            while (true) {
                try {
                    ThreadUtil.sleep(INITIALIZE_TIMEOUT_MS);

                    ThreadUtil.getCurrentThreads().stream()
                            .filter(thread -> thread.getName().equals(AWT_EVENT_QUEUE_0_NAME))
                            .forEach(CyderWatchdog::startWatchDog);
                } catch (Exception e) {
                    Logger.log(LogTag.WATCHDOG, ExceptionHandler.getPrintableException(e));
                }
            }
        }, IgnoreThread.WatchdogInitializer.getName());
    }

    private enum WatchdogActionForThreadState {
        RUNNABLE(true, false),
        BLOCKED(false, true),
        WAITING(true, false),
        TIME_WAITING(false, true),
        UNKNOWN(true, false);

        /**
         * Whether an increment should be attempted.
         */
        private final boolean shouldIncrement;

        /**
         * Whether an exception should be thrown.
         */
        private final boolean shouldThrow;

        WatchdogActionForThreadState(boolean shouldIncrement, boolean shouldThrow) {
            this.shouldIncrement = shouldIncrement;
            this.shouldThrow = shouldThrow;
        }

        public boolean isShouldIncrement() {
            return shouldIncrement;
        }

        public boolean isShouldThrow() {
            return shouldThrow;
        }

        /**
         * Returns the watchdog action for the thread state provided.
         *
         * @param state the state
         * @return the watchdog action for the thread state provided.
         */
        public static WatchdogActionForThreadState getWatchdogActionForThreadState(Thread.State state) {
            Preconditions.checkNotNull(state);

            return switch (state) {
                case NEW, TERMINATED -> UNKNOWN;
                case RUNNABLE -> RUNNABLE;
                case BLOCKED -> BLOCKED;
                case WAITING -> WAITING;
                case TIMED_WAITING -> TIME_WAITING;
            };
        }
    }

    /**
     * Starts the watchdog checker after the AWT-EventQueue-0 thread has been started.
     *
     * @param awtEventQueueThread the AWT-EventQueue-0 thread
     * @throws IllegalArgumentException if the provided thread
     *                                  is not named {@link CyderWatchdog#AWT_EVENT_QUEUE_0_NAME}
     */
    private static void startWatchDog(Thread awtEventQueueThread) {
        Preconditions.checkArgument(awtEventQueueThread.getName().equals(AWT_EVENT_QUEUE_0_NAME));

        AtomicInteger maxSessionFreezeLength = new AtomicInteger();

        currentAwtEventQueueThreadState = awtEventQueueThread.getState();

        CyderThreadRunner.submit(() -> {
            while (true) {
                ThreadUtil.sleep(POLL_TIMEOUT);

                attemptWatchdogReset();

                currentAwtEventQueueThreadState = awtEventQueueThread.getState();
                WatchdogActionForThreadState action = WatchdogActionForThreadState
                        .getWatchdogActionForThreadState(currentAwtEventQueueThreadState);
                // todo switch on this

                // todo lets have a map of thread states to log messages and a last thread state stored here
                // todo should increment watchdog prop in program state enum
                ProgramState currentCyderState = ProgramStateManager.INSTANCE.getCurrentProgramState();
                if (currentCyderState.isShouldIncrementWatchdog()) {
                    Logger.log(LogTag.WATCHDOG, "Watchdog incremented as "
                            + "Cyder program state is: " + currentCyderState);
                } else {
                    Logger.log(LogTag.WATCHDOG, "Watchdog not incremented as "
                            + "Cyder program state is: " + currentCyderState);
                }

                // todo will be determined above
                watchdogCounter.getAndAdd(POLL_TIMEOUT);

                int currentFreezeLength = watchdogCounter.get();

                if (currentFreezeLength > maxSessionFreezeLength.get()) {
                    Logger.log("New max freeze detected by watchdog: " + currentFreezeLength
                            + TimeUtil.MILLISECOND_ABBREVIATION);
                    maxSessionFreezeLength.set(currentFreezeLength);
                }

                if (watchdogCounter.get() >= MAX_WATCHDOG_FREEZE_MS) {
                    Logger.log("UI halt detected by watchdog; checking if bootstrap is possible");
                    checkIfBoostrapPossible();
                }
            }
        }, IgnoreThread.CyderWatchdog.getName());
    }

    /**
     * Attempts to reset the watchdog counter using the AWT event dispatching thread.
     * If the thread is currently blocked, the counter will not be reset.
     */
    private static void attemptWatchdogReset() {
        SwingUtilities.invokeLater(() -> watchdogCounter.set(0));
    }

    /**
     * Checks for whether a boostrap can be attempted and if possible, attempts to bootstrap.
     * The following conditions must be met in order for a boostrap to be attempted:
     *
     * <ul>
     *     <li>The JVM instance was launched from a jar file</li>
     *     <li>The operating system is {@link cyder.utils.OsUtil.OperatingSystem#WINDOWS}</li>
     *     <li>The current JVM instance was not launched with JDWP args (debug mode)</li>
     * </ul>
     */
    private static void checkIfBoostrapPossible() {
        try {
            if (!OsUtil.isWindows()) {
                // todo test on Kali, Process API might act different
                onFailedBoostrap("Invalid operating system: " + OsUtil.OPERATING_SYSTEM);
            } else if (JvmUtil.currentInstanceLaunchedWithDebug()) {
                onFailedBoostrap("Current JVM was launched with JDWP args");
            } else {
                onBootstrapConditionsMet();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            onFailedBoostrap(e.getMessage());
        }
    }

    /**
     * Invokes a boostrap attempt after all of the proper conditions
     * outlined in {@link #checkIfBoostrapPossible()} are met.
     */
    private static void onBootstrapConditionsMet() {
        Logger.log(LogTag.WATCHDOG, "Boostrap conditions met");

        String resumeLogHash = SecurityUtil.generateUuid();

        // todo extract bootstrap methods out of Watchdog and move to Bootstrapper.java

        // todo need some kind of an argument to request to shutdown other instances if not singular instance
        String[] executionParams = new String[]{CMD_EXE, SLASH_C, JvmUtil.getFullJvmInvocationCommand(),
                "--resume-log-file", resumeLogHash};

        try {
            // todo need a method in process util to run a string array command and get output from

            // todo remove --resume-log-file if present and pass in reference to current log file
            Runtime.getRuntime().exec(executionParams);

            // todo send and be done, new client should request to end this session and we should comply
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        // todo get command, generate hashes, send, and start socket in sep process
    }

    // todo start writing to resume log file if present, insert bootstrap into it and then a debug call
    //  or actually bootstrap log tag and say bootstrap successful, if log file couldn't be used log that too

    // todo need to validate key props on start too? sufficient subroutine for that with a key validator util?
    // todo key util with validation and getter methods?

    /**
     * Logs a watchdog tagged log message with the provided reason and exits
     * with the exit condition of {@link ExitCondition#WatchdogBootstrapFail}.
     *
     * @param reason the reason the bootstrap  failed
     */
    @ForReadability
    private static void onFailedBoostrap(String reason) {
        Preconditions.checkNotNull(reason);
        Preconditions.checkArgument(!reason.isEmpty());

        Logger.log(LogTag.WATCHDOG, "Failed to boostrap: " + reason);
        OsUtil.exit(ExitCondition.WatchdogBootstrapFail);
    }
}
