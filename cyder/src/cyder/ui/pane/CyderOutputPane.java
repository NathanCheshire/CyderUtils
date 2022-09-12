package cyder.ui.pane;

import com.google.common.base.Preconditions;
import cyder.handlers.internal.Logger;
import cyder.utils.StringUtil;

import javax.swing.*;
import java.util.concurrent.Semaphore;

/**
 * A wrapper to associated a JTextPane, StringUtil, and Semaphore
 * into a thread-safe happy little entity.
 * <p>
 * Note that this does not make the provided objects immutable and make defensive copies.
 */
public class CyderOutputPane {
    /**
     * The linked JTextPane.
     */
    private final JTextPane jTextPane;

    /**
     * The StringUtil object to perform common operations on the JTextPane.
     */
    private final StringUtil stringUtil;

    /**
     * The linked Semaphore to make appending/removing to/from the JTextPane thread-safe.
     */
    private final Semaphore semaphore;

    private static final String INSTANTIATION_MESSAGE = "Instances of CyderOutputPane are not allowed "
            + "unless all parameters are given at once";

    /**
     * Instantiation not allowed unless all three arguments are provided
     */
    private CyderOutputPane() {
        throw new IllegalStateException(INSTANTIATION_MESSAGE);
    }

    /**
     * Constructs a new CyderOutputPane.
     *
     * @param jTextPane  the JTextPane to link
     * @param stringUtil the StringUtil to use for the JTextPane
     */
    public CyderOutputPane(JTextPane jTextPane, StringUtil stringUtil) {
        Preconditions.checkNotNull(jTextPane);
        Preconditions.checkNotNull(stringUtil);

        this.jTextPane = jTextPane;
        this.stringUtil = stringUtil;
        semaphore = new Semaphore(1);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Constructs a new CyderOutputPane.
     *
     * @param jTextPane the JTextPane to link to this instance of CyderOutputPane
     */
    public CyderOutputPane(JTextPane jTextPane) {
        Preconditions.checkNotNull(jTextPane);

        this.jTextPane = jTextPane;
        stringUtil = new StringUtil(this);
        semaphore = new Semaphore(1);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the linked JTextPane object.
     *
     * @return the linked JTextPane object
     */
    public JTextPane getJTextPane() {
        return jTextPane;
    }

    /**
     * Returns the linked StringUtil object.
     *
     * @return the linked StringUtil object
     */
    public StringUtil getStringUtil() {
        return stringUtil;
    }

    /**
     * Returns the Semaphore which controls the JTextPane.
     *
     * @return the Semaphore which controls the JTextPane
     */
    public Semaphore getSemaphore() {
        return semaphore;
    }
}