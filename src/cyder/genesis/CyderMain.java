package cyder.genesis;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.handler.ErrorHandler;
import cyder.handler.SessionLogger;
import cyder.threads.CyderThreadFactory;
import cyder.ui.CyderFrame;
import cyder.utilities.SecurityUtil;
import cyder.utilities.SystemUtil;
import cyder.utilities.IOUtil;
import cyder.widgets.GenericInform;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class CyderMain {
    /**
     * start the best program ever made
     * @param CA - the arguments passed in
     */
    public static void main(String[] CA)  {
        Runtime.getRuntime().addShutdownHook(new Thread(CyderMain::shutdown, "exit-hook"));

        //start the logger
        SessionLogger.SessionLogger();
        SessionLogger.log(SessionLogger.Tag.ENTRY, SystemUtil.getWindowsUsername());

        initSystemProperties();
        initUIManager();

        IOUtil.cleanUsers();
        IOUtil.deleteTempDir();
        IOUtil.logArgs(CA);
        IOUtil.cleanSandbox();
        IOUtil.fixLogs();

        GenesisShare.suspendFrameChecker();
        startFinalFrameDisposedChecker();

        if (osxSystem()) {
            SessionLogger.log(SessionLogger.Tag.LOGIN, "IMPROPER OS");
            GenesisShare.cancelFrameCheckerSuspention();

            CyderFrame retFrame = GenericInform.informRet("System OS not intended for Cyder use. You should" +
                    " install a dual boot or a VM or something.","OS Exception");
            retFrame.addCloseListener(e -> GenesisShare.exit(178));
            retFrame.setVisible(true);
            retFrame.setLocationRelativeTo(null);
        } else if (SecurityUtil.nathanLenovo() && IOUtil.getSystemData("AutoCypher").equals("1")) {
            SessionLogger.log(SessionLogger.Tag.LOGIN, "AUTOCYPHER ATTEMPT");
            Entry.autoCypher();
        } else if (IOUtil.getSystemData("Released").equals("1") || SecurityUtil.nathanLenovo()) {
            Entry.showEntryGUI();
        } else {
            try {
                GenesisShare.getExitingSem().acquire();
                GenesisShare.getExitingSem().release();
                GenesisShare.exit(-600);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }
    }

    /**
     * Initializes System.getProperty key/value pairs
     */
    private static void initSystemProperties() {
        System.setProperty("sun.java2d.uiScale", IOUtil.getSystemData("UISCALE"));
    }

    /**
     * Initializes UIManager.put key/value pairs
     */
    private static void initUIManager() {
        UIManager.put("ToolTip.background", CyderColors.tooltipBackgroundColor);
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createLineBorder(CyderColors.tooltipBorderColor, 2, true)));
        UIManager.put("ToolTip.font", CyderFonts.tahoma.deriveFont(22f));
        UIManager.put("ToolTip.foreground", CyderColors.tooltipForegroundColor);
        UIManager.put("Slider.onlyLeftMouseButtonDrag", Boolean.TRUE);
    }

    private static void startFinalFrameDisposedChecker() {
        Executors.newSingleThreadScheduledExecutor(
                new CyderThreadFactory("Final Frame Disposed Checker")).scheduleAtFixedRate(() -> {
            Frame[] frames = Frame.getFrames();
            int validFrames = 0;

            for (Frame f : frames) {
                if (f.isShowing()) {
                    validFrames++;
                }
            }

            if (validFrames < 1 && !GenesisShare.framesSuspended()) {
                GenesisShare.exit(120);
            }
        }, 10, 5, SECONDS);
    }

    /**
     * This is called from the shutdown hook, things imperitive to do
     * no matter what, before we close, System.exit has already been called here
     * so you shouldn't do any reading or writing to files or anything with locks/semaphores
     */
    private static void shutdown() {
        //delete temp dir
        IOUtil.deleteTempDir();
    }

    /**
     * Checks the OS and if it is an OSX build, notifies why we are going to exit
     * and exits the program. Cyder is built for Windows 10 (as it plainly says),
     * and also for some possible linux distros which it might work on.
     */
    private static boolean osxSystem() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.startsWith("mac os x");
    }
}