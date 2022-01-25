package cyder.helperscripts;

import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.ConsoleFrame;
import cyder.utilities.SystemUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PyExecutor {
    /**
     * Executes the USBq.py script.
     */
    public static void executeUSBq() {
        executePy("cyder/src/cyder/helperscripts/usbq.py");
    }

    /**
     * Executes the provided python script.
     *
     * @param pythonScriptPath the path to the python script to execute
     * @param args the arguments with the python script
     */
    public static void executePy(String pythonScriptPath, String... args) {
        //just to be safe, even though Runtime.getRuntime().exec() isn't safe to begin with
        if (!pythonScriptPath.endsWith(".py"))
            return;

        new Thread(() -> {
            try {
                if (SystemUtil.getOS().toLowerCase().contains("windows")) {
                    String deviceString = null;

                    try {
                        StringBuilder argBuilder = new StringBuilder();

                        for (String arg : args) {
                            argBuilder.append(arg).append(" ");
                        }

                        //create and execute python script
                        Process p = Runtime.getRuntime().exec(("python "
                                + pythonScriptPath + " " + argBuilder).trim());

                        //get contents of output
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                        //print outputs to the console frame
                        while ((deviceString = inputReader.readLine()) != null) {
                            ConsoleFrame.getConsoleFrame().getInputHandler().println(deviceString);
                        }
                    }
                    catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Python script executor. Script: " + pythonScriptPath).start();
    }
}
