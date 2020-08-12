package com.cyder.ui;

import com.cyder.utilities.Util;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class CyderFrame extends JFrame {

    private Util fUtil = new Util();
    private int width;
    private int height;
    private ImageIcon background;
    private DragLabel dl;
    private JLabel titleLabel;

    public CyderFrame(int width, int height, ImageIcon background) {
        this.width = width;
        this.height = height;
        this.background = background;
        setSize(new Dimension(width, height));

        setResizable(false);
        setUndecorated(true);
        setIconImage(fUtil.getCyderIcon().getImage());

        JLabel parentLabel = new JLabel();
        parentLabel.setBorder(new LineBorder(fUtil.navy, 5, false));
        parentLabel.setIcon(background);
        setContentPane(parentLabel);

        dl = new DragLabel(width, 30, this);
        dl.setBounds(0, 0, width, 30);
        parentLabel.add(dl);
    }

    @Override
    public void setTitle(String title) {
        titleLabel = new JLabel(title);
        titleLabel.setFont(fUtil.weatherFontSmall.deriveFont(20f));
        titleLabel.setForeground(fUtil.vanila);
        titleLabel.setBounds(5,2,((int) Math.ceil(14 * title.length())),25);
        dl.add(titleLabel);
    }
}
