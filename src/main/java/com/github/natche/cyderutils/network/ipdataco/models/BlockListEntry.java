package com.github.natche.cyderutils.network.ipdataco.models;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

/**
 * A blocked IP address entry.
 * Currently, ipdataco is partnered with three professional commercial blocklist providers:
 * <ul>
 *     <li>Honeydb</li>
 *     <li>Bambenek Consulting</li>
 *     <li>Cleantalk</li>
 * </ul>
 */
@Immutable
public final class BlockListEntry {
    /**
     * The name of the blocked entry.
     */
    private final String name;

    /**
     * The site of the blocked entry.
     */
    private final String site;

    /**
     * The type of the blocked entry.
     */
    private final String type;

    /**
     * Constructs a new BlockListEntry.
     *
     * @param name the name of the BlockListEntry
     * @param site the site of the BlockListEntry
     * @param type the type of the BlockListEntry
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if any parameter is empty
     */
    public BlockListEntry(String name, String site, String type) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(site);
        Preconditions.checkNotNull(type);
        Preconditions.checkArgument(!name.trim().isEmpty());
        Preconditions.checkArgument(!site.trim().isEmpty());
        Preconditions.checkArgument(!type.trim().isEmpty());

        this.name = name;
        this.site = site;
        this.type = type;
    }

    /**
     * Returns name of this blocked entry.
     *
     * @return name of this blocked entry
     */
    public String getName() {
        return name;
    }

    /**
     * Returns site of this blocked entry.
     *
     * @return site of this blocked entry
     */
    public String getSite() {
        return site;
    }

    /**
     * Returns type of this blocked entry.
     *
     * @return type of this blocked entry
     */
    public String getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = name.hashCode();
        ret = 31 * ret + site.hashCode();
        ret = 31 * ret + type.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof BlockListEntry)) {
            return false;
        }

        BlockListEntry other = (BlockListEntry) o;
        return name.equals(other.name)
                && site.equals(other.site)
                && type.equals(other.type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "BlockListEntry{"
                + "name=\"" + name + "\""
                + ", site=\"" + site + "\""
                + ", type=\"" + type + "\""
                + "}";
    }
}
