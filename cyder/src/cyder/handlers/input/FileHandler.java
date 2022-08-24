package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;
import cyder.utils.IOUtil;
import cyder.utils.OSUtil;
import cyder.utils.SpotlightUtil;

import java.io.File;

/**
 * A handler related to files and manipulation of them.
 */
public class FileHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private FileHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"wipe logs", "open current log", "open last log", "wipe"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputIgnoringSpacesMatches("wipelogs")) {
            OSUtil.deleteFile(OSUtil.buildFile(Dynamic.PATH, Dynamic.LOGS.getDirectoryName()));
            getInputHandler().println("Logs wiped");
        } else if (getInputHandler().inputIgnoringSpacesMatches("opencurrentlog")) {
            IOUtil.openFileOutsideProgram(Logger.getCurrentLog().getAbsolutePath());
        } else if (getInputHandler().inputIgnoringSpacesMatches("openlastlog")) {
            File[] logs = Logger.getCurrentLog().getParentFile().listFiles();

            if (logs != null) {
                if (logs.length == 1) {
                    getInputHandler().println("No previous logs found");
                } else if (logs.length > 1) {
                    IOUtil.openFileOutsideProgram(logs[logs.length - 2].getAbsolutePath());
                }
            }
        } else if (getInputHandler().inputIgnoringSpacesMatches("wipespotlights")) {
            SpotlightUtil.wipeSpotlights();
        } else if (getInputHandler().commandIs("wipe")) {
            if (getInputHandler().checkArgsLength(1)) {
                File requestedDeleteFile = new File(OSUtil.buildPath(
                        Dynamic.PATH, "users",
                        Console.INSTANCE.getUuid(), getInputHandler().getArg(0)));
                if (requestedDeleteFile.exists()) {
                    if (requestedDeleteFile.isDirectory()) {
                        if (OSUtil.deleteFile(requestedDeleteFile)) {
                            getInputHandler().println("Successfully deleted: "
                                    + requestedDeleteFile.getAbsolutePath());
                        } else {
                            getInputHandler().println("Could not delete folder at this time");
                        }
                    } else if (requestedDeleteFile.isFile()) {
                        if (OSUtil.deleteFile(requestedDeleteFile)) {
                            getInputHandler().println("Successfully deleted "
                                    + requestedDeleteFile.getAbsolutePath());
                        } else {
                            getInputHandler().println("Unable to delete file at this time");
                        }
                    } else {
                        throw new IllegalStateException(
                                "File is not a file nor directory. " + CyderStrings.EUROPEAN_TOY_MAKER);
                    }
                } else {
                    getInputHandler().println("Requested file does not exist: "
                            + requestedDeleteFile.getAbsolutePath());
                }
            } else {
                getInputHandler().print("Wipe command usage: wipe [directory/file within your user directory]");
            }
        } else if (getInputHandler().commandIs("cmd")) {
            OSUtil.openShell();
        } else {
            ret = false;
        }

        return ret;
    }
}
