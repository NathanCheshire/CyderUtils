package test.java;

import cyder.consts.CyderColors;
import cyder.consts.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.SessionHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollPane;
import cyder.utilities.ImageUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import static java.lang.System.out;

public class Debug {
    private Debug() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    private static boolean open = false;
    private static JTextPane printArea = new JTextPane();
    private static CyderScrollPane printScroll;
    private static StringUtil printingUtil = new StringUtil(printArea);
    private static CyderFrame debugFrame;

    //here incase we close the window so we can open it back up and be in the same place
    private static LinkedList<String> lines = new LinkedList<>();

    public static <T> void print(T objMaybe) {
        print(objMaybe, false, true);
    }

    public static <T> void print(T objMaybe, boolean showDebugConsole, boolean log) {
        //this should be the only System.out.print call in the whole program
        out.print(objMaybe);

        //log the debug print (this is why you should debug print from here)
        if (log) {
            SessionHandler.log(SessionHandler.Tag.DEBUG_PRINT, objMaybe);
        }

        //add new line to lines
        lines.add(objMaybe.toString());

        if (showDebugConsole) {
            initDebugWindow();

            printArea.setText("");

            //append everything needed to frame
            for (String str : lines) {
                printingUtil.print(str);
            }
        }
    }

    public static <T> void println(T objMaybe) {
       print(objMaybe + "\n", false, true);
    }

    public static <T> void println(T objMaybe, boolean showDebugConsole) {
        print(objMaybe + "\n", showDebugConsole, true);
    }

    public static <T> void println(T objMaybe, boolean showDebugConsole, boolean log) {
        print(objMaybe + "\n", showDebugConsole, log);
    }

    private static void bringMenuToFront() {
        if (debugFrame == null)
            throw new IllegalArgumentException("Frame is null");

        if (debugFrame.getState() == JFrame.ICONIFIED) {
            initDebugWindow();
        }
    }

    public static Semaphore debugWindowOpeningSem = new Semaphore(1);

    private static void initDebugWindow() {
        try {
            debugWindowOpeningSem.acquire();

            //just acquired sem so make sure that it isn't already open again
            if (!open) {
                if (debugFrame != null)
                    debugFrame.dispose();

                debugFrame = new CyderFrame(1050,400, ImageUtil.imageIconFromColor(new Color(21,23,24)));
                debugFrame.setTitle("Debug");
                debugFrame.setFrameType(CyderFrame.FrameType.POPUP);
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
                printScroll.setThumbColor(CyderColors.regularPink);
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
                debugFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());

                open = true;
            } else {
                bringMenuToFront();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            debugWindowOpeningSem.release();
        }
    }

    public static void here() {
        println("here",false);
    }

    public static void launchTests() {
        try {
            ConsoleFrame.getConsoleFrame().getInputHandler().println(CyderCommon.JAR_MODE);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
