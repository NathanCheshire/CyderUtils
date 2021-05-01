package com.cyder.handler;

import com.cyder.genesis.CyderMain;
import com.cyder.ui.ConsoleFrame;
import com.cyder.utilities.IOUtil;
import com.cyder.utilities.TimeUtil;
import com.cyder.widgets.GenericInform;

import java.io.*;

public class ErrorHandler {

    public static void handle(Exception e) {
        try {
            //find out whereto log the error
            String user = ConsoleFrame.getUUID();
            File throwsDir = null;
            String eFileString = "";

            //if no user, then put error in system throws folder
            if (user == null) {
                throwsDir = new File("src/com/cyder/genesis/Throws");
                eFileString = "src/com/cyder/genesis/Throws/" + TimeUtil.errorTime() + ".error";
            }

            else {
                throwsDir = new File("src/users/" + ConsoleFrame.getUUID() + "/Throws");
                eFileString = "src/users/" + ConsoleFrame.getUUID() + "/Throws/" + TimeUtil.errorTime() + ".error";
            }

            //make the dir if it doesn't exist
            if (!throwsDir.exists())
                throwsDir.mkdir();

            //make the file we are going to write to
            File eFile = new File(eFileString);
            eFile.createNewFile();

            //obtain a String object of the error and the line number
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stackTrack = sw.toString();
            int lineNumber = e.getStackTrace()[0].getLineNumber();
            Class c = e.getClass();

            //get our master string and write it to the file
            String write = "Error thrown by line: " + lineNumber + " from\n" + c +
                    "\n\nStack Trace:\n\n" + stackTrack;

            //write to file, flush, close
            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(eFileString));
            errorWriter.write(write);
            errorWriter.newLine();
            errorWriter.flush();
            errorWriter.close();

            if (IOUtil.getUserData("SilenceErrors").equals("0"))
                IOUtil.openFile(eFileString);
        }

        catch (Exception ex) {
            if (CyderMain.consoleFrame != null && CyderMain.consoleFrame.isVisible()) {
                //todo uncomment ConsoleFrame.notify(ex.getMessage());
                //todo make error handling better, don't auto matically open it up, instead do a popup
                //  and then add click listener on popup (inform) to open the file

                //error was thrown inside of here so we'll just generic inform the user of it
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);

                String stackTrack = sw.toString();
                int lineNumber = ex.getStackTrace()[0].getLineNumber();
                Class c = ex.getClass();

                String write = "Error thrown by line: " + lineNumber + " from\n" + c +
                        "\n\nStack Trace:\n\n" + stackTrack;

                GenericInform.inform(write,"Error trace");
            }
        }
    }

    public static void silentHandle(Exception e) {
        try {
            //find out whereto log the error
            String user = ConsoleFrame.getUUID();
            File throwsDir = null;
            String eFileString = "";

            //if no user, then put error in system throws folder
            if (user == null) {
                throwsDir = new File("src/com/cyder/genesis/Throws");
                eFileString = "src/com/cyder/genesis/Throws/" + TimeUtil.errorTime() + ".error";
            }

            else {
                throwsDir = new File("src/users/" + ConsoleFrame.getUUID() + "/Throws");
                eFileString = "src/users/" + ConsoleFrame.getUUID() + "/Throws/" + TimeUtil.errorTime() + ".error";
            }

            //make the dir if it doesn't exist
            if (!throwsDir.exists())
                throwsDir.mkdir();

            //make the file we are going to write to
            File eFile = new File(eFileString);
            eFile.createNewFile();

            //obtain a String object of the error and the line number
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stackTrack = sw.toString();
            int lineNumber = e.getStackTrace()[0].getLineNumber();
            Class c = e.getClass();

            //get our master string and write it to the file
            String write = "Error thrown by line: " + lineNumber + " from\n" + c +
                    "\n\nStack Trace:\n\n" + stackTrack;

            //write to file, flush, close
            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(eFileString));
            errorWriter.write(write);
            errorWriter.newLine();
            errorWriter.flush();
            errorWriter.close();
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
