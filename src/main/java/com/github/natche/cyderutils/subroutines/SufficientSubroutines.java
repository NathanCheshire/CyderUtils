package com.github.natche.cyderutils.subroutines;

import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.threads.CyderThreadRunner;
import com.google.common.collect.ImmutableList;

/** A subroutine for completing startup subroutines which are not necessary for Cyder to run properly. */
public final class SufficientSubroutines {

    /** The name for the thread which executes the sequential subroutines. */
    private static final String SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME = "Sufficient Subroutine Executor";

    /** Suppress default constructor. */
    private SufficientSubroutines() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /** The subroutines to execute. */
    public static final ImmutableList<Subroutine> subroutines = ImmutableList.of(
            new Subroutine(() -> false, "name"),
            new Subroutine(() -> false, "other name")
    );

    /** Executes the sufficient subroutines in a separate thread. */
    public static void executeSubroutines() {
        CyderThreadRunner.submit(() -> {
            for (Subroutine sufficientSubroutine : subroutines) {
                CyderThreadRunner.submitSupplier(sufficientSubroutine.getRoutine(),
                        sufficientSubroutine.getThreadName());
            }
        }, SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME);
    }
}
