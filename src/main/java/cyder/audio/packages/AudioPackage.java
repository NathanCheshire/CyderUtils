package cyder.audio.packages;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.utils.OperatingSystem;
import cyder.utils.OsUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Future;

/**
 * Audio package and third-party binaries Cyder uses.
 */
public enum AudioPackage {


    /**
     * A suite of libraries for handling video, audio, and other multimedia files and streams.
     */
    FFMPEG("ffmpeg", "-version", new ResourceDownloadLink(ImmutableMap.of(
            OperatingSystem.WINDOWS,
            new NamedResourceLink("ffmpeg_windows.zip", ffmpegResourceHeader + "windows/ffmpeg_windows.zip"),
            OperatingSystem.MAC,
            new NamedResourceLink("ffmpeg_mac.zip", ffmpegResourceHeader + "mac/ffmpeg_mac.zip"),
            OperatingSystem.GNU_LINUX,
            new NamedResourceLink("ffmpeg_linux.zip", ffmpegResourceHeader + "linux/ffmpeg_linux.zip")
    ))),

    /**
     * A command line tool in the ffmpeg library that analyzes multimedia streams and prints information about them.
     */
    FFPROBE("ffprobe", "-version", FFMPEG.resourceDownloadLinks),

    /**
     * A command line media player that supports most video and audio filters.
     */
    FFPLAY("ffplay", "-version", FFMPEG.resourceDownloadLinks),

    /**
     * A command line tool to download videos from YouTube and other video sites.
     */
    YOUTUBE_DL("youtube-dl", "--version", new ResourceDownloadLink(ImmutableMap.of(
            OperatingSystem.WINDOWS,
            new NamedResourceLink("youtube_dl_windows.zip", ""), // todo
            OperatingSystem.MAC,
            new NamedResourceLink("youtube_dl_mac.zip", ""), // todo
            OperatingSystem.GNU_LINUX,
            new NamedResourceLink("youtube_dl_linux.zip", "") // todo
    )));

    private static final String ffmpegResourceHeader
            = "https://github.com/NathanCheshire/CyderUtils/raw/main/src/main/java/cyder/audio/resources/";

    /**
     * A cache of audio packages which were found to be present and invokable.
     */
    private static final HashMap<AudioPackage, Boolean> presentAndInvokableCache = new HashMap<>();

    /**
     * The invocation name of this audio package.
     */
    private final String invocationName;

    /**
     * The argument used with the invocation name to determine the audio package's version.
     */
    private final String versionArgument;

    /**
     * The ResourceDownloadLink to download and extract the binary for different operating systems.
     */
    private final ResourceDownloadLink resourceDownloadLinks;

    AudioPackage(String invocationName, String versionArgument, ResourceDownloadLink resourceDownloadLinks) {
        this.invocationName = invocationName;
        this.versionArgument = versionArgument;
        this.resourceDownloadLinks = resourceDownloadLinks;
    }

    /**
     * Downloads this audio package to the provided directory.
     * Note the binary is compressed in zip format when downloaded.
     * Thus, the binary is extracted from compression and the old compressed file is deleted.
     *
     * @param directoryToDownloadTo the directory to download the compressed binary to
     * @return the binary once downloaded and extracted
     */
    @CanIgnoreReturnValue
    public Future<File> downloadAndExtractBinary(File directoryToDownloadTo) {
        return resourceDownloadLinks.downloadAndExtractResource(OsUtil.OPERATING_SYSTEM, directoryToDownloadTo);
    }

    /**
     * Returns the string to use in combination with the process API to invoke this audio package binary.
     *
     * @return the string to use in combination with the process API to invoke this audio package binary
     */
    public String getInvocationName() {
        return invocationName;
    }

    /**
     * Returns the argument this audio package binary uses to determine the version.
     *
     * @return the argument this audio package binary uses to determine the version
     */
    public String getVersionArgument() {
        return versionArgument;
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
            String versionCommand = invocationName + " " + versionArgument;
            Runtime.getRuntime().exec(versionCommand);
        } catch (IOException e) {
            ret = false;
        }
        presentAndInvokableCache.put(this, ret);
        return ret;
    }
}
