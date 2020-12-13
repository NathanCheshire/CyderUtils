package com.cyder.utilities;

import javax.swing.*;
import java.awt.*;

public class FrameAnimations {

    private GeneralUtil gu;

    public FrameAnimations() {
        gu = new GeneralUtil();
    }

    public void closeAnimation(JFrame frame) {
        try {
            if (frame != null && frame.isVisible()) {
                Point point = frame.getLocationOnScreen();
                int x = (int) point.getX();
                int y = (int) point.getY();

                for (int i = y; i >= 0 - frame.getHeight(); i -= 15) {
                    Thread.sleep(1);
                    frame.setLocation(x, i);
                }

                frame.dispose();
            }
        }

        catch (Exception e) {
            gu.handle(e);
        }
    }

    public void minimizeAnimation(JFrame frame) {
        Point point = frame.getLocationOnScreen();
        int x = (int) point.getX();
        int y = (int) point.getY();

        try {
            for (int i = y; i <= gu.getScreenHeight(); i += 15) {
                Thread.sleep(1);
                frame.setLocation(x, i);
            }

            frame.setState(JFrame.ICONIFIED);
        }

        catch (Exception e) {
            gu.handle(e);
        }
    }

    public void enterAnimation(JFrame frame) {
        frame.setVisible(false);
        frame.setLocationRelativeTo(null);

        int to = frame.getY();
        frame.setLocation(frame.getX(), 0 - frame.getHeight());

        frame.setVisible(true);

        for (int i = 0 - frame.getHeight() ; i < to ; i+= 15) {
            frame.setLocation(frame.getX(), i);
            try {
                Thread.sleep(1);
            }

            catch (Exception e) {
                gu.handle(e);
            }
        }

        frame.setLocationRelativeTo(null);
    }
}
