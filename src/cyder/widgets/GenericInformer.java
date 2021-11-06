package cyder.widgets;

import cyder.consts.CyderImages;
import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.BoundsUtil;

import java.awt.*;

public class GenericInformer {
    //returns the CyderFrame instance to be shown elsewhere
    public static CyderFrame informRet(String text, String title) {
        try {
            CyderLabel textLabel = new CyderLabel(text);
            BoundsUtil.BoundsString boundsString = BoundsUtil.widthHeightCalculation(text);
            textLabel.setText(BoundsUtil.addCenteringToHTML(boundsString.getText()));
            textLabel.setBounds(10,30, boundsString.getWidth(), boundsString.getHeight());

            CyderFrame informFrame = new CyderFrame(boundsString.getWidth() + 40,
                    boundsString.getHeight() + 40, CyderImages.defaultBackgroundLarge);
            informFrame.setFrameType(CyderFrame.FrameType.POPUP);
            informFrame.setTitle(title);
            informFrame.add(textLabel);

            return informFrame;
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }

    public static void inform(String text, String title) {
        informRelative(text, title, GenesisShare.getDominantFrame());
    }

    public static void informRelative(String text, String title, Component relativeTo) {
        try {
            CyderLabel textLabel = new CyderLabel(text);
            BoundsUtil.BoundsString boundsString = BoundsUtil.widthHeightCalculation(text);
            textLabel.setText(BoundsUtil.addCenteringToHTML(boundsString.getText()));
            textLabel.setBounds(10,30, boundsString.getWidth(), boundsString.getHeight());

            CyderFrame informFrame = new CyderFrame(boundsString.getWidth() + 20,
                    boundsString.getHeight() + 40, CyderImages.defaultBackgroundLarge);
            informFrame.setFrameType(CyderFrame.FrameType.POPUP);
            informFrame.setTitle(title);
            informFrame.add(textLabel);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(relativeTo);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
