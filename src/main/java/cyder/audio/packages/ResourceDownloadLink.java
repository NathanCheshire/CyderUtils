package cyder.audio.packages;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.files.FileUtil;
import cyder.network.NetworkUtil;
import cyder.threads.CyderThreadFactory;
import cyder.utils.OperatingSystem;
import cyder.utils.OsUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An encapsulator class for holding direct download links for resources depending on the operating system.
 * Links are presumed to be zipped binaries/direct executables. The caller may download and extract said
 * compressed binary and immediately invoke it from the extracted location.
 */
public final class ResourceDownloadLink {
    /**
     * The map of operating systems to direct resource download links.
     */
    private final ImmutableMap<OperatingSystem, NamedResourceLink> downloadLinks;

    /**
     * Constructs a new ResourceDownloadLink using the provided map.
     *
     * @param resourceDownloadLinks the map of operating systems to direct resource download links
     * @throws NullPointerException     if the provided map is null
     * @throws IllegalArgumentException if the provided map is empty
     */
    public ResourceDownloadLink(ImmutableMap<OperatingSystem, NamedResourceLink> resourceDownloadLinks) {
        Preconditions.checkNotNull(resourceDownloadLinks);
        Preconditions.checkArgument(!resourceDownloadLinks.isEmpty());

        this.downloadLinks = resourceDownloadLinks;
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
    public NamedResourceLink getNamedLink(OperatingSystem operatingSystem) {
        Preconditions.checkNotNull(operatingSystem);
        Preconditions.checkArgument(downloadLinks.containsKey(operatingSystem));

        return downloadLinks.get(operatingSystem);
    }

    /**
     * Returns the resource download links map.
     *
     * @return the resource download links map
     */
    public ImmutableMap<OperatingSystem, NamedResourceLink> getDownloadLinks() {
        return downloadLinks;
    }

    /**
     * Downloads this resource for the provided operating systems, extracts the binary from compression,
     * and deletes the old compressed binary.
     *
     * @param operatingSystem       the host operating system
     * @param directoryToDownloadTo the directory to download and extract the resource to
     * @return the downloaded and extracted file
     * @throws NullPointerException     if either the provided operating system or directory to download to are null
     * @throws IllegalArgumentException if the directory to download to does not exist or is not a directory or if the
     *                                  resource download links does not contain a key for the provided operating system
     */
    @CanIgnoreReturnValue
    public Future<File> downloadAndExtractResource(OperatingSystem operatingSystem, File directoryToDownloadTo) {
        Preconditions.checkNotNull(operatingSystem);
        Preconditions.checkNotNull(directoryToDownloadTo);
        Preconditions.checkArgument(directoryToDownloadTo.exists());
        Preconditions.checkArgument(directoryToDownloadTo.isDirectory());
        Preconditions.checkArgument(downloadLinks.containsKey(operatingSystem));

        NamedResourceLink namedResourceLink = downloadLinks.get(operatingSystem);
        @SuppressWarnings("DataFlowIssue") // Safe due to Precondition check
        String resourceName = namedResourceLink.getFilename();
        String resourceLink = namedResourceLink.getLink();

        File compressedBinary = OsUtil.buildFile(directoryToDownloadTo.getAbsolutePath(), resourceName);

        String threadFactoryName = "ResourceDownloadLink downloader, name: " + resourceName
                + ", resource: " + resourceLink;
        CyderThreadFactory threadFactory = new CyderThreadFactory(threadFactoryName);

        return Executors.newSingleThreadExecutor(threadFactory).submit(() -> {
            try {
                boolean noErrors = NetworkUtil.downloadResource(resourceLink, compressedBinary);
                while (!compressedBinary.exists()) Thread.onSpinWait();
                // todo these two return booleans we need to check and we also need to return the final file still
                FileUtil.unzip(compressedBinary, directoryToDownloadTo);
                OsUtil.deleteFile(compressedBinary);
                return null;
            } catch (IOException e) {
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return downloadLinks.hashCode();
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
        return downloadLinks.equals(other.downloadLinks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ResourceDownloadLink{resourceDownloadLinks=" + downloadLinks + "}";
    }
}
