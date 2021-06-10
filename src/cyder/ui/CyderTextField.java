package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CyderTextField extends JTextField {
    private int limit = 10;
    private Color backgroundColor = CyderColors.vanila;
    private String regex = null;

    public CyderTextField(int colnum) {
        super(colnum);

        this.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
            if (getText().length() >= limit) {
                evt.consume();
            } else if ((regex != null || regex.length() != 0) && !getText().matches(regex)) {
                evt.consume();
            }
            }
        });

        this.setSelectionColor(CyderColors.selectionColor);
        this.setFont(CyderFonts.weatherFontSmall);
        this.setForeground(CyderColors.navy);
        this.setCaretColor(CyderColors.navy);
        this.setCaret(new CyderCaret(CyderColors.navy));
    }

    @Override
    public void setBackground(Color newBackgroundColor) {
        super.setBackground(newBackgroundColor);
        backgroundColor = newBackgroundColor;
    }

    @Override
    public Color getBackground() {
        return backgroundColor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.setForeground(CyderColors.navy);
        this.setFont(CyderFonts.weatherFontSmall);
        this.setSelectionColor(CyderColors.selectionColor);
        this.setBackground(backgroundColor);
        this.setBorder(BorderFactory.createLineBorder(CyderColors.navy,5,false));

        super.paintComponent(g);
    }

    public void setRegexMatcher(String regex) {
        this.regex = regex;
    }

    public void removeRegexMatcher() {
        this.regex = null;
    }

    public String getRegexMatcher() {
        return regex;
    }

    public void setCharLimit(int limit) {
        this.limit = limit;
        if (getText().length() > limit) {
            setText(getText().substring(0,limit + 1));
        }
    }

    public int getCharLimit() {
        return this.limit;
    }

    @Override
    public String toString() {
        return "CyderTextField object, hash=" + this.hashCode();
    }
}
