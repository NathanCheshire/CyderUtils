package com.cyder.handler;

import com.cyder.utilities.GeneralUtil;
import com.cyder.utilities.IOUtil;
import com.cyder.utilities.TimeUtil;

import java.io.*;

public class ErrorHandler {

    //handle class
    public static void handle(Exception e) {
        try {
            File throwsDir = new File("src/users/" + GeneralUtil.getUserUUID() + "/Throws/");

            if (!throwsDir.exists())
                throwsDir.mkdir();

            String eFileString = "src/users/" + GeneralUtil.getUserUUID() + "/Throws/" + TimeUtil.errorTime() + ".error";
            File eFile = new File(eFileString);
            eFile.createNewFile();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stackTrack = sw.toString();
            int lineNumber = e.getStackTrace()[0].getLineNumber();
            Class c = e.getClass();

            String write = "Error thrown by line: " + lineNumber + " from\n" + c +
                    "\n\nStack Trace:\n\n" + stackTrack;

            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(eFileString));
            errorWriter.write(write);
            errorWriter.newLine();
            errorWriter.flush();
            errorWriter.close();

            if (IOUtil.getUserData("SilenceErrors").equals("1"))
                return;
            IOUtil.openFile(eFileString);
        }

        catch (Exception ex) {
            if (IOUtil.getUserData("SilenceErrors") != null && IOUtil.getUserData("SilenceErrors").equals("0")) {
                System.out.println("Exception in error logger:\n\n");
                e.printStackTrace();
                //ltodo show popup with inform on consoleframe
            }
        }
    }
}
