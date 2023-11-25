package cyder.audio.packages;

import com.google.common.collect.ImmutableMap;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.utils.OperatingSystem;

/**
 * Resource download links used for {@link AudioPackage}s, extracted for readability and testing.
 */
final class ResourceDownloadLinks {
    private static final String ffmpegWindows =
            "https://github.com/NathanCheshire/CyderUtils/raw/main/src/main/java/cyder/audio/resources/windows/ffmpeg_windows.zip";
    private static final String ffmpegMac =
            "https://github.com/NathanCheshire/CyderUtil/raw/main/src/main/java/cyder/audio/resources/mac/ffmpeg_mac.zip";
    private static final String ffmpegLinux =
            "https://github.com/NathanCheshire/CyderUtils/raw/main/src/main/java/cyder/audio/resources/linux/ffmpeg_linux.zip";

    static final ResourceDownloadLink FFMPEG_RESOURCE_DOWNLOAD_LINKS = new ResourceDownloadLink(
            ImmutableMap.of(
                    OperatingSystem.WINDOWS, new NamedResourceLink("ffmpeg_windows.zip", ffmpegWindows),
                    OperatingSystem.MAC, new NamedResourceLink("ffmpeg_mac.zip", ffmpegMac),
                    OperatingSystem.GNU_LINUX, new NamedResourceLink("ffmpeg_linux.zip", ffmpegLinux)
            )
    );

    static final ResourceDownloadLink YOUTUBE_DL_RESOURCE_DOWNLOAD_LINKS = new ResourceDownloadLink(
            ImmutableMap.of(
                    OperatingSystem.WINDOWS,
                    new NamedResourceLink("youtube_dl_windows.zip", ""), // todo
                    OperatingSystem.MAC,
                    new NamedResourceLink("youtube_dl_mac.zip", ""), // todo
                    OperatingSystem.GNU_LINUX,
                    new NamedResourceLink("youtube_dl_linux.zip", "") // todo
            )
    );

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private ResourceDownloadLinks() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
