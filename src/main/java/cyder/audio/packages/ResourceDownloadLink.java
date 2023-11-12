package cyder.audio.packages;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.utils.OsUtil;

import java.io.File;

/**
 * An encapsulator class for holding direct download links for resources depending on the operating system.
 * Links are presumed to be zipped binaries/direct executables. The caller may download and extract said
 * compressed binary and immediately invoke it from the extracted location.
 */
public final class ResourceDownloadLink {
    /**
     * The map of operating systems to direct resource download links.
     */
    private final ImmutableMap<OsUtil.OperatingSystem, String> resourceDownloadLinks;

    /**
     * Constructs a new ResourceDownloadLink using the provided map.
     *
     * @param resourceDownloadLinks the map of operating systems to direct resource download links
     * @throws NullPointerException     if the provided map is null
     * @throws IllegalArgumentException if the provided map is empty
     */
    public ResourceDownloadLink(ImmutableMap<OsUtil.OperatingSystem, String> resourceDownloadLinks) {
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
    public String getResourceDownloadLink(OsUtil.OperatingSystem operatingSystem) {
        Preconditions.checkNotNull(operatingSystem);
        Preconditions.checkArgument(resourceDownloadLinks.containsKey(operatingSystem));

        return resourceDownloadLinks.get(operatingSystem);
    }

    /**
     * Returns the resource download links map.
     *
     * @return the resource download links map
     */
    public ImmutableMap<OsUtil.OperatingSystem, String> getResourceDownloadLinks() {
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
    public File downloadAndExtractResource(OsUtil.OperatingSystem operatingSystem, File directoryToDownloadTo) {
        Preconditions.checkNotNull(operatingSystem);
        Preconditions.checkNotNull(directoryToDownloadTo);
        Preconditions.checkArgument(directoryToDownloadTo.exists());
        Preconditions.checkArgument(directoryToDownloadTo.isDirectory());

        // todo

        return null;
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
