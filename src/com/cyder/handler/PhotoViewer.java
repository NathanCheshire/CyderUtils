package com.cyder.handler;

import com.cyder.ui.CyderButton;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;

//todo implement a scroll animation, remember to take into account you've resized the image to make it fit on screen.
public class PhotoViewer {

    private LinkedList<File> validImages = new LinkedList<>();
    private File startFile;
    private int currentIndex;

    private Util imageUtil = new Util();
    private JFrame pictureFrame;
    private CyderButton closeDraw;
    private JPanel ParentPanel;

    private JLabel PictureLabel;

    private int xMouse;
    private int yMouse;

    private CyderButton nextImage;
    private CyderButton lastImage;

    public PhotoViewer(File start) {
        startFile = start;
    }

    public void draw() {
        File ImageName = startFile;

        if (pictureFrame != null) {
            imageUtil.closeAnimation(pictureFrame);
            pictureFrame.dispose();
        }

        BufferedImage Image = null;

        try {
            Image = ImageIO.read(ImageName);
        }

        catch (Exception ex) {
            imageUtil.handle(ex);
        }

        pictureFrame = new JFrame();

        pictureFrame.setUndecorated(true);

        pictureFrame.setTitle(ImageName.getName().replace(".png", ""));

        pictureFrame.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (pictureFrame != null && pictureFrame.isFocused()) {
                    pictureFrame.setLocation(x - xMouse, y - yMouse);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                xMouse = e.getX();
                yMouse = e.getY();
            }
        });

        pictureFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ParentPanel = new JPanel();

        ParentPanel.setBorder(new LineBorder(imageUtil.navy,8,false));

        ParentPanel.setLayout(new BorderLayout());

        pictureFrame.setContentPane(ParentPanel);

        PictureLabel = new JLabel(new ImageIcon(Image));

        ParentPanel.add(PictureLabel, BorderLayout.PAGE_START);

        JPanel buttonPanel = new JPanel();

        buttonPanel.setLayout(new GridLayout(1,3,5,5));

        lastImage = new CyderButton("Last");

        lastImage.setColors(imageUtil.regularRed);

        lastImage.setBorder(new LineBorder(imageUtil.navy,5,false));

        lastImage.setFocusPainted(false);

        lastImage.setBackground(imageUtil.regularRed);

        lastImage.setFont(imageUtil.weatherFontSmall);

        lastImage.addActionListener(e -> scrollBack());

        buttonPanel.add(lastImage);

        closeDraw = new CyderButton("Close");

        closeDraw.setColors(imageUtil.regularRed);

        closeDraw.setBorder(new LineBorder(imageUtil.navy,5,false));

        closeDraw.setFocusPainted(false);

        closeDraw.setBackground(imageUtil.regularRed);

        closeDraw.setFont(imageUtil.weatherFontSmall);

        closeDraw.addActionListener(e -> {
            imageUtil.closeAnimation(pictureFrame);
            pictureFrame.dispose();
            pictureFrame = null;
        });

        buttonPanel.add(closeDraw);

        nextImage = new CyderButton("Next");

        nextImage.setColors(imageUtil.regularRed);

        nextImage.setBorder(new LineBorder(imageUtil.navy,5,false));

        nextImage.setFocusPainted(false);

        nextImage.setBackground(imageUtil.regularRed);

        nextImage.setFont(imageUtil.weatherFontSmall);

        nextImage.addActionListener(e -> scrollFoward());

        buttonPanel.add(nextImage);

        buttonPanel.setBackground(imageUtil.navy);

        ParentPanel.add(buttonPanel,BorderLayout.PAGE_END);

        ParentPanel.repaint();

        pictureFrame.pack();

        pictureFrame.setVisible(true);

        pictureFrame.setLocationRelativeTo(null);

        pictureFrame.setAlwaysOnTop(true);

        pictureFrame.setAlwaysOnTop(false);

        pictureFrame.setResizable(false);

        pictureFrame.setIconImage(imageUtil.getCyderIcon().getImage());

        initFiles();
    }

    private void initFiles() {
        File[] possibles = startFile.getParentFile().listFiles();

        for (File f : possibles) {
            if (f.getName().endsWith(".png"))
                validImages.add(f);
        }

        for (int i = 0 ; i < validImages.size() ; i++) {
           if (validImages.get(i).getAbsolutePath().equalsIgnoreCase(startFile.getAbsolutePath())) {
               currentIndex = i;
           }
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int type, int img_width, int img_height) {
        BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, img_width, img_height, null);
        g.dispose();

        return resizedImage;
    }

    private void scrollFoward() {
        try {
            if (currentIndex + 1 < validImages.size()) {
                currentIndex++;
                ImageIcon next = checkImage(validImages.get(currentIndex));
                PictureLabel.setIcon(next);
                PictureLabel.repaint();
                ParentPanel.repaint();
                pictureFrame.pack();
                pictureFrame.revalidate();
                pictureFrame.setLocationRelativeTo(null);
            }
        }

        catch (Exception e) {
            imageUtil.handle(e);
        }
    }

    private void scrollBack() {
        try {
            if (currentIndex - 1 >= 0) {
                currentIndex--;
                ImageIcon last = checkImage(validImages.get(currentIndex));
                PictureLabel.setIcon(last);
                PictureLabel.repaint();
                ParentPanel.repaint();
                pictureFrame.pack();
                pictureFrame.revalidate();
                pictureFrame.setLocationRelativeTo(null);
            }
        }

        catch (Exception e) {
            imageUtil.handle(e);
        }
    }

    private ImageIcon checkImage(File im) {
        try {
            Dimension dim = imageUtil.getScreenSize();
            double screenX = dim.getWidth();
            double screenY = dim.getHeight();

            double aspectRatio = getAspectRatio(new ImageIcon(ImageIO.read(im)));
            ImageIcon originalIcon = new ImageIcon(ImageIO.read(im));
            BufferedImage bi = ImageIO.read(im);

            int width = originalIcon.getIconWidth();
            int height = originalIcon.getIconHeight();


            while (width > screenX || height > screenY) {
                width = (int) (width / aspectRatio);
                height = (int) (height / aspectRatio);
            }

            return new ImageIcon(bi.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }

        catch (Exception e) {
            imageUtil.handle(e);
        }

        return null;
    }

    private double getAspectRatio(ImageIcon im) {
        return ((double) im.getIconWidth() / (double) im.getIconHeight());
    }
}
