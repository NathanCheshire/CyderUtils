package com.github.natche.cyderutils.ui.pane;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.color.CyderColors;
import com.github.natche.cyderutils.exceptions.FatalException;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.strings.StringUtil;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.concurrent.Semaphore;

/**
 * A wrapper to associated a {@link JTextPane}, {@link StringUtil},
 * and {@link Semaphore} into a thread-safe happy little entity.
 * Note that this does not make the provided objects immutable or thread-safe.
 */
public class CyderOutputPane {
    /** The text used to generate a menu separation label. */
    private static final String magicMenuSepText = "NateCheshire";

    /** The starting x value for a menu separation. */
    private static final int menuSepX = 0;

    /** The starting y value for a menu separation. */
    private static final int menuSepY = 7;

    /** The width of menu separation components. */
    private static final int menuSepWidth = 175;

    /** The height of menu separation components. */
    private static final int menuSepHeight = 5;

    /** The bounds for a menu separation label. */
    private static final Rectangle menuSepBounds = new Rectangle(menuSepX, menuSepY, menuSepWidth, menuSepHeight);

    /** The default color of menu separator components. */
    private static final Color DEFAULT_MENU_SEP_COLOR = CyderColors.vanilla;

    /** The linked JTextPane. */
    private final JTextPane jTextPane;

    /** The StringUtil object to perform common operations on the JTextPane. */
    private final StringUtil stringUtil;

    /** The linked Semaphore to make appending/removing to/from the JTextPane thread-safe. */
    private final Semaphore semaphore;

    /** Instantiation not allowed unless all three arguments are provided */
    private CyderOutputPane() {
        throw new IllegalStateException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Constructs a new CyderOutputPane.
     *
     * @param jTextPane the JTextPane to link to this instance of CyderOutputPane
     */
    public CyderOutputPane(JTextPane jTextPane) {
        this.jTextPane = Preconditions.checkNotNull(jTextPane);

        stringUtil = new StringUtil(this);
        semaphore = new Semaphore(1);
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
     * Attempts to acquire the semaphore lock.
     *
     * @return whether the lock was acquired properly.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted") /* Readability */
    public boolean acquireLock() {
        try {
            semaphore.acquire();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Releases the semaphore lock. */
    public void releaseLock() {
        semaphore.release();
    }

    /**
     * Prints a menu separator to the {@link JTextPane} followed by a newline.
     *
     * @throws FatalException if the internal document cannot be appended to
     */
    public void printlnMenuSeparator() {
        try {
            stringUtil.printlnComponent(generateMenuSeparator());
            stringUtil.newline();
        } catch (BadLocationException ble) {
            throw new FatalException(ble);
        }
    }

    /**
     * Returns a new menu separator label.
     *
     * @return a new menu separator label
     */
    private JLabel generateMenuSeparator() {
        return generateMenuSeparator(DEFAULT_MENU_SEP_COLOR);
    }

    /**
     * Returns a new menu separator label.
     *
     * @return a new menu separator label
     */
    @SuppressWarnings("SameParameterValue")
    private JLabel generateMenuSeparator(Color color) {
        Preconditions.checkNotNull(color);

        JLabel sepLabel = new JLabel(magicMenuSepText) {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(getForeground());
                g.fillRect((int) menuSepBounds.getX(), (int) menuSepBounds.getY(),
                        (int) menuSepBounds.getWidth(), (int) menuSepBounds.getHeight());
                g.dispose();
            }
        };
        sepLabel.setForeground(color);
        return sepLabel;
    }
}
