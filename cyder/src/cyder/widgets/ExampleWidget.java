package cyder.widgets;

import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Widget;
import cyder.enums.LoggerTag;
import cyder.genesis.CyderShare;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderButton;
import cyder.ui.CyderDragLabel;
import cyder.ui.CyderFrame;

public class ExampleWidget {
    private ExampleWidget() {
        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    public static ExampleWidget getInstance() {
        return new ExampleWidget();
    }

    @SuppressCyderInspections(values = "WidgetInspection")
    @Widget(triggers = "example widget", description = "An example base widget for new Cyder developers")
    public static void showGUI() {
        getInstance().innerShowGUI();
    }

    public void innerShowGUI() {
        CyderFrame cyderFrame = new CyderFrame(600,600);
        cyderFrame.setTitle("My Title");
        cyderFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        CyderButton cyderButton = new CyderButton("Button");
        cyderButton.setBounds((600 - 200) / 2, (600 - 40 + CyderDragLabel.DEFAULT_HEIGHT) / 2,200,40);
        cyderButton.addActionListener(e -> {
            // your logic here or a lambda to a class level private method
            cyderFrame.notify("Hello World!");
        });
        cyderFrame.getContentPane().add(cyderButton);

        cyderFrame.setLocationRelativeTo(CyderShare.getDominantFrame());
        cyderFrame.setVisible(true);
    }
}