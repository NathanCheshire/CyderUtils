package cyder.test;

import cyder.annotations.ManualTest;
import cyder.annotations.SuppressCyderInspections;
import cyder.builders.InformBuilder;
import cyder.builders.NotificationBuilder;
import cyder.common.CyderInspection;
import cyder.common.SwitcherState;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.external.AudioPlayer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.layouts.CyderFlowLayout;
import cyder.layouts.CyderGridLayout;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.utilities.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Manual widgets used to test certain aspects/implementations of Cyder.
 */
public class ManualTests {
    /**
     * Restricts default instantiation.
     */
    private ManualTests() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Runs the tests within the method.
     * This method is used purely for testing purposes.
     */
    @ManualTest("test")
    @SuppressCyderInspections(CyderInspection.TestInspection) /* not ending in test */
    @SuppressWarnings({"EmptyTryBlock", "RedundantSuppression"}) /* for when try is empty and not empty */
    public static void launchTests() {
        CyderThreadRunner.submit(() -> {
            try {
              AudioPlayer.showGui();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Manual Tests Thread");
    }

    /**
     * Tests for the switcher.
     */
    @ManualTest("switcher test")
    public static void cyderSwitcherTest() {
        CyderFrame testFrame = new CyderFrame(280, 120);
        testFrame.setTitle("Switcher test");

        ArrayList<SwitcherState> states = new ArrayList<>();
        states.add(new SwitcherState("Uno", "uno long"));
        states.add(new SwitcherState("Dos", "dos long"));
        states.add(new SwitcherState("Tres", "tres long"));
        states.add(new SwitcherState("Cuatro", "cuatro long"));

        SwitcherState startingState = states.get(0);

        CyderSwitcher switcher = new CyderSwitcher(200, 40, states, startingState);
        switcher.setBounds(40, 40, 200, 40);
        testFrame.getContentPane().add(switcher);
        switcher.addOnChangeListener((param) -> {
            testFrame.notify(switcher.getNextState().mappedValue());

            return param;
        });

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the CyderGrid.
     */
    @ManualTest("grid test")
    public static void cyderGridTest() {
        CyderFrame cf = new CyderFrame(1000, 1000);
        cf.setTitle("Cyder Grid");

        CyderGrid cg = new CyderGrid(200, 800);
        cg.setBounds(100, 100, 800, 800);
        cf.getContentPane().add(cg);
        cg.setResizable(true);
        cg.setDrawGridLines(false);
        cg.installClickListener();
        cg.installDragListener();

        cf.finalizeAndShow();
    }

    /**
     * Tests for drag label buttons.
     */
    @ManualTest("drag label button test")
    public static void dragLabelButtonTest() {
        CyderFrame testFrame = new CyderFrame(600, 600, CyderIcons.defaultBackground);
        testFrame.setTitle("Test Frame");
        testFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JButton pinButton = new JButton("");
        pinButton.setToolTipText("Random button");
        pinButton.setIcon(CyderIcons.changeSizeIcon);
        pinButton.setContentAreaFilled(false);
        pinButton.setBorderPainted(false);
        pinButton.setFocusPainted(false);
        testFrame.getTopDragLabel().addButton(pinButton, 1);

        CyderButton cb = new CyderButton("Remove first");
        cb.setBounds(100, 100, 150, 40);
        cb.addActionListener(e -> testFrame.getTopDragLabel().removeButton(0));
        testFrame.getContentPane().add(cb);

        CyderButton cb1 = new CyderButton("Remove last");
        cb1.setBounds(100, 180, 150, 40);
        cb1.addActionListener(e -> testFrame.getTopDragLabel().removeButton(
                testFrame.getTopDragLabel().getButtonList().size() - 1));
        testFrame.getContentPane().add(cb1);

        CyderButton addPinFirst = new CyderButton("Add Random Butter first");
        addPinFirst.setBounds(100, 250, 150, 40);
        addPinFirst.addActionListener(e -> testFrame.getTopDragLabel().addButton(pinButton, 0));
        testFrame.getContentPane().add(addPinFirst);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for button positions.
     */
    @ManualTest("button position test")
    public static void buttonAndTitlePosTest() {
        CyderFrame testFrame = new CyderFrame(600, 400, CyderIcons.defaultBackground);
        testFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        testFrame.setTitle("Testing Title");

        CyderButton setLeftTitle = new CyderButton("Left title");
        setLeftTitle.setBounds(100, 100, 140, 40);
        setLeftTitle.addActionListener(e -> testFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT));
        testFrame.getContentPane().add(setLeftTitle);

        CyderButton setCenterTitle = new CyderButton("Center title");
        setCenterTitle.setBounds(100, 160, 140, 40);
        setCenterTitle.addActionListener(e -> testFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER));
        testFrame.getContentPane().add(setCenterTitle);

        CyderButton setRightTitle = new CyderButton("Right title");
        setRightTitle.setBounds(100, 220, 140, 40);
        setRightTitle.addActionListener(e -> testFrame.setTitlePosition(CyderFrame.TitlePosition.RIGHT));
        testFrame.getContentPane().add(setRightTitle);

        CyderButton setLeftButton = new CyderButton("Left button");
        setLeftButton.setBounds(300, 100, 150, 40);
        setLeftButton.addActionListener(e -> testFrame.setButtonPosition(CyderFrame.ButtonPosition.LEFT));
        testFrame.getContentPane().add(setLeftButton);

        CyderButton setRightButton = new CyderButton("Right button");
        setRightButton.setBounds(300, 160, 150, 40);
        setRightButton.addActionListener(e -> testFrame.setButtonPosition(CyderFrame.ButtonPosition.RIGHT));
        testFrame.getContentPane().add(setRightButton);

        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.finalizeAndShow();
    }

    /**
     * Tests for notifications.
     */
    @ManualTest("notification test")
    public static void notificationTest() {
        CyderFrame testFrame = new CyderFrame(600, 600, CyderIcons.defaultBackground);
        testFrame.setTitle("Notification Test");

        int milliDelay = 3000;

        CyderGridLayout layout = new CyderGridLayout(3, 3);

        CyderTextField ctf = new CyderTextField(0);
        ctf.setSize(150, 40);

        CyderButton topNotify = new CyderButton("Top");
        topNotify.setSize(150, 40);
        topNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(milliDelay);
            notificationBuilder.setArrowDir(Direction.TOP);
            notificationBuilder.setNotificationDirection(NotificationDirection.TOP);
            testFrame.notify(notificationBuilder);
        });

        CyderButton rightNotify = new CyderButton("Top Right");
        rightNotify.setSize(150, 40);
        rightNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(milliDelay);
            notificationBuilder.setArrowDir(Direction.RIGHT);
            notificationBuilder.setNotificationDirection(NotificationDirection.TOP_RIGHT);
            testFrame.notify(notificationBuilder);
        });

        CyderButton bottomNotify = new CyderButton("Bottom");
        bottomNotify.setSize(150, 40);
        bottomNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(milliDelay);
            notificationBuilder.setArrowDir(Direction.BOTTOM);
            notificationBuilder.setNotificationDirection(NotificationDirection.BOTTOM);
            testFrame.notify(notificationBuilder);
        });

        CyderButton leftNotify = new CyderButton("Top Left");
        leftNotify.setSize(150, 40);
        leftNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(milliDelay);
            notificationBuilder.setArrowDir(Direction.LEFT);
            notificationBuilder.setNotificationDirection(NotificationDirection.TOP_LEFT);
            testFrame.notify(notificationBuilder);
        });

        CyderButton centerLeftNotify = new CyderButton("Center Left");
        centerLeftNotify.setSize(150, 40);
        centerLeftNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(milliDelay);
            notificationBuilder.setArrowDir(Direction.LEFT);
            notificationBuilder.setNotificationDirection(NotificationDirection.LEFT);
            testFrame.notify(notificationBuilder);
        });

        CyderButton centerRightNotify = new CyderButton("Center Right");
        centerRightNotify.setSize(150, 40);
        centerRightNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(milliDelay);
            notificationBuilder.setArrowDir(Direction.RIGHT);
            notificationBuilder.setNotificationDirection(NotificationDirection.RIGHT);
            testFrame.notify(notificationBuilder);
        });

        CyderButton bottomLeftNotify = new CyderButton("Bottom Left");
        bottomLeftNotify.setSize(150, 40);
        bottomLeftNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(milliDelay);
            notificationBuilder.setArrowDir(Direction.LEFT);
            notificationBuilder.setNotificationDirection(NotificationDirection.BOTTOM_LEFT);
            testFrame.notify(notificationBuilder);
        });

        CyderButton bottomRightNotify = new CyderButton("Bottom Right");
        bottomRightNotify.setSize(170, 40);
        bottomRightNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(milliDelay);
            notificationBuilder.setArrowDir(Direction.RIGHT);
            notificationBuilder.setNotificationDirection(NotificationDirection.BOTTOM_RIGHT);
            testFrame.notify(notificationBuilder);
        });

        layout.addComponent(leftNotify, 0, 0);
        layout.addComponent(topNotify, 1, 0);
        layout.addComponent(rightNotify, 2, 0);

        layout.addComponent(centerLeftNotify, 0, 1);
        layout.addComponent(ctf, 1, 1);
        layout.addComponent(centerRightNotify, 2, 1);

        layout.addComponent(bottomLeftNotify, 0, 2);
        layout.addComponent(bottomNotify, 1, 2);
        layout.addComponent(bottomRightNotify, 2, 2);

        CyderPanel panel = new CyderPanel(layout);
        testFrame.setLayoutPanel(panel);

        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.finalizeAndShow();
    }

    @ManualTest("askew test")
    public static void askewTest() {
        CyderFrame testFrame = new CyderFrame(350, 300, CyderIcons.defaultBackground);
        testFrame.setTitle("Askew Test");

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(100, 100, 150, 40);
        testFrame.getContentPane().add(ctf);

        CyderButton cb = new CyderButton("Askew");
        cb.setBounds(100, 200, 150, 40);
        testFrame.getContentPane().add(cb);
        cb.addActionListener(e -> testFrame.rotateBackground(Integer.parseInt(ctf.getText())));

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the sliding icon label.
     */
    @ManualTest("sliding icon label test")
    public static void iconLabelSlidingTest() {
        ImageIcon theImage = new ImageIcon(ImageUtil.getImageGradient(600, 1200,
                CyderColors.regularPink, CyderColors.regularBlue, CyderColors.regularBlue));

        CyderFrame testFrame = new CyderFrame(600, 600, theImage);
        testFrame.setTitle("Sliding test");
        testFrame.initializeResizing();
        testFrame.setResizable(true);

        CyderButton slideUp = new CyderButton("UP");
        slideUp.setBounds(225, 150, 150, 40);
        slideUp.addActionListener(e -> CyderThreadRunner.submit(() -> {
            testFrame.getContentPane().setSize(600, 1200);
            ((JLabel) testFrame.getContentPane()).setIcon(theImage);

            try {
                int x = testFrame.getContentPane().getX();
                for (int i = testFrame.getContentPane().getY() ; i > -testFrame.getHeight() ; i--) {
                    testFrame.getContentPane().setLocation(x, i);
                    Thread.sleep(1);
                }
                testFrame.getContentPane().setLocation(0, 0);
                testFrame.refreshBackground();
                testFrame.getContentPane().revalidate();
            } catch (InterruptedException interruptedException) {
                ExceptionHandler.handle(interruptedException);
            }
        }, ""));
        testFrame.getContentPane().add(slideUp);

        CyderButton slideLeft = new CyderButton("LEFT");
        slideLeft.setBounds(225, 200, 150, 40);
        slideLeft.addActionListener(e -> CyderThreadRunner.submit(() -> {
            try {
                int y = testFrame.getContentPane().getY();
                for (int i = 0 ; i > -testFrame.getWidth() ; i--) {
                    testFrame.getContentPane().setLocation(i, y);
                    Thread.sleep(1);
                }
                testFrame.getContentPane().setLocation(0, 0);
                testFrame.refreshBackground();
                testFrame.getContentPane().revalidate();
            } catch (InterruptedException interruptedException) {
                ExceptionHandler.handle(interruptedException);
            }
        }, ""));
        testFrame.getContentPane().add(slideLeft);

        CyderButton slideDown = new CyderButton("DOWN");
        slideDown.setBounds(225, 250, 150, 40);
        slideDown.addActionListener(e -> CyderThreadRunner.submit(() -> {
            try {
                int x = testFrame.getContentPane().getX();
                for (int i = 0 ; i < testFrame.getHeight() ; i++) {
                    testFrame.getContentPane().setLocation(x, i);
                    Thread.sleep(1);
                }
                testFrame.getContentPane().setLocation(0, 0);
                testFrame.refreshBackground();
                testFrame.getContentPane().revalidate();
            } catch (InterruptedException interruptedException) {
                ExceptionHandler.handle(interruptedException);
            }
        }, ""));
        testFrame.getContentPane().add(slideDown);

        CyderButton slideRight = new CyderButton("RIGHT");
        slideRight.setBounds(225, 300, 150, 40);
        slideRight.addActionListener(e -> CyderThreadRunner.submit(() -> {
            try {
                int y = testFrame.getContentPane().getY();
                for (int i = 0 ; i < testFrame.getWidth() ; i++) {
                    testFrame.getContentPane().setLocation(i, y);
                    Thread.sleep(1);
                }
                testFrame.getContentPane().setLocation(0, 0);
                testFrame.refreshBackground();
                testFrame.getContentPane().revalidate();
            } catch (InterruptedException interruptedException) {
                ExceptionHandler.handle(interruptedException);
            }
        }, ""));
        testFrame.getContentPane().add(slideRight);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for checkboxes.
     */
    @ManualTest("checkbox test")
    public static void checkboxTest() {
        CyderFrame testFrame = new CyderFrame(400, 400, CyderIcons.defaultBackground);
        testFrame.setTitle("Checkbox Test");

        CyderCheckbox cb = new CyderCheckbox();
        cb.setBounds(175, 150, 50, 50);
        cb.setRoundedCorners(true);
        testFrame.getContentPane().add(cb);

        CyderCheckbox cb1 = new CyderCheckbox();
        cb1.setBounds(175, 225, 50, 50);
        cb1.setRoundedCorners(false);
        testFrame.getContentPane().add(cb1);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the progress bar ui.
     */
    @ManualTest("progress bar test")
    public static void progressBarTest() {
        CyderFrame cf = new CyderFrame(400, 100);
        cf.setTitle("ProgressBar Test");

        JProgressBar jpb = new JProgressBar(0, 500);
        jpb.setBounds(40, 40, 320, 20);
        jpb.setOrientation(SwingConstants.HORIZONTAL);
        CyderProgressUI ui = new CyderProgressUI();
        ui.setAnimationDirection(CyderProgressUI.AnimationDirection.LEFT_TO_RIGHT);
        ui.setColors(CyderColors.regularBlue, CyderColors.regularPink);
        jpb.setUI(ui);
        jpb.setValue(50);
        cf.getContentPane().add(jpb);
        cf.finalizeAndShow();

        CyderThreadRunner.submit(() -> {
            for (int i = 0 ; i <= jpb.getMaximum() / 2 ; i++) {
                jpb.setValue(i);
                try {
                    Thread.sleep(2000 / jpb.getMaximum());
                } catch (InterruptedException e) {
                    ExceptionHandler.handle(e);
                }
            }

            for (int i = jpb.getMaximum() / 2 ; i <= jpb.getMaximum() ; i++) {
                jpb.setValue(i);
                try {
                    Thread.sleep(500 / jpb.getMaximum());
                } catch (InterruptedException e) {
                    ExceptionHandler.handle(e);
                }
            }
        }, "ProgressBar Animator");
    }

    /**
     * Tests for the slider ui.
     */
    @ManualTest("slider test")
    public static void cyderSliderTest() {
        CyderFrame testFrame = new CyderFrame(400, 400);
        testFrame.setTitle("Cyder Slider Test");

        JSlider audioVolumeSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
        CyderSliderUI UI = new CyderSliderUI(audioVolumeSlider);
        UI.setThumbDiameter(25);
        UI.setSliderShape(CyderSliderUI.SliderShape.CIRCLE);
        UI.setFillColor(CyderColors.regularPink);
        UI.setOutlineColor(CyderColors.regularPink);
        UI.setNewValColor(CyderColors.navy);
        UI.setOldValColor(CyderColors.regularBlue);
        UI.setTrackStroke(new BasicStroke(4.0f));
        audioVolumeSlider.setUI(UI);
        audioVolumeSlider.setBounds(50, 150, 300, 40);
        audioVolumeSlider.setMinimum(0);
        audioVolumeSlider.setMaximum(100);
        audioVolumeSlider.setPaintTicks(false);
        audioVolumeSlider.setPaintLabels(false);
        audioVolumeSlider.setVisible(true);
        audioVolumeSlider.setValue(50);
        audioVolumeSlider.setOpaque(false);
        audioVolumeSlider.setToolTipText("Volume");
        audioVolumeSlider.setFocusable(false);
        audioVolumeSlider.repaint();
        testFrame.getContentPane().add(audioVolumeSlider);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the frame title length.
     */
    @ManualTest("frame length test")
    public static void frameTitleLengthTest() {
        CyderFrame cf = new CyderFrame(600, 200);
        cf.setTitle("Title Length Test");
        cf.setTitlePosition(CyderFrame.TitlePosition.LEFT);

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(40, 40, 600 - 80, 40);
        cf.getContentPane().add(ctf);
        ctf.addActionListener(e -> cf.setTitle(ctf.getText().trim()));

        CyderButton cb = new CyderButton("Set Title");
        cb.setBounds(40, 100, 600 - 80, 40);
        cf.getContentPane().add(cb);
        cb.addActionListener(e -> cf.setTitle(ctf.getText().trim()));

        cf.finalizeAndShow();
    }

    /**
     * Tests for the switch.
     */
    @ManualTest("switch test")
    public static void switchTest() {
        CyderFrame testFrame = new CyderFrame(500, 500);
        testFrame.setTitle("CyderSwitch test");

        CyderSwitch cs = new CyderSwitch(300, 100);
        cs.setBounds(100, 100, 300, 100);
        cs.setState(CyderSwitch.State.OFF);
        testFrame.getContentPane().add(cs);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the ripple label.
     */
    @ManualTest("ripple label test")
    public static void rippleLabelTest() {
        CyderFrame rippleTestFrame = new CyderFrame(600, 600);
        rippleTestFrame.setTitle("Ripple Test");

        CyderLabel ripplingLabel = new CyderLabel("<html>" + CyderStrings.QUICK_BROWN_FOX + "<br/>" +
                CyderStrings.QUICK_BROWN_FOX + "<br/><br/>" + CyderStrings.QUICK_BROWN_FOX + "<br/>" +
                CyderStrings.QUICK_BROWN_FOX + "<br/>" + CyderStrings.QUICK_BROWN_FOX + "<br/>" +
                CyderStrings.QUICK_BROWN_FOX + "<br/><br/>Love,<br/>Nathan Cheshire" + "</html>");
        ripplingLabel.setFont(CyderFonts.segoe20);

        //fill content area with label
        ripplingLabel.setBounds(40, 40,
                rippleTestFrame.getWidth() - 40 * 2, rippleTestFrame.getHeight() - 40 * 2);
        rippleTestFrame.getContentPane().add(ripplingLabel);

        //fast timeout and relatively high char count
        ripplingLabel.setRippleMsTimeout(10);
        ripplingLabel.setRippleChars(15);

        //enable rippling
        ripplingLabel.setRippling(true);

        rippleTestFrame.finalizeAndShow();
    }

    /**
     * Tests for the checkbox group.
     */
    @ManualTest("checkbox group test")
    public static void checkboxGroupTest() {
        CyderFrame testFrame = new CyderFrame(400, 110);
        testFrame.setTitle("Checkbox group test");

        CyderCheckboxGroup cbg = new CyderCheckboxGroup();

        int startX = 50;

        for (int i = 0 ; i < 5 ; i++) {
            CyderCheckbox cb = new CyderCheckbox();
            cb.setBounds(startX + (60) * i, 40, 50, 50);
            testFrame.getContentPane().add(cb);

            if (i != 4)
                cbg.addCheckbox(cb);
        }

        testFrame.finalizeAndShow();
    }

    /**
     * Test for the grid layout.
     */
    @ManualTest("grid layout test")
    public static void cyderGridLayoutTest() {
        //regular frame calls
        CyderFrame gridTestFrame = new CyderFrame(800, 800);
        gridTestFrame.setTitle("Grid Layout Test");

        //init the main panel layout
        CyderGridLayout layout = new CyderGridLayout(2, 2);

        //add components to the layout at specified position
        CyderButton testButton = new CyderButton("This");
        testButton.setSize(100, 100);
        testButton.addActionListener(e -> gridTestFrame.notify(
                new NotificationBuilder("Notified button clicked")));
        layout.addComponent(testButton, 0, 0, CyderGridLayout.Position.MIDDLE_RIGHT);

        CyderLabel testLabel2 = new CyderLabel("A");
        testLabel2.setSize(50, 50);
        layout.addComponent(testLabel2, 0, 1);

        CyderLabel testLabel3 = new CyderLabel("IS");
        testLabel3.setSize(50, 50);
        layout.addComponent(testLabel3, 1, 0);

        CyderLabel testLabel4 = new CyderLabel("Test");
        testLabel4.setSize(50, 50);
        CyderButton testButton1 = new CyderButton("Click");
        testButton1.setSize(150, 40);

        //sub grid
        CyderGridLayout cyderGridLayout2 = new CyderGridLayout(2, 1);
        cyderGridLayout2.addComponent(testLabel4, 0, 0);
        cyderGridLayout2.addComponent(testButton1, 1, 0);

        //make sub panel and set layout as sub grid
        CyderPanel subPanel = new CyderPanel(cyderGridLayout2);
        layout.addComponent(subPanel, 1, 1);

        //create master panel with the layout we have added components to
        CyderPanel panel = new CyderPanel(layout);
        //set the frame's content panel
        gridTestFrame.setLayoutPanel(panel);

        //resizing on
        gridTestFrame.initializeResizing();
        gridTestFrame.setResizable(true);
        gridTestFrame.setMaximumSize(new Dimension(1200, 1200));
        gridTestFrame.setBackgroundResizing(true);

        //regular final frame calls
        gridTestFrame.finalizeAndShow();
    }

    /**
     * Test for the flow layout.
     */
    @ManualTest("flow layout test")
    public static void flowLayoutTest() {
        CyderFrame testFrame = new CyderFrame(600, 600);
        testFrame.setTitle("Flow Layout Test");

        // make layout
        CyderFlowLayout layout = new CyderFlowLayout(CyderFlowLayout.HorizontalAlignment.CENTER,
                CyderFlowLayout.VerticalAlignment.CENTER, 25, 15);

        //add 10 buttons to layout
        for (int i = 1 ; i < 11 ; i++) {
            CyderButton cb = new CyderButton("Test Button " + i);
            cb.setSize(200, 50);
            int finalI = i;
            cb.addActionListener(e -> testFrame.notify(
                    new NotificationBuilder(finalI + " button: " + cb)));
            layout.addComponent(cb);
        }

        //make panel and set as frame's content panel
        CyderPanel panel = new CyderPanel(layout);
        testFrame.setLayoutPanel(panel);

        //resizing on
        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.setMaximumSize(new Dimension(2000, 2000));
        testFrame.setMinimumSize(new Dimension(300, 300));
        testFrame.setBackgroundResizing(true);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for popups switcher.
     */
    @ManualTest("inform test")
    public static void popupTest() {
        CyderFrame testFrame = new CyderFrame(400, 120);
        testFrame.setTitle("Inform Test");

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(40, 40, 320, 40);
        ctf.addActionListener(e -> {
            String text = ctf.getText();

            if (!text.isEmpty()) {
                InformHandler.inform(text);
            }
        });
        testFrame.getContentPane().add(ctf);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for CyderFrame menu.
     */
    @ManualTest("menu test")
    public static void frameMenuTest() {
        CyderFrame testFrame = new CyderFrame(400, 400);
        testFrame.setTitle("Menu Test");

        testFrame.setMenuEnabled(true);
        testFrame.setCurrentMenuType(CyderFrame.MenuType.RIBBON);

        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.setMaximumSize(new Dimension(1000, 1000));

        testFrame.addMenuItem("hello", () -> testFrame.notify("hello"));
        testFrame.addMenuItem("darkness", () -> testFrame.notify("darkness"));
        testFrame.addMenuItem("my old", () -> testFrame.notify("my old"));
        testFrame.addMenuItem("friend", () -> testFrame.notify("friend"));
        testFrame.addMenuItem("I've come to talk", () -> testFrame.notify("I've come to talk"));
        testFrame.addMenuItem("with you again", () -> testFrame.notify("with you again"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("done", () -> testFrame.notify("done"));

        CyderButton switchMenuType = new CyderButton("Switch Menu");
        switchMenuType.setSize(200, 40);
        switchMenuType.addActionListener(e -> {
            if (testFrame.getCurrentMenuType() == CyderFrame.MenuType.PANEL) {
                testFrame.setCurrentMenuType(CyderFrame.MenuType.RIBBON);
            } else {
                testFrame.setCurrentMenuType(CyderFrame.MenuType.PANEL);
            }
        });

        CyderTextField addMenuItem = new CyderTextField(0);
        addMenuItem.setSize(200, 40);
        addMenuItem.addActionListener(e -> {
            if (addMenuItem.getText().trim().length() < 3)
                return;

            testFrame.addMenuItem(addMenuItem.getText(), () -> testFrame.notify(addMenuItem.getText()));
        });

        CyderGridLayout gridLayout = new CyderGridLayout(1, 2);
        gridLayout.addComponent(switchMenuType);
        gridLayout.addComponent(addMenuItem);

        CyderPanel panel = new CyderPanel(gridLayout);
        testFrame.setLayoutPanel(panel);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the notify and possibly overflow
     * onto an inform pane custom container test.
     */
    @ManualTest("notify container test")
    public static void notifyAndInformCustomContainerTest() {
        CyderFrame testFrame = new CyderFrame(400, 400);
        testFrame.setTitle("Notify Container Test");

        JLabel container = new JLabel("<html><div>Creatine water weight, yeah boi</div></html>",
                SwingConstants.CENTER);
        container.setSize(500, 500);
        container.setFont(CyderFonts.defaultFont);

        // needs to be opaque to fill background
        container.setOpaque(true);
        container.setBackground(CyderColors.notificationBackgroundColor);
        container.setForeground(CyderColors.vanila);

        CyderTextField ctf = new CyderTextField(0);
        ctf.setSize(200, 40);
        ctf.addActionListener(e -> {
            NotificationBuilder builder = new NotificationBuilder("NULL");
            builder.setContainer(container);
            testFrame.notify(builder);
        });

        CyderFlowLayout cyderFlow = new CyderFlowLayout(
                CyderFlowLayout.HorizontalAlignment.CENTER_STATIC,
                CyderFlowLayout.VerticalAlignment.CENTER_STATIC);
        cyderFlow.addComponent(ctf);

        CyderPanel panel = new CyderPanel(cyderFlow);
        testFrame.setLayoutPanel(panel);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the notify and possibly overflow
     * onto an inform pane custom container test.
     */
    @ManualTest("disable relative to test")
    public static void informDisableRelativeToTest() {
        CyderFrame testFrame = new CyderFrame(400, 400);
        testFrame.setTitle("Disable RelativeTo test");

        testFrame.finalizeAndShow();

        InformBuilder builder = new InformBuilder("Hello");
        builder.setDisableRelativeTo(true);
        builder.setRelativeTo(testFrame);
        InformHandler.inform(builder);
    }
}
