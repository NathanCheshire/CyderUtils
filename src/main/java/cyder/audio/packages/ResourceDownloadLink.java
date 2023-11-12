package cyder.audio.packages;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.enumerations.Dynamic;
import cyder.enumerations.Extension;
import cyder.files.FileUtil;
import cyder.network.NetworkUtil;
import cyder.process.Program;
import cyder.threads.CyderThreadFactory;
import cyder.utils.OperatingSystem;
import cyder.utils.OsUtil;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An encapsulator class for holding direct download links for resources depending on the operating system.
 * Links are presumed to be zipped binaries/direct executables. The caller may download and extract said
 * compressed binary and immediately invoke it from the extracted location.
 */
@SuppressWarnings("ClassCanBeRecord") /* Readability */
public final class ResourceDownloadLink {
    /**
     * The map of operating systems to direct resource download links.
     */
    private final ImmutableMap<OperatingSystem, String> resourceDownloadLinks;
    // todo second param needs to be named link maybe use record for this?

    /**
     * Constructs a new ResourceDownloadLink using the provided map.
     *
     * @param resourceDownloadLinks the map of operating systems to direct resource download links
     * @throws NullPointerException     if the provided map is null
     * @throws IllegalArgumentException if the provided map is empty
     */
    public ResourceDownloadLink(ImmutableMap<OperatingSystem, String> resourceDownloadLinks) {
        Preconditions.checkNotNull(resourceDownloadLinks);
        Preconditions.checkArgument(!resourceDownloadLinks.isEmpty());

        this.resourceDownloadLinks = resourceDownloadLinks;
    }

    /**
     * Returns the resource download link for the provided operating systems.
     *
     * @param operatingSystem the operating system to return the download link of
     * @return the resource download link for the provided operating systems
     * @throws NullPointerException     if the provided operating system is null
     * @throws IllegalArgumentException if the provided operating systems is not
     *                                  contained in the internal resource download link map
     */
    public String getResourceDownloadLink(OperatingSystem operatingSystem) {
        Preconditions.checkNotNull(operatingSystem);
        Preconditions.checkArgument(resourceDownloadLinks.containsKey(operatingSystem));

        return resourceDownloadLinks.get(operatingSystem);
    }

    /**
     * Returns the resource download links map.
     *
     * @return the resource download links map
     */
    public ImmutableMap<OperatingSystem, String> getResourceDownloadLinks() {
        return resourceDownloadLinks;
    }

    /**
     * Downloads this resource for the provided operating systems, extracts the binary from compression,
     * and deletes the old compressed binary.
     *
     * @param operatingSystem       the host operating system
     * @param directoryToDownloadTo the directory to download and extract the resource to
     * @return the downloaded and extracted file
     */
    @CanIgnoreReturnValue
    public Future<File> downloadAndExtractResource(OperatingSystem operatingSystem, File directoryToDownloadTo) {
        Preconditions.checkNotNull(operatingSystem);
        Preconditions.checkNotNull(directoryToDownloadTo);
        Preconditions.checkArgument(directoryToDownloadTo.exists());
        Preconditions.checkArgument(directoryToDownloadTo.isDirectory());

        // todo configure thread factory name
        CyderThreadFactory threadFactory = new CyderThreadFactory("todo name me");
        return Executors.newSingleThreadExecutor(threadFactory).submit(() -> {
            File downloadZip = new File(""); // todo name and extension and path

            NetworkUtil.downloadResource(youtubeDlResourceDownload, downloadZip);
            while (!downloadZip.exists()) Thread.onSpinWait();

            File extractFolder = Dynamic.buildDynamic(Dynamic.EXES.getFileName());

            FileUtil.unzip(downloadZip, extractFolder);
            OsUtil.deleteFile(downloadZip);

            return Dynamic.buildDynamic(Dynamic.EXES.getFileName(),
                    Program.YOUTUBE_DL.getProgramName() + Extension.EXE.getExtension()).exists();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return resourceDownloadLinks.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ResourceDownloadLink)) {
            return false;
        }

        ResourceDownloadLink other = (ResourceDownloadLink) o;
        return resourceDownloadLinks.equals(other.resourceDownloadLinks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ResourceDownloadLink{resourceDownloadLinks=" + resourceDownloadLinks + "}";
    }
}
