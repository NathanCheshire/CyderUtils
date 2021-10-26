package cyder.testing;

import cyder.consts.CyderColors;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollPane;
import cyder.utilities.ImageUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

public class DebugConsole {
    private static boolean open = false;
    private static JTextPane printArea = new JTextPane();
    private static CyderScrollPane printScroll;
    private static StringUtil printingUtil = new StringUtil(printArea);
    private static CyderFrame debugFrame;

    //here incase we close the window so we can open it back up and be in the same place
    private static LinkedList<String> lines = new LinkedList<>();

    public static <T> void print(T objMaybe) {
        //this should be the only System.out.print call in the whole program
        System.out.print(objMaybe);

        if (!open) {
            initDebugWindow();

            printArea.setText("");

            //append everything needed to frame
            for (String str : lines) {
                printingUtil.print(str);
            }
        } else {
            bringMenuToFront();
        }

        //add new to lines and print
        lines.add(objMaybe.toString());
        printingUtil.print(objMaybe.toString());
    }

    public static <T> void println(T objMaybe) {
       print(objMaybe + "\n");
    }

    private static void bringMenuToFront() {
        if (debugFrame == null)
            throw new IllegalArgumentException("Frame is null");

        if (debugFrame.getState() == JFrame.ICONIFIED) {
            initDebugWindow();
        }
    }

    private static void initDebugWindow() {
        if (debugFrame != null)
            debugFrame.dispose();

        debugFrame = new CyderFrame(1050,400, ImageUtil.imageIconFromColor(new Color(21,23,24)));
        debugFrame.setTitle("Prints");
        debugFrame.setBackground(new Color(21,23,24));

        printArea.setBounds(20, 40, 500 - 40, 500 - 80);
        printArea.setBackground(new Color(21,23,24));
        printArea.setBorder(null);
        printArea.setFocusable(false);
        printArea.setEditable(false);
        printArea.setFont(new Font("Agency FB",Font.BOLD, 26));
        printArea.setForeground(new Color(85,181,219));
        printArea.setCaretColor(printArea.getForeground());

        printScroll = new CyderScrollPane(printArea,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        printScroll.setThumbColor(CyderColors.intellijPink);
        printScroll.setBounds(20, 40, 1050 - 40, 400 - 80);
        printScroll.getViewport().setOpaque(false);
        printScroll.setOpaque(false);
        printScroll.setBorder(null);
        printArea.setAutoscrolls(true);

        debugFrame.getContentPane().add(printScroll);

        debugFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                open = false;
            }

            @Override
            public void windowClosed(WindowEvent e) {
                open = false;
            }
        });
        debugFrame.setVisible(true);
        debugFrame.setAlwaysOnTop(true);
        debugFrame.setLocation(0, SystemUtil.getScreenHeight() - debugFrame.getHeight());

        open = true;
    }

    public static void launchTests() {
        //todo all windows should be added to an animated stack which will replace the menu
        // should be similar to music controls panel, slide in and out and move input/output fields out of the way
        // should be full height of consoleFrame too
        // should be using labels on labels that have a priority the same as notifications but slightly lower so that
        // notifications are always on top, could also have drag listoners on these "tiles"

        //todo figure out how storage is going to work for dynamic files such as user files, shouldn't just be plain
        // folder and accessible, maybe a zip writer or some other format
        //todo anything set during a user session that resides in sys.json needs to be moved to user data

        //todo update to java 17 and fix all gradle issues

        //todo embed TOR and send logs to "me" before exiting program
    }
}
