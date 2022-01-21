package cyder.genesis;

import cyder.consts.CyderStrings;
import cyder.handlers.internal.LoginHandler;
import cyder.handlers.internal.SessionHandler;
import cyder.utilities.IOUtil;
import cyder.utilities.SystemUtil;

import static cyder.genesis.CyderSplash.setLoadingMessage;

public class Cyder {
    /**
     * Setup and start the best program ever made :D
     * @param CA possible command line args passed in. They serve no purpose yet
     *           but we shall log them regardless (just like Big Brother would want)
     */
    public static void main(String[] CA)  {
        //set start time
        GenesisShare.setAbsoluteStartTime(System.currentTimeMillis());

        //set shutdown hook
        CyderSetup.addCommonExitHook();

        //start session logger
        SessionHandler.SessionLogger();
        SessionHandler.log(SessionHandler.Tag.ENTRY, SystemUtil.getWindowsUsername());

        //CyderSetup subroutines
        CyderSetup.initSystemProperties();
        CyderSetup.initUIManager();

        //prevent multiple instances, fatal subroutine if failure
        if (!CyderSetup.checkInstances()) {
            SessionHandler.log(SessionHandler.Tag.EXCEPTION, "ATTEMPTED MULTIPLE CYDER INSTANCES");
            CyderSetup.exceptionExit("Multiple instances of Cyder are not allowed. " +
                    "Terminate other instances before launching a new one.", "Instance Exception");
            return;
        }

        //make sure all fonts are loaded, fatal subroutine if failure
        if (!CyderSetup.registerFonts()) {
            SessionHandler.log(SessionHandler.Tag.EXCEPTION, "SYSTEM FAILURE");
            CyderSetup.exceptionExit("Font required by system could not be loaded","Font failure");
            return;
        }

        //launch splash screen since we will most likely be launching Cyder
        CyderSplash.showSplash();

        setLoadingMessage("Checkinging for exit collisions");
        if (IOUtil.checkForExitCollisions()) {
            SessionHandler.log(SessionHandler.Tag.EXCEPTION, "DUPLICATE EXIT CODES");
            CyderSetup.exceptionExit("You messed up exit codes :/","Exit Codes Exception");
            return;
        }

        if (SystemUtil.osxSystem()) {
            SessionHandler.log(SessionHandler.Tag.EXCEPTION, "IMPROPER OS");
            CyderSetup.exceptionExit("System OS not intended for Cyder use. You should" +
                    " install a dual boot or a VM or something.","OS Exception");
            return;
        }

        //IOUtil necessary subroutines to complete with success before continuing
        setLoadingMessage("Checking system data");
        IOUtil.checkSystemData();
        setLoadingMessage("Fixing users");
        IOUtil.fixUsers();
        setLoadingMessage("Fixing logs");
        IOUtil.fixLogs();
        setLoadingMessage("Cleaning users");
        IOUtil.cleanUsers();

        //IOUtil secondary subroutines that can be executed when program has started essentially
        new Thread(() -> {
            setLoadingMessage("Logging JVM args");
            IOUtil.logArgs(CA);

            IOUtil.cleanSandbox();
            IOUtil.deleteTempDir();
        },"Cyder Start Secondary Subroutines").start();

        //start GUI exiting failsafe
        GenesisShare.startFinalFrameDisposedChecker();

        //offship how to login to the LoginHandler since all subroutines finished
        LoginHandler.beginLogin();
    }

    //just to be safe
    private Cyder() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }
}