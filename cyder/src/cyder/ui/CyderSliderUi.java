package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.handlers.internal.Logger;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

/**
 * A ui layer for {@link JSlider}s utilized by Cyder.
 */
public class CyderSliderUi extends BasicSliderUI {
    /**
     * The default stroke width for the slider and thumb strokes.
     */
    public static final float DEFAULT_STROKE_WIDTH = 3.0f;

    /**
     * The stroke for painting the slider track.
     */
    private BasicStroke sliderStroke = new BasicStroke(DEFAULT_STROKE_WIDTH);

    /**
     * The stroke for painting the thumb.
     */
    private BasicStroke thumbStroke = new BasicStroke(DEFAULT_STROKE_WIDTH);

    /**
     * The track color left of the thumb.
     */
    private Color leftThumbColor;

    /**
     * The track color right of the thumb.
     */
    private Color rightThumbColor;

    /**
     * The color to fill the thumb with.
     */
    private Color thumbFillColor;

    /**
     * The outline color for the thumb.
     */
    private Color thumbOutlineColor;

    /**
     * The thumb radius when the slider shape is an ellipse.
     */
    private int thumbRadius = 10;

    /**
     * The slider this ui manager is controlling.
     */
    private final JSlider slider;

    /**
     * The shape of the thumb.
     */
    private ThumbShape thumbShape = ThumbShape.RECT;

    /**
     * Sets the radius of the thumb.
     *
     * @param radius the radius of the thumb.
     */
    public void setThumbRadius(int radius) {
        Preconditions.checkArgument(radius > 0);
        thumbRadius = radius;
    }

    /**
     * Returns the radius of the thumb.
     *
     * @return the radius of the thumb
     */
    public int getThumbRadius() {
        return thumbRadius;
    }

    /**
     * Sets the shape of the slider thumb.
     *
     * @param shape the shape of the slider thumb
     */
    public void setThumbShape(ThumbShape shape) {
        thumbShape = shape;
    }

    /**
     * Sets the track color to the left of the thumb
     *
     * @param color the track color to the left of the thumb
     */
    public void setLeftThumbColor(Color color) {
        leftThumbColor = color;
    }

    /**
     * Sets the track color to the right of the thumb.
     *
     * @param color the track color to the right of the thumb
     */
    public void setRightThumbColor(Color color) {
        rightThumbColor = color;
    }

    /**
     * Sets the fill color of the thumb.
     *
     * @param color the fill color of the thumb
     */
    public void setThumbFillColor(Color color) {
        thumbFillColor = color;
    }

    /**
     * Sets the outline color of the thumb.
     *
     * @param color the outline color of the thumb
     */
    public void setThumbOutlineColor(Color color) {
        thumbOutlineColor = color;
    }

    /**
     * Sets the stroke of the track.
     *
     * @param stroke the stroke of the track
     */
    public void setTrackStroke(BasicStroke stroke) {
        sliderStroke = stroke;
    }

    /**
     * Sets the stroke of the thumb.
     *
     * @param stroke the stroke of the thumb
     */
    public void setThumbStroke(BasicStroke stroke) {
        thumbStroke = stroke;
    }

    /**
     * Creates a new CyderSliderUi object.
     *
     * @param slider the slider this ui is controlling and styling
     */
    public CyderSliderUi(JSlider slider) {
        super(slider);
        this.slider = slider;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void calculateThumbSize() {
        super.calculateThumbSize();
        thumbRect.setSize(thumbRect.width, thumbRect.height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BasicSliderUI.TrackListener createTrackListener(JSlider slider) {
        return new RangeTrackListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void calculateThumbLocation() {
        super.calculateThumbLocation();

        // snap to ticks
        if (slider.getSnapToTicks()) {
            int upperValue = slider.getValue() + slider.getExtent();
            int majorTickSpacing = slider.getMajorTickSpacing();
            int minorTickSpacing = slider.getMinorTickSpacing();
            int tickSpacing = 0;

            if (minorTickSpacing > 0) {
                tickSpacing = minorTickSpacing;
            } else if (majorTickSpacing > 0) {
                tickSpacing = majorTickSpacing;
            }

            int snappedValue = upperValue;

            if (tickSpacing != 0) {
                if ((upperValue - slider.getMinimum()) % tickSpacing != 0) {
                    float temp = (float) (upperValue - slider.getMinimum()) / (float) tickSpacing;
                    int whichTick = Math.round(temp);
                    snappedValue = slider.getMinimum() + (whichTick * tickSpacing);
                }

                if (snappedValue != upperValue) {
                    slider.setExtent(snappedValue - slider.getValue());
                }
            }
        }

        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int upperPosition = xPositionForValue(slider.getValue() + slider.getExtent());
            thumbRect.x = upperPosition - (thumbRect.width / 2);
            thumbRect.y = trackRect.y;

        } else {
            int upperPosition = yPositionForValue(slider.getValue() + slider.getExtent());
            thumbRect.x = trackRect.x;
            thumbRect.y = upperPosition - (thumbRect.height / 2);
        }

        slider.repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Dimension getThumbSize() {
        return super.getThumbSize();
    }

    private Shape createThumbShape(int width, int height) {
        return new Rectangle2D.Double(0, 0, width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke old = g2d.getStroke();

        g2d.setStroke(sliderStroke);
        g2d.setPaint(rightThumbColor);

        Rectangle trackBounds = trackRect;

        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            g2d.drawLine(trackRect.x, trackRect.y + trackRect.height / 2,
                    trackRect.x + trackRect.width, trackRect.y + trackRect.height / 2);

            int lowerX = thumbRect.width / 2;
            int upperX = thumbRect.x + (thumbRect.width / 2);
            int cy = (trackBounds.height / 2) - 2;

            g2d.translate(trackBounds.x, trackBounds.y + cy);
            g2d.setColor(leftThumbColor);
            g2d.drawLine(lowerX - trackBounds.x, 2,
                    upperX - trackBounds.x - (thumbShape == ThumbShape.HOLLOW_CIRCLE ? 10 : 0), 2);
            g2d.translate(-trackBounds.x, -(trackBounds.y + cy));
        }
        g2d.setStroke(old);
    }

    /**
     * Returns the location of the center of the thumb relative to the slider's bounds.
     *
     * @return the location of the center of the thumb relative to the slider's bounds
     */
    public Point getThumbCenter() {
        return new Point((int) (trackRect.getX() + trackRect.getWidth()
                * ((float) slider.getValue() / slider.getMaximum())),
                (int) (trackRect.getY() + trackRect.getHeight()
                        * ((float) slider.getValue() / slider.getMaximum())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2d;

        switch (thumbShape) {
            case CIRCLE -> {
                g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(thumbFillColor);
                int x = (int) (trackRect.getX() + trackRect.getWidth() * slider.getValue() / slider.getMaximum() -
                        (thumbRadius / 4));
                int y = (int) (trackRect.getY() + trackRect.getHeight() / 2 - (thumbRadius / 4));
                g.fillOval(x, y, thumbRadius / 2, thumbRadius / 2);
                g2d.dispose();
            }
            case RECT -> {
                Rectangle knobBounds = thumbRect;
                int w = knobBounds.width;
                int h = knobBounds.height;
                g2d = (Graphics2D) g.create();
                Shape thumbShape = createThumbShape(w - 1, h - 1);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.translate(knobBounds.x, knobBounds.y);
                g2d.setColor(thumbFillColor);
                g2d.fill(thumbShape);

                g2d.setColor(thumbOutlineColor);
                g2d.draw(thumbShape);
                g2d.dispose();
            }
            case HOLLOW_CIRCLE -> {
                g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setStroke(thumbStroke);
                Rectangle t = thumbRect;
                g2d.setColor(thumbFillColor);
                g2d.drawOval(t.x - 5, t.y, 20, 20);
                g2d.dispose();
            }
            case NONE -> {
            }
            default -> throw new IllegalArgumentException("Invalid slider shape: " + thumbShape);
        }
    }

    private boolean upperDragging;

    /**
     * A custom range track listener.
     */
    private class RangeTrackListener extends BasicSliderUI.TrackListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }
            currentMouseX -= thumbRect.width / 2;
            moveUpperThumb();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (slider.isRequestFocusEnabled()) {
                slider.requestFocus();
            }

            if (thumbRect.contains(currentMouseX, currentMouseY)) {
                switch (slider.getOrientation()) {
                    case JSlider.VERTICAL -> offset = currentMouseY - thumbRect.y;
                    case JSlider.HORIZONTAL -> offset = currentMouseX - thumbRect.x;
                }

                upperDragging = true;
                return;
            }

            upperDragging = false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            upperDragging = false;
            slider.setValueIsAdjusting(false);
            super.mouseReleased(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (upperDragging) {
                slider.setValueIsAdjusting(true);
                moveUpperThumb();

            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean shouldScroll(int direction) {
            return false;
        }

        public void moveUpperThumb() {
            int thumbMiddle;
            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int halfThumbWidth = thumbRect.width / 2;
                int thumbLeft = currentMouseX - offset;
                int trackLeft = trackRect.x;
                int trackRight = trackRect.x + (trackRect.width - 1);
                int hMax = xPositionForValue(slider.getMaximum() -
                        slider.getExtent());

                if (drawInverted()) {
                    trackLeft = hMax;
                } else {
                    trackRight = hMax;
                }
                thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                setThumbLocation(thumbLeft, thumbRect.y);

                thumbMiddle = thumbLeft + halfThumbWidth;
                slider.setValue(valueForXPosition(thumbMiddle));
            }
        }
    }

    /**
     * Possible slider shapes for a CyderSlider thumb.
     */
    public enum ThumbShape {
        /**
         * A classic filled circle.
         */
        CIRCLE,
        /**
         * A rectangle
         */
        RECT,
        /**
         * A circle that isn't filled in, you can see where the track splits colors
         * if old value is different from new value.
         */
        HOLLOW_CIRCLE,
        /**
         * Why though.
         */
        NONE
    }
}