package cyder.constants;

import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

/**
 * A class of commonly used integers throughout Cyder
 */
public final class CyderNumbers {
    /**
     * Don't change your number.
     */
    public static final int JENNY = 8675309;

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private CyderNumbers() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
