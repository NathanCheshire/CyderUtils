package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.enumerations.Dynamic;
import cyder.files.FileUtil;
import cyder.network.NetworkUtil;
import cyder.utils.OsUtil;

import java.io.File;
import java.io.IOException;

/**
 * Audio package and third party library binaries Cyder uses.
 */
public enum AudioPackage {
    /**
     * A suite of libraries for handling video, audio, and other multimedia files and streams.
     */
    FFMPEG("ffmpeg -version"),

    /**
     * A command line tool in the ffmpeg library that analyzes multimedia streams and prints information about them.
     */
    FFPROBE("ffprobe -version"),

    /**
     * A command line media player that supports most video and audio filters.
     */
    FFPLAY("ffplay -version"),

    /**
     * A command line tool to download videos from YouTube and other video sites.
     */
    YOUTUBE_DL("youtube-dl --version");

    private final String versionCommand;

    AudioPackage(String versionCommand) {
        this.versionCommand = versionCommand;
    }

    private final String directDownloadLinkHeader =
            "https://github.com/NathanCheshire/CyderUtils/raw/main/src/main/java/cyder/audio/resources/";


    @CanIgnoreReturnValue
    public File downloadBinary(File directoryToDownloadTo) {
        switch (this) {
            case FFMPEG, FFPROBE, FFPLAY -> {
                switch (OsUtil.OPERATING_SYSTEM) {
                    case UNIX, SOLARIS -> {
                        String directDownloadLink = directDownloadLinkHeader + "ubuntu/ffmpeg_ubuntu.zip";
                    }
                    case WINDOWS -> {
                        String directDownloadLink = directDownloadLinkHeader + "windows/ffmpeg_windows.zip";
                    }
                    case OSX -> {
                        String directDownloadLink = directDownloadLinkHeader + "mac/ffmpeg_mac.zip";

                        File saveToFile = OsUtil.buildFile(directoryToDownloadTo.getAbsolutePath(), "ffmpeg");
                        NetworkUtil.downloadResource(directDownloadLink, saveToFile);
                        while (!saveToFile.exists()) Thread.onSpinWait();
                        FileUtil.unzip(saveToFile, directoryToDownloadTo);
                        OsUtil.deleteFile(pairedZipFile.file());
                    }
                    case UNKNOWN -> throw new IllegalStateException("Unknown operating system: "
                            + OsUtil.OPERATING_SYSTEM);
                }
            }
            case YOUTUBE_DL -> {}
            default -> throw new IllegalStateException("Unknown AudioPackage: " + this);
        }
    }

    @CanIgnoreReturnValue
    public File downloadAndExtractBinary(File directoryToDownloadTo, boolean deleteZipArchive) {
        Preconditions.checkNotNull(directoryToDownloadTo);
        Preconditions.checkArgument(directoryToDownloadTo.exists());
        Preconditions.checkArgument(directoryToDownloadTo.isDirectory());

        File downloadedBinaryZipped = downloadBinary(directoryToDownloadTo);
        // todo unzip
        // todo delete zip archive

        return null;
    }

    public String getInvocationCommand() {
        return "todo";
    }

    /**
     * Returns whether this audio package is present and setup in the host operating system
     * PATH such that it may be invoked via the Java process API.
     *
     * @return whether this audio package is present and setup in the host operating system
     * PATH such that it may be invoked via the Java process API
     */
    public boolean isPresentAndInvocable() {
        // todo think about caching this maybe
        try {
            Runtime.getRuntime().exec(versionCommand);
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
