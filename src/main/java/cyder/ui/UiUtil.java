package cyder.ui;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.HtmlTags;
import cyder.enums.Extension;
import cyder.exceptions.DeviceNotFoundException;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.time.TimeUtil;
import cyder.ui.drag.DragLabelButtonSize;
import cyder.ui.frame.CyderFrame;
import cyder.user.UserUtil;
import cyder.utils.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Utilities to control, update, modify, and create CyderFrames and ui components.
 */
public final class UiUtil {
    /**
     * Suppress default constructor.
     */
    private UiUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns a list of frames currently opened by this Jvm instance.
     *
     * @return a list of frames currently opened by this Jvm instance
     */
    public static ImmutableList<Frame> getFrames() {
        return ImmutableList.copyOf(Frame.getFrames());
    }

    /**
     * Returns a list of CyderFrames currently opened by this instance.
     *
     * @return a list of CyderFrames currently opened by this instance
     */
    public static ImmutableList<CyderFrame> getCyderFrames() {
        ArrayList<CyderFrame> ret = new ArrayList<>();

        for (Frame f : Frame.getFrames()) {
            if (f instanceof CyderFrame cyderFrame && !cyderFrame.isDisposed()) {
                ret.add((CyderFrame) f);
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns a list of non CyderFrame frame objects opened by this instance.
     *
     * @return a list of non CyderFrame frame objects opened by this instance
     */
    public static ImmutableList<Frame> getNonCyderFrames() {
        ArrayList<Frame> ret = new ArrayList<>();

        for (Frame f : Frame.getFrames()) {
            if (!(f instanceof CyderFrame)) {
                ret.add(f);
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Saves a screenshot of all CyderFrames to the user's Files/ directory.
     */
    public static void screenshotCyderFrames() {
        for (CyderFrame frame : getCyderFrames()) {
            if (frame.isVisible() && frame.getWidth() >= CyderFrame.MINIMUM_WIDTH
                    && frame.getHeight() >= CyderFrame.MINIMUM_HEIGHT) {
                screenshotCyderFrame(frame);
            }
        }
    }

    /**
     * Saves a screenshot of the CyderFrame with the provided name to the user's Files/ directory.
     *
     * @param cyderFrameName the name of the CyderFrame to screenshot
     * @return whether the screenshot was successfully saved
     */
    public static boolean screenshotCyderFrame(String cyderFrameName) {
        for (CyderFrame frame : getCyderFrames()) {
            if (frame.getTitle().equalsIgnoreCase(cyderFrameName)) {
                return screenshotCyderFrame(frame, generateScreenshotSaveFile(frame));
            }
        }

        return false;
    }

    /**
     * The max allowable length when including a frame's title in a filename.
     */
    public static final int MAX_FRAME_TITLE_FILE_LENGTH = 15;

    /**
     * Saves a screenshot of the CyderFrame with the provided name to the user's Files/ directory.
     *
     * @param cyderFrame the CyderFrame to screenshot
     */
    public static void screenshotCyderFrame(CyderFrame cyderFrame) {
        Preconditions.checkNotNull(cyderFrame);

        screenshotCyderFrame(cyderFrame, generateScreenshotSaveFile(cyderFrame));
    }

    @ForReadability
    private static File generateScreenshotSaveFile(CyderFrame cyderFrame) {
        String saveName = cyderFrame.getTitle().substring(0, Math.min(MAX_FRAME_TITLE_FILE_LENGTH,
                cyderFrame.getTitle().length()));
        return UserUtil.createFileInUserSpace(saveName.trim() + "_"
                + TimeUtil.logTime().trim() + Extension.PNG.getExtension());
    }

    /**
     * Saves a screenshot of the provided CyderFrame to the provided reference file.
     *
     * @param frame the CyderFrame to take a screenshot of
     * @return whether the screenshot was successfully saved
     */
    public static boolean screenshotCyderFrame(CyderFrame frame, File saveFile) {
        Preconditions.checkNotNull(frame);
        Preconditions.checkNotNull(saveFile);

        boolean ret = false;

        try {
            ret = ImageIO.write(ImageUtil.screenshotComponent(frame),
                    Extension.PNG.getExtensionWithoutPeriod(), saveFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Attempts to set the provided frame to the monitor specified,
     * if valid, with the provided starting location.
     *
     * @param requestPoint the point to position the frame's top left at
     * @param frame        the frame to set the location/size of
     */
    public static void requestFramePosition(Point requestPoint, CyderFrame frame) {
        Preconditions.checkNotNull(requestPoint);
        Preconditions.checkNotNull(frame);

        requestFramePosition(requestPoint.x, requestPoint.y, frame);
    }

    /**
     * Attempts to set the provided frame to the monitor specified,
     * if valid, with the provided starting location.
     *
     * @param requestedX the x value to set the frame to
     * @param requestedY the y value to set the frame to
     * @param frame      the frame to set the location/size of
     */
    public static void requestFramePosition(int requestedX, int requestedY, CyderFrame frame) {
        Preconditions.checkNotNull(frame);

        Rectangle mergedMonitors = getMergedMonitors();

        int absoluteMinX = mergedMonitors.x;
        int absoluteMinY = mergedMonitors.y;
        int totalWidth = mergedMonitors.width;
        int totalHeight = mergedMonitors.height;

        // Correct complete horizontal over/underflow
        if (requestedX + frame.getWidth() > absoluteMinX + totalWidth) {
            requestedX = absoluteMinX + totalWidth - frame.getWidth();
        } else if (requestedX < absoluteMinX) {
            requestedX = absoluteMinX;
        }

        // Correct complete vertical over/underflow
        if (requestedY + frame.getHeight() > absoluteMinY + totalHeight) {
            requestedY = absoluteMinY + totalHeight - frame.getHeight();
        } else if (requestedY < absoluteMinY) {
            requestedY = absoluteMinY;
        }

        frame.setLocation(requestedX, requestedY);
    }

    /**
     * Returns a list of the graphics devices found from the local graphics environment.
     *
     * @return a list of the graphics devices found from the local graphics environment
     */
    public static ImmutableList<GraphicsDevice> getGraphicsDevices() {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ImmutableList.copyOf(graphicsEnvironment.getScreenDevices());
    }

    /**
     * Returns a rectangle representing the currently connected monitors to this PC and their coalesced bounds.
     *
     * @return a rectangle representing the currently connected monitors to this PC and their coalesced bounds
     */
    public static Rectangle getMergedMonitors() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (GraphicsDevice device : getGraphicsDevices()) {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();

            minX = Math.min(minX, bounds.x);
            minY = Math.min(minY, bounds.y);
            maxX = Math.max(maxX, bounds.x + bounds.width);
            maxY = Math.max(maxY, bounds.y + bounds.height);
        }

        return new Rectangle(minX, minY, Math.abs(maxX - minX), Math.abs(maxY - minY));
    }

    /**
     * Returns whether the monitor the frame is on is full screen supported.
     *
     * @param frame the frame
     * @return whether the monitor the frame is on is full screen supported
     */
    public static boolean frameMonitorIsFullscreenSupported(CyderFrame frame) {
        Preconditions.checkNotNull(frame);

        return getGraphicsDevice(frame.getMonitor()).isFullScreenSupported();
    }

    /**
     * Returns the graphics device with the provided id if found.
     *
     * @param id the id
     * @return the device with the provided id
     * @throws DeviceNotFoundException if a device with the provided id cannot be found
     */
    public static GraphicsDevice getGraphicsDevice(int id) {
        for (GraphicsDevice device : getGraphicsDevices()) {
            int localId = Integer.parseInt(device.getIDstring()
                    .replaceAll(CyderRegexPatterns.nonNumberRegex, ""));
            if (localId == id) {
                return device;
            }
        }

        throw new DeviceNotFoundException("Could not find device with id: " + id);
    }

    /**
     * Closes all instances of {@link Frame} by invoking {@link Frame#dispose()} on all instances.
     */
    public static void closeAllFrames() {
        for (Frame frame : Frame.getFrames()) {
            frame.dispose();
        }
    }

    /**
     * Closes all instances of Frame. If a frame is an instance of CyderFrame,
     * fastClose follows the value provided.
     *
     * @param fastClose    whether to fastClose any instances of CyderFrame
     * @param ignoreFrames frames to not dispose if encountered
     */
    public static void closeAllFrames(boolean fastClose, Frame... ignoreFrames) {
        for (Frame frame : Frame.getFrames()) {
            boolean skip = false;

            if (ignoreFrames.length > 0) {
                for (Frame ignoreFrame : ignoreFrames) {
                    if (frame.equals(ignoreFrame)) {
                        skip = true;
                        break;
                    }
                }
            }

            if (skip) continue;

            if (frame instanceof CyderFrame cyderFrame) {
                cyderFrame.dispose(fastClose);
            } else {
                frame.dispose();
            }
        }
    }

    /**
     * Closes all instances of CyderFrame.
     *
     * @param fastClose whether to invoke fast close on all CyderFrames found
     */
    public static void closeAllCyderFrames(boolean fastClose) {
        for (CyderFrame f : getCyderFrames()) {
            f.dispose(fastClose);
        }
    }

    /**
     * Repaints all valid instances of CyderFrame.
     */
    public static void repaintCyderFrames() {
        for (CyderFrame frame : getCyderFrames()) {
            frame.repaint();
        }
    }

    /**
     * Minimizes all {@link Frame} instances by setting their state to {@link Frame#ICONIFIED}.
     * Found {@link CyderFrame}s have their {@link CyderFrame#minimizeAndIconify()} invoked instead.
     */
    public static void minimizeAllFrames() {
        for (Frame f : getFrames()) {
            if (f instanceof CyderFrame) {
                ((CyderFrame) f).minimizeAndIconify();
            } else {
                f.setState(Frame.ICONIFIED);
            }
        }
    }

    /**
     * Generates the common runnable invoked when a CyderFrame TaskbarIcon is clicked in the Console menu.
     *
     * @param frame the CyderFrame to create the runnable for
     * @return the common runnable invoked when a CyderFrame TaskbarIcon is clicked in the Console menu
     */
    public static Runnable generateCommonFrameTaskbarIconRunnable(CyderFrame frame) {
        Preconditions.checkNotNull(frame);

        return () -> {
            if (frame.getState() == Frame.NORMAL) {
                frame.minimizeAndIconify();
            } else {
                frame.setState(Frame.NORMAL);
            }
        };
    }

    /**
     * The index which determines which color to choose for the border color.
     */
    private static int colorIndex;

    /**
     * Returns the color to be associated with a CyderFrame's TaskbarIcon border color.
     *
     * @return the color to be associated with a CyderFrame's TaskbarIcon border color
     */
    public static Color getTaskbarBorderColor() {
        Color ret = CyderColors.TASKBAR_BORDER_COLORS.get(colorIndex);
        colorIndex++;

        if (colorIndex > CyderColors.TASKBAR_BORDER_COLORS.size() - 1) {
            colorIndex = 0;
        }

        return ret;
    }

    /**
     * Generates a key adapter to use for a field to invoke the provided runnable.
     *
     * @param typed    whether the runnable should be invoked when a key is typed
     * @param pressed  whether the runnable should be invoked when a key is pressed
     * @param released whether the runnable should be invoked when a key is released
     * @param runnable the runnable to invoke
     * @return the generated key adapter
     */
    public static KeyAdapter generateKeyAdapter(boolean typed, boolean pressed, boolean released, Runnable runnable) {
        Preconditions.checkNotNull(runnable);

        return new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (typed) {
                    runnable.run();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (pressed) {
                    runnable.run();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (released) {
                    runnable.run();
                }
            }
        };
    }

    /**
     * Returns the common mouse adapter linked to all cyder ui components to log when they are clicked.
     *
     * @return the common mouse adapter linked to all cyder ui components to log when they are clicked
     */
    public static MouseAdapter generateCommonUiLogMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(LogTag.UI_ACTION, e.getComponent());
            }
        };
    }

    /**
     * Closes the frame using fast close if not null.
     *
     * @param cyderFrame the frame to fast close if not null
     */
    public static void closeIfOpen(CyderFrame cyderFrame) {
        closeIfOpen(cyderFrame, true);
    }

    /**
     * Closes the provided frame if not null.
     *
     * @param cyderFrame the frame to close
     * @param fastClose  whether to fast close the frame
     */
    public static void closeIfOpen(CyderFrame cyderFrame, boolean fastClose) {
        if (cyderFrame != null) {
            cyderFrame.dispose(fastClose);
        }
    }

    /**
     * Returns whether the provided component is visible.
     *
     * @return whether the provided component is visible
     */
    public static boolean notNullAndVisible(Component component) {
        return component != null && component.isVisible();
    }

    /**
     * Returns the width of the monitor the provided frame is on.
     *
     * @param frame the frame
     * @return the width of the monitor the provided frame is on
     */
    public static int getMonitorWidth(CyderFrame frame) {
        Preconditions.checkNotNull(frame);

        return (int) frame.getMonitorBounds().getWidth();
    }

    /**
     * Returns the height of the monitor the provided frame is on.
     *
     * @param frame the frame
     * @return the height of the monitor the provided frame is on
     */
    public static int getMonitorHeight(CyderFrame frame) {
        Preconditions.checkNotNull(frame);

        return (int) frame.getMonitorBounds().getHeight();
    }

    /**
     * Returns the default graphics configuration monitor width.
     *
     * @return the default graphics configuration monitor width
     */
    public static int getDefaultMonitorWidth() {
        return (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    }

    /**
     * Returns the default graphics configuration monitor height.
     *
     * @return the default graphics configuration monitor height
     */
    public static int getDefaultMonitorHeight() {
        return (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    }

    /**
     * Returns the Windows taskbar height of the primary monitor.
     *
     * @return the Windows taskbar height of the primary monitor
     */
    public static int getWindowsTaskbarHeight() {
        return getDefaultScreenInsets().bottom;
    }

    /**
     * Returns the insets of the default graphics device.
     *
     * @return the insets of the default graphics device
     */
    public static Insets getDefaultScreenInsets() {
        GraphicsDevice defaultScreenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration defaultConfiguration = defaultScreenDevice.getDefaultConfiguration();
        return Toolkit.getDefaultToolkit().getScreenInsets(defaultConfiguration);
    }

    /**
     * Returns the Windows taskbar width of the primary monitor.
     *
     * @return the Windows taskbar width of the primary monitor
     */
    public static int getWindowsTaskbarWidth() {
        int ret = (int) (getDefaultMonitorWidth() - GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds().getWidth());
        return ret == 0 ? getDefaultMonitorWidth() : ret;
    }

    /**
     * Returns a list of all display modes for all connected graphics devices.
     *
     * @return a list of all display modes for all connected graphics devices
     */
    public static ImmutableList<DisplayMode> getMonitorDisplayModes() {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = graphicsEnvironment.getScreenDevices();

        ArrayList<DisplayMode> ret = new ArrayList<>();
        Arrays.stream(devices).forEach(device -> ret.add(device.getDisplayMode()));

        return ImmutableList.copyOf(ret);
    }

    /**
     * Generates and returns a new {@link JTextPane} with the
     * {@link JTextPane#getScrollableTracksViewportWidth()} overridden
     * to ensure the horizontal scroll bar is never visible.
     *
     * @return the custom JTextPane
     */
    public static JTextPane generateJTextPaneWithInvisibleHorizontalScrollbar() {
        return new JTextPane() {
            /**
             * Overridden to disable horizontal scrollbar since setting
             * the policy doesn't work apparently, thanks JDK devs.
             */
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }
        };
    }

    /**
     * Generates the text to use for a custom component that extends JLabel to
     * for the component to paint with the necessary size for the component
     * to be visible. This is a Cyder specific method.
     *
     * @param numLines the number of lines of text to return
     * @return the text to use for the JLabel's text
     */
    public static String generateTextForCustomComponent(int numLines) {
        Preconditions.checkArgument(numLines > 0);

        StringBuilder ret = new StringBuilder();
        ret.append(HtmlTags.openingHtml);

        IntStream.range(0, numLines).forEach(index
                -> ret.append(CyderStrings.space).append(HtmlTags.breakTag));
        ret.append(CyderStrings.space).append(HtmlTags.closingHtml);
        return ret.toString();
    }

    /**
     * Returns the drag label button size corresponding to the provided string.
     *
     * @param size the size string
     * @return the drag label button size
     */
    public static DragLabelButtonSize dragLabelButtonSizeFromString(String size) {
        return switch (size) {
            case "small" -> DragLabelButtonSize.SMALL;
            case "medium" -> DragLabelButtonSize.MEDIUM;
            case "large" -> DragLabelButtonSize.LARGE;
            case "full_drag_label" -> DragLabelButtonSize.FULL_DRAG_LABEL;
            default -> throw new FatalException("Invalid drag label button size specified by prop: " + size);
        };
    }
}