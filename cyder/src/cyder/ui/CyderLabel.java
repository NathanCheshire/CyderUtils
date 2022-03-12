package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.objects.TaggedString;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class CyderLabel extends JLabel {
    public CyderLabel() {
        this("CyderLabel default text");
    }

    public CyderLabel(String text) {
        setText(text);
        setForeground(CyderColors.navy);
        setFont(CyderFonts.defaultFontSmall);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
            }
        });

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns html text which constrains its parent label to the provided pixel bounds.
     *
     * @param text the text of the label
     * @param width the width of the label
     * @param height the height of the label
     * @return html text which constrains its parent label to the provided pixel bounds
     */
    public static String generateConstraintedWidthTag(String text, int width, int height) {
        return "<div style=\"width:" + width + "px; height:" + height + "px; background:#000000\">" + text + "</div>";
    }

    public CyderLabel(String text, int horizontalAlignment) {
        setText(text);
        setForeground(CyderColors.navy);
        setFont(CyderFonts.defaultFontSmall);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        setHorizontalAlignment(horizontalAlignment);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
            }
        });

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    @Override
    public void setText(String text) {
        if (text == null || text.isEmpty()) {
            super.setText(text);
        } else if (!text.startsWith("<html>")) {
            super.setText("<html><div style='text-align: center;'>" + text + "</html>");
        } else {
            super.setText(text);
        }
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    //rippling
    private Color rippleColor = CyderColors.regularRed;
    private long rippleMsTimeout = 100;
    private int rippleChars = 1;
    private boolean isRippling;

    public int getRawTextLength() {
        return Jsoup.clean(getText(), Safelist.none()).length();
    }

    public int getRippleChars() {
        return rippleChars;
    }

    public void setRippleChars(int rippleChars) {
        if (getText() != null && rippleChars > getRawTextLength() / 2)
            this.rippleChars = getRawTextLength() / 2;
        else
            this.rippleChars = rippleChars;
    }

    public Color getRippleColor() {
        return rippleColor;
    }

    public void setRippleColor(Color rippleColor) {
        this.rippleColor = rippleColor;
    }

    public long getRippleMsTimeout() {
        return rippleMsTimeout;
    }

    public void setRippleMsTimeout(long rippleMsTimeout) {
        this.rippleMsTimeout = rippleMsTimeout;
    }

    public boolean isRippling() {
        return isRippling;
    }

    public void setRippling(boolean rippling) {
        isRippling = rippling;

        if (rippling)
            beginRippleSequence();
    }

    private void beginRippleSequence() {
        new Thread(() -> {
            try {
                //restore color so everything goes back to original foreground
                Color restoreColor = getForeground();

                String originalText = getText();

                //used to insert color properly
                String parsedChars = Jsoup.clean(getText(), Safelist.none());

                //init list for strings by tag
                LinkedList<TaggedString> taggedStrings = StringUtil.getTaggedStrings(originalText);

                //init ripple iterations list
                LinkedList<String> rippleTextIterations = new LinkedList<>();

                //find ripple steps: this takes < 1ms usually

                //still used parsed chars here since that's all we care about rippling anyway
                for (int i = 0 ; i < parsedChars.length() ; i++) {
                    //init builder for this iteration where the ith char
                    // (could be from any non-html tag), is ripple color
                    StringBuilder builder = new StringBuilder();

                    //charSum is how many chars we have passed of the Text tagged string
                    int charSum = 0;

                    //how many characters we've set to the rippling char
                    int rippled = 0;

                    //loop through all our tagged string
                    for (TaggedString ts : taggedStrings) {
                        //if it's html simply add it to the builder
                        if (ts.getType() == TaggedString.Type.HTML) {
                            builder.append(ts.getText());
                        }
                        //otherwise we might need to ripple some  chars
                        else {
                            //loop through all the chars of this Text tagged string
                            for (char c : ts.getText().toCharArray()) {
                                //first we need to pass as many raw chars
                                // as the iteration "i" we are on, next we need to make sure
                                // we havne't used up all the ripple chars for this iteration
                                if (charSum >= i && rippled < rippleChars) {
                                    //ripple this char and inc rippled
                                    builder.append(getColoredText(String.valueOf(c), rippleColor));
                                    rippled++;
                                }
                                //otherwise append the char normal, without extra styling
                                else {
                                    builder.append(c);
                                }

                                //increment our position in the Text tagged strings
                                charSum++;
                            }
                        }
                    }

                    //add this text iteration to our list
                    if (builder.toString().startsWith("<html>"))
                        rippleTextIterations.add(builder.toString());
                    else
                        rippleTextIterations.add("<html>" + builder + "</html>");
                }

                //now ripple through our ripple iterations
                RIPPLING:
                    while (isRippling && !((((CyderFrame) SwingUtilities.getWindowAncestor(this))).isDisposed())) {
                        for (String rippleText : rippleTextIterations) {
                            setText(rippleText);

                            repaint();
                            Thread.sleep(rippleMsTimeout);

                            //check for break to free resources quickly
                            if (!isRippling)
                                break RIPPLING;
                        }

                        if (!isRippling)
                            break;
                    }

                //fix foreground and text
                setText(originalText);
                setForeground(restoreColor);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Rippled thread for CyderLabel: " + this).start();
    }

    private String getColoredText(String text, Color c) {
        return "<font color = rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")>" + text + "</font>";
    }
}
