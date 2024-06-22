package cyderutils.audio.packages;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

/**
 * A class to associate a common name with a URL/link.
 */
@SuppressWarnings("ClassCanBeRecord") /* Readability */
@Immutable
public final class NamedResourceLink {
    /**
     * The filename to use for the downloaded resource.
     */
    private final String filename;

    /**
     * The remote resource link.
     */
    private final String link;

    /**
     * Constructs a new NamedLink.
     *
     * @param filename the name to use for the downloaded resource
     * @param link the remote resource link
     * @throws NullPointerException     if either the name or link are null
     * @throws IllegalArgumentException if either the name or link are empty
     */
    public NamedResourceLink(String filename, String link) {
        Preconditions.checkNotNull(filename);
        Preconditions.checkNotNull(link);
        Preconditions.checkArgument(!filename.trim().isEmpty());
        Preconditions.checkArgument(!link.trim().isEmpty());

        this.filename = filename;
        this.link = link;
    }

    /**
     * Returns the name to use for the downloaded resource.
     *
     * @return the name to use for the downloaded resource
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Returns the remote resource link.
     *
     * @return the remote resource link
     */
    public String getLink() {
        return link;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = filename.hashCode();
        ret = 31 * ret + link.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "NamedLink{"
                + "name=\"" + filename + "\""
                + ", link=\"" + link + "\""
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof NamedResourceLink)) {
            return false;
        }

        NamedResourceLink other = (NamedResourceLink) o;
        return other.link.equals(link)
                && other.filename.equals(filename);
    }
}
