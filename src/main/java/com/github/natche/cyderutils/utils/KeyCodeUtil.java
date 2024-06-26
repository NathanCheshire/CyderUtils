package com.github.natche.cyderutils.utils;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

// todo seems like this could be an object, like CyderKeyEvent, build from code or KeyEvent
//  and can perform ops on it like the ones here or maybe even invoke via robot manager?

/** Utilities related to key codes/events. */
public final class KeyCodeUtil {
    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private KeyCodeUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns whether the provided key code is down on the arrow keys.
     *
     * @param code the key code
     * @return whether the provided key code is down on the arrow keys
     */
    public static boolean down(int code) {
        return code == KeyEvent.VK_DOWN;
    }

    /**
     * Returns whether the provided key code is down or right on the arrow keys.
     *
     * @param code the key code
     * @return whether the provided key code is down or right on the arrow keys
     */
    public static boolean downOrRight(int code) {
        return down(code) || code == KeyEvent.VK_RIGHT;
    }

    /**
     * Returns whether the provided key code is up on the arrow keys.
     *
     * @param code the key code
     * @return whether the provided key code is up on the arrow keys
     */
    public static boolean up(int code) {
        return code == KeyEvent.VK_UP;
    }

    /**
     * Returns whether the provided key code is up or left on the arrow keys.
     *
     * @param code the key code
     * @return whether the provided key code is up or left on the arrow keys
     */
    public static boolean upOrLeft(int code) {
        return up(code) || code == KeyEvent.VK_LEFT;
    }

    /**
     * Returns whether the provided key event contains the control key down.
     *
     * @param e the key event
     * @return whether the provided key event contains the control key down
     */
    public static boolean isControlC(KeyEvent e) {
        Preconditions.checkNotNull(e);

        return (e.getKeyCode() == KeyEvent.VK_C)
                && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0);
    }

    /**
     * Returns whether the provided key even contains control and alt held and down pressed.
     *
     * @param e the key event
     * @return whether the provided key even contains control and alt held and down pressed
     */
    public static boolean isControlAltDown(KeyEvent e) {
        Preconditions.checkNotNull(e);

        return (e.getKeyCode() == KeyEvent.VK_DOWN)
                && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0);
    }

    /**
     * Returns whether the provided key even contains control and alt held and right pressed.
     *
     * @param e the key event
     * @return whether the provided key even contains control and alt held and right pressed
     */
    public static boolean isControlAltRight(KeyEvent e) {
        Preconditions.checkNotNull(e);

        return (e.getKeyCode() == KeyEvent.VK_RIGHT)
                && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0);
    }

    /**
     * Returns whether the provided key even contains control and alt held and up pressed.
     *
     * @param e the key event
     * @return whether the provided key even contains control and alt held and up pressed
     */
    public static boolean isControlAltUp(KeyEvent e) {
        Preconditions.checkNotNull(e);

        return (e.getKeyCode() == KeyEvent.VK_UP)
                && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0);
    }

    /**
     * Returns whether the provided key even contains control and alt held and left pressed.
     *
     * @param e the key event
     * @return whether the provided key even contains control and alt held and left pressed
     */
    public static boolean isControlAltLeft(KeyEvent e) {
        Preconditions.checkNotNull(e);

        return (e.getKeyCode() == KeyEvent.VK_LEFT)
                && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0);
    }
}
