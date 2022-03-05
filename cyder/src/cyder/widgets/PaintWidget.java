package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.enums.SliderShape;
import cyder.genesis.CyderCommon;
import cyder.layouts.CyderGridLayout;
import cyder.ui.*;
import cyder.utilities.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * A painting widget, not currently intended to be able to edit/markup images.
 */
public class PaintWidget {
    /**
     * The length of the frame.
     */
    public static final int frameLength = 800;

    /**
     * The master painting frame.
     */
    private static CyderFrame paintFrame;

    /**
     * The painting grid.
     */
    private static CyderGrid cyderGrid;

    /**
     * Prevent illegal class instantiation.
     */
    private PaintWidget() {
        throw new IllegalStateException(CyderStrings.attemptedInstantiation);
    }

    /**
     * ShowGUI method standard.
     */
    @Widget(triggers = {"paint", "draw"}, description =
            "A painting widget")
    public static void showGUI() {
        if (paintFrame != null)
            paintFrame.dispose(true);

        int padding = 0;

        paintFrame = new CyderFrame(frameLength + padding * 2,
                frameLength + DragLabel.DEFAULT_HEIGHT + padding * 2);
        paintFrame.setTitle("Paint");
        paintFrame.setBackground(CyderIcons.defaultBackgroundLarge);
        paintFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (paintControlsFrame != null)
                    paintControlsFrame.dispose(true);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (paintControlsFrame != null)
                    paintControlsFrame.dispose(true);
            }
        });

        cyderGrid = new CyderGrid(200, frameLength);
        cyderGrid.setBounds(padding,DragLabel.DEFAULT_HEIGHT + padding - 5, frameLength, frameLength);
        paintFrame.getContentPane().add(cyderGrid);
        cyderGrid.setDrawExtendedBorder(true);
        cyderGrid.setResizable(true);
        cyderGrid.setDrawGridLines(false);
        cyderGrid.installClickPlacer();
        cyderGrid.installDragPlacer();
        cyderGrid.setSmoothScrolling(true);
        cyderGrid.setDrawWidth(DEFAULT_BRUSH_WIDTH);
        cyderGrid.setNodeColor(currentPaintColor);

        paintFrame.setVisible(true);
        paintFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());

        installControlFrames();
    }

    /**
     * The controls frame.
     */
    private static CyderFrame paintControlsFrame;

    private static ArrayList<Color> recentColors;
    private static JLabel recentColorsBlock;

    private static Color currentPaintColor = CyderColors.regularPink;
    private static CyderTextField colorHexField;
    private static CyderCheckbox add;

    /**
     * Opens the paint controls frame.
     */
    private static void installControlFrames() {
        if (paintControlsFrame != null)
            paintControlsFrame.dispose();

        recentColors = new ArrayList<>();

        paintControlsFrame = new CyderFrame(frameLength,200);
        paintControlsFrame.setTitle("Paint Controls");
        paintControlsFrame.setResizable(true);

        CyderGridLayout parentLayout = new CyderGridLayout(1,2);

        CyderGridLayout topLayout = new CyderGridLayout(1,3);
        CyderPanel topLayoutPanel = new CyderPanel(topLayout);
        parentLayout.addComponent(topLayoutPanel, 0, 0);

        CyderGridLayout bottomLayout = new CyderGridLayout(6,1);
        CyderPanel bottomLayoutPanel = new CyderPanel(bottomLayout);
        parentLayout.addComponent(bottomLayoutPanel, 0, 1);

        // vars used for drawing custom component
        final int colorRows = 2;
        final int colorsPerRow = 6;
        final int colorBlockLen = 20;
        final int padding = 5;

        recentColorsBlock = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,colorsPerRow * colorBlockLen + 2 * padding,50);
                g.setColor(CyderColors.vanila);
                g.fillRect(padding,padding,colorsPerRow * colorBlockLen,40);

                int numColorsPainted = 0;
                int currentX = padding;
                int currentY = padding;

                // paint 10 colors at most
                while (numColorsPainted < Math.min(colorRows * colorsPerRow, recentColors.size())) {
                    g.setColor(recentColors.get(recentColors.size() - numColorsPainted - 1));
                    g.fillRect(currentX, currentY, colorBlockLen, colorBlockLen);

                    currentX += colorBlockLen;

                    if (currentX >= padding + colorsPerRow * colorBlockLen) {
                        currentX = padding;
                        currentY += colorBlockLen;
                    }

                    numColorsPainted++;
                }

                // draw sep lines between colors
                g.setColor(Color.BLACK);
                // horizontal lines
                for (int i = 0 ; i < colorRows ; i++) {
                    g.drawLine(0, padding + colorBlockLen,
                            2 * padding + colorsPerRow * colorBlockLen, padding + colorBlockLen);
                }
                // vertical lines
                for (int i = 0 ; i < colorsPerRow ; i++) {
                    g.drawLine(padding + i * colorBlockLen, 0, padding + i * colorBlockLen,
                            2 * padding + colorRows * colorBlockLen);
                }
            }
        };
        recentColorsBlock.setSize(130,50);
        paintControlsFrame.add(recentColorsBlock);
        recentColorsBlock.setLocation(50,50);
        recentColorsBlock.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e .getY();

                // sub padding from both
                x -= padding;
                y -= padding;

                // figure out grid points
                int xGrid = x / colorBlockLen;
                int yGrid = y / colorBlockLen;
                int revIndex = xGrid + yGrid * colorsPerRow;

                // make sure in bounds
                if (recentColors.size() < 1 + revIndex)
                    return;

                // get clicked color and set
                setNewPaintColor(recentColors.get(recentColors.size() - 1 - revIndex));
            }
        });

        // initial colors
        setNewPaintColor(CyderColors.navy);
        setNewPaintColor(CyderColors.regularPink);
        setNewPaintColor(CyderColors.regularOrange);
        setNewPaintColor(CyderColors.regularGreen);
        setNewPaintColor(CyderColors.regularBlue);
        setNewPaintColor(CyderColors.tooltipForegroundColor);

        colorHexField = new CyderTextField(11);
        colorHexField.setHorizontalAlignment(JTextField.CENTER);
        colorHexField.setToolTipText("Format: 45FF00 for hex or 255,255,255 for rgb");
        colorHexField.setSize(150, 40);
        colorHexField.setRegexMatcher(CyderRegexPatterns.rgbOrHex);
        colorHexField.addActionListener(e -> {
            String text = colorHexField.getText();

            if (text.contains(",")) {
                String[] parts = text.split(",");

                if (parts.length != 3) {
                    paintControlsFrame.notify("Could not parse color");
                } else {
                    try {
                        int r = Integer.parseInt(parts[0]);
                        int g = Integer.parseInt(parts[1]);
                        int b = Integer.parseInt(parts[2]);

                        Color newColor = new Color(r, g, b);
                        setNewPaintColor(newColor);
                    } catch (Exception ignored) {
                        paintControlsFrame.notify("Could not parse color");
                    }
                }
            } else {
                try {
                    Color newColor = ColorUtil.hexToRgb(colorHexField.getText());
                    setNewPaintColor(newColor);
                } catch (Exception ignored) {
                    paintControlsFrame.notify("Could not parse color");
                }
            }
        });
        colorHexField.setText(ColorUtil.rgbToHexString(currentPaintColor));
        //todo add hex field

        CyderCheckboxGroup group = new CyderCheckboxGroup();

        CyderLabel addLabel = new CyderLabel("Add");
        addLabel.setSize(50,20);
        //todo add addlabel

        add = new CyderCheckbox();
        add.setToolTipText("Paint cells");
        add.setSize(50, 50);
        add.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cyderGrid.setMode(CyderGrid.Mode.ADD);
            }
        });
        group.addCheckbox(add);
        add.setSelected();
        //todo add add

        CyderLabel deleteLabel = new CyderLabel("Delete");
        deleteLabel.setSize(50,20);
        //todo add deletelabel

        CyderCheckbox delete = new CyderCheckbox();
        delete.setSize(50, 50);
        delete.setToolTipText("Delete cells");
        delete.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cyderGrid.setMode(CyderGrid.Mode.DELETE);
            }
        });
        group.addCheckbox(delete);
        //todo add delete

        CyderLabel brushLabel = new CyderLabel("Brush width: " + brushWidth);
        brushLabel.setSize(250, 40);
        //todo add to label

        JSlider brushWidthSlider = new JSlider(JSlider.HORIZONTAL, MIN_BRUSH_WIDTH,
                MAX_BRUSH_WIDTH, DEFAULT_BRUSH_WIDTH);
        CyderSliderUI UI = new CyderSliderUI(brushWidthSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setSliderShape(SliderShape.RECT);
        UI.setFillColor(Color.black);
        UI.setOutlineColor(CyderColors.navy);
        UI.setNewValColor(CyderColors.regularBlue);
        UI.setOldValColor(CyderColors.regularPink);
        UI.setTrackStroke(new BasicStroke(3.0f));
        brushWidthSlider.setUI(UI);
        brushWidthSlider.setSize(250, 40);
        brushWidthSlider.setPaintTicks(false);
        brushWidthSlider.setPaintLabels(false);
        brushWidthSlider.setVisible(true);
        brushWidthSlider.setValue(DEFAULT_BRUSH_WIDTH);
        brushWidthSlider.addChangeListener(e -> {
            int newWidth = brushWidthSlider.getValue();
            brushWidth = newWidth;
            brushLabel.setText("Brush width: " + newWidth);
            cyderGrid.setDrawWidth(brushWidth);
        });
        brushWidthSlider.setOpaque(false);
        brushWidthSlider.setToolTipText("Brush Width");
        brushWidthSlider.setFocusable(false);
        brushWidthSlider.repaint();
        //todo add brush width slider

        CyderButton undo = new CyderButton("<");
        undo.setSize(30, 35);
        undo.setToolTipText("Undo");
        undo.addActionListener(e -> {
            cyderGrid.backwardState();
            cyderGrid.revalidate();
            cyderGrid.repaint();
            paintFrame.revalidate();
            paintFrame.repaint();
        });
        //todo add undo find icon

        CyderButton redo = new CyderButton(">");
        redo.setSize(30, 35);
        redo.setToolTipText("Redo");
        redo.addActionListener(e -> {
            cyderGrid.forwardState();
            cyderGrid.revalidate();
            cyderGrid.repaint();
            paintFrame.revalidate();
            paintFrame.repaint();
        });
        //todo add redo find icon

        CyderIconButton selectionTool = new CyderIconButton("Select Region",
                new ImageIcon("static/pictures/paint/select.png"),
                new ImageIcon("static/pictures/paint/select_hover.png"), null);
        selectionTool.addActionListener(e -> toggleSelectionMode());
        selectionTool.setSize(50, 50);
        bottomLayout.addComponent(selectionTool, 0,0);

        CyderIconButton cropToRegion = new CyderIconButton("Crop Region",
                new ImageIcon("static/pictures/paint/crop.png"),
                new ImageIcon("static/pictures/paint/crop_hover.png"), null);
        cropToRegion.setSize(50, 50);
        cropToRegion.addActionListener(e -> cyderGrid.cropToRegion());
        bottomLayout.addComponent(cropToRegion, 1,0);

        CyderIconButton deleteRegion = new CyderIconButton("Delete Region",
                new ImageIcon("static/pictures/paint/cut.png"),
                new ImageIcon("static/pictures/paint/cut_hover.png"), null);
        deleteRegion.setSize(66, 50);
        deleteRegion.setToolTipText("Cut region");
        deleteRegion.addActionListener(e -> cyderGrid.deleteRegion());
        bottomLayout.addComponent(deleteRegion, 2,0);

        CyderIconButton selectColor = new CyderIconButton("Select Color",
                new ImageIcon("static/pictures/paint/select_color.png"),
                new ImageIcon("static/pictures/paint/select_color_hover.png"), null);
        selectColor.setSize(50, 50);
        selectColor.addActionListener(e -> toggleColorSelection());
        bottomLayout.addComponent(selectColor, 3,0);

        CyderIconButton rotate = new CyderIconButton("Rotate Region",
                new ImageIcon("static/pictures/paint/rotate.png"),
                new ImageIcon("static/pictures/paint/rotate_hover.png"), null);
        rotate.setSize(50, 50);
        rotate.addActionListener(e -> cyderGrid.rotateRegion());
        bottomLayout.addComponent(rotate, 4,0);

        // selection region reflecting
        CyderIconButton reflect = new CyderIconButton("Reflect Region",
                new ImageIcon("static/pictures/paint/reflect.png"),
                new ImageIcon("static/pictures/paint/reflect_hover.png"), null);
        reflect.setSize(51, 50);
        reflect.addActionListener(e -> cyderGrid.reflectRegionHorizontally());
        bottomLayout.addComponent(reflect, 5,0);

        // use master layout as content pane
        CyderPanel panel = new CyderPanel(parentLayout);
        paintControlsFrame.setContentPanel(panel);

        // init resizing since we can due to the layout
        paintControlsFrame.initializeResizing();
        paintControlsFrame.setResizable(true);
        paintControlsFrame.setMinimumSize(paintControlsFrame.getSize());
        paintControlsFrame.setMaximumSize(new Dimension(
                (int) (paintControlsFrame.getWidth() * 1.5),
                (int) (paintControlsFrame.getHeight() * 1.5)));
        paintControlsFrame.setBackgroundResizing(true);

        // set visibility
        paintControlsFrame.setVisible(true);

        // place controls right below grid frame
        int y = paintFrame.getY() + paintFrame.getHeight();
        if (y + paintControlsFrame.getHeight() > paintFrame.getMonitorBounds().getHeight())
            y = paintFrame.getMonitorBounds().getBounds().height - paintControlsFrame.getHeight();
        paintControlsFrame.setLocation(paintFrame.getX(), y);
    }

    // --------------
    // paint controls
    // --------------

    /**
     * The default brush width.
     */
    public static final int DEFAULT_BRUSH_WIDTH = 2;

    /**
     * The maximum brush width.
     */
    public static final int MAX_BRUSH_WIDTH = 20;

    /**
     * The minimum brush width.
     */
    public static final int MIN_BRUSH_WIDTH = 1;

    /**
     * The default brush width.
     */
    private static int brushWidth = DEFAULT_BRUSH_WIDTH;

    /**
     * Sets the current color and updates the recent colors block
     *
     * @param newColor the new color
     */
    public static void setNewPaintColor(Color newColor) {
        // if no change, ignore
        if (newColor.equals(currentPaintColor))
            return;

        // set the current paint
        currentPaintColor = newColor;

        // update the hex field with our current color
        //todo colorHexField.setText(ColorUtil.rgbToHexString(newColor));

        // ensure if list contains color, it's pulled to the front
        // of recent colors and is not duplicated in the list
        if (recentColors.contains(newColor)) {
            ArrayList<Color> newRecentColors = new ArrayList<>();

            // add all colors that aren't the new one, remove possible duplicates somehow
            for (Color recentColor : recentColors) {
                if (!recentColor.equals(newColor) && !newRecentColors.contains(recentColor))
                    newRecentColors.add(recentColor);
            }

            // add the new one to the end
            newRecentColors.add(newColor);

            // set recent colors to new object
            recentColors = newRecentColors;
        } else {
            recentColors.add(newColor);
        }

        // repaint block to update colors
        recentColorsBlock.repaint();

        // set grid's paint
        cyderGrid.setNodeColor(currentPaintColor);

        // if the mode is selection, change to add
        if (cyderGrid.getMode() == CyderGrid.Mode.SELECTION) {
            toggleSelectionMode();
        } else if (cyderGrid.getMode() == CyderGrid.Mode.COLOR_SELECTION) {
            toggleColorSelection();
        }
    }

    /**
     * Handles the button press for selection mode.
     */
    private static void toggleSelectionMode() {
        CyderGrid.Mode newMode = cyderGrid.getMode() == CyderGrid.Mode.SELECTION
                ? CyderGrid.Mode.ADD : CyderGrid.Mode.SELECTION;

        if (newMode == CyderGrid.Mode.SELECTION) {
            paintFrame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            cyderGrid.setMode(CyderGrid.Mode.SELECTION);
        } else {
            paintFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            if (add.isEnabled()) {
                cyderGrid.setMode(CyderGrid.Mode.ADD);
            } else {
                cyderGrid.setMode(CyderGrid.Mode.DELETE);
            }
        }

        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * The icon used for color selection mode
     */
    private static final ImageIcon colorSelectionIcon = new ImageIcon("static/pictures/paint/select_color.png");

    /**
     * Toggles between states for color mode selection.
     */
    private static void toggleColorSelection() {
        CyderGrid.Mode newMode = cyderGrid.getMode() == CyderGrid.Mode.COLOR_SELECTION
                ? CyderGrid.Mode.ADD : CyderGrid.Mode.COLOR_SELECTION;

        if (newMode == CyderGrid.Mode.COLOR_SELECTION) {
            Image image = colorSelectionIcon.getImage();
            cyderGrid.setMode(newMode);
            Cursor c = Toolkit.getDefaultToolkit().createCustomCursor(
                    image, new Point(0, 30), "eyedropper");
            paintFrame.setCursor(c);
        } else {
            paintFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            if (add.isEnabled()) {
                cyderGrid.setMode(CyderGrid.Mode.ADD);
            } else {
                cyderGrid.setMode(CyderGrid.Mode.DELETE);
            }
        }

        Toolkit.getDefaultToolkit().sync();
    }
}
