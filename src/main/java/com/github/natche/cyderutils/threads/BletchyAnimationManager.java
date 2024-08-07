package com.github.natche.cyderutils.threads;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.github.natche.cyderutils.exceptions.FatalException;
import com.github.natche.cyderutils.math.NumberUtil;
import com.github.natche.cyderutils.strings.StringUtil;
import com.github.natche.cyderutils.ui.pane.CyderOutputPane;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/** A class used to perform Bletchy animations on a specific JTextPane. */
public enum BletchyAnimationManager {
    /** The Bletchy animation manager instance. */
    INSTANCE;

    /** The number of iterations per char of the Bletchy animation. */
    @SuppressWarnings("FieldCanBeLocal")
    private final int iterationsPerChar = 5;

    /** Whether the Bletchy animation animator has been initialized. */
    private final AtomicBoolean initialized = new AtomicBoolean();

    /** Character list of all lowercase latin characters. */
    private final ImmutableList<Character> lowercaseAlphabetChars = ImmutableList.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    );

    /** Character list of all arabic digits. */
    private final ImmutableList<Character> digitChars = ImmutableList.of(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    );

    /** The Unicode index of the starting card suite character. */
    @SuppressWarnings("FieldCanBeLocal")
    private final int minCardSuiteIndex = 9824;

    /** The Unicode index of the ending card suite character. */
    @SuppressWarnings("FieldCanBeLocal")
    private final int maxCardSuiteIndex = 9835;

    /** Character list of all unicode card suit characters. */
    private final ImmutableList<Character> CARD_SUITS;

    {
        ArrayList<Character> ret = new ArrayList<>();

        for (int i = minCardSuiteIndex ; i <= maxCardSuiteIndex ; i++) {
            ret.add((char) i);
        }

        CARD_SUITS = ImmutableList.copyOf(ret);
    }

    /** Character list of unicode chars used for bletchy animations. */
    private final ImmutableList<Character> UNICODE_CHARS;

    /** The starting index of the unicode chars to use for bletchy animations. */
    @SuppressWarnings("FieldCanBeLocal")
    private final int usableUnicodeCharsStartIndex = 880;

    /** The ending index of the unicode chars to use for bletchy animations. */
    @SuppressWarnings("FieldCanBeLocal")
    private final int usableUnicodeEndIndex = 1023;

    {
        ArrayList<Character> ret = new ArrayList<>();

        for (int index = usableUnicodeCharsStartIndex ; index <= usableUnicodeEndIndex ; index++) {
            ret.add((char) index);
        }

        UNICODE_CHARS = ImmutableList.copyOf(ret);
    }

    /** The inner animator class. */
    private Animator animator;

    /** The output pane the bletchy animations will be printed to. */
    private CyderOutputPane outputPane;

    /** Common printing methods Cyder uses for JTextPane access. */
    private StringUtil printer;

    /**
     * Initializes the bletchy animations to be produced on the provided output pane.
     * Print calls are blocked during an animation.
     *
     * @param outputPane the output pane belonging to the console to use for printing
     */
    public void initialize(CyderOutputPane outputPane) {
        Preconditions.checkNotNull(outputPane);
        Preconditions.checkState(!initialized.get());

        initialized.set(true);

        this.outputPane = outputPane;
        this.printer = outputPane.getStringUtil();
    }

    /**
     * Deactivates this manager, removing set variables, objects, and trackers.
     *
     * @throws IllegalStateException if this manager has not been initialized via {@link #initialize(CyderOutputPane)}
     */
    public synchronized void deactivate() {
        Preconditions.checkState(initialized.get());

        this.outputPane = null;
        this.printer = null;

        kill();

        initialized.set(false);
    }

    /**
     * Returns whether this manager is initialized.
     *
     * @return whether this manager is initialized
     */
    public synchronized boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Invoke the bletchy decode animation with the following parameters on the linked JTextPane.
     *
     * @param decodeString the final string to decode and display after
     *                     the bletchy animation has finished
     * @param useNumbers   whether to use
     *                     numbers in the alphabetic characters for the animation
     * @param millisDelay  the millisecond delay in between animation frames
     * @param useUnicode   whether to use
     *                     more than just latin letters and possibly numbers
     */
    public void bletchy(String decodeString, boolean useNumbers, int millisDelay, boolean useUnicode) {
        Preconditions.checkState(initialized.get());
        Preconditions.checkNotNull(decodeString);
        Preconditions.checkArgument(!decodeString.isEmpty());
        Preconditions.checkArgument(millisDelay > 0);

        if (isActive() || YoutubeUuidCheckerManager.INSTANCE.hasActiveCheckers()) {
            // todo
            //            Console.INSTANCE.getConsoleCyderFrame().notify("Cannot start bletchy/YouTube thread"
            //                    + " at the same time as another instance.");
        } else {
            animator = new Animator(decodeString, useNumbers, useUnicode, millisDelay);
            animator.start();
        }
    }

    /** Inner class used to invoke the bletchy animation. */
    private class Animator {
        /** Whether the animation is active. */
        private final AtomicBoolean animationActive = new AtomicBoolean();

        /** The string to decode. */
        private final String decodeString;

        /** Whether to use numbers during the Bletchy animation. */
        private final boolean useNumbers;

        /** Whether to use unicode during the Bletchy animation. */
        private final boolean useUnicode;

        /** The delay in ms between prints. */
        private final int millisDelay;

        /** The Bletchy animation steps. */
        private final ArrayList<String> animationSteps;

        /**
         * Constructs and a new BletchyAnimator thread.
         *
         * @param decodeString the string to decode during the Bletchy animation
         * @param useNumbers   whether to use the numbers in the decode strings
         * @param useUnicode   whether to use unicode in the decode strings
         * @param millisDelay  the delay in ms between prints
         */
        Animator(String decodeString, boolean useNumbers, boolean useUnicode, int millisDelay) {
            Preconditions.checkNotNull(decodeString);
            Preconditions.checkArgument(!decodeString.isEmpty());
            Preconditions.checkArgument(millisDelay > 0);

            this.decodeString = decodeString;
            this.useNumbers = useNumbers;
            this.useUnicode = useUnicode;
            this.millisDelay = millisDelay;

            this.animationSteps = getBletchyArray(decodeString, useNumbers, useUnicode);
        }

        /** Starts the bletchy animation this animator is setup to perform. */
        private void start() {
            Preconditions.checkArgument(!animationActive.get());
            animationActive.set(true);

            String threadName = "Bletchy printing thread, finalString: \""
                    + animationSteps.get(animationSteps.size() - 1) + "\"";
            CyderThreadRunner.submit(() -> {
                try {
                    if (!outputPane.acquireLock()) {
                        throw new FatalException("Failed to acquire output pane lock");
                    }

                    animationSteps.forEach(print -> {
                        if (!animationActive.get()) return;

                        try {
                            printer.println(print);

                            ThreadUtil.sleep(millisDelay);

                            printer.removeLastElement();
                            printer.removeLastElement();

                            if (printer.documentContainsMoreThanDefaultElements()) {
                                printer.println("");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    printer.println(animationSteps.get(animationSteps.size() - 1));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    outputPane.releaseLock();
                    kill();
                }
            }, threadName);
        }

        /** Kills this bletchy thread. */
        public void kill() {
            animationActive.set(true);
        }

        /**
         * Returns whether this animator is active.
         *
         * @return whether this animator is active
         */
        public boolean isActive() {
            return animationActive.get();
        }

        /**
         * Returns the string to decode during the Bletchy animation.
         *
         * @return the string to decode during the Bletchy animation
         */
        public String getDecodeString() {
            return decodeString;
        }

        /**
         * Returns whether numbers will be used for this Bletchy animation.
         *
         * @return whether numbers will be used for this Bletchy animation
         */
        public boolean isUseNumbers() {
            return useNumbers;
        }

        /**
         * Returns whether unicode will be used for this Bletchy animation.
         *
         * @return whether unicode will be used for this Bletchy animation
         */
        public boolean isUseUnicode() {
            return useUnicode;
        }

        /**
         * Returns the millisecond delay between animation frames.
         *
         * @return the millisecond delay between animation frames
         */
        public int getMillisDelay() {
            return millisDelay;
        }
    }

    /**
     * Returns whether this BletchyThread has an animation thread underway.
     *
     * @return whether this BletchyThread has an animation thread underway
     */
    public boolean isActive() {
        return animator != null && animator.isActive();
    }

    /** Kills any and all bletchy printing threads */
    public void kill() {
        if (animator != null) {
            animator.kill();
            animator = null;
        }
    }

    /**
     * Returns an array list of Strings abiding by the parameters for a bletchy thread to print.
     *
     * @param decodeString the string to decode
     * @param useNumbers   a boolean turning on number usage
     * @param useUnicode   a boolean turning on random unicode chars
     * @return the array list of strings to be used by a bletchy thread
     */
    private ArrayList<String> getBletchyArray(String decodeString, boolean useNumbers, boolean useUnicode) {
        Preconditions.checkNotNull(decodeString);
        Preconditions.checkArgument(!decodeString.isEmpty());

        ArrayList<Character> charsToUse = new ArrayList<>(lowercaseAlphabetChars);
        if (useNumbers) {
            charsToUse.addAll(digitChars);
        }

        if (useUnicode) {
            charsToUse.addAll(CARD_SUITS);
            charsToUse.addAll(UNICODE_CHARS);
        }

        ArrayList<String> retList = new ArrayList<>();

        String decodeUsage = decodeString.toLowerCase().trim();
        int len = decodeUsage.length();

        IntStream.range(0, decodeUsage.length()).forEach(i ->
                IntStream.range(0, iterationsPerChar).forEach(j -> {
                    StringBuilder current = new StringBuilder();

                    IntStream.range(0, len + 1).forEach(k -> current.append(charsToUse.get(
                            NumberUtil.generateRandomInt(charsToUse.size() - 1))));

                    retList.add((decodeUsage.substring(0, i) + current.substring(i, len)).toUpperCase());
                }));

        retList.add(decodeUsage);
        return retList;
    }
}
