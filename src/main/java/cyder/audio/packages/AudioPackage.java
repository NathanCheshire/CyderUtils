package cyder.audio.packages;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.files.FileUtil;
import cyder.network.NetworkUtil;
import cyder.utils.OsUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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

    private static final HashMap<AudioPackage, Boolean> presentAndInvokableCache = new HashMap<>();

    private final String versionCommand;

    AudioPackage(String versionCommand) {
        this.versionCommand = versionCommand;
    }

    private static final ImmutableMap<OsUtil.OperatingSystem, String> ffmpegDownloadLinks = ImmutableMap.of(
            OsUtil.OperatingSystem.WINDOWS,
            "https://github.com/NathanCheshire/CyderUtils/raw/main/src/main/java/cyder/audio/resources/windows/ffmpeg_windows.zip",
            OsUtil.OperatingSystem.OSX,
            "https://github.com/NathanCheshire/CyderUtils/raw/main/src/main/java/cyder/audio/resources/mac/ffmpeg_mac.zip",
            OsUtil.OperatingSystem.UNIX,
            "https://github.com/NathanCheshire/CyderUtils/raw/main/src/main/java/cyder/audio/resources/ubuntu/ffmpeg_ubuntu.zip"
    );
    private static final ResourceDownloadLink FFMPEG_RESOURCE_DOWNLOADS = new ResourceDownloadLink(ffmpegDownloadLinks);

    /**
     * Downloads this audio package to the provided directory.
     * Note the binary is compressed in zip format when downloaded.
     * See {@link #downloadAndExtractBinary(File, boolean)} to both download and extract.
     *
     * @param directoryToDownloadTo the directory to download the compressed binary to
     * @return the compressed binary once downloaded
     */
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
        if (presentAndInvokableCache.containsKey(this) && presentAndInvokableCache.containsKey(this)) return true;
        boolean ret = true;
        try {
            Runtime.getRuntime().exec(versionCommand);
        } catch (IOException e) {
            ret = false;
        }
        presentAndInvokableCache.put(this, ret);
        return ret;
    }
}
