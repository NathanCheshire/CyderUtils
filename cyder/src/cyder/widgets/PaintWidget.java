package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.ui.*;
import cyder.utilities.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class PaintWidget {
    private static CyderFrame paintFrame;
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

        int len = 800;
        int padding = 0;

        paintFrame = new CyderFrame(len + padding * 2,
                len + DragLabel.DEFAULT_HEIGHT + padding * 2);
        paintFrame.setTitle("Paint");
        paintFrame.setBackground(CyderIcons.defaultBackgroundLarge);

        cyderGrid = new CyderGrid(200, len);
        cyderGrid.setBounds(padding,DragLabel.DEFAULT_HEIGHT + padding - 5, len, len);
        paintFrame.getContentPane().add(cyderGrid);
        cyderGrid.setDrawExtendedBorder(true);
        cyderGrid.setResizable(true);
        cyderGrid.setDrawGridLines(false);
        cyderGrid.installClickPlacer();
        cyderGrid.installDragPlacer();
        cyderGrid.setSmoothScrolling(true);

        paintFrame.setVisible(true);
        paintFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());

        installControlFrames();
    }

    private static CyderFrame paintControlsFrame;

    private static final Stack<Color> backwardColors = new Stack<>();
    private static final Stack<Color> forwardColors = new Stack<>();
    private static Color currentPaintColor = CyderColors.regularPink;
    private static CyderTextField colorHexField;

    /**
     * Opens the paint controls frame
     */
    private static void installControlFrames() {
        if (paintControlsFrame != null)
            paintControlsFrame.dispose();

        paintControlsFrame = new CyderFrame(600,150);
        paintControlsFrame.setTitle("Controls");
        paintControlsFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);

        int colorLabelX = 70;
        int colorLabelY = 40;
        JLabel recentColorsLabel = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(CyderColors.navy);
                g.fillRect(0,0, colorLabelX, colorLabelY);

                g.setColor(currentPaintColor);
                g.fillRect(5,5, colorLabelX - 10, colorLabelY - 10);
            }
        };
        recentColorsLabel.setBounds(50,DragLabel.DEFAULT_HEIGHT + 10 + 10 + 40, colorLabelX, colorLabelY);
        recentColorsLabel.setToolTipText(ColorUtil.rgbToHexString(currentPaintColor));
        paintControlsFrame.getContentPane().add(recentColorsLabel);

        colorHexField = new CyderTextField(11);
        colorHexField.setToolTipText("Format: 45FF00 for hex or 255,255,255 for rgb");
        colorHexField.setBounds(10, DragLabel.DEFAULT_HEIGHT + 10, 150, 40);
        paintControlsFrame.getContentPane().add(colorHexField);
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
                        setNewPaintColor(newColor, recentColorsLabel);
                    } catch (Exception ignored) {
                        paintControlsFrame.notify("Could not parse color");
                    }
                }
            } else {
                try {
                    Color newColor = ColorUtil.hexToRgb(colorHexField.getText());
                    setNewPaintColor(newColor, recentColorsLabel);
                } catch (Exception ignored) {
                    paintControlsFrame.notify("Could not parse color");
                }
            }
        });
        colorHexField.setText(ColorUtil.rgbToHexString(currentPaintColor));

        backwardColors.clear();
        forwardColors.clear();

        CyderButton backwards = new CyderButton("<");
        backwards.setBounds(10, DragLabel.DEFAULT_HEIGHT + 10 + 10 + 40, 30, 40);
        paintControlsFrame.getContentPane().add(backwards);
        backwards.addActionListener(e -> backwards(recentColorsLabel));

        CyderButton forwards = new CyderButton(">");
        forwards.setBounds(130, DragLabel.DEFAULT_HEIGHT + 10 + 10 + 40, 30, 40);
        paintControlsFrame.getContentPane().add(forwards);
        forwards.addActionListener(e -> forwards(recentColorsLabel));

        CyderCheckboxGroup group = new CyderCheckboxGroup();

        CyderLabel addLabel = new CyderLabel("Add");
        addLabel.setBounds(180,34,50,20);
        paintControlsFrame.getContentPane().add(addLabel);

        CyderCheckbox add = new CyderCheckbox();
        add.setToolTipText("Paint cells");
        add.setBounds(180,60, 50, 50);
        add.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cyderGrid.setMode(CyderGrid.Mode.ADD);
            }
        });
        paintControlsFrame.getContentPane().add(add);
        group.addCheckbox(add);
        add.setSelected();

        CyderLabel deleteLabel = new CyderLabel("Delete");
        deleteLabel.setBounds(235,34,50,20);
        paintControlsFrame.getContentPane().add(deleteLabel);

        CyderCheckbox delete = new CyderCheckbox();
        delete.setBounds(180 + 50 + 10,60, 50, 50);
        delete.setToolTipText("Delete cells");
        delete.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cyderGrid.setMode(CyderGrid.Mode.DELETE);
            }
        });
        paintControlsFrame.getContentPane().add(delete);
        group.addCheckbox(delete);

        cyderGrid.setDefaultNodeColor(currentPaintColor);

        paintControlsFrame.setVisible(true);
        paintControlsFrame.setLocation(paintFrame.getX(), paintFrame.getY() + paintFrame.getWidth() + 20);
    }

    /**
     * Sets the current color and wipes any forward history.
     *
     * @param newColor the new color
     * @param previewLabel the preview label
     */
    private static void setNewPaintColor(Color newColor, final JLabel previewLabel) {
        if (!backwardColors.isEmpty() && backwardColors.peek().equals(newColor))
            return;
        if (newColor.equals(currentPaintColor))
            return;

        System.out.println("here");

        backwardColors.push(currentPaintColor);
        currentPaintColor = newColor;
        previewLabel.repaint();
        previewLabel.setToolTipText(ColorUtil.rgbToHexString(newColor));
        colorHexField.setText(ColorUtil.rgbToHexString(newColor));

        // new path so clear forward colors
        forwardColors.clear();

        // set paint
        cyderGrid.setDefaultNodeColor(currentPaintColor);
    }

    /**
     * Steps back a color if possible from the backwards color stack.
     *
     * @param recentColorsLabel the preview label to update
     */
    private static void backwards(JLabel recentColorsLabel) {
        // if we can go backwards
        if (!backwardColors.isEmpty()) {
            // if we can push to forwards, do so
            if (forwardColors.isEmpty() || forwardColors.peek() != currentPaintColor) {
                forwardColors.push(currentPaintColor);
            }

            // get the new color by going backwards
            currentPaintColor = backwardColors.pop();

            // repaint the preview label and set its tooltip
            recentColorsLabel.repaint();
            recentColorsLabel.setToolTipText(ColorUtil.rgbToHexString(currentPaintColor));

            // update the text field
            colorHexField.setText(ColorUtil.rgbToHexString(currentPaintColor));

            // set paint color
            cyderGrid.setDefaultNodeColor(currentPaintColor);
        }
    }

    /**
     * Steps forward a color if possible from the forwards color stack.
     *
     * @param recentColorsLabel the preview label to update
     */
    private static void forwards(JLabel recentColorsLabel) {
        // if we can go forwards
        if (!forwardColors.isEmpty()) {
            // if we can push to backwards, do so
            if (backwardColors.isEmpty() || backwardColors.peek() != currentPaintColor) {
                backwardColors.push(currentPaintColor);
            }

            // get the new color by going backwards
            currentPaintColor = forwardColors.pop();

            // repaint the preview label and set its tooltip
            recentColorsLabel.repaint();
            recentColorsLabel.setToolTipText(ColorUtil.rgbToHexString(currentPaintColor));

            // update the text field
            colorHexField.setText(ColorUtil.rgbToHexString(currentPaintColor));

            // set paint color
            cyderGrid.setDefaultNodeColor(currentPaintColor);
        }
    }
}
