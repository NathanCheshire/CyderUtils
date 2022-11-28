package cyder.time;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
     * The default poll timeout for the watchdog.
     */
    public static final int DEFAULT_POLL_TIMEOUT = 100;

    /**
     * The time in ms to wait between checking the AWT-EventQueue-0 thread for its status.
     */
    public static final int POLL_TIMEOUT;

    static {
        if (PropLoader.propExists("watchdog_poll_timeout")) {
            POLL_TIMEOUT = PropLoader.getInteger("watchdog_poll_timeout");
        } else {
            POLL_TIMEOUT = DEFAULT_POLL_TIMEOUT;
        }
    }

    /**
     * The standard name of the AWT-EventQueue-0 thread.
     */
    public static final String AWT_EVENT_QUEUE_0_NAME = "AWT-EventQueue-0";

    /**
     * The watchdog counter to detect a halt if it is not reset by the time
     * {@link #MAX_WATCHDOG_FREEZE_MS} value is reached.
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
            Logger.log(LogTag.WATCHDOG, "Watchdog deactivated from props");
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

    /**
     * Returns whether the watchdog counter should be incremented if the watch thread is in a particular state.
     *
     * @param threadState the state
     * @return whether the watchdog counter should be incremented if the watch thread is in a particular state
     */
    public static boolean shouldIncrementWatchdogForThreadState(Thread.State threadState) {
        Preconditions.checkNotNull(threadState);

        return switch (threadState) {
            case NEW, WAITING, TERMINATED -> false;
            case RUNNABLE, BLOCKED, TIMED_WAITING -> true;
        };
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

                ProgramState currentCyderState = ProgramStateManager.INSTANCE.getCurrentProgramState();

                if (currentCyderState.isShouldIncrementWatchdog()) {
                    if (shouldIncrementWatchdogForThreadState(currentAwtEventQueueThreadState)) {
                        watchdogCounter.getAndAdd(POLL_TIMEOUT);
                    }
                } else {
                    Logger.log(LogTag.WATCHDOG, "Watchdog not incremented as"
                            + " Cyder program state is: " + currentCyderState);
                }

                int currentFreezeLength = watchdogCounter.get();

                if (currentFreezeLength > maxSessionFreezeLength.get()) {
                    Logger.log(LogTag.WATCHDOG, "New max freeze detected by watchdog: "
                            + currentFreezeLength + TimeUtil.MILLISECOND_ABBREVIATION);
                    maxSessionFreezeLength.set(currentFreezeLength);
                }

                if (watchdogCounter.get() >= MAX_WATCHDOG_FREEZE_MS) {
                    onUiHaltDetected();
                    break;
                }
            }
        }, IgnoreThread.CyderWatchdog.getName());
    }

    /**
     * The actions to invoke when a UI halt is detected by the watchdog.
     */
    private static void onUiHaltDetected() {
        Logger.log(LogTag.WATCHDOG, "UI halt detected by watchdog; checking if bootstrap is possible");
        invokeBoostrapIfConditionsMet();
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
     *     <li>The operating system is {@link cyder.utils.OsUtil.OperatingSystem#WINDOWS}</li>
     *     <li>The attempt_boostrap prop is true if present</li>
     *     <li>The current JVM instance was not launched with JDWP args (debug mode)</li>
     * </ul>
     *
     * @return whether a bootstrap was possible and invoked
     */
    @CanIgnoreReturnValue
    private static boolean invokeBoostrapIfConditionsMet() {
        try {
            if (!OsUtil.isWindows()) {
                onFailedBoostrap("Invalid operating system: " + OsUtil.OPERATING_SYSTEM);
            } else if (JvmUtil.currentInstanceLaunchedWithDebug()) {
                onFailedBoostrap("Current JVM was launched with JDWP args");
            } else if (PropLoader.propExists("attempt_bootstrap")
                    && !PropLoader.getBoolean("attempt_bootstrap")) {
                onFailedBoostrap("attempt_boostrap prop set to false");
            } else {
                onBootstrapConditionsMet();
                return true;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            onFailedBoostrap(e.getMessage());
        }

        return false;
    }

    /**
     * Invokes a boostrap attempt after all of the proper conditions
     * outlined in {@link #invokeBoostrapIfConditionsMet()} are met.
     */
    private static void onBootstrapConditionsMet() {
        Logger.log(LogTag.WATCHDOG, "Boostrap conditions met");

        // todo remove me
        if (true) return;

        String resumeLogHash = SecurityUtil.generateUuid();

        // todo extract bootstrap methods out of Watchdog and move to Bootstrapper.java

        // todo need some kind of an argument to request to shutdown other instances if not singular instance
        //        String[] executionParams = new String[]{CMD_EXE, SLASH_C, JvmUtil.getFullJvmInvocationCommand(),
        //                "--resume-log-file", resumeLogHash};
        // todo use OS util method to execute

        try {
            // todo need a method in process util to run a string array command and get output from

            // todo remove --resume-log-file if present and pass in reference to current log file
            // Runtime.getRuntime().exec(executionParams);

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

    // todo Prop class <T> of some type such as boolean, hold default value,
    // instead of get(String string) we'll accept this prop class and have overloaded methods for return type
    // if not exist in props, we return the default value

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
