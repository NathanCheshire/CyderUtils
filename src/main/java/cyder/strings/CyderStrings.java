package cyder.strings;

import com.google.common.collect.ImmutableList;
import cyder.exceptions.IllegalMethodException;

/**
 * Common strings used throughout Cyder.
 */
public final class CyderStrings {
    /**
     * A string to be displayed when an illegal constructor is invoked.
     */
    public static final String ILLEGAL_CONSTRUCTOR = "Illegal constructor";

    /**
     * General renowned sentence in English for using all 26 latin chars.
     */
    public static final String QUICK_BROWN_FOX = "The quick brown fox jumps over the lazy dog";

    /**
     * Error message for static classes upon attempted instantiation.
     */
    public static final String ATTEMPTED_INSTANTIATION = "Instances are not permitted for this class";

    /**
     * A bullet point character used for numerous purposes.
     */
    public static final String BULLET_POINT = "•";

    /**
     * The echo char to use for any instance of CyderPasswordField.
     */
    public static final char ECHO_CHAR = '•';

    /**
     * The downward pointing triangle character (▼).
     */
    public static final String DOWN_ARROW = "▼";

    /**
     * A string used to denote something is not available.
     */
    public static final String NOT_AVAILABLE = "N/A";

    /**
     * The string used for loading labels when waiting for future processes to complete.
     */
    public static final String LOADING = "Loading...";

    /**
     * A newline character.
     */
    public static final String newline = "\n";

    /**
     * The NULL string.
     */
    public static final String NULL = "NULL";

    /**
     * The NUL string.
     */
    public static final String NUL = "NUL";

    /**
     * The strings counted as "null" and empty by various String util methods.
     */
    public static final ImmutableList<String> NULL_STRINGS = ImmutableList.of(CyderStrings.NULL, CyderStrings.NUL);

    /**
     * The empty string.
     */
    public static final String EMPTY = "";

    /**
     * A null character.
     */
    public static final String nullChar = "\0";

    /**
     * A carriage return char.
     */
    public static final String carriageReturnChar = "\r";

    // todo need an ellipse util that allows n starting, k ending, middle chars, etc.
    /**
     * The dots for a title or menu item that is cut off due to being too long.
     */
    public static final String ellipseDots = "...";

    /**
     * Suppress default constructor.
     *
     * @throws cyder.exceptions.IllegalMethodException if invoked.
     */
    private CyderStrings() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
    }
}