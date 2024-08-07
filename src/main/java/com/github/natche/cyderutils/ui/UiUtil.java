package com.github.natche.cyderutils.ui;

import com.github.natche.cyderutils.annotations.ForReadability;
import com.github.natche.cyderutils.constants.CyderRegexPatterns;
import com.github.natche.cyderutils.constants.HtmlTags;
import com.github.natche.cyderutils.enumerations.Extension;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.image.CyderImage;
import com.github.natche.cyderutils.time.TimeUtil;
import com.github.natche.cyderutils.ui.drag.DragLabelButtonSize;
import com.github.natche.cyderutils.ui.exceptions.DeviceNotFoundException;
import com.github.natche.cyderutils.ui.frame.CyderFrame;
import com.github.natche.cyderutils.utils.OsUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.natche.cyderutils.ui.UiConstants.*;

/** Utilities to control, update, modify, and create CyderFrames and ui components. */
public final class UiUtil {
    /** The map of supported prop configurable drag label button sizes to button size objects. */
    private static final ImmutableMap<String, DragLabelButtonSize> dragLabelButtonRepresentations =
            new ImmutableMap.Builder<String, DragLabelButtonSize>()
                    .put("small", DragLabelButtonSize.SMALL)
                    .put("medium", DragLabelButtonSize.MEDIUM)
                    .put("large", DragLabelButtonSize.LARGE)
                    .put("full_drag_label", DragLabelButtonSize.FULL_DRAG_LABEL)
                    .build();

    /** Suppress default constructor. */
    private UiUtil() {
        throw new IllegalMethodException("Instances of UiUtils are not allowed");
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
        return ImmutableList.copyOf(Arrays.stream(Frame.getFrames())
                .filter(f -> f instanceof CyderFrame frame && !frame.isDisposed())
                .map(frame -> (CyderFrame) frame)
                .collect(Collectors.toList()));
    }

    /**
     * Returns a list of non CyderFrame frame objects opened by this instance.
     *
     * @return a list of non CyderFrame frame objects opened by this instance
     */
    public static ImmutableList<Frame> getNonCyderFrames() {
        return ImmutableList.copyOf(Arrays.stream(Frame.getFrames())
                .filter(f -> !(f instanceof CyderFrame)).collect(Collectors.toList()));
    }

    /**
     * Saves a screenshot of all CyderFrames to the user's Files/ directory.
     *
     * @throws IOException if a screenshot cannot be saved to the system
     */
    public static void screenshotCyderFrames() throws IOException {
        for (CyderFrame cyderFrame : getCyderFrames()) {
            if (cyderFrame.isVisible()) {
                screenshotCyderFrame(cyderFrame);
            }
        }
    }

    /**
     * Saves a screenshot of the CyderFrame with the provided name to the user's Files/ directory.
     *
     * @param cyderFrameName the name of the CyderFrame to screenshot
     * @return whether the screenshot was successfully saved
     * @throws IOException if the provided CyderFrame cannot save the screenshot
     */
    public static boolean screenshotCyderFrame(String cyderFrameName) throws IOException {
        for (CyderFrame cyderFrame : getCyderFrames()) {
            if (cyderFrame.getTitle().equalsIgnoreCase(cyderFrameName)) {
                return screenshotCyderFrame(cyderFrame, generateScreenshotSaveFile(cyderFrame));
            }
        }

        return false;
    }

    /** The max allowable length when including a frame's title in a filename. */
    public static final int MAX_FRAME_TITLE_FILE_LENGTH = 15;

    /**
     * Saves a screenshot of the CyderFrame with the provided name to the current user's Files/ directory.
     *
     * @param cyderFrame the CyderFrame to screenshot
     * @return the file reference the screenshot was saved to. Null indicates the save filed
     * @throws IOException if the screenshot cannot be saved to the system
     */
    @CanIgnoreReturnValue
    public static File screenshotCyderFrame(CyderFrame cyderFrame) throws IOException {
        Preconditions.checkNotNull(cyderFrame);

        File saveFile = generateScreenshotSaveFile(cyderFrame);
        if (screenshotCyderFrame(cyderFrame, saveFile)) {
            return saveFile;
        }
        return null;
    }

    /**
     * Generates the file to save the screenshot of the provided frame to.
     *
     * @param cyderFrame the frame
     * @return the file to save the screenshot of the provided frame to
     * @throws IOException if the operating system fails to create the file.
     */
    @ForReadability
    private static File generateScreenshotSaveFile(CyderFrame cyderFrame) throws IOException {
        String saveName = cyderFrame.getTitle()
                .substring(0, Math.min(MAX_FRAME_TITLE_FILE_LENGTH, cyderFrame.getTitle().length())).trim();
        String timestampSuffix = TimeUtil.screenshotTime();
        String filename = saveName + "_" + timestampSuffix + Extension.PNG.getExtension();

        File saveDir = new File("."); // todo pass in? builder lol?
        File createFile = new File(saveDir, filename);
        if (!OsUtil.createFile(createFile, true)) {
            throw new IOException("Failed to create file: " + createFile);
        }
        return createFile;
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
            CyderImage image = CyderImage.fromComponent(frame);
            ret = ImageIO.write(image.getBufferedImage(), Extension.PNG.getExtensionWithoutPeriod(), saveFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Attempts to set the provided frame to the requested position.
     * Note this position is relative meaning absolute positioning should be used
     * if multiple monitors are being used on the host OS.
     * <p>
     * This method also ensures that no part of the frame goes out of bounds of the monitor(s) meaning
     * the requested position might not be the position the frame is set to.
     *
     * @param requestPoint the point to set the frame to if valid
     * @param frame        the frame to set the location/size of
     */
    public static void requestFramePosition(Point requestPoint, CyderFrame frame) {
        Preconditions.checkNotNull(requestPoint);
        Preconditions.checkNotNull(frame);

        Rectangle mergedMonitors = getMergedMonitors();

        int absoluteMinX = mergedMonitors.x;
        int absoluteMinY = mergedMonitors.y;
        int totalWidth = mergedMonitors.width;
        int totalHeight = mergedMonitors.height;

        int requestedX = (int) requestPoint.getX();
        int requestedY = (int) requestPoint.getY();

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
        return getGraphicsDevices().stream().filter(device -> Integer.parseInt(device.getIDstring()
                        .replaceAll(CyderRegexPatterns.nonNumberRegex, "")) == id)
                .findFirst().orElseThrow(() -> new DeviceNotFoundException("Could not find device with id: " + id));
    }

    /** Closes all instances of {@link Frame} by invoking {@link Frame#dispose()} on all instances. */
    public static void disposeAllFrames() {
        Arrays.stream(Frame.getFrames()).forEach(Frame::dispose);
    }

    /**
     * Disposes all instances of {@link Frame}. If a frame is an instance of CyderFrame,
     * fastClose follows the value provided.
     *
     * @param fastClose    whether to fastClose any instances of CyderFrame
     * @param ignoreFrames frames to not dispose if encountered
     */
    public static void disposeAllFrames(boolean fastClose, Frame... ignoreFrames) {
        ImmutableList<Frame> ignores = ImmutableList.copyOf(ignoreFrames);
        Arrays.stream(Frame.getFrames())
                .filter(frame -> !ignores.contains(frame))
                .forEach(frame -> {
                    if (frame instanceof CyderFrame cyderFrame) {
                        cyderFrame.dispose(fastClose);
                    } else {
                        frame.dispose();
                    }
                });
    }

    /**
     * Disposes all instances of {@link CyderFrame}.
     *
     * @param fastClose whether to invoke fast close on all CyderFrames found
     */
    public static void closeAllCyderFrames(boolean fastClose) {
        getCyderFrames().forEach(frame -> frame.dispose(fastClose));
    }

    /** Repaints all valid instances of CyderFrame. */
    public static void repaintCyderFrames() {
        getCyderFrames().forEach(CyderFrame::repaint);
    }

    /**
     * Minimizes all {@link Frame} instances by setting their state to {@link Frame#ICONIFIED}.
     * Found {@link CyderFrame}s have their {@link CyderFrame#minimizeAndIconify()} invoked instead.
     */
    public static void minimizeAllFrames() {
        getFrames().forEach(frame -> {
            if (frame instanceof CyderFrame cyderFrame) {
                cyderFrame.minimizeAndIconify();
            } else {
                frame.setState(UiConstants.FRAME_ICONIFIED);
            }
        });
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
            if (frame.getState() == UiConstants.FRAME_NORMAL) {
                frame.minimizeAndIconify();
            } else {
                frame.setState(UiConstants.FRAME_NORMAL);
            }
        };
    }

    /** The index which determines which color to choose for the border color. */
    private static final AtomicInteger colorIndex = new AtomicInteger();

    /**
     * Generates a key adapter to use for a field to invoke the provided runnable.
     *
     * @param typed    whether the runnable should be invoked when a key is typed
     * @param pressed  whether the runnable should be invoked when a key is pressed
     * @param released whether the runnable should be invoked when a key is released
     * @param runnable the runnable to invoke
     * @return the generated key adapter
     */
    public static KeyAdapter generateKeyAdapter(boolean typed,
                                                boolean pressed,
                                                boolean released,
                                                Runnable runnable) {
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
    public static MouseAdapter generateUiActionLoggingMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // todo used to be logging here, need some hook
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
        ArrayList<DisplayMode> ret = new ArrayList<>();
        Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
                .forEach(device -> ret.add(device.getDisplayMode()));

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
                -> ret.append(" ").append(HtmlTags.breakTag));
        ret.append(" ").append(HtmlTags.closingHtml);
        return ret.toString();
    }

    /**
     * Returns the drag label button size corresponding to the provided string.
     *
     * @param size the size string
     * @return the drag label button size
     */
    public static DragLabelButtonSize dragLabelButtonSizeFromString(String size) {
        Preconditions.checkNotNull(size);
        Preconditions.checkArgument(!size.isEmpty());

        return dragLabelButtonRepresentations.get(size);
    }

    /**
     * Returns whether the provided frame has not had a drag event during its lifetime.
     *
     * @param frame the frame
     * @return whether the provided frame has not had a drag event during its lifetime
     */
    public static boolean frameNotYetDragged(CyderFrame frame) {
        Point restorePoint = frame.getRestorePoint();
        return restorePoint.getX() == CyderFrame.FRAME_NOT_YET_DRAGGED
                || restorePoint.getY() == CyderFrame.FRAME_NOT_YET_DRAGGED;
    }

    /**
     * Returns whether there are multiple monitors attached to this device currently.
     *
     * @return whether there are multiple monitors attached to this device currently
     */
    public static boolean areMultipleMonitors() {
        return getGraphicsDevices().size() > 1;
    }

    /**
     * Generates a neffex label used for the center of debug lines if enabled.
     *
     * @param length      the length of the label
     * @param color       the color of the label
     * @param strokeWidth the stroke width of the lines
     * @return the generated neffex label
     */
    public static JLabel generateNeffexLabel(int length, Color color, int strokeWidth) {
        JLabel label = new JLabel() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

                // Top left triangle
                g2d.drawLine(length / 4, 0, length / 2, length / 2);
                g2d.drawLine(length / 2, length / 2, 0, length / 2);
                g2d.drawLine(0, length / 2, length / 4, 0);

                // Top right triangle
                g2d.drawLine(length / 2, length / 2, length * 3 / 4, 0);
                g2d.drawLine(length * 3 / 4, 0, length, length / 2);
                g2d.drawLine(length, length / 2, length / 2, length / 2);

                // Bottom primary triangle
                g2d.drawLine(length / 8, length / 4, length * 7 / 8, length / 4);
                g2d.drawLine(length * 7 / 8, length / 4, length / 2, length);
                g2d.drawLine(length / 2, length, length / 8, length / 4);
            }
        };
        label.setSize(length, length);
        return label;
    }

    /**
     * Generates and returns a new abstract action which invokes the provided runnable.
     *
     * @param runnable the runnable
     * @return a new abstract action which invokes the provided runnable
     */
    public static AbstractAction generateAbstractAction(Runnable runnable) {
        Preconditions.checkNotNull(runnable);

        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        };
    }

    /** The alignment for a {@link JTextPane}s {@link javax.swing.text.StyledDocument}. */
    public enum JTextPaneAlignment {
        LEFT, CENTER, RIGHT
    }

    /**
     * Sets the alignment of the provided text pane's document to the provided alignment.
     *
     * @param jTextPane the text pane
     * @param alignment the desired alignment
     */
    public static void setJTextPaneDocumentAlignment(JTextPane jTextPane, JTextPaneAlignment alignment) {
        Preconditions.checkNotNull(jTextPane);
        Preconditions.checkNotNull(alignment);

        int alignmentConstant = switch (alignment) {
            case LEFT -> StyleConstants.ALIGN_LEFT;
            case CENTER -> StyleConstants.ALIGN_CENTER;
            case RIGHT -> StyleConstants.ALIGN_RIGHT;
        };

        StyledDocument document = jTextPane.getStyledDocument();
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributeSet, alignmentConstant);
        document.setParagraphAttributes(0, document.getLength(), attributeSet, false);
    }

    /** Initializes all ui-manager look and feel key-value props. */
    public static void initializeUiAndSystemProps() {
        initializeUiManagerTooltipProps();
        UIManager.put(SLIDER_ONLY_LEFT_MOUSE_DRAG, Boolean.TRUE);
    }

    /** Initializes UIManager tooltip key-value props. */
    private static void initializeUiManagerTooltipProps() {
        UIManager.put(TOOLTIP_BACKGROUND, TOOLTIP_BACKGROUND_COLOR);
        UIManager.put(TOOLTIP_BORDER, TOOLTIP_BORDER_RESOURCE);
        UIManager.put(TOOLTIP_FONT_KEY, TOOLTIP_FONT);
        UIManager.put(TOOLTIP_FOREGROUND, TOOLTIP_FOREGROUND_COLOR);
    }
}
