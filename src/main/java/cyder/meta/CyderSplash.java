package cyder.meta;

import com.google.common.base.Preconditions;
import cyder.animation.HarmonicRectangle;
import cyder.constants.CyderColors;
import cyder.enumerations.Direction;
import cyder.enumerations.ExitCondition;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.Props;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.enumerations.FrameType;
import cyder.ui.label.CyderLabel;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The splash screen for Cyder when it is originally first launched.
 */
public enum CyderSplash {
    /**
     * The CyderSplash enum instance.
     */
    INSTANCE;

    /**
     * The padding from the borders to the Cyder logo.
     */
    private static final int LOGO_BORDER_PADDING = 40;

    /**
     * The padding between the top/bottom of the frame and the harmonic rectangles.
     */
    private static final int harmonicYPadding = 10;

    /**
     * The padding between harmonic rectangles themselves.
     */
    private static final int harmonicXInnerPadding = 10;

    /**
     * The number of harmonic rectangles to draw
     */
    private static final int numHarmonicRectangles = 20;

    /**
     * The name for the loading thread.
     */
    private static final String SPLASH_LOADER_THREAD_NAME = "Splash Loader";

    /**
     * The frame title.
     */
    private static final String FRAME_TITLE = "Cyder Splash";

    /**
     * The main splash animator thread name.
     */
    private static final String SPLASH_ANIMATOR = "Splash Animation";

    /**
     * The text for the fatal exception information popup.
     */
    private static final String FATAL_EXCEPTION_TEXT = "Splash failed to be disposed; Console failed to load";

    /**
     * The post close action for the fatal exception information popup frame.
     */
    private static final Runnable FATAL_EXCEPTION_POST_CLOSE_ACTION = () -> OsUtil.exit(ExitCondition.FatalTimeout);

    /**
     * The startup exception text for the fatal exception information popup frame title.
     */
    private static final String STARTUP_EXCEPTION = "Startup Exception";

    /**
     * The name of the fast dispose thread. The method might wait before disposing if the
     * allow splash completion prop is set to true.
     */
    private static final String FAST_DISPOSE_THREAD_NAME = "Cyder Splash Dispose Waiter";

    /**
     * The horizontal padding for the animation letter blocks.
     */
    private static final int blockHorizontalPadding = 20;

    /**
     * The animation increment for the letter blocks.
     */
    private static final int blockAnimationIncrement = 5;

    /**
     * The animation delay for the letter blocks.
     */
    private static final int blockAnimationDelay = 6;

    /**
     * The minor axis length of animated borders.
     */
    private static final int minorAxisBorderLength = 10;

    /**
     * The animated border timeout.
     */
    private static final int borderAnimationTimeout = 3;

    /**
     * The animated border increment.
     */
    private static final int borderAnimationIncrement = 5;

    /**
     * The creator label text.
     */
    private static final String creatorText = "by Nate Cheshire";

    /**
     * The creator label animation timeout.
     */
    private static final int creatorLabelAnimationTimeout = 5;

    /**
     * The creator label animation increment.
     */
    private static final int creatorLabelAnimationIncrement = 5;

    /**
     * The thread name for the loading label updater.
     */
    private static final String LOADING_LABEL_UPDATER = "Splash Loading Label Updater";

    /**
     * The offset from the bottom of the frame for the loading label.
     */
    private static final int LOADING_LABEL_BOTTOM_OFFSET = 100;

    /**
     * The default harmonic padding.
     */
    private static final int defaultHarmonicPadding = 20;

    /**
     * The harmonic rectangle minimum height.
     */
    private static final int harmonicMinHeight = 40;

    /**
     * The harmonic rectangle maximum height.
     */
    private static final int harmonicMaxHeight = 70;

    /**
     * The harmonic animation increment.
     */
    private static final int harmonicAnimationInc = 2;

    /**
     * The harmonic animation delay.
     */
    private static final int harmonicAnimationDelay = 15;

    /**
     * The length of the C and Y icons.
     */
    private static final int ICON_LEN = 150;

    /**
     * The length of the primary letter axis.
     */
    private static final int LETTER_LEN = 30;

    /**
     * The width of the C icon lines.
     */
    private static final int C_BLOCK_WIDTH = 95;

    /**
     * The height of the C icon lines.
     */
    private static final int C_BLOCK_HEIGHT = 25;

    /**
     * The width of the y block.
     */
    private static final int Y_BLOCK_WIDTH = 95;

    /**
     * The default loading message for the splash to display.
     */
    private static final String DEFAULT_LOADING_MESSAGE = "Loading Components";

    /**
     * Whether the splash has been disposed this instance.
     */
    private final AtomicBoolean disposed = new AtomicBoolean();

    /**
     * The width and height for the borderless splash frame.
     */
    private static final int FRAME_LEN = 600;

    /**
     * The length of the blue borders that animate in to surround the Cyder logo.
     */
    private static final int LOGO_BORDER_LEN = 200;

    /**
     * The timeout between loading label updates.
     */
    private static final int loadingLabelUpdateTimeout = 25;

    /**
     * The maximum seconds of the splash should be visible for.
     */
    private static final int showLoadingLabelTimeout = 30 * 1000;

    /**
     * The number of times to update the loading label.
     */
    private static final int loadingLabelUpdateIterations = (showLoadingLabelTimeout) / loadingLabelUpdateTimeout;

    /**
     * The timeout before starting to display loading messages after finishing the splash animation.
     */
    private static final int loadingMessageStartTimeout = 500;

    /**
     * The font used for the loading label messages.
     */
    private final Font loadingLabelFont = new Font("Agency FB", Font.BOLD, 40);

    /**
     * The name of the font for the developer signature label.
     */
    private static final String developerSignatureFontName = "Condiment";

    /**
     * The milliseconds before all harmonic rectangles will have started their animation.
     */
    private static final int millisToStartAllHarmonicRectangles = 1000;

    /**
     * The name of the thread which animates the harmonic rectangles together.
     */
    private static final String harmonicRectangleAnimationThreadName = "Splash Harmonic Rectangle Animator";

    /**
     * The font used for the author signature.
     */
    private final Font developerSignatureFont = new Font(developerSignatureFontName, Font.BOLD, 50);

    /**
     * The list of harmonic rectangles.
     */
    private final ArrayList<HarmonicRectangle> harmonicRectangles = new ArrayList<>();

    /**
     * The semaphore for adding or removing harmonic rectangles.
     */
    private final Semaphore harmonicRectangleSemaphore = new Semaphore(1);

    /**
     * The center point to place the console frame/login frame at if the splash frame is relocated during startup.
     */
    private final AtomicReference<Point> relocatedCenterPoint = new AtomicReference<>();

    /**
     * Whether the splash animation has completed.
     * The splash completes as the developer name finishes animating in and the harmonic rectangles are started.
     */
    private final AtomicBoolean splashAnimationCompleted = new AtomicBoolean();

    /**
     * The window adapter for the splash frame.
     */
    private final WindowAdapter splashFrameWindowAdapter = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            stopHarmonicRectangles();
        }

        @Override
        public void windowClosed(WindowEvent e) {
            stopHarmonicRectangles();
        }
    };
    /**
     * The color for the generated animation border sides.
     */
    private final Color animationBorderSideColor = CyderColors.regularBlue;

    /**
     * Whether the splash screen has been shown.
     */
    private boolean splashShown;

    /**
     * The splash screen CyderFrame.
     */
    private CyderFrame splashFrame;

    /**
     * The end drag event callback to set the console relative position if the splash frame is moved before disposal.
     */
    private final Runnable dragEventCallback = () -> relocatedCenterPoint.set(splashFrame.getCenterPointOnScreen());

    /**
     * The label used to display what Cyder is currently doing in the startup routine.
     */
    private CyderLabel loadingLabel;

    /**
     * The loading message to display on the loading label.
     */
    private final AtomicReference<String> loadingMessage = new AtomicReference<>(DEFAULT_LOADING_MESSAGE);

    /**
     * The C icon.
     */
    private ImageIcon C_ICON = null;

    /**
     * The Y icon.
     */
    private ImageIcon Y_ICON = null;

    /**
     * Whether the harmonic rectangles should be animating currently.
     */
    private final AtomicBoolean shouldAnimateHarmonicRectangles = new AtomicBoolean(true);

    /**
     * Returns the point to place the console frame/login frame at.
     *
     * @return the point to place the console frame/login frame at
     */
    public Point getRelocatedCenterPoint() {
        return relocatedCenterPoint.get();
    }

    /**
     * Stops the animation of all harmonic rectangles.
     */
    private void stopHarmonicRectangles() {
        try {
            harmonicRectangleSemaphore.acquire();
        } catch (InterruptedException e) {
            ExceptionHandler.handle(e);
        }

        shouldAnimateHarmonicRectangles.set(false);

        harmonicRectangleSemaphore.release();
    }

    /**
     * Shows the splash screen as long as it has not already been shown.
     */
    public void showSplash() {
        Preconditions.checkState(!splashShown);
        splashShown = true;

        CyderThreadRunner.submit(() -> {
            constructFrame();

            CyderThreadRunner.submit(() -> {
                addAndAnimateLetterBlocks();
                addAndAnimateBorders();
                addAndAnimateCreatorLabel();

                ThreadUtil.sleep(loadingMessageStartTimeout);

                splashAnimationCompleted.set(true);

                addAndUpdateLoadingLabel();

                constructAndAddHarmonicRectangles();
                animateHarmonicRectangles();

                ThreadUtil.sleep(showLoadingLabelTimeout);

                loadingLabel.setText(loadingMessage.get());
                loadingLabel.repaint();

                if (!disposed.get() && Props.disposeSplash.getValue()) {
                    fatalError();
                }
            }, SPLASH_ANIMATOR);

            splashFrame.finalizeAndShow();
        }, SPLASH_LOADER_THREAD_NAME);
    }

    /**
     * Constructs the splash frame.
     */
    private void constructFrame() {
        splashFrame = new CyderFrame.Builder()
                .setWidth(FRAME_LEN)
                .setHeight(FRAME_LEN)
                .setBackgroundColor(CyderColors.navy)
                .setTitle(FRAME_TITLE)
                .setType(FrameType.POPUP)
                .setBorderless(true)
                .build();
        splashFrame.addWindowListener(splashFrameWindowAdapter);
        splashFrame.addEndDragEventCallback(dragEventCallback);
    }

    /**
     * Closes the frame and informs the user that a fatal exception occurred.
     */
    private void fatalError() {
        splashFrame.dispose(true);
        new InformHandler.Builder(FATAL_EXCEPTION_TEXT)
                .setTitle(STARTUP_EXCEPTION)
                .setPostCloseAction(FATAL_EXCEPTION_POST_CLOSE_ACTION).inform();
    }

    /**
     * Adds and animates the letter blocks.
     */
    private void addAndAnimateLetterBlocks() {
        JLabel cBlock = new JLabel(generateCIcon());
        cBlock.setBounds(blockHorizontalPadding, FRAME_LEN / 2 - ICON_LEN / 2, ICON_LEN, ICON_LEN);
        splashFrame.getContentPane().add(cBlock);

        JLabel yBlock = new JLabel(generateYIcon());
        yBlock.setBounds(FRAME_LEN - ICON_LEN - blockHorizontalPadding,
                FRAME_LEN / 2 - ICON_LEN / 2, ICON_LEN, ICON_LEN);
        splashFrame.getContentPane().add(yBlock);

        while (cBlock.getX() < FRAME_LEN / 2 - cBlock.getWidth() / 2) {
            cBlock.setLocation(cBlock.getX() + blockAnimationIncrement, cBlock.getY());
            yBlock.setLocation(yBlock.getX() - blockAnimationIncrement, yBlock.getY());
            ThreadUtil.sleep(blockAnimationDelay);
        }
    }

    /**
     * Adds and animates the borders.
     */
    private void addAndAnimateBorders() {
        JLabel topBorder = generateAnimationBorderSide(Direction.TOP);
        topBorder.setBounds(FRAME_LEN / 2 - LOGO_BORDER_LEN / 2,
                -minorAxisBorderLength, LOGO_BORDER_LEN, minorAxisBorderLength);
        splashFrame.getContentPane().add(topBorder);

        while (topBorder.getY() < FRAME_LEN / 2 - (LOGO_BORDER_LEN - LOGO_BORDER_PADDING) / 2
                - 2 * minorAxisBorderLength) {
            topBorder.setLocation(topBorder.getX(), topBorder.getY() + borderAnimationIncrement);
            ThreadUtil.sleep(borderAnimationTimeout);
        }

        JLabel rightBorder = generateAnimationBorderSide(Direction.RIGHT);
        rightBorder.setBounds(FRAME_LEN, splashFrame.getHeight() / 2 - LOGO_BORDER_LEN / 2,
                minorAxisBorderLength, LOGO_BORDER_LEN);
        splashFrame.getContentPane().add(rightBorder);

        while (rightBorder.getX() > FRAME_LEN / 2
                + (LOGO_BORDER_LEN - LOGO_BORDER_PADDING) / 2 + minorAxisBorderLength) {
            rightBorder.setLocation(rightBorder.getX() - borderAnimationIncrement, rightBorder.getY());
            ThreadUtil.sleep(borderAnimationTimeout);
        }

        JLabel bottomBorder = generateAnimationBorderSide(Direction.BOTTOM);
        bottomBorder.setBounds(FRAME_LEN / 2 - LOGO_BORDER_LEN / 2,
                splashFrame.getHeight() + minorAxisBorderLength,
                LOGO_BORDER_LEN, minorAxisBorderLength);
        splashFrame.getContentPane().add(bottomBorder);

        while (bottomBorder.getY() > FRAME_LEN / 2
                + (LOGO_BORDER_LEN - LOGO_BORDER_PADDING) / 2 + minorAxisBorderLength) {
            bottomBorder.setLocation(bottomBorder.getX(), bottomBorder.getY() - borderAnimationIncrement);
            ThreadUtil.sleep(borderAnimationTimeout);
        }

        JLabel leftBorder = generateAnimationBorderSide(Direction.LEFT);
        leftBorder.setBounds(-minorAxisBorderLength, splashFrame.getHeight() / 2
                - LOGO_BORDER_LEN / 2, minorAxisBorderLength, LOGO_BORDER_LEN);
        splashFrame.getContentPane().add(leftBorder);

        while (leftBorder.getX() < FRAME_LEN / 2
                - (LOGO_BORDER_LEN - LOGO_BORDER_PADDING) / 2 - 2 * minorAxisBorderLength) {
            leftBorder.setLocation(leftBorder.getX() + borderAnimationIncrement, leftBorder.getY());
            ThreadUtil.sleep(borderAnimationTimeout);
        }
    }

    /**
     * Adds and animates the creator label.
     */
    private void addAndAnimateCreatorLabel() {
        CyderLabel creatorLabel = new CyderLabel(creatorText);
        creatorLabel.setFont(developerSignatureFont);
        creatorLabel.setForeground(CyderColors.vanilla);
        creatorLabel.setBounds(0, FRAME_LEN, FRAME_LEN,
                StringUtil.getMinHeight(creatorLabel.getText(), developerSignatureFont) + 10);
        splashFrame.getContentPane().add(creatorLabel);

        while (creatorLabel.getY() > FRAME_LEN / 2 + ICON_LEN / 2 + 40) {
            creatorLabel.setLocation(creatorLabel.getX(), creatorLabel.getY() - creatorLabelAnimationIncrement);
            ThreadUtil.sleep(creatorLabelAnimationTimeout);
        }
    }

    // ---------------
    // Icon generation
    // ---------------

    /**
     * Adds the loading label and spawns the thread to update it {@link #loadingLabelUpdateIterations} times.
     */
    private void addAndUpdateLoadingLabel() {
        loadingLabel = new CyderLabel(loadingMessage.get());
        loadingLabel.setFocusable(false);
        loadingLabel.setFont(loadingLabelFont);
        loadingLabel.setForeground(CyderColors.vanilla);
        loadingLabel.setSize(FRAME_LEN,
                StringUtil.getMinHeight(loadingMessage.get(), loadingLabelFont));
        loadingLabel.setLocation(0, FRAME_LEN - LOADING_LABEL_BOTTOM_OFFSET);

        splashFrame.getContentPane().add(loadingLabel);

        CyderThreadRunner.submit(() -> {
            for (int i = 0 ; i < loadingLabelUpdateIterations ; i++) {
                loadingLabel.setText(loadingMessage.get());
                loadingLabel.repaint();

                ThreadUtil.sleep(loadingLabelUpdateTimeout);

                if (splashFrame.isDisposed()) return;
            }
        }, LOADING_LABEL_UPDATER);
    }

    /**
     * The minor axis length, width, of the harmonic rectangles.
     */
    private static final int harmonicRectangleMinorAxisLength = (FRAME_LEN - 2 * defaultHarmonicPadding
            - (numHarmonicRectangles - 1) * harmonicXInnerPadding) / numHarmonicRectangles;

    /**
     * The padding between the frame borders and the harmonic rectangles.
     */
    private static final int harmonicRectanglePadding = (FRAME_LEN - harmonicRectangleMinorAxisLength
            * numHarmonicRectangles - harmonicXInnerPadding * (numHarmonicRectangles - 1)) / 2;

    /**
     * Constructs and adds the harmonic rectangles.
     */
    private void constructAndAddHarmonicRectangles() {
        try {
            harmonicRectangleSemaphore.acquire();
        } catch (InterruptedException e) {
            ExceptionHandler.handle(e);
        }

        for (int i = 0 ; i < numHarmonicRectangles ; i++) {
            if (disposed.get()) break;
            int currentRectangleX = harmonicRectanglePadding
                    + i * harmonicRectangleMinorAxisLength
                    + i * harmonicXInnerPadding;

            HarmonicRectangle harmonicRectangle = new HarmonicRectangle(harmonicRectangleMinorAxisLength,
                    harmonicMinHeight, harmonicRectangleMinorAxisLength, harmonicMaxHeight);

            harmonicRectangle.setHarmonicDirection(HarmonicRectangle.HarmonicDirection.VERTICAL);
            harmonicRectangle.setAnimationInc(harmonicAnimationInc);
            harmonicRectangle.setAnimationDelay(harmonicAnimationDelay);
            harmonicRectangle.setLocation(currentRectangleX, harmonicYPadding);
            splashFrame.getContentPane().add(harmonicRectangle);

            harmonicRectangles.add(harmonicRectangle);
        }

        harmonicRectangleSemaphore.release();
    }

    /**
     * Starts animating the harmonic rectangles
     */
    private void animateHarmonicRectangles() {
        CyderThreadRunner.submit(() -> {
            float harmonicRectangleStartFrequency =
                    millisToStartAllHarmonicRectangles / (float) harmonicRectangles.size();
            long startingTime = System.currentTimeMillis();

            while (shouldAnimateHarmonicRectangles.get()) {
                for (int i = 0 ; i < harmonicRectangles.size() ; i++) {
                    int maxAnimateIndex = (int) ((System.currentTimeMillis() - startingTime)
                            / harmonicRectangleStartFrequency);

                    if (i <= maxAnimateIndex) {
                        harmonicRectangles.get(i).animationStep();
                    }

                    if (!shouldAnimateHarmonicRectangles.get()) break;
                }

                ThreadUtil.sleep(harmonicAnimationDelay);
            }
        }, harmonicRectangleAnimationThreadName);
    }

    /**
     * Generates an animation border side for the provided direction.
     *
     * @param direction the direction the border is for
     * @return the animation border side
     */
    private JLabel generateAnimationBorderSide(Direction direction) {
        Preconditions.checkNotNull(direction);

        return new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(animationBorderSideColor);

                switch (direction) {
                    case LEFT, RIGHT -> g.fillRect(0, 0, 10, LOGO_BORDER_LEN);
                    case TOP, BOTTOM -> g.fillRect(0, 0, LOGO_BORDER_LEN, 10);
                }
            }
        };
    }

    /**
     * Disposes the splashFrame using fast close.
     */
    public void fastDispose() {
        if (disposed.get()) return;
        if (!Props.disposeSplash.getValue()) return;

        CyderThreadRunner.submit(() -> {
            if (Props.allowSplashCompletion.getValue()) {
                while (!splashAnimationCompleted.get()) {
                    Thread.onSpinWait();
                }
            }

            splashFrame.dispose(true);
            disposed.set(true);
        }, FAST_DISPOSE_THREAD_NAME);
    }

    /**
     * Returns the splash frame.
     *
     * @return the splash frame
     */
    public CyderFrame getSplashFrame() {
        return splashFrame;
    }

    /**
     * Sets the loading label and updates the splash frame.
     *
     * @param newLoadingMessage the message to set to the loading label
     */
    public void setLoadingMessage(String newLoadingMessage) {
        Preconditions.checkNotNull(newLoadingMessage);
        Preconditions.checkArgument(!newLoadingMessage.isEmpty());

        newLoadingMessage = StringUtil.getTrimmedText(newLoadingMessage);
        Logger.log(LogTag.SPLASH_LOADING_MESSAGE, newLoadingMessage);

        if (splashFrame == null || splashFrame.isDisposed()) return;

        loadingMessage.set(newLoadingMessage);

        if (loadingLabel != null) {
            loadingLabel.setText(newLoadingMessage);
            loadingLabel.revalidate();
            loadingLabel.repaint();
        }
    }

    /**
     * Generates and returns the C symbol for the splash animation.
     *
     * @return the C symbol for the splash
     */
    private ImageIcon generateCIcon() {
        if (C_ICON != null) return C_ICON;

        BufferedImage drawMe = new BufferedImage(ICON_LEN, ICON_LEN, BufferedImage.TYPE_INT_ARGB);

        Graphics g = drawMe.getGraphics();
        g.setColor(CyderColors.vanilla);
        g.fillRect(0, 0, C_BLOCK_WIDTH, C_BLOCK_HEIGHT);
        g.fillRect(0, ICON_LEN - C_BLOCK_HEIGHT, C_BLOCK_WIDTH, C_BLOCK_HEIGHT);
        g.fillRect(0, 0, LETTER_LEN, ICON_LEN);

        C_ICON = ImageUtil.toImageIcon(drawMe);
        return C_ICON;
    }

    /**
     * Generates and returns the Y symbol for the splash animation.
     *
     * @return the Y symbol for the splash animation
     */
    private ImageIcon generateYIcon() {
        if (Y_ICON != null) return Y_ICON;

        BufferedImage drawMe = new BufferedImage(ICON_LEN, ICON_LEN, BufferedImage.TYPE_INT_ARGB);

        Graphics g = drawMe.getGraphics();
        g.setColor(CyderColors.vanilla);
        g.fillRect(ICON_LEN - LETTER_LEN, 0, LETTER_LEN, ICON_LEN);
        g.fillRect(60, 60, Y_BLOCK_WIDTH, LETTER_LEN);

        Y_ICON = ImageUtil.toImageIcon(drawMe);
        return Y_ICON;
    }
}
