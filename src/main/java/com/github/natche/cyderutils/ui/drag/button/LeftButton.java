package com.github.natche.cyderutils.ui.drag.button;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.ui.drag.DragLabelButtonSize;

import java.awt.*;

/** An arrow button pointing left. */
public class LeftButton extends CyderDragLabelButton {
    /** The text for the left button. */
    private static final String LEFT = "Left";

    /** The padding between the edges of the painted left button. */
    private static final int PAINT_PADDING = 4;

    /** The size of the rectangles to draw in the paint method. */
    private static final int drawnRectangleLength = 2;

    /** The size this left button will be painted with. */
    private DragLabelButtonSize size;

    /** Constructs a new left button. */
    public LeftButton() {
        this(DEFAULT_SIZE);
    }

    /**
     * Constructs a new left button.
     *
     * @param size the size of this left button
     */
    public LeftButton(DragLabelButtonSize size) {
        this.size = Preconditions.checkNotNull(size);

        setToolTipText(LEFT);

        setSize(size.getSize(), size.getSize());
        repaint();
    }

    /**
     * Returns the actual size of the painted left button after accounting for padding.
     *
     * @return the actual size of the painted left button after accounting for padding
     */
    private int getPaintLength() {
        Preconditions.checkNotNull(size);
        return size.getSize() - 2 * PAINT_PADDING;
    }

    /** {@inheritDoc} */
    @Override
    public void paintDragLabelButton(Graphics g) {
        Preconditions.checkNotNull(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(PAINT_PADDING, PAINT_PADDING);

        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(getPaintColor());

        for (int i = getPaintLength() ; i >= 0 ; i--) {
            g2d.fillRect(getPaintLength() - i, getPaintLength() - i / 2,
                    drawnRectangleLength, drawnRectangleLength);
            g2d.fillRect(i, getPaintLength() / 2 - i / 2, drawnRectangleLength, drawnRectangleLength);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setSize(DragLabelButtonSize size) {
        this.size = Preconditions.checkNotNull(size);
        repaint();
    }

    /** {@inheritDoc} */
    @Override
    public String getSpecificStringRepresentation() {
        return LEFT;
    }
}
