package cyder.audio.packages;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

/**
 * A class to associate a common name with a URL/link.
 */
@SuppressWarnings("ClassCanBeRecord") /* Readability */
@Immutable
public final class NamedLink {
    /**
     * The name of the resource link.
     */
    private final String name;

    /**
     * The remote resource link.
     */
    private final String link;

    /**
     * Constructs a new NamedLink.
     *
     * @param name the name of the remote link
     * @param link the link
     * @throws NullPointerException     if either the name or link are null
     * @throws IllegalArgumentException if either the name or link are empty
     */
    public NamedLink(String name, String link) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(link);
        Preconditions.checkArgument(!name.trim().isEmpty());
        Preconditions.checkArgument(!link.trim().isEmpty());

        this.name = name;
        this.link = link;
    }

    /**
     * Returns the name of the resource link.
     *
     * @return the name of the resource link
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the link.
     *
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = name.hashCode();
        ret = 31 * ret + link.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "NamedLink{"
                + "name=\"" + name
                + "\", link=\"" + link
                + "\"}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof NamedLink)) {
            return false;
        }

        NamedLink other = (NamedLink) o;
        return other.link.equals(link)
                && other.name.equals(name);
    }
}
