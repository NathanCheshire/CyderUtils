package cyder.network.ipdataco;

import com.google.common.base.Preconditions;

/**
 * A blocked IP address entry.
 */
public class BlockListEntry {
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
     * @throws NullPointerException if any parameter is null
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
}
